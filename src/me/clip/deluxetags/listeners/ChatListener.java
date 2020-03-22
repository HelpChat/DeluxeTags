package me.clip.deluxetags.listeners;

import me.clip.deluxetags.DeluxeTags;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {
	
	DeluxeTags plugin;
	/**
	 * Add chat compatibility for other plugins
	 */
	public ChatListener(DeluxeTags i) {
		plugin = i;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onChat(AsyncPlayerChatEvent e) {
		
		String format = e.getFormat();
		
		format = DeluxeTags.setPlaceholders(e.getPlayer(), format, null);
		
		e.setFormat(format);
	}
}
