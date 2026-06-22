package me.clip.deluxetags.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.Test;

public class MySqlConfigTest {

  @Test
  public void appliesMysqlDefaultsToSeparateConfig() {
    YamlConfiguration config = new YamlConfiguration();

    MySqlConfig.applyDefaults(config);

    assertFalse(config.getBoolean("enabled"));
    assertEquals("localhost", config.getString("host"));
    assertEquals(3306, config.getInt("port"));
    assertEquals("deluxetags", config.getString("database"));
    assertEquals("root", config.getString("username"));
    assertEquals("", config.getString("password"));
    assertEquals("deluxetags_", config.getString("table_prefix"));
    assertEquals(5, config.getInt("pool.maximum_pool_size"));
    assertTrue(config.getBoolean("migration.import_yaml_missing"));
    assertTrue(config.getBoolean("fallback.replay_pending"));
  }

  @Test
  public void keepsConfiguredMysqlValuesWhenApplyingDefaults() {
    YamlConfiguration config = new YamlConfiguration();
    config.set("enabled", true);
    config.set("host", "db.example.test");
    config.set("pool.maximum_pool_size", 12);

    MySqlConfig.applyDefaults(config);

    assertTrue(config.getBoolean("enabled"));
    assertEquals("db.example.test", config.getString("host"));
    assertEquals(12, config.getInt("pool.maximum_pool_size"));
  }

  @Test
  public void sanitizesTablePrefixForSqlIdentifierUse() {
    assertEquals("deluxetags_", MySqlConfig.sanitizeIdentifier("deluxe-tags.;_"));
    assertEquals("", MySqlConfig.sanitizeIdentifier("../"));
  }
}
