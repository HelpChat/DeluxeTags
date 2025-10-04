package me.clip.deluxetags.listeners;

import me.clip.deluxetags.DeluxeTags;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {
	
	final DeluxeTags plugin;

	public JoinListener(DeluxeTags instance) {
		plugin = instance;
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onChat(final PlayerJoinEvent event) {
		plugin.getTagsHandler().updateTagForPlayer(event.getPlayer());
	}
}