package me.clip.deluxetags;

import java.util.Iterator;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class CleanupTask implements Runnable {
	
	DeluxeTags plugin;
	
	public CleanupTask(DeluxeTags instance) {
		plugin = instance;
	}
	
	@Override
	public void run() {
		
		if (DeluxeTag.getLoadedPlayers() == null || DeluxeTag.getLoadedPlayers().isEmpty()) {
			return;
		}
		
		Bukkit.getScheduler().runTask(plugin, new Runnable() {

			@Override
			public void run() {
				
				Iterator<String> it = DeluxeTag.getLoadedPlayers().iterator();
				
				while (it.hasNext()) {
					String uuid = it.next();
					
					Player p = Bukkit.getServer().getPlayer(UUID.fromString(uuid));
					
					if (p == null) {
						DeluxeTag.removePlayer(uuid);
					}
				}
			}
		});
		
		
		
	}
}
