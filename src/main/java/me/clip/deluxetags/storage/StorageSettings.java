package me.clip.deluxetags.storage;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;

public final class StorageSettings {
    public final boolean mysql;
    public final String host, database, username, password, prefix, sslMode;
    public final int port, connectTimeout, socketTimeout, interval;

    public static Map<String, Object> defaults() {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("type", "yaml");
        values.put("sync-interval-seconds", 5);
        values.put("mysql.host", "localhost");
        values.put("mysql.port", 3306);
        values.put("mysql.database", "deluxetags");
        values.put("mysql.username", "deluxetags");
        values.put("mysql.password", "");
        values.put("mysql.table-prefix", "deluxetags_");
        values.put("mysql.ssl-mode", "PREFERRED");
        values.put("mysql.connect-timeout-ms", 3000);
        values.put("mysql.socket-timeout-ms", 5000);
        return values;
    }

    public StorageSettings(ConfigurationSection config) {
        String type = config.getString("storage.type", "yaml");
        if (!"yaml".equalsIgnoreCase(type) && !"mysql".equalsIgnoreCase(type)) {
            throw new IllegalArgumentException("storage.type must be yaml or mysql");
        }
        mysql = "mysql".equalsIgnoreCase(type);
        host = config.getString("storage.mysql.host", "localhost");
        database = config.getString("storage.mysql.database", "deluxetags");
        username = config.getString("storage.mysql.username", "deluxetags");
        password = config.getString("storage.mysql.password", "");
        prefix = config.getString("storage.mysql.table-prefix", "deluxetags_");
        sslMode = config.getString("storage.mysql.ssl-mode", "PREFERRED").toUpperCase(Locale.ROOT);
        port = integer(config, "storage.mysql.port", 3306, mysql);
        connectTimeout = integer(config, "storage.mysql.connect-timeout-ms", 3000, mysql);
        socketTimeout = integer(config, "storage.mysql.socket-timeout-ms", 5000, mysql);
        interval = integer(config, "storage.sync-interval-seconds", 5, true);
        if (interval < 1 || interval > 3600) {
            throw new IllegalArgumentException("storage.sync-interval-seconds must be between 1 and 3600");
        }
        if (!mysql) return;
        if (host == null || host.trim().isEmpty() || database == null || database.trim().isEmpty()
                || username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("MySQL host, database and username must not be empty");
        }
        if (!prefix.matches("[A-Za-z0-9_]{0,40}")) {
            throw new IllegalArgumentException("MySQL table-prefix must contain at most 40 letters, digits or underscores");
        }
        if (port < 1 || port > 65535 || connectTimeout < 250 || connectTimeout > 5000
                || socketTimeout < 250 || socketTimeout > 5000) {
            throw new IllegalArgumentException("MySQL port must be 1-65535 and timeouts must be 250-5000 ms");
        }
        if (!Arrays.asList("DISABLED", "PREFERRED", "REQUIRED", "VERIFY_CA", "VERIFY_IDENTITY").contains(sslMode)) {
            throw new IllegalArgumentException("Invalid storage.mysql.ssl-mode");
        }
    }

    private static int integer(ConfigurationSection config, String path, int fallback, boolean validate) {
        if (validate && config.isSet(path) && !config.isInt(path)) {
            throw new IllegalArgumentException(path + " must be an integer");
        }
        return config.getInt(path, fallback);
    }

    /** Do not expose this snapshot in logs: it includes the password. */
    public static Map<String, Object> snapshot(ConfigurationSection config) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        defaults().forEach((key, value) -> snapshot.put(key, config.get("storage." + key, value)));
        return snapshot;
    }
}
