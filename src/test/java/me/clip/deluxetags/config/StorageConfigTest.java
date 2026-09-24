package me.clip.deluxetags.config;

import static org.junit.Assert.*;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import me.clip.deluxetags.storage.StorageSettings;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class StorageConfigTest {
    @Rule public TemporaryFolder temp = new TemporaryFolder();

    @Test public void createsStorageDefaults() throws Exception {
        File file = new File(temp.getRoot(), "mysql.yml");
        YamlConfiguration loaded = StorageConfig.load(file);
        assertTrue(file.isFile());
        assertFalse(new StorageSettings(loaded).mysql);
        assertEquals(5, loaded.getInt("storage.sync-interval-seconds"));
    }

    @Test public void preservesConfiguredCredentialsAcrossReloads() throws Exception {
        File file = new File(temp.getRoot(), "mysql.yml");
        YamlConfiguration configured = new YamlConfiguration();
        configured.set("storage.type", "mysql");
        configured.set("storage.mysql.password", "special: # password ' value");
        configured.set("storage.mysql.port", 3307);
        configured.save(file);
        StorageConfig.load(file);
        StorageSettings reloaded = new StorageSettings(StorageConfig.load(file));
        assertEquals("special: # password ' value", reloaded.password);
        assertEquals(3307, reloaded.port);
        assertTrue(reloaded.mysql);
    }

    @Test public void invalidYamlIsNotOverwritten() throws Exception {
        File file = temp.newFile("mysql.yml");
        byte[] original = "storage: [\n".getBytes(StandardCharsets.UTF_8);
        Files.write(file.toPath(), original);
        try { StorageConfig.load(file); fail("Malformed YAML accepted"); }
        catch (InvalidConfigurationException expected) { }
        assertArrayEquals(original, Files.readAllBytes(file.toPath()));
    }
}
