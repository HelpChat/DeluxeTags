package me.clip.deluxetags.storage;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import me.clip.deluxetags.config.ConfigWrapper;
import org.bukkit.configuration.file.FileConfiguration;

public class YamlSavedTagStorage implements SavedTagStorage {

  private final ConfigWrapper playerFile;
  private final Executor executor;
  private final Object lock = new Object();

  public YamlSavedTagStorage(ConfigWrapper playerFile, Executor executor) {
    this.playerFile = playerFile;
    this.executor = executor;
  }

  @Override
  public CompletableFuture<SavedTagEntry> load(final UUID uuid) {
    return CompletableFuture.supplyAsync(() -> loadSync(uuid), executor);
  }

  @Override
  public CompletableFuture<Map<UUID, SavedTagEntry>> loadAll(final Collection<UUID> uuids) {
    return CompletableFuture.supplyAsync(() -> loadAllSync(uuids), executor);
  }

  @Override
  public CompletableFuture<Void> save(final UUID uuid, final String tagIdentifier, final long updatedAtEpochMillis) {
    return CompletableFuture.runAsync(() -> saveSync(uuid, tagIdentifier, updatedAtEpochMillis), executor);
  }

  @Override
  public CompletableFuture<Void> clear(final UUID uuid, final long updatedAtEpochMillis) {
    return save(uuid, null, updatedAtEpochMillis);
  }

  @Override
  public CompletableFuture<Void> clearAll(final Collection<UUID> uuids, final long updatedAtEpochMillis) {
    return CompletableFuture.runAsync(() -> clearAllSync(uuids, updatedAtEpochMillis), executor);
  }

  @Override
  public CompletableFuture<Void> clearTagIdentifier(final String tagIdentifier, final long updatedAtEpochMillis) {
    return CompletableFuture.runAsync(() -> clearTagIdentifierSync(tagIdentifier, updatedAtEpochMillis), executor);
  }

  @Override
  public CompletableFuture<Boolean> healthCheck() {
    return CompletableFuture.completedFuture(true);
  }

  public SavedTagEntry loadSync(UUID uuid) {
    synchronized (lock) {
      FileConfiguration config = playerFile.getConfig();
      String key = uuid.toString();
      if (config.contains(key) && config.isString(key) && config.getString(key) != null) {
        return new SavedTagEntry(uuid, config.getString(key), 0L);
      }
      return null;
    }
  }

  public Map<UUID, SavedTagEntry> loadAllSync(Collection<UUID> uuids) {
    Map<UUID, SavedTagEntry> entries = new HashMap<>();
    if (uuids == null || uuids.isEmpty()) {
      return entries;
    }

    synchronized (lock) {
      FileConfiguration config = playerFile.getConfig();
      for (UUID uuid : uuids) {
        String key = uuid.toString();
        if (config.contains(key) && config.isString(key) && config.getString(key) != null) {
          entries.put(uuid, new SavedTagEntry(uuid, config.getString(key), 0L));
        }
      }
    }
    return entries;
  }

  public Map<UUID, SavedTagEntry> loadAllSavedSync(long importedAtEpochMillis) {
    Map<UUID, SavedTagEntry> entries = new LinkedHashMap<>();
    synchronized (lock) {
      FileConfiguration config = playerFile.getConfig();
      for (String key : config.getKeys(false)) {
        if (!config.isString(key) || config.getString(key) == null) {
          continue;
        }

        try {
          UUID uuid = UUID.fromString(key);
          entries.put(uuid, new SavedTagEntry(uuid, config.getString(key), importedAtEpochMillis));
        } catch (IllegalArgumentException ignored) {
        }
      }
    }
    return entries;
  }

  public Map<UUID, SavedTagEntry> findByTagIdentifierSync(String tagIdentifier, long updatedAtEpochMillis) {
    Map<UUID, SavedTagEntry> entries = new LinkedHashMap<>();
    if (tagIdentifier == null) {
      return entries;
    }

    synchronized (lock) {
      FileConfiguration config = playerFile.getConfig();
      for (String key : config.getKeys(false)) {
        if (!config.isString(key) || config.getString(key) == null) {
          continue;
        }

        String savedIdentifier = config.getString(key);
        if (!tagIdentifier.equalsIgnoreCase(savedIdentifier)) {
          continue;
        }

        try {
          UUID uuid = UUID.fromString(key);
          entries.put(uuid, new SavedTagEntry(uuid, null, updatedAtEpochMillis));
        } catch (IllegalArgumentException ignored) {
        }
      }
    }
    return entries;
  }

  public void saveSync(UUID uuid, String tagIdentifier, long updatedAtEpochMillis) {
    synchronized (lock) {
      playerFile.getConfig().set(uuid.toString(), tagIdentifier);
      playerFile.saveConfig();
    }
  }

  public void clearAllSync(Collection<UUID> uuids, long updatedAtEpochMillis) {
    if (uuids == null || uuids.isEmpty()) {
      return;
    }

    boolean requiresSave = false;
    synchronized (lock) {
      FileConfiguration config = playerFile.getConfig();
      for (UUID uuid : uuids) {
        String key = uuid.toString();
        if (config.contains(key)) {
          config.set(key, null);
          requiresSave = true;
        }
      }

      if (requiresSave) {
        playerFile.saveConfig();
      }
    }
  }

  public void clearTagIdentifierSync(String tagIdentifier, long updatedAtEpochMillis) {
    if (tagIdentifier == null) {
      return;
    }

    boolean requiresSave = false;
    synchronized (lock) {
      FileConfiguration config = playerFile.getConfig();
      for (String key : config.getKeys(false)) {
        if (!config.isString(key) || config.getString(key) == null) {
          continue;
        }

        if (tagIdentifier.equalsIgnoreCase(config.getString(key))) {
          config.set(key, null);
          requiresSave = true;
        }
      }

      if (requiresSave) {
        playerFile.saveConfig();
      }
    }
  }

  @Override
  public void close() {
  }
}
