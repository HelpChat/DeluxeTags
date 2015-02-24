package me.clip.deluxetags;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.entity.Player;

/**
 * DeluxeTag object
 * @author Ryan McCarthy
 *
 */
public class DeluxeTag {
	
	private static Map<String, DeluxeTag> configTags;
	
	private static Map<String, DeluxeTag> playerTags;
	
	private String identifier;
	
	private String displayTag;
	
	/**
	 * DeluxeTag object initializer
	 * @param identifier String identifier of this DeluxeTag
	 * @param displayTag String displayTag of this DeluxeTag
	 */
	public DeluxeTag(String identifier, String displayTag) {
		this.identifier = identifier;
		this.displayTag = displayTag;
	}
	
	public static void unload() {
		configTags = null;
		playerTags = null;
	}
	
	/**
	 * get the identifier of this tag
	 * @return tag identifier
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
	 * set the display tag for this DeluxeTag
	 * @param newDisplayTag new display tag String
	 */
	public void setDisplayTag(String newDisplayTag) {
		this.displayTag = newDisplayTag;
	}
	
	/**
	 * load this tag into the tag list
	 */
	public void updateTag() {
		if (configTags == null) {
			configTags = new HashMap<String, DeluxeTag>();
		}
		
		configTags.put(identifier, this);
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
	 * check if a player has permission for this tag
	 * @param p Player to check permission for
	 * @return true if player has permission
	 */
	public boolean hasTagPermission(Player p) {
		return p.hasPermission("deluxetags.tag."+identifier);
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
	 * check if a player has a tag loaded in the playerTags map
	 * @param uuid Player uuid to check
	 * @return true if uuid is loaded in the playerTags map
	 */
	public static boolean hasTagLoaded(String uuid) {
		if (playerTags == null || playerTags.isEmpty()) {
			return false;
		}
		return playerTags.keySet().contains(uuid);
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
			playerTags = new HashMap<String, DeluxeTag>();
		}
		
		if (playerTags.isEmpty()) {
			playerTags.put(uuid, this);
			return true;
		}
		
		if (playerTags.keySet().contains(uuid) && playerTags.get(uuid) == this) {
			return false;
		}
		
		playerTags.put(uuid, this);
		return true;
	}
	
	/**
	 * get a players current display tag if they have one set, an empty string if not
	 * @param player Player to get display tag for
	 * @return players current active display tag if they have one set
	 */
	public static String getPlayerDisplayTag(Player player) {
		return getPlayerDisplayTag(player.getUniqueId().toString());
	}
	
	/**
	 * get a players current display tag if they have one set, an empty string if not
	 * @param uuid Player uuid to get display tag for
	 * @return players current active display tag if they have one set
	 */
	public static String getPlayerDisplayTag(String uuid) {
		
		if (playerTags == null) {
			playerTags = new HashMap<String, DeluxeTag>();
		}
		
		if (playerTags.isEmpty()) {
			return "";
		}
		
		if (playerTags.keySet().contains(uuid) && playerTags.get(uuid) != null) {
			if (playerTags.get(uuid).getDisplayTag() != null) {
				return playerTags.get(uuid).getDisplayTag();
			}
		}
		
		return "";
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
	public static String getPlayerTagIdentifier(String uuid) {
		
		if (playerTags == null) {
			playerTags = new HashMap<String, DeluxeTag>();
		}
		
		if (playerTags.isEmpty()) {
			return null;
		}
		
		if (playerTags.keySet().contains(uuid) && playerTags.get(uuid) != null) {
			if (playerTags.get(uuid).getIdentifier() != null) {
				return playerTags.get(uuid).getIdentifier();
			}
		}
		
		return null;
	}
	
	/**
	 * get the DeluxeChat tag object loaded by its identifier String
	 * @param identifier String identifier of the tag object to get
	 * @return null if there is no DeluxeTag object loaded for the identifier provided
	 */
	public static DeluxeTag getLoadedTag(String identifier) {
		if (configTags != null && !configTags.isEmpty()) {
		
			if (configTags.keySet().contains(identifier)) {
				return configTags.get(identifier);
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
		
		Iterator<DeluxeTag> it = getLoadedTags().iterator();
		
		List<String> identifiers = new ArrayList<String>();
		DeluxeTag t = null;
		while (it.hasNext()) {
			t = it.next();
			if (t.hasTagPermission(p)) {
				identifiers.add(t.getIdentifier());
			}
		}
		return identifiers;
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
	
}
