package me.clip.deluxetags.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.logging.Logger;
import me.clip.deluxetags.config.MySqlConfig;

public class MySqlSavedTagStorage implements SavedTagStorage {

  private static final int QUERY_BATCH_SIZE = 500;

  private final HikariDataSource dataSource;
  private final Executor executor;
  private final String tableName;

  public MySqlSavedTagStorage(MySqlConfig mysqlConfig, Logger logger, Executor executor)
      throws SQLException, ClassNotFoundException {
    this.executor = executor;
    this.tableName = mysqlConfig.tableName();

    Class.forName("com.mysql.cj.jdbc.Driver");

    HikariConfig hikariConfig = new HikariConfig();
    hikariConfig.setPoolName("DeluxeTags-MySQL");
    hikariConfig.setJdbcUrl(mysqlConfig.jdbcUrl());
    hikariConfig.setUsername(mysqlConfig.username());
    hikariConfig.setPassword(mysqlConfig.password());
    hikariConfig.setMaximumPoolSize(mysqlConfig.maximumPoolSize());
    hikariConfig.setMinimumIdle(1);
    hikariConfig.setConnectionTimeout(5000L);
    hikariConfig.setInitializationFailTimeout(5000L);

    dataSource = new HikariDataSource(hikariConfig);
    createTable();
    logger.info("DeluxeTags MySQL storage connected using table " + tableName + ".");
  }

  @Override
  public CompletableFuture<SavedTagEntry> load(final UUID uuid) {
    return CompletableFuture.supplyAsync(() -> {
      try {
        return loadSync(uuid);
      } catch (SQLException ex) {
        throw new StorageException(ex);
      }
    }, executor);
  }

  @Override
  public CompletableFuture<Map<UUID, SavedTagEntry>> loadAll(final Collection<UUID> uuids) {
    return CompletableFuture.supplyAsync(() -> {
      try {
        return loadAllSync(uuids);
      } catch (SQLException ex) {
        throw new StorageException(ex);
      }
    }, executor);
  }

  @Override
  public CompletableFuture<Void> save(final UUID uuid, final String tagIdentifier, final long updatedAtEpochMillis) {
    return CompletableFuture.runAsync(() -> {
      try {
        saveSync(uuid, tagIdentifier, updatedAtEpochMillis);
      } catch (SQLException ex) {
        throw new StorageException(ex);
      }
    }, executor);
  }

  @Override
  public CompletableFuture<Void> clear(final UUID uuid, final long updatedAtEpochMillis) {
    return save(uuid, null, updatedAtEpochMillis);
  }

  @Override
  public CompletableFuture<Void> clearAll(final Collection<UUID> uuids, final long updatedAtEpochMillis) {
    return CompletableFuture.runAsync(() -> {
      try {
        clearAllSync(uuids, updatedAtEpochMillis);
      } catch (SQLException ex) {
        throw new StorageException(ex);
      }
    }, executor);
  }

  @Override
  public CompletableFuture<Void> clearTagIdentifier(final String tagIdentifier, final long updatedAtEpochMillis) {
    return CompletableFuture.runAsync(() -> {
      try {
        clearTagIdentifierSync(tagIdentifier, updatedAtEpochMillis);
      } catch (SQLException ex) {
        throw new StorageException(ex);
      }
    }, executor);
  }

  @Override
  public CompletableFuture<Boolean> healthCheck() {
    return CompletableFuture.supplyAsync(() -> {
      try (Connection connection = dataSource.getConnection();
           PreparedStatement statement = connection.prepareStatement("SELECT 1")) {
        statement.executeQuery();
        return true;
      } catch (SQLException ex) {
        return false;
      }
    }, executor);
  }

  public SavedTagEntry loadSync(UUID uuid) throws SQLException {
    String sql = "SELECT tag_identifier, updated_at_epoch_ms FROM `" + tableName + "` WHERE uuid = ?";
    try (Connection connection = dataSource.getConnection();
         PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setBytes(1, toBytes(uuid));
      try (ResultSet resultSet = statement.executeQuery()) {
        if (resultSet.next()) {
          return new SavedTagEntry(uuid, resultSet.getString("tag_identifier"), resultSet.getLong("updated_at_epoch_ms"));
        }
      }
    }
    return null;
  }

  public Map<UUID, SavedTagEntry> loadAllSync(Collection<UUID> uuids) throws SQLException {
    Map<UUID, SavedTagEntry> entries = new HashMap<>();
    if (uuids == null || uuids.isEmpty()) {
      return entries;
    }

    List<UUID> batch = new ArrayList<>(QUERY_BATCH_SIZE);
    for (UUID uuid : uuids) {
      batch.add(uuid);
      if (batch.size() >= QUERY_BATCH_SIZE) {
        entries.putAll(loadBatchSync(batch));
        batch.clear();
      }
    }

    if (!batch.isEmpty()) {
      entries.putAll(loadBatchSync(batch));
    }

    return entries;
  }

  public void saveSync(UUID uuid, String tagIdentifier, long updatedAtEpochMillis) throws SQLException {
    String sql = "INSERT INTO `" + tableName + "` (uuid, tag_identifier, updated_at_epoch_ms) VALUES (?, ?, ?) "
        + "ON DUPLICATE KEY UPDATE tag_identifier = VALUES(tag_identifier), updated_at_epoch_ms = VALUES(updated_at_epoch_ms)";

    try (Connection connection = dataSource.getConnection();
         PreparedStatement statement = connection.prepareStatement(sql)) {
      bindEntry(statement, new SavedTagEntry(uuid, tagIdentifier, updatedAtEpochMillis));
      statement.executeUpdate();
    }
  }

  public void saveIfNewerSync(SavedTagEntry entry) throws SQLException {
    String sql = "INSERT INTO `" + tableName + "` (uuid, tag_identifier, updated_at_epoch_ms) VALUES (?, ?, ?) "
        + "ON DUPLICATE KEY UPDATE "
        + "tag_identifier = IF(updated_at_epoch_ms <= VALUES(updated_at_epoch_ms), VALUES(tag_identifier), tag_identifier), "
        + "updated_at_epoch_ms = IF(updated_at_epoch_ms <= VALUES(updated_at_epoch_ms), VALUES(updated_at_epoch_ms), updated_at_epoch_ms)";

    try (Connection connection = dataSource.getConnection();
         PreparedStatement statement = connection.prepareStatement(sql)) {
      bindEntry(statement, entry);
      statement.executeUpdate();
    }
  }

  public void clearAllSync(Collection<UUID> uuids, long updatedAtEpochMillis) throws SQLException {
    if (uuids == null || uuids.isEmpty()) {
      return;
    }

    String sql = "INSERT INTO `" + tableName + "` (uuid, tag_identifier, updated_at_epoch_ms) VALUES (?, NULL, ?) "
        + "ON DUPLICATE KEY UPDATE tag_identifier = NULL, updated_at_epoch_ms = VALUES(updated_at_epoch_ms)";

    try (Connection connection = dataSource.getConnection();
         PreparedStatement statement = connection.prepareStatement(sql)) {
      for (UUID uuid : uuids) {
        statement.setBytes(1, toBytes(uuid));
        statement.setLong(2, updatedAtEpochMillis);
        statement.addBatch();
      }
      statement.executeBatch();
    }
  }

  public void clearTagIdentifierSync(String tagIdentifier, long updatedAtEpochMillis) throws SQLException {
    if (tagIdentifier == null || tagIdentifier.trim().isEmpty()) {
      return;
    }

    String sql = "UPDATE `" + tableName + "` SET tag_identifier = NULL, updated_at_epoch_ms = ? WHERE tag_identifier = ?";
    try (Connection connection = dataSource.getConnection();
         PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setLong(1, updatedAtEpochMillis);
      statement.setString(2, tagIdentifier);
      statement.executeUpdate();
    }
  }

  public void clearTagIdentifierIfOlderSync(String tagIdentifier, long updatedAtEpochMillis) throws SQLException {
    if (tagIdentifier == null || tagIdentifier.trim().isEmpty()) {
      return;
    }

    String sql = "UPDATE `" + tableName + "` SET tag_identifier = NULL, updated_at_epoch_ms = ? "
        + "WHERE tag_identifier = ? AND updated_at_epoch_ms <= ?";
    try (Connection connection = dataSource.getConnection();
         PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setLong(1, updatedAtEpochMillis);
      statement.setString(2, tagIdentifier);
      statement.setLong(3, updatedAtEpochMillis);
      statement.executeUpdate();
    }
  }

  public void importMissingSync(Map<UUID, SavedTagEntry> entries) throws SQLException {
    if (entries == null || entries.isEmpty()) {
      return;
    }

    String sql = "INSERT IGNORE INTO `" + tableName + "` (uuid, tag_identifier, updated_at_epoch_ms) VALUES (?, ?, ?)";
    try (Connection connection = dataSource.getConnection();
         PreparedStatement statement = connection.prepareStatement(sql)) {
      for (SavedTagEntry entry : entries.values()) {
        if (entry == null || !entry.hasTagIdentifier()) {
          continue;
        }
        bindEntry(statement, entry);
        statement.addBatch();
      }
      statement.executeBatch();
    }
  }

  private Map<UUID, SavedTagEntry> loadBatchSync(List<UUID> uuids) throws SQLException {
    Map<UUID, SavedTagEntry> entries = new HashMap<>();
    if (uuids.isEmpty()) {
      return entries;
    }

    StringBuilder placeholders = new StringBuilder();
    for (int i = 0; i < uuids.size(); i++) {
      if (i > 0) {
        placeholders.append(", ");
      }
      placeholders.append("?");
    }

    String sql = "SELECT uuid, tag_identifier, updated_at_epoch_ms FROM `" + tableName + "` WHERE uuid IN (" + placeholders + ")";
    try (Connection connection = dataSource.getConnection();
         PreparedStatement statement = connection.prepareStatement(sql)) {
      for (int i = 0; i < uuids.size(); i++) {
        statement.setBytes(i + 1, toBytes(uuids.get(i)));
      }

      try (ResultSet resultSet = statement.executeQuery()) {
        while (resultSet.next()) {
          UUID uuid = fromBytes(resultSet.getBytes("uuid"));
          entries.put(uuid, new SavedTagEntry(uuid, resultSet.getString("tag_identifier"), resultSet.getLong("updated_at_epoch_ms")));
        }
      }
    }
    return entries;
  }

  private void createTable() throws SQLException {
    String sql = "CREATE TABLE IF NOT EXISTS `" + tableName + "` ("
        + "uuid BINARY(16) NOT NULL,"
        + "tag_identifier VARCHAR(128) NULL,"
        + "updated_at_epoch_ms BIGINT NOT NULL,"
        + "PRIMARY KEY (uuid),"
        + "INDEX idx_tag_identifier (tag_identifier)"
        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";

    try (Connection connection = dataSource.getConnection();
         Statement statement = connection.createStatement()) {
      statement.executeUpdate(sql);
    }
  }

  private static void bindEntry(PreparedStatement statement, SavedTagEntry entry) throws SQLException {
    statement.setBytes(1, toBytes(entry.getUuid()));
    if (entry.hasTagIdentifier()) {
      statement.setString(2, entry.getTagIdentifier());
    } else {
      statement.setNull(2, java.sql.Types.VARCHAR);
    }
    statement.setLong(3, entry.getUpdatedAtEpochMillis());
  }

  private static byte[] toBytes(UUID uuid) {
    ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[16]);
    byteBuffer.putLong(uuid.getMostSignificantBits());
    byteBuffer.putLong(uuid.getLeastSignificantBits());
    return byteBuffer.array();
  }

  private static UUID fromBytes(byte[] bytes) {
    ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
    long mostSignificant = byteBuffer.getLong();
    long leastSignificant = byteBuffer.getLong();
    return new UUID(mostSignificant, leastSignificant);
  }

  @Override
  public void close() {
    dataSource.close();
  }

  private static class StorageException extends RuntimeException {
    StorageException(Throwable cause) {
      super(cause);
    }
  }
}
