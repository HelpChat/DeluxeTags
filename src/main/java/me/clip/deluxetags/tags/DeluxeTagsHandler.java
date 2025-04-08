package me.clip.deluxetags.tags;

import me.clip.deluxetags.DeluxeTags;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

public class DeluxeTagsHandler {

    private final TreeMap<Integer, DeluxeTag> configTags = new TreeMap<>();
    private final Map<UUID, DeluxeTag> playerTags = new HashMap<>();

    /**
     * load this tag into the tag list
     */
    public void loadTag(@NotNull final DeluxeTag tag) {
        configTags.put(tag.getPriority(), tag);
    }

    /**
     * unload this tag from the tags list if it is loaded
     * @return true if it was loaded and removed, false otherwise
     */
    public boolean unloadTag(@NotNull final DeluxeTag tag) {
        if (configTags.isEmpty() || !configTags.containsKey(tag.getPriority())) {
            return false;
        }

        configTags.remove(tag.getPriority());
        return true;
    }

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
        return true;
    }

    /**
     * remove this tag from any player who has it set as the active tag
     * @return list of uuids that were removed from this tag
     */
    public List<UUID> removeActivePlayers(@NotNull final DeluxeTag tag) {
        if (playerTags.isEmpty()) {
            return null;
        }

        List<UUID> remove = new ArrayList<>();

        for (UUID uuid : playerTags.keySet()) {
            if (getPlayerDisplayTag(uuid).equals(tag.getDisplayTag())) {
                remove.add(uuid);
                removePlayer(uuid);
            }
        }

        return remove;
    }


    /**
     * get list of DeluxeTag objects that have been loaded
     * @return null if no tags have been loaded
     */
    public Collection<DeluxeTag> getLoadedTags() {
        return configTags.values();
    }


    /**
     * check if a player has an active tag
     * @param uuid Player uuid to check
     * @return true if uuid is loaded in the playerTags map
     */
    public boolean hasTagLoaded(@NotNull final UUID uuid) {
        if (playerTags.isEmpty()) {
            return false;
        }

        return playerTags.containsKey(uuid);
    }


    /**
     * check if a player has a tag loaded in the playerTags map
     * @param player Player to check
     * @return true if player is loaded in the playerTags map
     */
    public boolean hasTagLoaded(@NotNull final Player player) {
        return hasTagLoaded(player.getUniqueId());
    }

    /**
     * get a players current tag identifier
     * @param player Player to get the identifier for
     * @return null if player has no loaded tag
     */
    public String getPlayerTagIdentifier(@NotNull final Player player) {
        return getPlayerTagIdentifier(player.getUniqueId());
    }

    /**
     * get a players current tag
     * @param uuid Players uuid to get the tag for
     * @return null if player has no loaded tag
     */
    public DeluxeTag getTag(@NotNull final UUID uuid) {
        if (playerTags.isEmpty() || !playerTags.containsKey(uuid) || playerTags.get(uuid) == null) {
            return null;
        }

        return playerTags.get(uuid);
    }

    /**
     * get a players current tag
     * @param player Player to get the tag for
     * @return null if player has no loaded tag
     */
    public DeluxeTag getTag(@NotNull final Player player) {
        return getTag(player.getUniqueId());
    }

    /**
     * get a players current tag identifier
     * @param uuid Players uuid to get the identifier for
     * @return null if player has no loaded tag
     */
    public String getPlayerTagIdentifier(@NotNull final UUID uuid) {
        if (playerTags.isEmpty() || !playerTags.containsKey(uuid) || playerTags.get(uuid) == null) {
            return null;
        }

        return playerTags.get(uuid).getIdentifier();
    }


    /**
     * get a players current display tag if they have one set, an empty string if not
     * @param player Player to get display tag for
     * @return players current active display tag if they have one set
     */
    public String getPlayerDisplayTag(@NotNull final Player player) {
        String d = getPlayerDisplayTag(player.getUniqueId());
        return DeluxeTags.papi() ? PlaceholderAPI.setPlaceholders(player, d): d;
    }

    /**
     * get a players current display tag if they have one set, an empty string if not
     * @param uuid Player uuid to get display tag for
     * @return players current active display tag if they have one set
     */
    public String getPlayerDisplayTag(@NotNull final UUID uuid) {
        if (playerTags.isEmpty() || !playerTags.containsKey(uuid) || playerTags.get(uuid) == null) {
            return "";
        }

        return playerTags.get(uuid).getDisplayTag();
    }


    /**
     * get a players current tag description if they have a tag set, an empty string if not
     * @param player Player to get tag description for
     * @return players current tag description if they have a tag set, empty string otherwise
     */
    public String getPlayerTagDescription(@NotNull final Player player) {
        String d = getPlayerTagDescription(player.getUniqueId());
        return DeluxeTags.papi() ? PlaceholderAPI.setPlaceholders(player, d): d;
    }

    /**
     * get a players current tag description if they have a tag set, an empty string if not
     * @param uuid Player uuid to get tag description for
     * @return players current tag description if they have a tag set, empty string otherwise
     */
    public String getPlayerTagDescription(@NotNull final UUID uuid) {
        if (playerTags.isEmpty() || !playerTags.containsKey(uuid) || playerTags.get(uuid) == null) {
            return "";
        }

        return playerTags.get(uuid).getDescription();
    }


    /**
     * get a players current tag description if they have a tag set, an empty string if not
     * @param player Player to get tag description for
     * @return players current tag description if they have a tag set, empty string otherwise
     */
    public int getPlayerTagPriority(@NotNull final Player player) {
        return getPlayerTagPriority(player.getUniqueId());
    }

    /**
     * get a players current tag priority if they have a tag set, -1 if not
     * @param uuid Player uuid to get tag priority for
     * @return players current tag priority if they have a tag set, -1 otherwise
     */
    public int getPlayerTagPriority(@NotNull final UUID uuid) {
        if (playerTags.isEmpty() || !playerTags.containsKey(uuid) || playerTags.get(uuid) == null) {
            return -1;
        }

        return playerTags.get(uuid).getPriority();
    }


    /**
     * get the DeluxeTag tag object loaded by its identifier String
     * @param identifier String identifier of the tag object to get
     * @return null if there is no DeluxeTag object loaded for the identifier provided
     */
    public DeluxeTag getLoadedTag(@NotNull final String identifier) {
        if (getLoadedTags() == null || getLoadedTags().isEmpty()) {
            return null;
        }

        return getLoadedTags().stream().filter(tag -> tag != null && tag.getIdentifier().equals(identifier)).min(Comparator.comparingInt(DeluxeTag::getPriority)).orElse(null);
    }

    public DeluxeTag getForcedTag(@NotNull final Player player) {
        if (getLoadedTags() == null || getLoadedTags().isEmpty()) {
            return null;
        }

        return getLoadedTags().stream().filter(tag -> tag != null && tag.hasForceTagPermission(player)).min(Comparator.comparingInt(DeluxeTag::getPriority)).orElse(null);
    }

    public DeluxeTag getDefaultTag(@NotNull final Player player) {
        if (getLoadedTags() == null || getLoadedTags().isEmpty()) {
            return null;
        }

        return getLoadedTags().stream().filter(tag -> tag != null && tag.hasDefaultTagPermission(player)).min(Comparator.comparingInt(DeluxeTag::getPriority)).orElse(null);
    }

    /**
     * get a list of all available tag identifiers a player has permission for
     * @param player Player to get tag identifiers for
     * @return null if no tags are loaded, empty list if player doesn't have permission to any tags
     */
    public List<String> getAvailableTagIdentifiers(@NotNull final Player player) {
        if (getLoadedTags() == null || getLoadedTags().isEmpty()) {
            return null;
        }

        return getLoadedTags().stream().filter(tag -> tag != null && tag.hasPermissionToUse(player)).map(DeluxeTag::getIdentifier).collect(Collectors.toList());
    }

    /**
     * get a list of all available tag identifiers that have been loaded
     * @return null if no tags have been loaded
     */
    public List<String> getAllTagIdentifiers() {
        if (getLoadedTags() == null || getLoadedTags().isEmpty()) {
            return null;
        }

        return getLoadedTags().stream().filter(Objects::nonNull).map(DeluxeTag::getIdentifier).collect(Collectors.toList());
    }

    /**
     * get a list of all tag identifiers that a player can see
     * @param player Player to get all visible tag identifiers for
     * @return null if no tags are loaded, empty list if player can't see any tags
     */
    public List<String> getAllVisibleTagIdentifiers(@NotNull final Player player) {
        if (getLoadedTags() == null || getLoadedTags().isEmpty()) {
            return null;
        }

        return getLoadedTags().stream().filter(tag -> tag != null && (tag.hasPermissionToSee(player) || tag.hasPermissionToUse(player))).map(DeluxeTag::getIdentifier).collect(Collectors.toList());
    }

    public int getLoadedTagsAmount() {
        if (configTags.isEmpty()) {
            return 0;
        }

        return configTags.size();
    }

    public Set<Integer> getLoadedPriorities() {
        if (configTags.isEmpty()) {
            return Collections.emptySet();
        }

        return configTags.keySet();
    }


    /**
     * get a list of all uuids that are currently loaded into the cache
     * @return null if no players are loaded
     */
    public Set<UUID> getLoadedPlayers() {
        if (playerTags.isEmpty()) {
            return null;
        }

        return playerTags.keySet();
    }

    /**
     * remove a player from the cache
     * @param uuid Player uuid to remove
     */
    public void removePlayer(@NotNull final UUID uuid) {
        if (hasTagLoaded(uuid)) {
            playerTags.remove(uuid);
        }
    }

    public void unloadData() {
        configTags.clear();
        playerTags.clear();
    }
}
