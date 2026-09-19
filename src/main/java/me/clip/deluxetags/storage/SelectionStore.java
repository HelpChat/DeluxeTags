package me.clip.deluxetags.storage;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

/** Blocking persistence interface; MySQL implementations are only called by the storage worker. */
public interface SelectionStore extends AutoCloseable {
    void initialize() throws Exception;
    Map<UUID, Selection> load(Collection<UUID> players) throws Exception;
    Selection write(UUID player, String identifier) throws Exception;
    @Override
    void close();
}
