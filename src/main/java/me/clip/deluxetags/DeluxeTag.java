package me.clip.deluxetags;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

/**
 * DeluxeTag class
 * @author Ryan McCarthy
 *
 */
public class DeluxeTag {
	
	private static TreeMap<Integer, DeluxeTag> configTags;
	
	private static Map<String, DeluxeTag> playerTags;
	
	private String identifier, displayTag, description, permission;
	private int priority;
	
	/**
	 * DeluxeTag object initializer
	 * @param identifier String identifier of this DeluxeTag
	 * @param displayTag String displayTag of this DeluxeTag
	 */
	public DeluxeTag(int priority, String identifier, String displayTag, String description) {
		this.priority = priority;
		this.identifier = identifier;
		this.displayTag = displayTag;
		this.description = description;
	}

	/**
	 * get the identifier of this tag
	 * @return tag identifier String
	 */
	public String getIdentifier() {
		return identifier;
	}
	
	/**
	 * get the display tag of this DeluxeTag
	 * @return display tag String
	 */
	public String getDisplayTag() {
		return displayTag;
	}
	
	/**
	 * get the description of this DeluxeTag
	 * @return description String
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * set the display tag for this DeluxeTag
	 * @param newDisplayTag new display tag String
	 */
	public void setDisplayTag(String newDisplayTag) {
		this.displayTag = newDisplayTag;
	}
	
	/**
	 * set the description for this DeluxeTag
	 * @param newDescription new description String
	 */
	public void setDescription(String newDescription) {
		this.description = newDescription;
	}
	
	/**
	 * get the priority associated with this tag
	 * @return integer representing this tags priority
	 */
	public int getPriority() {
		return priority;
	}

	/**
	 * set the priority of this tag
	 * @param priority int value to set this tag to
	 */
	public void setPriority(int priority) {
		this.priority = priority;
	}

	/**
	 * get the permission of this tag
	 * @return string permission node associated with this tag
	 */
	public String getPermission() {
		return permission == null ? "deluxetags.tag." + identifier : permission;
	}

	/**
	 * set the permission node associated with this tag
	 * @param permission to set for this tag
	 */
	public void setPermission(String permission) {
		this.permission = permission;
	}

	/**
	 * load this tag into the tag list
	 */
	public void load() {
		if (configTags == null) {
			configTags = new TreeMap<>();
		}
		
		configTags.put(priority, this);
	}
	
	/**
	 * unload this tag from the tags list if it is loaded
	 * @return true if it was loaded and removed, false otherwise
	 */
	public boolean unload() {
		if (configTags == null || configTags.isEmpty()) {
			return false;
		}
		
		if (configTags.containsKey(priority)) {
			configTags.remove(priority);
			return true;
		}
		
		return false;
	}
	
	/**
	 * check if a player has permission for this tag
	 * @param p Player to check permission for
	 * @return true if player has permission
	 */
	public boolean hasTagPermission(Player p) {
		return p.hasPermission(getPermission());
	}
	
	/**
	 * check if a player has permission for this tag to be forced
	 * @param p Player to check permission for
	 * @return true if player has permission
	 */
	public boolean hasForceTagPermission(Player p) {
		return p.hasPermission("deluxetags.forcetag."+identifier);
	}
	
	/**
	 * set a players active tag to this tag
	 * @param p Player to set the tag to
	 * @return true if the players tag was set to a new tag
	 */
	public boolean setPlayerTag(Player p) {
		return setPlayerTag(p.getUniqueId().toString());
	}
	/**
	 * set a players active tag to this tag
	 * @param uuid Players uuid
	 * @return true if the players tag was set to a new tag
	 */
	public boolean setPlayerTag(String uuid) {
		
		if (playerTags == null) {
			playerTags = new HashMap<>();
		}
		
		if (playerTags.isEmpty()) {
			playerTags.put(uuid, this);
			return true;
		}
		
		if (playerTags.containsKey(uuid) && playerTags.get(uuid) == this) {
			return false;
		}
		
		playerTags.put(uuid, this);
		return true;
	}
	
	/**
	 * remove this tag from any player who has it set as the active tag
	 * @return list of uuids that were removed from this tag
	 */
	public List<String> removeActivePlayers() {
		
		if (playerTags == null || playerTags.isEmpty()) {
			return null;
		}
		
		List<String> remove = new ArrayList<>();
		
		for (String uuid : playerTags.keySet()) {
			if (getPlayerDisplayTag(uuid).equals(this.displayTag)) {
				remove.add(uuid);
			}
		}
		if (!remove.isEmpty()) {
			for (String uuid : remove) {
				removePlayer(uuid);
			}
		}
		
		return remove;
	}
	
	/**
	 * get list of DeluxeTag objects that have been loaded
	 * @return null if no tags have been loaded
	 */
	public static Collection<DeluxeTag> getLoadedTags() {
		if (configTags == null) {
			return null;
		}
		return configTags.values();
	}
	
	/**
	 * check if a player has an active tag
	 * @param uuid Player uuid to check
	 * @return true if uuid is loaded in the playerTags map
	 */
	public static boolean hasTagLoaded(String uuid) {
		if (playerTags == null || playerTags.isEmpty()) {
			return false;
		}
		return playerTags.containsKey(uuid);
	}
	
	/**
	 * check if a player has a tag loaded in the playerTags map
	 * @param player Player to check
	 * @return true if player is loaded in the playerTags map
	 */
	public static boolean hasTagLoaded(Player player) {
		return hasTagLoaded(player.getUniqueId().toString());
	}
	
	/**
	 * get a players current tag identifier
	 * @param p Player to get the identifier for
	 * @return null if player has no loaded tag
	 */
	public static String getPlayerTagIdentifier(Player p) {
		return getPlayerTagIdentifier(p.getUniqueId().toString());
	}
	
	/**
	 * get a players current tag identifier
	 * @param uuid Players uuid to get the identifier for
	 * @return null if player has no loaded tag
	 */
	public static DeluxeTag getTag(String uuid) {
		
		if (playerTags == null) {
			playerTags = new HashMap<>();
		}
		
		if (playerTags.isEmpty()) {
			return null;
		}
		
		if (playerTags.containsKey(uuid) && playerTags.get(uuid) != null) {
			return playerTags.get(uuid);
		}
		
		return null;
	}
	
	/**
	 * get a players current tag identifier
	 * @param uuid Players uuid to get the identifier for
	 * @return null if player has no loaded tag
	 */
	public static String getPlayerTagIdentifier(String uuid) {
		
		if (playerTags == null) {
			playerTags = new HashMap<>();
		}
		
		if (playerTags.isEmpty()) {
			return null;
		}
		
		if (playerTags.containsKey(uuid) && playerTags.get(uuid) != null) {
			if (playerTags.get(uuid).getIdentifier() != null) {
				return playerTags.get(uuid).getIdentifier();
			}
		}
		
		return null;
	}
	
	/**
	 * get a players current display tag if they have one set, an empty string if not
	 * @param player Player to get display tag for
	 * @return players current active display tag if they have one set
	 */
	@SuppressWarnings("deprecation")
	public static String getPlayerDisplayTag(Player player) {
		String d = getPlayerDisplayTag(player.getUniqueId().toString());
		return DeluxeTags.papi() ? PlaceholderAPI.setPlaceholders(player, d): d;
	}
	
	/**
	 * get a players current display tag if they have one set, an empty string if not
	 * @param uuid Player uuid to get display tag for
	 * @return players current active display tag if they have one set
	 */
	public static String getPlayerDisplayTag(String uuid) {
		
		if (playerTags == null) {
			playerTags = new HashMap<>();
		}
		
		if (playerTags.isEmpty()) {
			return "";
		}
		
		if (playerTags.containsKey(uuid) && playerTags.get(uuid) != null) {
			if (playerTags.get(uuid).getDisplayTag() != null) {
				return playerTags.get(uuid).getDisplayTag();
			}
		}
		
		return "";
	}
	
	/**
	 * get a players current tag description if they have a tag set, an empty string if not
	 * @param p Player to get tag description for
	 * @return players current tag description if they have a tag set, empty string otherwise
	 */
	@SuppressWarnings("deprecation")
	public static String getPlayerTagDescription(Player p) {
		String d = getPlayerTagDescription(p.getUniqueId().toString());
		return DeluxeTags.papi() ? PlaceholderAPI.setPlaceholders(p, d): d;
	}
	
	/**
	 * get a players current tag description if they have a tag set, an empty string if not
	 * @param uuid Player uuid to get tag description for
	 * @return players current tag description if they have a tag set, empty string otherwise
	 */
	public static String getPlayerTagDescription(String uuid) {
		
		if (playerTags == null) {
			playerTags = new HashMap<>();
		}
		
		if (playerTags.isEmpty()) {
			return "";
		}
		
		if (playerTags.containsKey(uuid) && playerTags.get(uuid) != null) {
			if (playerTags.get(uuid).getDescription() != null) {
				return playerTags.get(uuid).getDescription();
			}
		}
		
		return "";
	}
	
	/**
	 * get the DeluxeChat tag object loaded by its identifier String
	 * @param identifier String identifier of the tag object to get
	 * @return null if there is no DeluxeTag object loaded for the identifier provided
	 */
	public static DeluxeTag getLoadedTag(String identifier) {
		if (configTags != null && !configTags.isEmpty()) {

			for (DeluxeTag t : getLoadedTags()) {
				if (t.getIdentifier().equals(identifier)) {
					return t;
				}
			}
		}
		
		return null;
	}
	
	public static DeluxeTag getForcedTag(Player p) {
		
		if (getLoadedTags() == null || getLoadedTags().isEmpty()) {
			return null;
		}
		Iterator<Integer> it = configTags.keySet().iterator();
		
		DeluxeTag t;
		
		while (it.hasNext()) {
			
			t = configTags.get(it.next());
			
			if (t != null && t.hasForceTagPermission(p)) {
				return t;
			}
		}
		return null;
	}
	
	/**
	 * get a list of all available tag identifiers a player has permission for
	 * @param p Player to get tag identifiers for
	 * @return null if no tags are loaded, empty list if player doesn't have permission to any tags
	 */
	public static List<String> getAvailableTagIdentifiers(Player p) {
		
		if (getLoadedTags() == null || getLoadedTags().isEmpty()) {
			return null;
		}
		
		Iterator<Integer> it = configTags.keySet().iterator();
		
		List<String> identifiers = new ArrayList<>();
		DeluxeTag t;
		while (it.hasNext()) {
			t = configTags.get(it.next());
			if (t != null && t.hasTagPermission(p)) {
				identifiers.add(t.getIdentifier());
			}
		}
		return identifiers;
	}
	
	public static int getLoadedTagsAmount() {
		if (configTags == null || configTags.isEmpty()) {
			return 0;
		}
		return configTags.size();
	}
	
	/**
	 * get a list of all uuids that are currently loaded into the cache
	 * @return null if no players are loaded
	 */
	public static Set<String> getLoadedPlayers() {
		if (playerTags == null || playerTags.isEmpty()) {
			return null;
		}
		
		return playerTags.keySet();
	}
	
	/**
	 * remove a player from the cache
	 * @param uuid Player uuid to remove
	 */
	public static void removePlayer(String uuid) {
		if (hasTagLoaded(uuid)) {
			playerTags.remove(uuid);
		}
	}
	
	public static void unloadData() {
		configTags = null;
		playerTags = null;
	}
}
