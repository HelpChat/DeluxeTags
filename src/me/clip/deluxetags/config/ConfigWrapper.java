package me.clip.deluxetags.config;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigWrapper {

	private final JavaPlugin plugin;
	private FileConfiguration config;
	private File configFile;
	private final String folderName, fileName;

	public ConfigWrapper(final JavaPlugin instance, final String folderName, final String fileName) {
		this.plugin = instance;
		this.folderName = folderName;
		this.fileName = fileName;
	}

	public void createNewFile(final String message, final String header) {
		reloadConfig();
		saveConfig();
		loadConfig(header);

		if (message != null) {
			plugin.getLogger().info(message);
		}
	}

	public FileConfiguration getConfig() {
		if (config == null) {
			reloadConfig();
		}
		return config;
	}

	public void loadConfig(final String header) {
		config.options().header(header);
		config.options().copyDefaults(true);
		saveConfig();
	}

	public void reloadConfig() {
		if (configFile == null) {
			if (folderName != null && !folderName.isEmpty()) {
				configFile = new File(plugin.getDataFolder() + File.separator + folderName, fileName);
			} else {
				configFile = new File(plugin.getDataFolder(), fileName);
			}
		}
		config = YamlConfiguration.loadConfiguration(configFile);
	}

	public void saveConfig() {
		if (config == null || configFile == null) {
			return;
		}
		try {
			getConfig().save(configFile);
		} catch (final IOException ex) {
			plugin.getLogger().log(Level.SEVERE, "Could not save config to " + configFile, ex);
		}
	}
}
