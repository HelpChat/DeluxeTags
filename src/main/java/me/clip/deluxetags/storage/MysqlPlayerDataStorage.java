package me.clip.deluxetags.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.ConfigurationSection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;

public class MysqlPlayerDataStorage implements PlayerDataStorage {

    private final ConfigurationSection config;
    private final String tableName;
    private HikariDataSource dataSource;

    public MysqlPlayerDataStorage(ConfigurationSection config) {
        this.config = config;
        this.tableName = validateTableName(config.getString("table_prefix", "deluxetags_") + "player_data");
    }

    @Override
    public void initialize() throws Exception {
        HikariConfig hikariConfig = new HikariConfig();
        String host = config.getString("host", "localhost");
        int port = config.getInt("port", 3306);
        String database = config.getString("database", "deluxetags");
        boolean useSsl = config.getBoolean("use_ssl", false);
        hikariConfig.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database
                + "?useSSL=" + useSsl + "&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=utf8");
        hikariConfig.setUsername(config.getString("username", "root"));
        hikariConfig.setPassword(config.getString("password", "password"));
        hikariConfig.setMaximumPoolSize(Math.max(1, config.getInt("pool_size", 10)));
        hikariConfig.setMinimumIdle(1);
        hikariConfig.setPoolName("DeluxeTags-MySQL");
        hikariConfig.setConnectionTimeout(10000L);
        dataSource = new HikariDataSource(hikariConfig);

        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS `" + tableName + "` ("
                    + "`player_uuid` CHAR(36) NOT NULL,"
                    + "`tag_identifier` VARCHAR(255) NULL,"
                    + "`data` JSON NULL,"
                    + "`updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,"
                    + "PRIMARY KEY (`player_uuid`)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
        }
    }

    @Override
    public PlayerData load(UUID uuid) throws Exception {
        String sql = "SELECT `tag_identifier`, `data` FROM `" + tableName + "` WHERE `player_uuid` = ?";
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? new PlayerData(result.getString("tag_identifier"), result.getString("data")) : null;
            }
        }
    }

    @Override
    public void save(UUID uuid, PlayerData playerData) throws Exception {
        String sql = "INSERT INTO `" + tableName + "` (`player_uuid`, `tag_identifier`, `data`) VALUES (?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE `tag_identifier` = VALUES(`tag_identifier`), `data` = VALUES(`data`)";
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            statement.setString(2, playerData.getTagIdentifier());
            statement.setString(3, playerData.getData());
            statement.executeUpdate();
        }
    }

    @Override
    public void delete(UUID uuid) throws Exception {
        String sql = "DELETE FROM `" + tableName + "` WHERE `player_uuid` = ?";
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            statement.executeUpdate();
        }
    }

    @Override
    public void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    private static String validateTableName(String tableName) {
        if (!tableName.matches("[A-Za-z0-9_]+")) {
            throw new IllegalArgumentException("MySQL table prefix may only contain letters, numbers, and underscores");
        }
        return tableName;
    }
}