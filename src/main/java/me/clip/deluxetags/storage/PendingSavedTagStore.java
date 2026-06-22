package me.clip.deluxetags.storage;

import java.util.Collection;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import me.clip.deluxetags.config.ConfigWrapper;
import org.bukkit.configuration.file.FileConfiguration;

public class PendingSavedTagStore {

  private static final String TAG_CLEARS_PATH = "tag_clears";

  private final ConfigWrapper pendingFile;
  private final FileConfiguration memoryConfig;
  private final Object lock = new Object();

  public PendingSavedTagStore(ConfigWrapper pendingFile) {
    this.pendingFile = pendingFile;
    this.memoryConfig = null;
  }

  PendingSavedTagStore(FileConfiguration memoryConfig) {
    this.pendingFile = null;
    this.memoryConfig = memoryConfig;
  }

  public void record(SavedTagEntry entry) {
    if (entry == null) {
      return;
    }

    synchronized (lock) {
      String path = entry.getUuid().toString();
      FileConfiguration config = getConfig();
      config.set(path + ".tag_identifier", entry.getTagIdentifier());
      config.set(path + ".cleared", !entry.hasTagIdentifier());
      config.set(path + ".updated_at_epoch_ms", entry.getUpdatedAtEpochMillis());
      save();
    }
  }

  public void recordAll(Collection<SavedTagEntry> entries) {
    if (entries == null || entries.isEmpty()) {
      return;
    }

    synchronized (lock) {
      FileConfiguration config = getConfig();
      for (SavedTagEntry entry : entries) {
        if (entry == null) {
          continue;
        }

        String path = entry.getUuid().toString();
        config.set(path + ".tag_identifier", entry.getTagIdentifier());
        config.set(path + ".cleared", !entry.hasTagIdentifier());
        config.set(path + ".updated_at_epoch_ms", entry.getUpdatedAtEpochMillis());
      }
      save();
    }
  }

  public void recordTagClear(String tagIdentifier, long updatedAtEpochMillis) {
    if (tagIdentifier == null || tagIdentifier.trim().isEmpty()) {
      return;
    }

    synchronized (lock) {
      String key = encodeTagIdentifier(tagIdentifier);
      String path = TAG_CLEARS_PATH + "." + key;
      FileConfiguration config = getConfig();
      long currentTimestamp = config.getLong(path + ".updated_at_epoch_ms", Long.MIN_VALUE);
      if (currentTimestamp > updatedAtEpochMillis) {
        return;
      }

      config.set(path + ".tag_identifier", tagIdentifier);
      config.set(path + ".updated_at_epoch_ms", updatedAtEpochMillis);
      save();
    }
  }

  public Map<UUID, SavedTagEntry> loadAll() {
    Map<UUID, SavedTagEntry> entries = new LinkedHashMap<>();
    synchronized (lock) {
      FileConfiguration config = getConfig();
      for (String key : config.getKeys(false)) {
        if (TAG_CLEARS_PATH.equals(key)) {
          continue;
        }

        try {
          UUID uuid = UUID.fromString(key);
          boolean cleared = config.getBoolean(key + ".cleared", false);
          String tagIdentifier = cleared ? null : config.getString(key + ".tag_identifier");
          long updatedAt = config.getLong(key + ".updated_at_epoch_ms", 0L);
          entries.put(uuid, new SavedTagEntry(uuid, tagIdentifier, updatedAt));
        } catch (IllegalArgumentException ignored) {
        }
      }
    }
    return entries;
  }

  public Map<String, PendingTagClear> loadTagClears() {
    Map<String, PendingTagClear> entries = new LinkedHashMap<>();
    synchronized (lock) {
      FileConfiguration config = getConfig();
      if (!config.isConfigurationSection(TAG_CLEARS_PATH)) {
        return entries;
      }

      for (String key : config.getConfigurationSection(TAG_CLEARS_PATH).getKeys(false)) {
        String path = TAG_CLEARS_PATH + "." + key;
        String tagIdentifier = config.getString(path + ".tag_identifier");
        if (tagIdentifier == null || tagIdentifier.trim().isEmpty()) {
          continue;
        }

        long updatedAt = config.getLong(path + ".updated_at_epoch_ms", 0L);
        entries.put(key, new PendingTagClear(key, tagIdentifier, updatedAt));
      }
    }
    return entries;
  }

  public void removeAll(Collection<UUID> uuids) {
    if (uuids == null || uuids.isEmpty()) {
      return;
    }

    synchronized (lock) {
      FileConfiguration config = getConfig();
      for (UUID uuid : uuids) {
        config.set(uuid.toString(), null);
      }
      save();
    }
  }

  public void removeTagClears(Collection<String> keys) {
    if (keys == null || keys.isEmpty()) {
      return;
    }

    synchronized (lock) {
      FileConfiguration config = getConfig();
      for (String key : keys) {
        config.set(TAG_CLEARS_PATH + "." + key, null);
      }

      if (config.isConfigurationSection(TAG_CLEARS_PATH)
          && config.getConfigurationSection(TAG_CLEARS_PATH).getKeys(false).isEmpty()) {
        config.set(TAG_CLEARS_PATH, null);
      }
      save();
    }
  }

  private static String encodeTagIdentifier(String tagIdentifier) {
    return Base64.getUrlEncoder().withoutPadding().encodeToString(tagIdentifier.getBytes(java.nio.charset.StandardCharsets.UTF_8));
  }

  private FileConfiguration getConfig() {
    return memoryConfig != null ? memoryConfig : pendingFile.getConfig();
  }

  private void save() {
    if (pendingFile != null) {
      pendingFile.saveConfig();
    }
  }

  public static class PendingTagClear {
    private final String key;
    private final String tagIdentifier;
    private final long updatedAtEpochMillis;

    PendingTagClear(String key, String tagIdentifier, long updatedAtEpochMillis) {
      this.key = key;
      this.tagIdentifier = tagIdentifier;
      this.updatedAtEpochMillis = updatedAtEpochMillis;
    }

    public String getKey() {
      return key;
    }

    public String getTagIdentifier() {
      return tagIdentifier;
    }

    public long getUpdatedAtEpochMillis() {
      return updatedAtEpochMillis;
    }
  }
}
