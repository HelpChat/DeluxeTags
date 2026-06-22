package me.clip.deluxetags.storage;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface SavedTagStorage extends AutoCloseable {

  CompletableFuture<SavedTagEntry> load(UUID uuid);

  CompletableFuture<Map<UUID, SavedTagEntry>> loadAll(Collection<UUID> uuids);

  CompletableFuture<Void> save(UUID uuid, String tagIdentifier, long updatedAtEpochMillis);

  CompletableFuture<Void> clear(UUID uuid, long updatedAtEpochMillis);

  CompletableFuture<Void> clearAll(Collection<UUID> uuids, long updatedAtEpochMillis);

  CompletableFuture<Void> clearTagIdentifier(String tagIdentifier, long updatedAtEpochMillis);

  CompletableFuture<Boolean> healthCheck();

  @Override
  void close();
}
