package me.clip.deluxetags.tags;

import me.clip.deluxetags.DeluxeTags;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

public class DeluxeTagsHandler {

    private final DeluxeTags plugin;

    private final TreeMap<Integer, DeluxeTag> configTags = new TreeMap<>();
    private final Map<UUID, DeluxeTag> playerTags = new HashMap<>();

    private final List<UUID> playersUsingDefaultTag = new ArrayList<>();
    private final List<UUID> playersUsingForcedTag = new ArrayList<>();

    public DeluxeTagsHandler(@NotNull final DeluxeTags plugin) {
        this.plugin = plugin;
    }

    // Player functions

    /**
     * set a players active tag to this tag
     * @param player Player to set the tag to
     * @return true if the players tag was set to a new tag
     */
    public boolean setPlayerTag(@NotNull final Player player, @NotNull final DeluxeTag tag) {
        return setPlayerTag(player.getUniqueId(), tag);
    }

    /**
     * set a players active tag to this tag
     * @param uuid Players uuid
     * @return true if the players tag was set to a new tag
     */
    public boolean setPlayerTag(@NotNull final UUID uuid, @NotNull final DeluxeTag tag) {
        if (playerTags.containsKey(uuid) && playerTags.get(uuid) == tag) {
            return false;
        }

        playerTags.put(uuid, tag);
        playersUsingDefaultTag.remove(uuid);
        playersUsingForcedTag.remove(uuid);
        return true;
    }

    /**
     * check if a player has an active tag
     * @param player Player to check for
     * @return true if player has an active tag
     */
    public boolean playerHasActiveTag(@NotNull final Player player) {
        return playerHasActiveTag(player.getUniqueId());
    }

    /**
     * check if a player has an active tag
     * @param uuid UUID of player to check for
     * @return true if player has an active tag
     */
    public boolean playerHasActiveTag(@NotNull final UUID uuid) {
        if (playerTags.isEmpty()) {
            return false;
        }

        return playerTags.containsKey(uuid);
    }

    /**
     * get a player's active tag
     * @param player Player to get the tag for
     * @return null if player has no active tag
     */
    public @Nullable DeluxeTag getPlayerActiveTag(@NotNull final Player player) {
        return getPlayerActiveTag(player.getUniqueId());
    }

    /**
     * get a player's active tag
     * @param uuid UUID of player to get the tag for
     * @return null if player has no active tag
     */
    public @Nullable DeluxeTag getPlayerActiveTag(@NotNull final UUID uuid) {
        return playerTags.get(uuid);
    }

    /**
     * trigger a tag update for a player. will attempt to set player's forced tag, active tag or default tag
     * @param player Player to update the tag for
     */
    public void updateTagForPlayer(@NotNull final Player player) {
        // Forced tags take priority over all other tags
        if (plugin.getCfg().forceTags() && setForcedTag(player)) {
            return;
        }

        // If player has an active tag, and permissions to use it, keep that tag
        final DeluxeTag currentTag = getPlayerActiveTag(player);
        if (currentTag != null && currentTag != plugin.getDummyTag() && currentTag.hasPermissionToUse(player)) {
            return;
        }

        // If player has an active tag (saved on file), and permissions to use it, use that tag
        if (setSavedTag(player)) {
            return;
        }

        // If the player has no forced or active tag, try to use a default tag, if the player has one
        if (setDefaultTag(player)) {
            return;
        }

        // The player has no forced, active or default tag. Use the dummy tag TODO: NOT SURE WHY THE DUMMY TAG EXISTS?
        setPlayerTag(player, plugin.getDummyTag());
        plugin.removeSavedTag(player.getUniqueId().toString());
    }


    // Tag functions

    /**
     * load this tag into the tag list. if a tag with the same priority already exists, it will be overwritten
     */
    public void loadTag(@NotNull final DeluxeTag tag) {
        configTags.put(tag.getPriority(), tag);
    }

    /**
     * unload this tag if it is loaded. priority is used to identify the tag
     * @return true if it was loaded and removed, false otherwise
     */
    public boolean unloadTag(@NotNull final DeluxeTag tag) {
        return configTags.remove(tag.getPriority()) != null;
    }

    /**
     * remove this tag from any player who has it set as the active tag
     * @return list of uuids of players that had the tag removed
     */
    public List<UUID> removeActivePlayers(@NotNull final DeluxeTag tag) {
        if (playerTags.isEmpty()) {
            return null;
        }

        final List<UUID> removedFrom = new ArrayList<>();

        for (final UUID uuid: getPlayersWithActiveTags()) {
            final DeluxeTag activeTag = getPlayerActiveTag(uuid);
            if (activeTag == null || !activeTag.getIdentifier().equals(tag.getIdentifier())) {
                continue;
            }

            removedFrom.add(uuid);
            removeActiveTagFromPlayer(uuid);
        }

        return removedFrom;
    }

    /**
     * get a DeluxeTag by its identifier. if multiple tags have the same identifier, the one with the lowest priority will be returned
     * @param identifier Identifier of the tag to get
     * @return null if there is no DeluxeTag for the identifier provided
     */
    public @Nullable DeluxeTag getTagByIdentifier(@NotNull final String identifier) {
        return getAllTags().stream()
                .filter(tag -> tag.getIdentifier().equals(identifier))
                .min(Comparator.comparingInt(DeluxeTag::getPriority))
                .orElse(null);
    }

    /**
     * get a DeluxeTag that a player is forced to use. if the player has multiple tags forced, the one with the lowest priority will be returned
     * @param player Player to get the tag for
     * @return null if the player has no forced tag
     */
    public @Nullable DeluxeTag getForcedTag(@NotNull final Player player) {
        return getAllTags().stream()
                .filter(tag -> tag.hasForceTagPermission(player))
                .min(Comparator.comparingInt(DeluxeTag::getPriority))
                .orElse(null);
    }

    /**
     * get a DeluxeTag that is set as the default tag for a player. if the player has multiple default tags, the one with the lowest priority will be returned
     * @param player Player to get the tag for
     * @return null if the player has no default tag
     */
    public @Nullable DeluxeTag getDefaultTag(@NotNull final Player player) {
        return getAllTags().stream()
                .filter(tag -> tag.hasDefaultTagPermission(player))
                .min(Comparator.comparingInt(DeluxeTag::getPriority))
                .orElse(null);
    }


    // Tag list functions

    /**
     * get list containing all DeluxeTags that have been loaded in increasing order of priority
     * @return a non-null collection of all loaded tags
     */
    public @NotNull Collection<@NotNull DeluxeTag> getAllTags() {
        return configTags.values();
    }

    /**
     * get a list of all available tag identifiers that have been loaded in increasing order of priority
     * @return empty list if no tags are loaded
     */
    public @NotNull List<@NotNull String> getAllTagIdentifiers() {
        return getAllTags().stream()
                .sorted(Comparator.comparingInt(DeluxeTag::getPriority))
                .map(DeluxeTag::getIdentifier)
                .collect(Collectors.toList());
    }

    /**
     * get a list of all available tag identifiers a player has permission for in increasing order of priority
     * @param player Player to get tag identifiers for
     * @return empty list if player doesn't have permission to any tags or no tags are loaded
     */
    public @NotNull List<@NotNull String> getPlayerAvailableTagIdentifiers(@NotNull final Player player) {
        return getAllTags().stream()
                .filter(tag -> tag.hasPermissionToUse(player))
                .sorted(Comparator.comparingInt(DeluxeTag::getPriority))
                .map(DeluxeTag::getIdentifier)
                .collect(Collectors.toList());
    }

    /**
     * get a list of all tag identifiers that a player can see or use (players can see tags they have permission to use) in increasing order of priority
     * @param player Player to get tag identifiers for
     * @return empty list if player can't see any tags or no tags are loaded
     */
    public @NotNull List<@NotNull String> getPlayerVisibleTagIdentifiers(@NotNull final Player player) {
        return getAllTags().stream()
                .filter(tag -> tag.hasPermissionToSee(player) || tag.hasPermissionToUse(player))
                .sorted(Comparator.comparingInt(DeluxeTag::getPriority))
                .map(DeluxeTag::getIdentifier)
                .collect(Collectors.toList());
    }


    // General functions

    /**
     * get the count of all loaded tags
     * @return 0 if no tags are loaded
     */
    public int getLoadedTagsAmount() {
        if (configTags.isEmpty()) {
            return 0;
        }

        return configTags.size();
    }

    /**
     * get a list of all priorities set for loaded tags
     * @return empty set if no tags are loaded
     */
    public @NotNull Set<@NotNull Integer> getLoadedPriorities() {
        return configTags.keySet();
    }

    /**
     * get a list of uuids of all players that have a tag active
     * @return empty list if no players have tags active
     */
    public @NotNull Set<@NotNull UUID> getPlayersWithActiveTags() {
        return playerTags.keySet();
    }

    /**
     * remove a player's active tag
     * @param uuid UUID of the player to remove the tag from
     */
    public void removeActiveTagFromPlayer(@NotNull final UUID uuid) {
        if (!playerHasActiveTag(uuid)) {
            return;
        }

        playerTags.remove(uuid);
        playersUsingDefaultTag.remove(uuid);
        playersUsingForcedTag.remove(uuid);
    }

    /**
     * remove all active tags and unload all loaded tags
     */
    public void unloadData() {
        configTags.clear();
        playerTags.clear();

        playersUsingDefaultTag.clear();
        playersUsingForcedTag.clear();
    }


    // Internal functions

    private boolean setForcedTag(@NotNull final Player player) {
        final DeluxeTag forcedTag = getForcedTag(player);
        if (forcedTag == null) {
            return false;
        }

        setPlayerTag(player, forcedTag);
        playersUsingForcedTag.add(player.getUniqueId());
        return true;
    }

    private boolean setSavedTag(@NotNull final Player player) {
        final String identifier = plugin.getSavedTagIdentifier(player.getUniqueId().toString());
        if (identifier == null) {
            return false;
        }

        final DeluxeTag tag = getTagByIdentifier(identifier);
        if (tag == null || !tag.hasPermissionToUse(player)) {
            return false;
        }

        setPlayerTag(player, tag);
        return true;
    }

    private boolean setDefaultTag(@NotNull final Player player) {
        final DeluxeTag tag = getDefaultTag(player);
        if (tag == null) {
            return false;
        }

        setPlayerTag(player, tag);
        playersUsingForcedTag.add(player.getUniqueId());
        return true;
    }

    /**
     * check if the player's active tag is a default tag
     * @param player Player to check for
     * @return true if the player is using a default tag
     */
    public boolean isUsingDefaultTag(@NotNull final Player player) {
        return playersUsingDefaultTag.contains(player.getUniqueId());
    }


    /**
     * check if the player's active tag is a forced tag
     * @param player Player to check for
     * @return true if the player is using a forced tag
     */
    public boolean isUsingForcedTag(@NotNull final Player player) {
        return playersUsingForcedTag.contains(player.getUniqueId());
    }
}
