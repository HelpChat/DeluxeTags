package me.clip.deluxetags.storage.yaml;

import me.clip.deluxetags.storage.TagsStorage;
import me.clip.deluxetags.tags.DeluxeTag;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class YAMLStorage implements TagsStorage {

    private final Logger logger;
    private final File playerTagsFile;
    private final File tagsFile;
    private YamlConfiguration playerTags;
    private YamlConfiguration tags;

    public YAMLStorage(final @NotNull Logger logger,
                       final @NotNull File playerTagsFile,
                       final @NotNull File tagsFile) {
        this.logger = logger;
        this.playerTagsFile = playerTagsFile;
        this.tagsFile = tagsFile;

        reload();
    }

    @Override
    public void saveTag(final @NotNull DeluxeTag tag) {
        saveTag(tag.getPriority(),
                tag.getIdentifier(),
                tag.getDisplayTag(),
                tag.getDescription(),
                tag.getPermission());
    }

    @Override
    public void saveTag(final int priority,
                        final @NotNull String identifier,
                        final @NotNull String display,
                        final @NotNull String description,
                        final @NotNull String permission) {
        ConfigurationSection tagsSection = tags.getConfigurationSection("deluxetags");
        if (tagsSection == null) {
            tagsSection = tags.createSection("deluxetags");
        }

        ConfigurationSection tagSection = tagsSection.getConfigurationSection(identifier);
        if (tagSection == null) {
            tagSection = tagsSection.createSection(identifier);
        }

        tagSection.set("tag", display);
        tagSection.set("order", priority);
        tagSection.set("description", description);
        tagSection.set("permission", permission);

        saveTags();
    }

    @Override
    public void removeTag(final @NotNull DeluxeTag tag) {
        removeTag(tag.getIdentifier());
    }

    @Override
    public void removeTag(final @NotNull String identifier) {
        ConfigurationSection tagsSection = tags.getConfigurationSection("deluxetags");
        if (tagsSection == null) {
            tagsSection = tags.createSection("deluxetags");
        }

        tagsSection.set(identifier, null);
        saveTags();
    }

    @Override
    public int loadTags() {
        int loadedTagsCount = 0;

        final ConfigurationSection tagsSection = tags.getConfigurationSection("deluxetags");
        if (tagsSection == null) {
            return loadedTagsCount;
        }

        final Set<String> identifiers = tagsSection.getKeys(false);
        if (identifiers.isEmpty()) {
            return loadedTagsCount;
        }

        for (final String identifier : identifiers) {
            if (identifier == null) {
                continue;
            }

            final ConfigurationSection tagSection = tagsSection.getConfigurationSection(identifier);
            if (tagSection == null) {
                logger.log(Level.INFO, "Could not load tag: " + identifier + ". Possible configuration error.");
                continue;
            }

            if (!tagSection.contains("tag")) {
                logger.log(Level.INFO, "Could not load tag: " + identifier + " because it does not have a display set.");
                continue;
            }
            final String display = tagSection.getString("tag");

            if (!tagSection.contains("order")) {
                logger.log(Level.INFO, "Could not load tag: " + identifier + " because it does not have an order set.");
                continue;
            }
            final int priority = tagSection.getInt("order");

            final String description = tagSection.isList("description")
                    ? String.join("\n", tagSection.getStringList("description"))
                    : tagSection.getString("description", "&f");

            final String permission = tagSection.getString("permission", "deluxetags.tag." + identifier);

            final DeluxeTag tag = new DeluxeTag(priority, identifier, display, description, permission);
            tag.load();
            loadedTagsCount++;
        }

        return loadedTagsCount;
    }



    @Override
    public @Nullable String getSelectedTag(final @NotNull String uuid) {
        if (playerTags.contains(uuid) && playerTags.isString(uuid) && playerTags.getString(uuid) != null) {
            return playerTags.getString(uuid);
        }

        return null;
    }

    @Override
    public void setSelectedTag(final @NotNull String uuid, final @NotNull String identifier) {
        playerTags.set(uuid, identifier);
        savePlayerTags();
    }

    @Override
    public void removeSelectedTag(final @NotNull String uuid) {
        playerTags.set(uuid, null);
        savePlayerTags();
    }

    @Override
    public @NotNull Map<@NotNull String, @Nullable String> getSelectedTags(@NotNull List<@NotNull String> uuids) {
        return uuids.stream().collect(HashMap::new, (map, uuid) -> map.put(uuid, getSelectedTag(uuid)), HashMap::putAll);
    }

    @Override
    public void setSelectedTags(@NotNull List<@NotNull String> uuids, @NotNull String identifier) {
        uuids.forEach(uuid -> setSelectedTag(uuid, identifier));
    }

    @Override
    public void removeSelectedTags(@NotNull List<@NotNull String> uuids) {
        uuids.forEach(this::removeSelectedTag);
    }

    public void reload() {
        reloadPlayerTags();
        reloadTags();
    }

    public void save() {
        savePlayerTags();
        saveTags();
    }

    private void reloadPlayerTags() {
        playerTags = YamlConfiguration.loadConfiguration(playerTagsFile);
    }

    private void reloadTags() {
        tags = YamlConfiguration.loadConfiguration(tagsFile);
    }

    private void savePlayerTags() {
        saveConfiguration(playerTags, playerTagsFile);
    }

    private void saveTags() {
        saveConfiguration(tags, tagsFile);
    }

    private void saveConfiguration(final @NotNull YamlConfiguration configuration,
                                   final @NotNull File file) {
        try {
            configuration.save(file);
        } catch (final IOException exception) {
            logger.log(Level.SEVERE, "Could not save config to " + file, exception);
        }
    }
}
