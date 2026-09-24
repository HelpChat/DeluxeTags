package me.clip.deluxetags.storage;

import static org.junit.Assert.*;
import com.mysql.cj.jdbc.MysqlDataSource;
import java.io.File;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

/** Opt-in real database tests. Every test owns a random table prefix and removes only those tables. */
public class MySqlIntegrationTest {
    @Rule public TemporaryFolder temp = new TemporaryFolder();
    private StorageSettings settings;
    private final List<MySqlSelectionStore> stores = new ArrayList<>();

    @Before public void setup() {
        Assume.assumeTrue(System.getenv("DELUXETAGS_MYSQL_DATABASE") != null);
        YamlConfiguration config = new YamlConfiguration();
        config.set("storage.type", "mysql");
        config.set("storage.mysql.host", env("HOST", "localhost"));
        config.set("storage.mysql.port", Integer.parseInt(env("PORT", "3306")));
        config.set("storage.mysql.database", env("DATABASE", "deluxetags_test"));
        config.set("storage.mysql.username", env("USERNAME", "root"));
        config.set("storage.mysql.password", env("PASSWORD", ""));
        config.set("storage.mysql.table-prefix", "dt_" + UUID.randomUUID().toString().replace("-", "") + "_");
        settings = new StorageSettings(config);
    }

    private String env(String name, String fallback) {
        String value = System.getenv("DELUXETAGS_MYSQL_" + name);
        return value == null ? fallback : value;
    }

    private MySqlSelectionStore store(File folder) {
        MySqlSelectionStore store = new MySqlSelectionStore(settings, folder, ignored -> { });
        stores.add(store);
        return store;
    }

    @After public void cleanup() throws Exception {
        for (MySqlSelectionStore store : stores) store.close();
        if (settings == null) return;
        MysqlDataSource source = new MysqlDataSource();
        source.setServerName(settings.host); source.setPort(settings.port);
        source.setDatabaseName(settings.database); source.setUser(settings.username); source.setPassword(settings.password);
        try (Connection connection = source.getConnection(); Statement sql = connection.createStatement()) {
            sql.executeUpdate("DROP TABLE IF EXISTS `" + settings.prefix + "players`");
            sql.executeUpdate("DROP TABLE IF EXISTS `" + settings.prefix + "imports`");
        }
    }

    @Test public void importsOncePreservingExistingRowsUnicodeAndSourceFile() throws Exception {
        UUID conflict = UUID.randomUUID(), imported = UUID.randomUUID(), noTag = UUID.randomUUID();
        MySqlSelectionStore first = store(temp.newFolder()); first.initialize();
        first.write(conflict, "database-value");
        File folder = temp.newFolder();
        File file = new File(folder, "userdata/player_tags.yml");
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set(conflict.toString(), "old-yaml");
        // Legacy SnakeYAML emits supplementary characters as !!binary, not strings.
        yaml.set(imported.toString(), "タグ");
        yaml.set(noTag.toString(), Selection.NO_TAG);
        yaml.set("invalid-uuid", "skip");
        yaml.set(UUID.randomUUID().toString(), 42);
        yaml.save(file);
        byte[] original = Files.readAllBytes(file.toPath());
        MySqlSelectionStore second = store(folder); second.initialize();
        Map<UUID, Selection> rows = first.load(Arrays.asList(conflict, imported, noTag));
        assertEquals("database-value", rows.get(conflict).getIdentifier());
        assertEquals("タグ", rows.get(imported).getIdentifier());
        assertEquals(Selection.NO_TAG, rows.get(noTag).getIdentifier());
        assertArrayEquals(original, Files.readAllBytes(file.toPath()));
        first.write(imported, "タグ🌟");
        assertEquals("タグ🌟", second.load(Collections.singleton(imported)).get(imported).getIdentifier());
        first.write(imported, null);
        second.close();
        MySqlSelectionStore reopened = store(folder); reopened.initialize();
        Selection cleared = reopened.load(Collections.singleton(imported)).get(imported);
        assertNull(cleared.getIdentifier());
        assertEquals(3, cleared.getRevision());
        UUID later = UUID.randomUUID(); yaml.set(later.toString(), "must-not-import"); yaml.save(file);
        MySqlSelectionStore third = store(folder); third.initialize();
        assertEquals(0, third.load(Collections.singleton(later)).get(later).getRevision());
    }

    @Test public void failedImportCanRetryWithoutLosingSourceData() throws Exception {
        File folder = temp.newFolder();
        File file = new File(folder, "userdata/player_tags.yml");
        Files.createDirectories(file.getParentFile().toPath());
        Files.write(file.toPath(), "invalid: [\n".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        MySqlSelectionStore store = store(folder);
        try { store.initialize(); fail("Invalid YAML accepted"); }
        catch (org.bukkit.configuration.InvalidConfigurationException expected) { }
        UUID player = UUID.randomUUID();
        YamlConfiguration yaml = new YamlConfiguration(); yaml.set(player.toString(), "recovered"); yaml.save(file);
        store.initialize();
        assertEquals("recovered", store.load(Collections.singleton(player)).get(player).getIdentifier());
    }

    @Test public void concurrentServersSerializeRevisionsAndShareClearedSelections() throws Exception {
        MySqlSelectionStore first = store(temp.newFolder()), second = store(temp.newFolder());
        first.initialize(); second.initialize();
        UUID player = UUID.randomUUID();
        ExecutorService workers = Executors.newFixedThreadPool(2);
        try {
            List<Future<?>> writes = new ArrayList<>();
            for (int i = 0; i < 30; i++) {
                final int number = i;
                writes.add(workers.submit(() -> { first.write(player, "first-" + number); return null; }));
                writes.add(workers.submit(() -> { second.write(player, "second-" + number); return null; }));
            }
            for (Future<?> write : writes) write.get(30, TimeUnit.SECONDS);
        } finally { workers.shutdownNow(); }
        Selection result = first.load(Collections.singleton(player)).get(player);
        assertEquals(60, result.getRevision());
        assertEquals(result.getIdentifier(), second.load(Collections.singleton(player)).get(player).getIdentifier());
        first.write(player, Selection.NO_TAG);
        assertEquals(Selection.NO_TAG, second.load(Collections.singleton(player)).get(player).getIdentifier());
        second.write(player, null);
        Selection cleared = first.load(Collections.singleton(player)).get(player);
        assertNull(cleared.getIdentifier());
        assertEquals(62, cleared.getRevision());
    }
}
