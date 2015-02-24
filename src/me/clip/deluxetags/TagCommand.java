package me.clip.deluxetags;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TagCommand implements CommandExecutor {

	DeluxeTags plugin;

	public TagCommand(DeluxeTags i) {
		plugin = i;
	}

	private void sms(CommandSender s, String msg) {
		s.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
	}

	@Override
	public boolean onCommand(CommandSender s, Command c, String label,
			String[] args) {

		if (!(s instanceof Player)) {

			sms(s, "&cConsole commands not supported yet!");
			return true;
		}

		Player p = (Player) s;

		if (args.length <= 0) {

			sms(s, "&8&m+----------------+");
			sms(s, "&5&lDeluxeTags &f&o"+ plugin.getDescription().getVersion());
			sms(s, "&7Created by &f&oextended_clip");
			sms(s, "&8&m+----------------+");
			return true;
			
		} else if (args[0].equalsIgnoreCase("help")) {
			
		
			sms(s, "&8&m+----------------+");
			sms(s, "&5&lDeluxeTags &f&oHelp");
			sms(s, " ");
			sms(s, "&d/tags list");
			sms(s, "&f&oView tags available to you");
			sms(s, "&d/tags select <tag>");
			sms(s, "&f&oSelect a tag as your active tag");
			if (p.hasPermission("deluxetags.reload")) {
				
				sms(s, "&d/tags reload");
				sms(s, "&f&oReload the tags config");
				
			}
			sms(s, "&8&m+----------------+");
			return true;
		
		
		} else if (args[0].equalsIgnoreCase("reload")) {
			
			if (!p.hasPermission("deluxetags.reload")) {
				sms(p, "&cYou don't have permission to do that!");
				return true;
			}

			plugin.reloadConfig();
			plugin.saveConfig();
			DeluxeTag.unload();
			int loaded = plugin.cfg.loadTags();
			
			
			plugin.playerFile.reload();
			plugin.playerFile.save();
			
			for (Player online : Bukkit.getServer().getOnlinePlayers()) {
				if (!DeluxeTag.hasTagLoaded(online)) {
					
					String identifier = plugin.playerFile.getTagIdentifier(online.getUniqueId().toString());
					
					if (identifier != null 
							&& DeluxeTag.getLoadedTag(identifier) != null 
							&& DeluxeTag.getLoadedTag(identifier).hasTagPermission(online)) {
						
						DeluxeTag.getLoadedTag(identifier).setPlayerTag(online);
					} else {
					
						plugin.dummy.setPlayerTag(online);
					}
				}
			}
			
			sms(p, "&aConfiguration successfully reloaded! &f"+loaded+" &atags loaded!");
			
			
			return true;
		
		
		} else if (args[0].equalsIgnoreCase("list")) {

			if (!p.hasPermission("deluxetags.list")) {
				sms(p, "&cYou don't have permission to do that!");
				return true;
			}

			if (DeluxeTag.getLoadedTags() == null
					|| DeluxeTag.getLoadedTags().isEmpty()) {
				sms(s, "&cThere are no tags loaded!");
				return true;
			}

			if (DeluxeTag.getAvailableTagIdentifiers(p) == null
					|| DeluxeTag.getAvailableTagIdentifiers(p).isEmpty()) {
				sms(s, "&cYou don't have any tags availabled!");
				return true;
			}
			
			String tags = DeluxeTag.getAvailableTagIdentifiers(p).toString()
					.replace("[", "&7").replace(",", "&a,&7").replace("]", "");
			
			sms(p, "&aAvailable tags: "+tags);
			return true;
		} else if (args[0].equalsIgnoreCase("select")) {
			
			if (!p.hasPermission("deluxetags.select")) {
				sms(p, "&cYou don't have permission to do that!");
				return true;
			}
			
			if (args.length != 2) {
				sms(p, "&cIncorrect usage! &7/tags select <tag>");
				return true;
			}
			
			if (DeluxeTag.getLoadedTags() == null
					|| DeluxeTag.getLoadedTags().isEmpty()) {
				sms(s, "&cThere are no tags loaded!");
				return true;
			}

			List<String> avail = DeluxeTag.getAvailableTagIdentifiers(p);
			
			if (avail == null || avail.isEmpty()) {
				sms(s, "&cYou don't have any tags availabled!");
				return true;
			}
			
			String tag = args[1];
			
			for (String t : avail) {
				if (t.equalsIgnoreCase(tag)) {
					
					DeluxeTag set = DeluxeTag.getLoadedTag(t);
					
					if (set != null) {
						set.setPlayerTag(p);
						plugin.playerFile.saveTagIdentifier(p.getUniqueId().toString(), set.getIdentifier());
						sms(s, "&7Your tag was set to: &r"+set.getDisplayTag());
						return true;
					}
				}
			}
			
			sms(s, "&cTag name specified was invalid");
			return true;
			
		} else {
			sms(s, "&cIncorrect usage! Use &7/tags help");
		}
		return true;
	}
}
