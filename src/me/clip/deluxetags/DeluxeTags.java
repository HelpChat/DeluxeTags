package me.clip.deluxetags;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

/**
 * DeluxeTags main class
 * @author Ryan McCarthy
 */
public class DeluxeTags extends JavaPlugin {

	protected TagConfig cfg;
	
	protected TagsFile playerFile;
	
	protected DeluxeTag dummy;
	
	private BukkitTask cleanupTask = null;
	
	@Override
	public void onEnable() {
		if (Bukkit.getPluginManager().isPluginEnabled("DeluxeChat")) {
			
			dummy = new DeluxeTag("", "");
			
			cfg = new TagConfig(this);
			
			cfg.loadDefConfig();
			
			getLogger().info(cfg.loadTags()+" tags loaded");
			
			playerFile = new TagsFile(this);
			
			playerFile.reload();
			
			playerFile.save();
		
			Bukkit.getPluginManager().registerEvents(new ChatListener(this), this);
			
			getCommand("tags").setExecutor(new TagCommand(this));
			
			cleanupTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this, new CleanupTask(this), 20L*300, 20L*300);
			
		} else {
			
			getLogger().warning("Could not hook into DeluxeChat! DeluxeTags will now disable!");
			
			Bukkit.getPluginManager().disablePlugin(this);
		}
	}
	
	@Override
	public void onDisable() {
		
		if (cleanupTask != null) {
			
			cleanupTask.cancel();
			
			cleanupTask = null;
			
		}
		
		DeluxeTag.unload();
		
		dummy = null;
	}
}
