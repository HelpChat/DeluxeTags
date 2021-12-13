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
		if (DeluxeTag.getLoadedPlayers() == null || DeluxeTag.getLoadedPlayers().isEmpty()) {
			return;
		}

		List<String> remove = new ArrayList<>();

		Bukkit.getScheduler().runTask(plugin, () -> {
			for (String uuid: DeluxeTag.getLoadedPlayers()) {
				Player player = Bukkit.getPlayer(UUID.fromString(uuid));
				if (player == null) remove.add(uuid);
			}

			if (remove.isEmpty()) return;
			for (String uuid : remove) DeluxeTag.removePlayer(uuid);
		});
	}
}
