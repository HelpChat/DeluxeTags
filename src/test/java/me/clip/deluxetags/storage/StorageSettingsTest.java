package me.clip.deluxetags.storage;

import static org.junit.Assert.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.Test;

public class StorageSettingsTest {
    @Test public void defaultsPreserveYamlAndFiveSecondSync() {
        StorageSettings settings = new StorageSettings(new YamlConfiguration());
        assertFalse(settings.mysql);
        assertEquals(5, settings.interval);
        assertEquals(3306, settings.port);
        assertEquals("PREFERRED", settings.sslMode);
    }

    @Test public void rejectsInvalidConfigurationWithoutEchoingSecrets() {
        String[][] cases = {{"type", "unknown"}, {"mysql.table-prefix", "x`; DROP TABLE players;--"},
                {"mysql.ssl-mode", "password-secret"}, {"mysql.host", ""}, {"mysql.port", "not-a-number"},
                {"sync-interval-seconds", "five"}};
        for (String[] invalid : cases) {
            YamlConfiguration config = new YamlConfiguration();
            config.set("storage.type", "mysql");
            config.set("storage." + invalid[0], invalid[1]);
            try { new StorageSettings(config); fail("Accepted " + invalid[0]); }
            catch (IllegalArgumentException expected) { assertFalse(expected.getMessage().contains("password-secret")); }
        }
        String[] paths = {"mysql.port", "mysql.connect-timeout-ms", "mysql.socket-timeout-ms", "sync-interval-seconds"};
        for (String path : paths) {
            YamlConfiguration config = new YamlConfiguration();
            config.set("storage.type", "mysql");
            config.set("storage." + path, 0);
            try { new StorageSettings(config); fail("Accepted " + path); }
            catch (IllegalArgumentException expected) { }
        }
    }

    @Test public void detectsConnectionChangesForRestartMessage() {
        YamlConfiguration config = new YamlConfiguration();
        java.util.Map<String, Object> original = StorageSettings.snapshot(config);
        config.set("storage.mysql.password", "new-password");
        assertNotEquals(original, StorageSettings.snapshot(config));
    }
}
