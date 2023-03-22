package me.clip.deluxetags.storage;

import me.clip.deluxetags.tags.DeluxeTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public interface TagsStorage {

    /**
     * Load all tags from storage
     * @return the number of tags that were successfully loaded
     */
    int loadTags();

    /**
     * Save a tag to storage. If a tag with the same identifier already exists, it will be overwritten
     * @param tag the tag to save
     */
    void saveTag(final @NotNull DeluxeTag tag);

    /**
     * Save a tag to storage. If a tag with the same identifier already exists, it will be overwritten
     * @param priority the priority of the tag
     * @param identifier the identifier of the tag
     * @param display the display tag
     * @param description the description of the tag
     * @param permission the permission of the tag
     */
    void saveTag(final int priority,
                 final @NotNull String identifier,
                 final @NotNull String display,
                 final @NotNull String description,
                 final @NotNull String permission);

    /**
     * Completely remove a tag from storage
     * @param tag the tag to remove
     */
    void removeTag(final @NotNull DeluxeTag tag);

    /**
     * Completely remove a tag from storage
     * @param identifier the identifier of the tag to remove
     */
    void removeTag(final @NotNull String identifier);

    /**
     * Get the tag a player has selected
     * @param uuid the player's uuid
     * @return the identifier of the tag the player has selected or null if the player has no tag selected
     */
    @Nullable String getSelectedTag(final @NotNull String uuid);

    /**
     * Set the tag as selected for a player
     * @param uuid the player's uuid
     * @param identifier the identifier of the tag to set as selected
     */
    void setSelectedTag(final @NotNull String uuid, final @NotNull String identifier);

    /**
     * Remove the tag a player has selected if they have one selected
     * @param uuid the player's uuid
     */
    void removeSelectedTag(final @NotNull String uuid);

    /**
     * Get the tags a list of players have selected
     * @param uuids the list of players' uuids
     * @return a map of each player's uuid and the identifier of the tag they have selected or null if they have no tag selected
     */
    @NotNull Map<@NotNull String, @Nullable String> getSelectedTags(final @NotNull List<@NotNull String> uuids);

    /**
     * Set the tag as selected for every player in the list
     * @param uuids the list of players' uuids
     * @param identifier the identifier of the tag to set as selected
     */
    void setSelectedTags(final @NotNull List<@NotNull String> uuids, final @NotNull String identifier);

    /**
     * Remove the tags a list of players have selected if they have one selected
     * @param uuids the list of players' uuids
     */
    void removeSelectedTags(final @NotNull List<@NotNull String> uuids);

    /**
     * Reload everything from storage
     */
    void reload();

    /**
     * Save everything to storage
     */
    void save();
}
