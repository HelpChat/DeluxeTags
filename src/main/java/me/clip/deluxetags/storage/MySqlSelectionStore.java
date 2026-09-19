package me.clip.deluxetags.storage;

import com.mysql.cj.jdbc.MysqlDataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import org.bukkit.configuration.file.YamlConfiguration;

public final class MySqlSelectionStore implements SelectionStore {
    private final StorageSettings settings;
    private final File dataFolder;
    private final Consumer<String> log;
    private final String playersTable, importsTable;
    private HikariDataSource pool;
    private boolean initialized;

    public MySqlSelectionStore(StorageSettings settings, File dataFolder, Consumer<String> log) {
        this.settings = settings;
        this.dataFolder = dataFolder;
        this.log = log;
        playersTable = "`" + settings.prefix + "players`";
        importsTable = "`" + settings.prefix + "imports`";
    }

    @Override
    public void initialize() throws Exception {
        if (pool == null) {
            // Construct the bundled DataSource directly; do not depend on server JDBC registrations.
            MysqlDataSource source = new MysqlDataSource();
            source.setServerName(settings.host);
            source.setPort(settings.port);
            source.setDatabaseName(settings.database);
            source.setUser(settings.username);
            source.setPassword(settings.password);
            source.setSslMode(settings.sslMode);
            source.setConnectTimeout(settings.connectTimeout);
            source.setSocketTimeout(settings.socketTimeout);
            source.setCharacterEncoding("UTF-8");
            HikariConfig config = new HikariConfig();
            config.setDataSource(source);
            config.setPoolName("DeluxeTags-storage");
            config.setMaximumPoolSize(2);
            config.setMinimumIdle(0);
            config.setConnectionTimeout(settings.connectTimeout);
            config.setValidationTimeout(Math.min(settings.connectTimeout, 1000));
            config.setInitializationFailTimeout(-1);
            pool = new HikariDataSource(config);
        }
        if (initialized) return;
        try (Connection connection = pool.getConnection(); Statement statement = connection.createStatement()) {
            statement.setQueryTimeout(5);
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + playersTable
                    + " (uuid CHAR(36) CHARACTER SET ascii COLLATE ascii_bin PRIMARY KEY,"
                    + " tag_identifier TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL,"
                    + " revision BIGINT NOT NULL) ENGINE=InnoDB");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + importsTable
                    + " (installation_id CHAR(36) CHARACTER SET ascii COLLATE ascii_bin PRIMARY KEY,"
                    + " completed BOOLEAN NOT NULL DEFAULT FALSE) ENGINE=InnoDB");
        }
        importYaml();
        initialized = true;
    }

    private PreparedStatement prepare(Connection connection, String sql) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setQueryTimeout(5);
        return statement;
    }

    private UUID installationId() throws Exception {
        File file = new File(dataFolder, "storage-installation-id");
        if (!file.exists()) {
            Files.createDirectories(dataFolder.toPath());
            Files.write(file.toPath(), UUID.randomUUID().toString().getBytes(StandardCharsets.US_ASCII),
                    java.nio.file.StandardOpenOption.CREATE_NEW);
        }
        return UUID.fromString(new String(Files.readAllBytes(file.toPath()), StandardCharsets.US_ASCII).trim());
    }

    private void importYaml() throws Exception {
        UUID installation = installationId();
        try (Connection connection = pool.getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement statement = prepare(connection, "INSERT INTO " + importsTable
                        + " (installation_id, completed) VALUES (?, FALSE) ON DUPLICATE KEY UPDATE installation_id=installation_id")) {
                    statement.setString(1, installation.toString());
                    statement.executeUpdate();
                }
                try (PreparedStatement statement = prepare(connection, "SELECT completed FROM " + importsTable
                        + " WHERE installation_id=? FOR UPDATE")) {
                    statement.setString(1, installation.toString());
                    try (ResultSet rows = statement.executeQuery()) {
                        if (rows.next() && rows.getBoolean(1)) {
                            connection.commit();
                            return;
                        }
                    }
                }
                YamlConfiguration yaml = new YamlConfiguration();
                File source = new File(dataFolder, "userdata/player_tags.yml");
                if (source.exists()) yaml.load(source);
                int valid = 0, skipped = 0;
                try (PreparedStatement statement = prepare(connection, "INSERT INTO " + playersTable
                        + " (uuid, tag_identifier, revision) VALUES (?, ?, 1) ON DUPLICATE KEY UPDATE uuid=uuid")) {
                    for (String key : yaml.getKeys(false)) {
                        UUID uuid;
                        try {
                            uuid = UUID.fromString(key);
                            if (!uuid.toString().equalsIgnoreCase(key) || !yaml.isString(key)) {
                                skipped++;
                                continue;
                            }
                        } catch (IllegalArgumentException e) {
                            skipped++;
                            continue;
                        }
                        String identifier = yaml.getString(key);
                        if (identifier == null || identifier.getBytes(StandardCharsets.UTF_8).length > 65535) {
                            skipped++;
                            continue;
                        }
                        statement.setString(1, uuid.toString());
                        statement.setString(2, identifier);
                        statement.executeUpdate();
                        valid++;
                    }
                }
                try (PreparedStatement statement = prepare(connection, "UPDATE " + importsTable
                        + " SET completed=TRUE WHERE installation_id=?")) {
                    statement.setString(1, installation.toString());
                    statement.executeUpdate();
                }
                connection.commit();
                log.accept("MySQL YAML import complete: " + valid + " valid records processed (existing records preserved), "
                        + skipped + " malformed records skipped. Source YAML was not modified.");
            } catch (Exception e) {
                try { connection.rollback(); } catch (SQLException ignored) { }
                throw e;
            }
        }
    }

    @Override
    public Map<UUID, Selection> load(Collection<UUID> players) throws SQLException {
        Map<UUID, Selection> result = new HashMap<>();
        List<UUID> ids = new ArrayList<>(players);
        // Even an empty poll verifies connectivity, so outages recover without any online players.
        try (Connection connection = pool.getConnection()) {
            if (ids.isEmpty()) {
                try (PreparedStatement statement = prepare(connection, "SELECT 1"); ResultSet ignored = statement.executeQuery()) { }
            }
            for (int offset = 0; offset < ids.size(); offset += 250) {
                List<UUID> batch = ids.subList(offset, Math.min(ids.size(), offset + 250));
                for (UUID uuid : batch) result.put(uuid, Selection.ABSENT);
                String placeholders = String.join(",", Collections.nCopies(batch.size(), "?"));
                try (PreparedStatement statement = prepare(connection, "SELECT uuid, tag_identifier, revision FROM "
                        + playersTable + " WHERE uuid IN (" + placeholders + ")")) {
                    for (int i = 0; i < batch.size(); i++) statement.setString(i + 1, batch.get(i).toString());
                    try (ResultSet rows = statement.executeQuery()) {
                        while (rows.next()) {
                            result.put(UUID.fromString(rows.getString(1)), new Selection(rows.getString(2), rows.getLong(3)));
                        }
                    }
                }
            }
        }
        return result;
    }

    @Override
    public Selection write(UUID player, String identifier) throws SQLException {
        try (Connection connection = pool.getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement statement = prepare(connection, "INSERT INTO " + playersTable
                        + " (uuid, tag_identifier, revision) VALUES (?, ?, 1)"
                        + " ON DUPLICATE KEY UPDATE tag_identifier=?, revision=revision+1")) {
                    statement.setString(1, player.toString());
                    statement.setString(2, identifier);
                    statement.setString(3, identifier);
                    statement.executeUpdate();
                }
                Selection result;
                // The upsert's row lock remains held until commit, including this revision read.
                try (PreparedStatement statement = prepare(connection, "SELECT tag_identifier, revision FROM "
                        + playersTable + " WHERE uuid=?")) {
                    statement.setString(1, player.toString());
                    try (ResultSet rows = statement.executeQuery()) {
                        if (!rows.next()) throw new SQLException("Missing row after upsert");
                        result = new Selection(rows.getString(1), rows.getLong(2));
                    }
                }
                connection.commit();
                return result;
            } catch (SQLException e) {
                try { connection.rollback(); } catch (SQLException ignored) { }
                throw e;
            }
        }
    }

    @Override
    public void close() {
        if (pool != null) pool.close();
    }
}
