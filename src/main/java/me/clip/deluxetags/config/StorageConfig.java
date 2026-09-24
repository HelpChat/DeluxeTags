package me.clip.deluxetags.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import me.clip.deluxetags.storage.StorageSettings;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

/** Loads and validates the standalone mysql.yml storage configuration. */
public final class StorageConfig {
    private StorageConfig() { }

    public static YamlConfiguration load(File file)
            throws IOException, InvalidConfigurationException {
        YamlConfiguration config = new YamlConfiguration();
        if (file.exists()) config.load(file);
        if (config.isSet("storage") && !config.isConfigurationSection("storage")) {
            throw new IllegalArgumentException("mysql.yml storage must be a configuration section");
        }
        StorageSettings.defaults().forEach((key, value) -> {
            if (!config.isSet("storage." + key)) config.set("storage." + key, value);
        });
        new StorageSettings(config); // Validate before saving settings.
        config.options().header("DeluxeTags player selection storage\n"
                + "Use storage.type: yaml or mysql. All storage setting changes require a restart.\n"
                + "MySQL imports userdata/player_tags.yml once per installation and database/table prefix.\n"
                + "Shared selections refresh every sync-interval-seconds; database outages pause changes.\n"
                + "TLS modes: DISABLED, PREFERRED, REQUIRED, VERIFY_CA, VERIFY_IDENTITY.\n"
                + "Connection/socket timeouts: 250-5000 milliseconds.");
        Path target = file.toPath().toAbsolutePath();
        Files.createDirectories(target.getParent());
        Path temporary = Files.createTempFile(target.getParent(), "mysql-", ".tmp");
        try {
            config.save(temporary.toFile());
            try {
                Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
        return config;
    }
}
