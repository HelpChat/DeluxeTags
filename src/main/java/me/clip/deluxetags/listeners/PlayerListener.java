package me.clip.deluxetags.listeners;

import me.clip.deluxetags.DeluxeTags;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import me.clip.deluxetags.utils.Scheduler;

public class PlayerListener implements Listener {
	
	DeluxeTags plugin;

	public PlayerListener(DeluxeTags instance) {
		plugin = instance;
	}	
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onChat(final AsyncPlayerChatEvent event) {
		Scheduler.callAtEntity(plugin, event.getPlayer(), () -> {
			plugin.getTagsHandler().updateTagForPlayer(event.getPlayer());
			return Boolean.TRUE;
		}, Boolean.FALSE);
	}
}
