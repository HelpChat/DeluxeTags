package me.clip.deluxetags.storage;

import java.io.File;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.configuration.file.YamlConfiguration;

/** YAML I/O remains synchronous for compatibility; readers use an immutable-value cache. */
public final class YamlSelectionStore implements SelectionStore {
    private final File file;
    private YamlConfiguration config;
    private final Map<UUID, Selection> values = new ConcurrentHashMap<>();
    private long revision;

    public YamlSelectionStore(File file) { this.file = file; }

    @Override
    public synchronized void initialize() throws Exception {
        YamlConfiguration loaded = new YamlConfiguration();
        if (file.exists()) loaded.load(file);
        Map<UUID, Selection> replacement = new HashMap<>();
        long next = ++revision;
        for (String key : loaded.getKeys(false)) {
            try {
                if (loaded.isString(key)) replacement.put(UUID.fromString(key), new Selection(loaded.getString(key), next));
            } catch (IllegalArgumentException ignored) { }
        }
        config = loaded;
        values.clear();
        values.putAll(replacement);
    }

    public Selection get(UUID uuid) { return values.getOrDefault(uuid, Selection.ABSENT); }

    @Override
    public Map<UUID, Selection> load(Collection<UUID> players) {
        Map<UUID, Selection> result = new HashMap<>();
        for (UUID uuid : players) result.put(uuid, get(uuid));
        return result;
    }

    @Override
    public synchronized Selection write(UUID player, String identifier) throws Exception {
        Object old = config.get(player.toString());
        config.set(player.toString(), identifier);
        Path temporary = null;
        try {
            Files.createDirectories(file.toPath().toAbsolutePath().getParent());
            temporary = Files.createTempFile(file.toPath().toAbsolutePath().getParent(), "player_tags-", ".tmp");
            config.save(temporary.toFile());
            try {
                Files.move(temporary, file.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporary, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {
            config.set(player.toString(), old);
            throw e;
        } finally {
            if (temporary != null) Files.deleteIfExists(temporary);
        }
        Selection value = new Selection(identifier, ++revision);
        values.put(player, value);
        return value;
    }

    @Override
    public void close() { }
}
