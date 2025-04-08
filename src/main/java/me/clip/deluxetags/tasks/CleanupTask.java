package me.clip.deluxetags.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import me.clip.deluxetags.DeluxeTags;
import me.clip.deluxetags.tags.DeluxeTag;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class CleanupTask implements Runnable {
	
	DeluxeTags plugin;
	
	public CleanupTask(DeluxeTags instance) {
		plugin = instance;
	}
	
	@Override
	public void run() {
		if (plugin.getTagsHandler().getLoadedPlayers() == null || plugin.getTagsHandler().getLoadedPlayers().isEmpty()) {
			return;
		}

		List<UUID> remove = new ArrayList<>();

		Bukkit.getScheduler().runTask(plugin, () -> {
			for (UUID uuid: plugin.getTagsHandler().getLoadedPlayers()) {
				Player player = Bukkit.getPlayer(uuid);
				if (player == null) remove.add(uuid);
			}

			if (remove.isEmpty()) return;
			for (UUID uuid : remove) plugin.getTagsHandler().removePlayer(uuid);
		});
	}
}
