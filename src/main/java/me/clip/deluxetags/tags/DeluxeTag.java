package me.clip.deluxetags.tags;

import me.clip.deluxetags.DeluxeTags;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * DeluxeTag class
 *
 * @author Ryan McCarthy
 */
public class DeluxeTag {

    private final String identifier;

    private String displayTag;
    private String description;
    private String permission;
    private String category;
    private int priority;

    /**
     * DeluxeTag object initializer
     *
     * @param priority    int value representing the priority of this tag
     * @param identifier  String value representing the identifier of this tag
     * @param displayTag  String value representing the display tag of this tag
     * @param description String value representing the description of this tag
     */
    public DeluxeTag(final int priority, @NotNull final String identifier, @NotNull final String displayTag, @NotNull String description) {
        this(priority, identifier, displayTag, description, DeluxeTagCategory.GENERAL_IDENTIFIER);
    }

    public DeluxeTag(final int priority, @NotNull final String identifier, @NotNull final String displayTag, @NotNull String description, @NotNull final String category) {
        this.priority = priority;
        this.identifier = identifier;
        this.displayTag = displayTag;
        this.description = description;
        setCategory(category);
    }

    /**
     * get the identifier of this tag
     *
     * @return tag identifier String
     */
    public @NotNull String getIdentifier() {
        return identifier;
    }

    /**
     * get the display tag of this DeluxeTag
     *
     * @return display tag String
     */
    public @NotNull String getDisplayTag() {
        return displayTag;
    }

    /**
     * set the display tag for this DeluxeTag
     *
     * @param newDisplayTag new display tag String
     */
    public void setDisplayTag(@NotNull final String newDisplayTag) {
        this.displayTag = newDisplayTag;
    }

    /**
     * get the display tag of this DeluxeTag
     *
     * @return display tag String
     */
    public @NotNull String getDisplayTag(final @Nullable OfflinePlayer player) {
        return DeluxeTags.papi() ? PlaceholderAPI.setPlaceholders(player, displayTag) : displayTag;
    }

    /**
     * get the description of this DeluxeTag
     *
     * @return description String
     */
    public @NotNull String getDescription() {
        return description;
    }

    /**
     * set the description for this DeluxeTag
     *
     * @param newDescription new description String
     */
    public void setDescription(@NotNull final String newDescription) {
        this.description = newDescription;
    }

    /**
     * get the description of this DeluxeTag with placeholders replaced
     *
     * @param player to get the description for
     * @return description String
     */
    public @NotNull String getDescription(final @Nullable OfflinePlayer player) {
        return DeluxeTags.papi() ? PlaceholderAPI.setPlaceholders(player, description) : description;
    }

    /**
     * get the priority associated with this tag
     *
     * @return integer representing this tags priority
     */
    public int getPriority() {
        return priority;
    }

    /**
     * set the priority of this tag
     *
     * @param priority int value to set this tag to
     */
    public void setPriority(final int priority) {
        this.priority = priority;
    }

    /**
     * get the category associated with this tag
     *
     * @return category identifier String
     */
    public @NotNull String getCategory() {
        return category == null ? DeluxeTagCategory.GENERAL_IDENTIFIER : category;
    }

    /**
     * set the category associated with this tag
     *
     * @param category category identifier to set for this tag
     */
    public void setCategory(@NotNull final String category) {
        if (category.trim().isEmpty() || category.equalsIgnoreCase(DeluxeTagCategory.ALL_IDENTIFIER)) {
            this.category = DeluxeTagCategory.GENERAL_IDENTIFIER;
            return;
        }

        this.category = category;
    }

    /**
     * get the permission of this tag
     *
     * @return string permission node associated with this tag
     */
    public String getPermission() {
        return permission == null ? "deluxetags.tag." + identifier : permission;
    }

    /**
     * set the permission node associated with this tag
     *
     * @param permission to set for this tag
     */
    public void setPermission(@NotNull final String permission) {
        this.permission = permission;
    }

    /**
     * check if a player has permission to use this tag
     *
     * @param player Player to check permission for
     * @return true if player has permission
     */
    public boolean hasPermissionToUse(@NotNull final Player player) {
        return player.hasPermission(getPermission());
    }

    /**
     * check if player has permission to see this tag without checking permission to use
     *
     * @param player Player to check visibility for
     * @return true if player can see the tag, false otherwise
     */
    public boolean hasPermissionToSee(@NotNull final Player player) {
        return player.hasPermission("deluxetags.see.all") || player.hasPermission("deluxetags.see." + identifier);
    }

    /**
     * check if a player has permission for this tag to be forced
     *
     * @param player Player to check permission for
     * @return true if player has permission
     */
    public boolean hasForceTagPermission(@NotNull final Player player) {
        return player.hasPermission("deluxetags.forcetag." + identifier);
    }

    /**
     * check if a player has permission for this tag to be the default tag
     *
     * @param player Player to check permission for
     * @return true if player has permission
     */
    public boolean hasDefaultTagPermission(@NotNull final Player player) {
        return player.hasPermission("deluxetags.defaulttag." + identifier);
    }
}
