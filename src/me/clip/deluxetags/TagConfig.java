package me.clip.deluxetags;

import java.util.Set;

import org.bukkit.configuration.file.FileConfiguration;

public class TagConfig {

	DeluxeTags plugin;
	
	public TagConfig(DeluxeTags i) {
		plugin = i;
	}
	
	public void loadDefConfig() {
		FileConfiguration config = plugin.getConfig();
		config.options().header("DeluxeTags version: "+plugin.getDescription().getVersion()+" Main Configuration"+
				  "\n" 
				+ "\nCreate your tags using the following format:"
				+ "\n" 
				+ "\ntags:"
				+ "\n  VIP: '&7[&eVIP&7]'"
				+ "\n  donator: '&7[&eDonator&7]'");
		
		if (!config.contains("tags")) {
			config.set("tags.example", "&8[&aExampleTag&8]");
		}
		config.options().copyDefaults(true);
		plugin.saveConfig();
		plugin.reloadConfig();
	}
	
	public int loadTags() {
		
		FileConfiguration c = plugin.getConfig();
		
		int loaded = 0;
		if (c.contains("tags")) {
			
			Set<String> keys = c.getConfigurationSection("tags").getKeys(false);
			
			if (keys != null && !keys.isEmpty()) {
				
				for (String identifier : keys) {
					String value = c.getString("tags."+identifier);
					if (value != null) {
						DeluxeTag tag = new DeluxeTag(identifier, value);
						tag.updateTag();
						loaded++;
					}
				}
			}
		}
		return loaded;
	}
	
	
}
