package me.clip.deluxetags.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import me.clip.deluxetags.DeluxeTags;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class CleanupTask implements Runnable {
	
	DeluxeTags plugin;

	/**
	 * This task removes active tags from players who are no longer online.
	 */
	public CleanupTask(DeluxeTags instance) {
		plugin = instance;
	}
	
	@Override
	public void run() {
		final Set<UUID> playersWithActiveTags = plugin.getTagsHandler().getPlayersWithActiveTags();
        if (playersWithActiveTags.isEmpty()) {
			return;
		}

		final List<UUID> toRemove = new ArrayList<>();

		Bukkit.getScheduler().runTask(plugin, () -> {
			for (final UUID uuid: playersWithActiveTags) {
				final Player player = Bukkit.getPlayer(uuid);
				if (player == null) {
					toRemove.add(uuid);
				}
			}

			if (toRemove.isEmpty()) {
				return;
			}

			for (final UUID uuid : toRemove) {
				plugin.getTagsHandler().removeActiveTagFromPlayer(uuid);
			}
		});
	}
}
