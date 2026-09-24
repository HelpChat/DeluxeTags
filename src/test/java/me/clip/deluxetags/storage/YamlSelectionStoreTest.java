package me.clip.deluxetags.storage;

import static org.junit.Assert.*;
import java.io.File;
import java.util.Collections;
import java.util.UUID;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class YamlSelectionStoreTest {
    @Rule public TemporaryFolder temp = new TemporaryFolder();

    @Test public void readsLegacyFileAndPreservesExplicitNoTagAcrossRestart() throws Exception {
        File file = new File(temp.getRoot(), "player_tags.yml");
        UUID player = UUID.randomUUID();
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set(player.toString(), "vip"); yaml.save(file);
        YamlSelectionStore store = new YamlSelectionStore(file); store.initialize();
        assertEquals("vip", store.load(Collections.singleton(player)).get(player).getIdentifier());
        store.write(player, Selection.NO_TAG);
        YamlSelectionStore reopened = new YamlSelectionStore(file); reopened.initialize();
        assertEquals(Selection.NO_TAG, reopened.get(player).getIdentifier());
        reopened.write(player, null);
        assertFalse(YamlConfiguration.loadConfiguration(file).contains(player.toString()));
    }

    @Test public void reloadReadsExternalEditsAndDoesNotOverwriteMalformedYaml() throws Exception {
        File file = temp.newFile();
        UUID player = UUID.randomUUID();
        YamlSelectionStore store = new YamlSelectionStore(file); store.initialize();
        store.write(player, "old");
        java.nio.file.Files.write(file.toPath(), (player + ": new\n").getBytes(java.nio.charset.StandardCharsets.UTF_8));
        store.initialize();
        assertEquals("new", store.get(player).getIdentifier());
        byte[] invalid = "bad: [\n".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        java.nio.file.Files.write(file.toPath(), invalid);
        try { store.initialize(); fail("Invalid YAML accepted"); } catch (org.bukkit.configuration.InvalidConfigurationException expected) { }
        assertArrayEquals(invalid, java.nio.file.Files.readAllBytes(file.toPath()));
    }

    @Test public void failedSaveDoesNotPublishNewSelection() throws Exception {
        File file = new File(temp.getRoot(), "blocked.yml");
        YamlSelectionStore store = new YamlSelectionStore(file); store.initialize();
        assertTrue(file.mkdir());
        UUID player = UUID.randomUUID();
        try { store.write(player, "vip"); fail("Save succeeded to directory"); } catch (java.io.IOException expected) { }
        assertNull(store.get(player).getIdentifier());
    }
}
