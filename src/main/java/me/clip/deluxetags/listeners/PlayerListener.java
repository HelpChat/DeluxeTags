package me.clip.deluxetags.listeners;

import me.clip.deluxetags.DeluxeTags;
import me.clip.deluxetags.tags.DeluxeTag;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class PlayerListener implements Listener {
	
	DeluxeTags plugin;

	public PlayerListener(DeluxeTags instance) {
		plugin = instance;
	}	
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onChat(final AsyncPlayerChatEvent event) {
		// Forced tags take priority over all other tags
		if (plugin.getCfg().forceTags() && setForcedTag(event.getPlayer())) {
			return;
		}

		// If player has a tag loaded in memory, and has permissions to use it, use that tag
		final DeluxeTag currentTag = plugin.getTagsHandler().getTag(event.getPlayer());
		if (currentTag != null && currentTag != plugin.getDummyTag() && currentTag.hasPermissionToUse(event.getPlayer())) {
			return;
		}

		// If player has a saved selected tag (in player_tags file), and permissions to use it, use that tag
		if (setSavedTag(event.getPlayer())) {
			return;
		}

		// If the player has no tag forced or selected try to load a default tag
		if (setDefaultTag(event.getPlayer())) {
			return;
		}

		// The player has no forced, selected tag or default tag. Use the dummy tag
		plugin.getTagsHandler().setPlayerTag(event.getPlayer(), plugin.getDummyTag());
		plugin.removeSavedTag(event.getPlayer().getUniqueId().toString());
	}

	private boolean setForcedTag(final Player player) {
		final DeluxeTag forcedTag = plugin.getTagsHandler().getForcedTag(player);
		if (forcedTag == null) {
			return false;
		}

		plugin.getTagsHandler().setPlayerTag(player, forcedTag);
		return true;
	}

	private boolean setSavedTag(final Player player) {
		final String identifier = plugin.getSavedTagIdentifier(player.getUniqueId().toString());
		if (identifier == null) {
			return false;
		}

		final DeluxeTag loadedTag = plugin.getTagsHandler().getLoadedTag(identifier);
		if (loadedTag == null || !loadedTag.hasPermissionToUse(player)) {
			return false;
		}

		plugin.getTagsHandler().setPlayerTag(player, loadedTag);
		return true;
	}

	private boolean setDefaultTag(final Player player) {
		final DeluxeTag tag = plugin.getTagsHandler().getDefaultTag(player);
		if (tag == null) {
			return false;
		}

		plugin.getTagsHandler().setPlayerTag(player, tag);
		return true;
	}
}
