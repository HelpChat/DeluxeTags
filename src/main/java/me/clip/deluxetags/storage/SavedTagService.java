package me.clip.deluxetags.storage;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import me.clip.deluxetags.DeluxeTags;
import me.clip.deluxetags.config.ConfigWrapper;
import me.clip.deluxetags.config.MySqlConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class SavedTagService {

  private final DeluxeTags plugin;
  private final YamlSavedTagStorage yamlStorage;
  private final SavedTagStorage activeStorage;
  private final PendingSavedTagStore pendingStore;
  private final boolean recordPendingWrites;
  private final boolean mysqlStorageActive;
  private final ConcurrentMap<UUID, SavedTagEntry> cache = new ConcurrentHashMap<>();

  private SavedTagService(
      DeluxeTags plugin,
      YamlSavedTagStorage yamlStorage,
      SavedTagStorage activeStorage,
      PendingSavedTagStore pendingStore,
      boolean recordPendingWrites,
      boolean mysqlStorageActive
  ) {
    this.plugin = plugin;
    this.yamlStorage = yamlStorage;
    this.activeStorage = activeStorage;
    this.pendingStore = pendingStore;
    this.recordPendingWrites = recordPendingWrites;
    this.mysqlStorageActive = mysqlStorageActive;
  }

  public static SavedTagService create(
      DeluxeTags plugin,
      ConfigWrapper playerFile,
      ConfigWrapper mysqlFile,
      ConfigWrapper pendingFile
  ) {
    Executor executor = command -> Bukkit.getScheduler().runTaskAsynchronously(plugin, command);
    YamlSavedTagStorage yamlStorage = new YamlSavedTagStorage(playerFile, executor);
    PendingSavedTagStore pendingStore = new PendingSavedTagStore(pendingFile);
    MySqlConfig mysqlConfig = new MySqlConfig(mysqlFile);

    if (!mysqlConfig.enabled()) {
      plugin.getLogger().info("DeluxeTags MySQL storage is disabled. Using local YAML storage.");
      return new SavedTagService(plugin, yamlStorage, yamlStorage, pendingStore, false, false);
    }

    if (!mysqlConfig.hasRequiredConnectionSettings()) {
      plugin.getLogger().warning("DeluxeTags MySQL storage is enabled but mysql.yml is missing required connection settings. Using local YAML storage.");
      return new SavedTagService(plugin, yamlStorage, yamlStorage, pendingStore, mysqlConfig.replayPendingFallbackWrites(), false);
    }

    try {
      MySqlSavedTagStorage mySqlStorage = new MySqlSavedTagStorage(mysqlConfig, plugin.getLogger(), executor);
      if (mysqlConfig.replayPendingFallbackWrites()) {
        replayPendingWrites(plugin, mySqlStorage, pendingStore);
      }

      if (mysqlConfig.importYamlMissing()) {
        importMissingYamlRows(plugin, mySqlStorage, yamlStorage);
      }

      return new SavedTagService(plugin, yamlStorage, mySqlStorage, pendingStore, false, true);
    } catch (Exception ex) {
      plugin.getLogger().log(Level.WARNING, "DeluxeTags could not connect to MySQL. Using local YAML storage for this run.", ex);
      return new SavedTagService(plugin, yamlStorage, yamlStorage, pendingStore, mysqlConfig.replayPendingFallbackWrites(), false);
    }
  }

  public boolean isMysqlStorageActive() {
    return mysqlStorageActive;
  }

  public String getCachedTagIdentifier(UUID uuid) {
    SavedTagEntry cachedEntry = cache.get(uuid);
    if (cachedEntry != null) {
      return cachedEntry.getTagIdentifier();
    }

    if (activeStorage == yamlStorage) {
      SavedTagEntry yamlEntry = yamlStorage.loadSync(uuid);
      if (yamlEntry != null) {
        cache.put(uuid, yamlEntry);
        return yamlEntry.getTagIdentifier();
      }
    }

    return null;
  }

  public void saveTagIdentifier(UUID uuid, String tagIdentifier) {
    SavedTagEntry entry = new SavedTagEntry(uuid, tagIdentifier, System.currentTimeMillis());
    cache.put(uuid, entry);
    writeEntry(entry);
  }

  public void clearTag(UUID uuid) {
    if (!hasKnownSavedState(uuid)) {
      cache.remove(uuid);
      return;
    }

    SavedTagEntry entry = new SavedTagEntry(uuid, null, System.currentTimeMillis());
    cache.put(uuid, entry);
    writeEntry(entry);
  }

  public void clearTags(Collection<UUID> uuids) {
    if (uuids == null || uuids.isEmpty()) {
      return;
    }

    long timestamp = System.currentTimeMillis();
    Collection<SavedTagEntry> pendingEntries = new ArrayList<>();
    for (UUID uuid : uuids) {
      SavedTagEntry entry = new SavedTagEntry(uuid, null, timestamp);
      cache.put(uuid, entry);
      pendingEntries.add(entry);
    }

    if (recordPendingWrites) {
      pendingStore.recordAll(pendingEntries);
    }

    activeStorage.clearAll(uuids, timestamp).whenComplete((unused, throwable) -> {
      if (throwable != null) {
        handleBatchWriteFailure(pendingEntries, throwable);
      }
    });
  }

  public void clearTagIdentifier(String tagIdentifier) {
    if (tagIdentifier == null || tagIdentifier.trim().isEmpty()) {
      return;
    }

    long timestamp = System.currentTimeMillis();
    for (Map.Entry<UUID, SavedTagEntry> cacheEntry : cache.entrySet()) {
      SavedTagEntry savedTagEntry = cacheEntry.getValue();
      if (savedTagEntry != null && tagIdentifier.equalsIgnoreCase(savedTagEntry.getTagIdentifier())) {
        cache.put(cacheEntry.getKey(), new SavedTagEntry(cacheEntry.getKey(), null, timestamp));
      }
    }

    if (recordPendingWrites) {
      pendingStore.recordTagClear(tagIdentifier, timestamp);
      pendingStore.recordAll(yamlStorage.findByTagIdentifierSync(tagIdentifier, timestamp).values());
    }

    activeStorage.clearTagIdentifier(tagIdentifier, timestamp).whenComplete((unused, throwable) -> {
      if (throwable != null) {
        plugin.getLogger().log(Level.WARNING, "Could not clear saved DeluxeTags identifier from storage. It will be recorded for replay.", throwable);
        pendingStore.recordTagClear(tagIdentifier, timestamp);
      }
    });
  }

  public void refreshPlayer(Player player) {
    if (player == null) {
      return;
    }

    UUID uuid = player.getUniqueId();
    long loadStartedAt = System.currentTimeMillis();
    activeStorage.load(uuid).whenComplete((entry, throwable) -> {
      if (throwable != null) {
        plugin.getLogger().log(Level.WARNING, "Could not load saved DeluxeTags identifier for " + player.getName() + ".", throwable);
        return;
      }

      applyLoadedEntry(uuid, entry, loadStartedAt);
      Bukkit.getScheduler().runTask(plugin, () -> {
        Player onlinePlayer = Bukkit.getPlayer(uuid);
        if (onlinePlayer != null) {
          plugin.getTagsHandler().updateTagForPlayer(onlinePlayer, true);
        }
      });
    });
  }

  public void refreshPlayers(Collection<? extends Player> players) {
    if (players == null || players.isEmpty()) {
      return;
    }

    Map<UUID, String> onlineNames = new java.util.HashMap<>();
    for (Player player : players) {
      onlineNames.put(player.getUniqueId(), player.getName());
    }

    long loadStartedAt = System.currentTimeMillis();
    activeStorage.loadAll(onlineNames.keySet()).whenComplete((entries, throwable) -> {
      if (throwable != null) {
        plugin.getLogger().log(Level.WARNING, "Could not bulk load saved DeluxeTags identifiers.", throwable);
        return;
      }

      for (UUID uuid : onlineNames.keySet()) {
        applyLoadedEntry(uuid, entries.get(uuid), loadStartedAt);
      }

      Bukkit.getScheduler().runTask(plugin, () -> {
        for (UUID uuid : onlineNames.keySet()) {
          Player onlinePlayer = Bukkit.getPlayer(uuid);
          if (onlinePlayer != null) {
            plugin.getTagsHandler().updateTagForPlayer(onlinePlayer, true);
          }
        }
      });
    });
  }

  public CompletableFuture<Boolean> healthCheck() {
    return activeStorage.healthCheck();
  }

  public void close() {
    activeStorage.close();
  }

  private void writeEntry(SavedTagEntry entry) {
    if (recordPendingWrites) {
      pendingStore.record(entry);
    }

    activeStorage.save(entry.getUuid(), entry.getTagIdentifier(), entry.getUpdatedAtEpochMillis())
        .whenComplete((unused, throwable) -> {
          if (throwable != null) {
            handleWriteFailure(entry, throwable);
          }
        });
  }

  private void handleWriteFailure(SavedTagEntry entry, Throwable throwable) {
    plugin.getLogger().log(Level.WARNING, "Could not write saved DeluxeTags identifier to storage. It will be recorded for replay.", throwable);
    pendingStore.record(entry);
    yamlStorage.save(entry.getUuid(), entry.getTagIdentifier(), entry.getUpdatedAtEpochMillis());
  }

  private void handleBatchWriteFailure(Collection<SavedTagEntry> entries, Throwable throwable) {
    plugin.getLogger().log(Level.WARNING, "Could not write saved DeluxeTags identifiers to storage. They will be recorded for replay.", throwable);
    pendingStore.recordAll(entries);
    for (SavedTagEntry entry : entries) {
      yamlStorage.save(entry.getUuid(), entry.getTagIdentifier(), entry.getUpdatedAtEpochMillis());
    }
  }

  private void applyLoadedEntry(UUID uuid, SavedTagEntry loadedEntry, long loadStartedAt) {
    SavedTagEntry cachedEntry = cache.get(uuid);
    if (loadedEntry == null) {
      if (cachedEntry == null || cachedEntry.getUpdatedAtEpochMillis() <= loadStartedAt) {
        cache.remove(uuid);
      }
      return;
    }

    if (cachedEntry == null || loadedEntry.getUpdatedAtEpochMillis() >= cachedEntry.getUpdatedAtEpochMillis()) {
      cache.put(uuid, loadedEntry);
    }
  }

  private boolean hasKnownSavedState(UUID uuid) {
    if (cache.containsKey(uuid)) {
      return true;
    }

    if (activeStorage == yamlStorage) {
      SavedTagEntry yamlEntry = yamlStorage.loadSync(uuid);
      if (yamlEntry != null) {
        cache.put(uuid, yamlEntry);
        return true;
      }
    }

    return false;
  }

  private static void replayPendingWrites(DeluxeTags plugin, MySqlSavedTagStorage mySqlStorage, PendingSavedTagStore pendingStore)
      throws SQLException {
    Map<String, PendingSavedTagStore.PendingTagClear> pendingTagClears = pendingStore.loadTagClears();
    Collection<String> replayedTagClearKeys = new ArrayList<>();
    for (PendingSavedTagStore.PendingTagClear pendingTagClear : pendingTagClears.values()) {
      mySqlStorage.clearTagIdentifierIfOlderSync(pendingTagClear.getTagIdentifier(), pendingTagClear.getUpdatedAtEpochMillis());
      replayedTagClearKeys.add(pendingTagClear.getKey());
    }
    pendingStore.removeTagClears(replayedTagClearKeys);

    Map<UUID, SavedTagEntry> pendingEntries = pendingStore.loadAll();
    for (SavedTagEntry entry : pendingEntries.values()) {
      mySqlStorage.saveIfNewerSync(entry);
    }
    pendingStore.removeAll(pendingEntries.keySet());

    int replayed = pendingTagClears.size() + pendingEntries.size();
    if (replayed > 0) {
      plugin.getLogger().info("Replayed " + replayed + " pending DeluxeTags MySQL storage writes.");
    }
  }

  private static void importMissingYamlRows(DeluxeTags plugin, MySqlSavedTagStorage mySqlStorage, YamlSavedTagStorage yamlStorage)
      throws SQLException {
    Map<UUID, SavedTagEntry> yamlEntries = yamlStorage.loadAllSavedSync(System.currentTimeMillis());
    if (yamlEntries.isEmpty()) {
      return;
    }

    mySqlStorage.importMissingSync(yamlEntries);
    plugin.getLogger().info("Imported missing DeluxeTags YAML saved-tag rows into MySQL when absent.");
  }
}
