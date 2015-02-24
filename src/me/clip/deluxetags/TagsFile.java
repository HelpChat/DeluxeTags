package me.clip.deluxetags;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class TagsFile {

	DeluxeTags plugin;
	private FileConfiguration dataConfig = null;
	private File dataFile = null;

	public TagsFile(DeluxeTags instance) {
		this.plugin = instance;
	}

	public boolean reload() {
		boolean firstLoad;
		
		if (this.dataFile == null) {
			this.dataFile = new File(plugin.getDataFolder()+File.separator+"userdata"+File.separator+"player_tags.yml");		
		}
		
		if (this.dataFile.exists()) {
			firstLoad = false;
		} else {
			firstLoad = true;
		}

		this.dataConfig = YamlConfiguration.loadConfiguration(this.dataFile);
		setHeader();
		return firstLoad;
	}

	public FileConfiguration load() {
		if (this.dataConfig == null) {
			reload();
		}
		return this.dataConfig;
	}

	public void save() {
		if ((this.dataConfig == null) || (this.dataFile == null))
			return;
		try {
			load().save(this.dataFile);
		} catch (IOException ex) {
			this.plugin.getLogger().log(Level.SEVERE, "Could not save to " + this.dataFile, ex);
		}
	}
	
	public void setHeader() {
		this.dataConfig.options().header("DeluxeTags player_tags.yml file\nYou do not have to edit this file!");
		this.dataConfig.options().copyDefaults(true);
		save();
	}
	
	public String getTagIdentifier(String uuid) {
		FileConfiguration c = this.dataConfig;
		if (c.contains(uuid) && c.isString(uuid) && c.getString(uuid) != null) {
			return c.getString(uuid);
		}
		return null;
	}
	
	public void saveTagIdentifier(String uuid, String tagIdentifier) {
		FileConfiguration c = this.dataConfig;
		c.set(uuid, tagIdentifier);
		save();
		return;
	}
	
	public void removePlayer(String uuid) {
		FileConfiguration c = this.dataConfig;
		c.set(uuid, null);
		save();
		return;
	}
	
	
}
