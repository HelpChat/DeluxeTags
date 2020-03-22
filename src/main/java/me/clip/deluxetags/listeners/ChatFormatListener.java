package me.clip.deluxetags.listeners;

import me.clip.deluxetags.DeluxeTags;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatFormatListener implements Listener {
	
	DeluxeTags plugin;
	
	public ChatFormatListener(DeluxeTags i) {
		plugin = i;
	}
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onChat(AsyncPlayerChatEvent e) {
		e.setFormat(plugin.getCfg().chatFormat());
	}
}
