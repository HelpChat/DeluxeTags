package me.clip.deluxetags.listeners;

import me.clip.deluxetags.tags.DeluxeTag;
import me.clip.deluxetags.DeluxeTags;
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
	public void onChat(AsyncPlayerChatEvent event) {
		if (!event.getPlayer().isOp() && DeluxeTags.forceTags()) {
			DeluxeTag tag = DeluxeTag.getForcedTag(event.getPlayer());
			if (tag != null) {
				tag.setPlayerTag(event.getPlayer());
			}
		}
		
		if (!DeluxeTag.hasTagLoaded(event.getPlayer())) {
			String identifier = plugin.getSavedTagIdentifier(event.getPlayer().getUniqueId().toString());
			if (identifier == null) {
				plugin.getDummy().setPlayerTag(event.getPlayer());
				plugin.removeSavedTag(event.getPlayer().getUniqueId().toString());
				return;
			}
			DeluxeTag loadedTag = DeluxeTag.getLoadedTag(identifier);
			if (loadedTag != null && loadedTag.hasTagPermission(event.getPlayer())) {
				loadedTag.setPlayerTag(event.getPlayer());
			} else {
				plugin.getDummy().setPlayerTag(event.getPlayer());
				plugin.removeSavedTag(event.getPlayer().getUniqueId().toString());
			}
		}
	}
}
