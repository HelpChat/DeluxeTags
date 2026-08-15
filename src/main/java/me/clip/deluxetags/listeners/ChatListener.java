package me.clip.deluxetags.listeners;

import me.clip.deluxetags.DeluxeTags;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import me.clip.deluxetags.utils.Scheduler;

public class ChatListener implements Listener {
	
	DeluxeTags plugin;
	/**
	 * Add chat compatibility for other plugins
	 */
	public ChatListener(DeluxeTags instance) {
		plugin = instance;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onChat(AsyncPlayerChatEvent event) {
		String original = event.getFormat();
		String format = Scheduler.callAtEntity(plugin, event.getPlayer(),
				() -> plugin.setPlaceholders(event.getPlayer(), original, null), original);
		event.setFormat(format);
	}
}
