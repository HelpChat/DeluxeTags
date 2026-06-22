package me.clip.deluxetags.config;

import org.bukkit.configuration.file.FileConfiguration;

public class MySqlConfig {

  private static final String PLAYER_TAGS_TABLE = "player_tags";

  private final ConfigWrapper wrapper;

  public MySqlConfig(ConfigWrapper wrapper) {
    this.wrapper = wrapper;
    applyDefaults(config());
    wrapper.saveConfig();
  }

  public static void applyDefaults(FileConfiguration config) {
    addDefault(config, "enabled", false);
    addDefault(config, "host", "localhost");
    addDefault(config, "port", 3306);
    addDefault(config, "database", "deluxetags");
    addDefault(config, "username", "root");
    addDefault(config, "password", "");
    addDefault(config, "table_prefix", "deluxetags_");
    addDefault(config, "pool.maximum_pool_size", 5);
    addDefault(config, "migration.import_yaml_missing", true);
    addDefault(config, "fallback.replay_pending", true);
    config.options().copyDefaults(true);
  }

  private static void addDefault(FileConfiguration config, String path, Object value) {
    config.addDefault(path, value);
    if (!config.isSet(path)) {
      config.set(path, value);
    }
  }

  public void reload() {
    wrapper.reloadConfig();
    applyDefaults(wrapper.getConfig());
    wrapper.saveConfig();
  }

  public boolean enabled() {
    return config().getBoolean("enabled", false);
  }

  public String host() {
    return config().getString("host", "localhost");
  }

  public int port() {
    return config().getInt("port", 3306);
  }

  public String database() {
    return config().getString("database", "deluxetags");
  }

  public String username() {
    return config().getString("username", "root");
  }

  public String password() {
    return config().getString("password", "");
  }

  public int maximumPoolSize() {
    return Math.max(1, config().getInt("pool.maximum_pool_size", 5));
  }

  public boolean importYamlMissing() {
    return config().getBoolean("migration.import_yaml_missing", true);
  }

  public boolean replayPendingFallbackWrites() {
    return config().getBoolean("fallback.replay_pending", true);
  }

  public String tableName() {
    return sanitizeIdentifier(config().getString("table_prefix", "deluxetags_")) + PLAYER_TAGS_TABLE;
  }

  public boolean hasRequiredConnectionSettings() {
    return !isBlank(host()) && !isBlank(database()) && !isBlank(username());
  }

  public String jdbcUrl() {
    return "jdbc:mysql://" + host() + ":" + port() + "/" + database()
        + "?useUnicode=true&characterEncoding=utf8&serverTimezone=UTC";
  }

  static String sanitizeIdentifier(String value) {
    if (value == null || value.trim().isEmpty()) {
      return "";
    }

    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < value.length(); i++) {
      char character = value.charAt(i);
      if ((character >= 'a' && character <= 'z')
          || (character >= 'A' && character <= 'Z')
          || (character >= '0' && character <= '9')
          || character == '_') {
        builder.append(character);
      }
    }
    return builder.toString();
  }

  private static boolean isBlank(String value) {
    return value == null || value.trim().isEmpty();
  }

  private FileConfiguration config() {
    return wrapper.getConfig();
  }
}
