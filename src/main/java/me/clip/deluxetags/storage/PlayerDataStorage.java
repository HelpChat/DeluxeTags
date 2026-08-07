package me.clip.deluxetags.storage;

import java.util.UUID;

public interface PlayerDataStorage extends AutoCloseable {

    void initialize() throws Exception;

    PlayerData load(UUID uuid) throws Exception;

    void save(UUID uuid, PlayerData playerData) throws Exception;

    void delete(UUID uuid) throws Exception;

    @Override
    void close();
}