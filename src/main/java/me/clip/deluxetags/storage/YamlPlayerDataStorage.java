package me.clip.deluxetags.storage;

import me.clip.deluxetags.config.ConfigWrapper;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.UUID;

public class YamlPlayerDataStorage implements PlayerDataStorage {

    private final ConfigWrapper playerFile;

    public YamlPlayerDataStorage(ConfigWrapper playerFile) {
        this.playerFile = playerFile;
    }

    @Override
    public void initialize() {
    }

    @Override
    public synchronized PlayerData load(UUID uuid) {
        FileConfiguration config = playerFile.getConfig();
        String path = uuid.toString();
        String tagIdentifier = config.isString(path) ? config.getString(path) : null;
        return tagIdentifier == null ? null : new PlayerData(tagIdentifier, null);
    }

    @Override
    public synchronized void save(UUID uuid, PlayerData playerData) {
        playerFile.getConfig().set(uuid.toString(), playerData.getTagIdentifier());
        playerFile.saveConfig();
    }

    @Override
    public synchronized void delete(UUID uuid) {
        playerFile.getConfig().set(uuid.toString(), null);
        playerFile.saveConfig();
    }

    @Override
    public void close() {
    }
}