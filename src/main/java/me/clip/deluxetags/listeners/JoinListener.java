package me.clip.deluxetags.listeners;

import me.clip.deluxetags.DeluxeTags;
import me.clip.deluxetags.tags.DeluxeTag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {
	
	DeluxeTags plugin;

	public JoinListener(DeluxeTags instance) {
		plugin = instance;
	}	
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onChat(PlayerJoinEvent event) {
		if (!event.getPlayer().isOp() && plugin.getCfg().forceTags()) {
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
