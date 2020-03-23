package me.clip.deluxetags.commands;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import me.clip.deluxetags.DeluxeTag;
import me.clip.deluxetags.DeluxeTags;
import me.clip.deluxetags.Lang;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TagConsoleCommand implements CommandExecutor {

	private DeluxeTags plugin;
	
	public TagConsoleCommand(DeluxeTags i) {
		plugin = i;
	}
	
	@Override
	public boolean onCommand(CommandSender s, Command c, String label, String[] args) {

		if (args.length == 0) {

			DeluxeTags.msg(s, "&8&m+----------------+");
			DeluxeTags.msg(s, "&5&lDeluxeTags &f&o" + plugin.getDescription().getVersion());
			DeluxeTags.msg(s, "&7Created by &f&oextended_clip");
			DeluxeTags.msg(s, "Use /tags help for console commands");
			DeluxeTags.msg(s, "&8&m+----------------+");


		} else if (args[0].equalsIgnoreCase("help")) {

			DeluxeTags.msg(s, "&8&m+----------------+");
			DeluxeTags.msg(s, "DeluxeTags help");
			DeluxeTags.msg(s, " ");
			DeluxeTags.msg(s, "/tags list all/playername");
			DeluxeTags.msg(s, "list all / players available tags");
			DeluxeTags.msg(s, "/tags set <player> <tag>");
			DeluxeTags.msg(s, Lang.CMD_HELP_ADMIN_SET.getConfigValue(null));
			DeluxeTags.msg(s, "/tags clear <player>");
			DeluxeTags.msg(s, Lang.CMD_HELP_ADMIN_CLEAR.getConfigValue(null));
			DeluxeTags.msg(s, "/tags create <identifier> <tag>");
			DeluxeTags.msg(s, Lang.CMD_HELP_ADMIN_CREATE.getConfigValue(null));
			DeluxeTags.msg(s, "/tags delete <identifier>");
			DeluxeTags.msg(s, Lang.CMD_HELP_ADMIN_DELETE.getConfigValue(null));
			DeluxeTags.msg(s, "/tags version");
			DeluxeTags.msg(s, Lang.CMD_HELP_VERSION.getConfigValue(null));
			DeluxeTags.msg(s, "/tags reload");
			DeluxeTags.msg(s, Lang.CMD_HELP_RELOAD.getConfigValue(null));
			DeluxeTags.msg(s, "&8&m+----------------+");
			return true;

		} else if (args[0].equalsIgnoreCase("version")) {

			DeluxeTags.msg(s, "&8&m+----------------+");
			DeluxeTags.msg(s, "&5&lDeluxeTags &f&o" + plugin.getDescription().getVersion());
			DeluxeTags.msg(s, "&7Created by &f&oextended_clip");
			DeluxeTags.msg(s, "&8&m+----------------+");

			return true;

		} else if (args[0].equalsIgnoreCase("list")) {

			if (args.length == 1 || args[1].equalsIgnoreCase("all")) {

				if (DeluxeTag.getLoadedTags() == null || DeluxeTag.getLoadedTags().isEmpty()) {

					DeluxeTags.msg(s, Lang.CMD_NO_TAGS_LOADED.getConfigValue(null));
					return true;
				}

				final Collection<DeluxeTag> t = DeluxeTag.getLoadedTags();

				StringBuilder sb = new StringBuilder();

				for (DeluxeTag d : t) {
					sb.append("&f").append(d.getIdentifier()).append("&7:&f").append(d.getDisplayTag())
							.append("&a, ");
				}

				String tags = sb.toString().trim();

				String amount = String.valueOf(t.size());

				DeluxeTags.msg(s, Lang.CMD_TAG_LIST_ALL.getConfigValue(new String[]{
						amount, tags
				}));

			} else {

				if (DeluxeTag.getLoadedTags() == null || DeluxeTag.getLoadedTags().isEmpty()) {

					DeluxeTags.msg(s, Lang.CMD_NO_TAGS_LOADED.getConfigValue(null));
					return true;
				}

				String tar = args[1];

				Player target = Bukkit.getPlayer(tar);

				if (target == null) {

					DeluxeTags.msg(s, Lang.CMD_TARGET_NOT_ONLINE.getConfigValue(new String[]{
							tar
					}));
					return true;
				}

				List<String> t = DeluxeTag.getAvailableTagIdentifiers(target);

				String tags = t.toString().replace("[", "&7").replace(",", "&a,&7").replace("]", "");

				String amount = String.valueOf(t.size());

				DeluxeTags.msg(s, Lang.CMD_TAG_LIST_TARGET.getConfigValue(new String[]{
						target.getName(), amount, tags
				}));

			}

			return true;

		} else if (args[0].equalsIgnoreCase("create")) {

			if (args.length < 3) {

				DeluxeTags.msg(s, Lang.CMD_ADMIN_CREATE_TAG_INCORRECT.getConfigValue(null));
				return true;
			}

			String id = args[1];

			if (DeluxeTag.getLoadedTag(id) != null) {
				DeluxeTags.msg(s, Lang.CMD_ADMIN_CREATE_TAG_FAIL.getConfigValue(new String[]{
						id
				}));
				return true;
			}

			String tag = StringUtils.join(Arrays.copyOfRange(args, 2, args.length), " ");

			if (!tag.isEmpty()) {

				if (tag.endsWith("_")) {
					tag = tag.substring(0, tag.length() - 1) + " ";
				}

				int priority = DeluxeTag.getLoadedTagsAmount() + 1;

				DeluxeTag dTag = new DeluxeTag(priority, id, tag, "");

				dTag.load();

				plugin.getCfg().saveTag(priority, id, tag, "&f", "deluxetags.tag" + id);

				DeluxeTags.msg(s, Lang.CMD_ADMIN_CREATE_TAG_SUCCESS.getConfigValue(new String[]{
						id, tag
				}));

				return true;
			}

		}  else if (args[0].equalsIgnoreCase("delete")) {
				
				if (args.length != 2) {
					
					DeluxeTags.msg(s, Lang.CMD_ADMIN_DELETE_TAG_INCORRECT.getConfigValue(null));
					return true;
				}
				
				String id = args[1];
				
				DeluxeTag tag = DeluxeTag.getLoadedTag(id);
				
				if (tag != null) {
					
					List<String> r = tag.removeActivePlayers();
					
					if (r != null && !r.isEmpty()) {
						
						plugin.removeSavedTags(r);
					}
					
					if (tag.unload()) {
						
						plugin.getCfg().removeTag(id);
						
						DeluxeTags.msg(s, Lang.CMD_ADMIN_DELETE_TAG_SUCCESS.getConfigValue(new String[] {
							id
						}));
						
						return true;
					}
				}
					
				DeluxeTags.msg(s, Lang.CMD_ADMIN_DELETE_TAG_FAIL.getConfigValue(new String[] {
					id
				}));

				return true;
				
			}  else if (args[0].equalsIgnoreCase("setdesc") || args[0].equalsIgnoreCase("setdescription")) {
				
				if (args.length < 3) {
					
					DeluxeTags.msg(s, Lang.CMD_ADMIN_SET_DESCRIPTION_INCORRECT.getConfigValue(null));
					return true;
				}
				
				String id = args[1];
				
				if (DeluxeTag.getLoadedTag(id) == null) {
					DeluxeTags.msg(s, Lang.CMD_ADMIN_SET_DESCRIPTION_FAIL.getConfigValue(new String[] {
							id
						}));
					return true;
				}
				
				DeluxeTag tag = DeluxeTag.getLoadedTag(id);
					
				String desc = StringUtils.join(Arrays.copyOfRange(args, 2, args.length), " ");
				
				if (desc.endsWith("_")) {
					desc = desc.substring(0, desc.length()-1)+" ";
				}
						
				tag.setDescription(desc);

				plugin.getCfg().saveTag(tag.getPriority(), tag.getIdentifier(), tag.getDisplayTag(), tag.getDescription(), tag.getPermission());
					
				DeluxeTags.msg(s, Lang.CMD_ADMIN_SET_DESCRIPTION_SUCCESS.getConfigValue(new String[] {
					id, tag.getDisplayTag(), desc
				}));

				return true;
				
			} else if (args[0].equalsIgnoreCase("set")) {
				
				if (args.length != 3) {
				
					DeluxeTags.msg(s, Lang.CMD_ADMIN_SET_INCORRECT_ARGS.getConfigValue(null));
					return true;
				}
				
				if (DeluxeTag.getLoadedTags() == null || DeluxeTag.getLoadedTags().isEmpty()) {
					
					DeluxeTags.msg(s, Lang.CMD_NO_TAGS_LOADED.getConfigValue(null));
					return true;
				}
				
				Player target = Bukkit.getPlayer(args[1]);
				
				if (target == null) {
					
					DeluxeTags.msg(s, Lang.CMD_TARGET_NOT_ONLINE.getConfigValue(new String[] {
						args[1]
					}));
					return true;
				}

				List<String> avail = DeluxeTag.getAvailableTagIdentifiers(target);
				
				if (avail == null || avail.isEmpty()) {
					
					DeluxeTags.msg(s, Lang.CMD_ADMIN_SET_NO_TAGS.getConfigValue(new String[] {
						target.getName()
					}));
					return true;
				}
				
				String tag = args[2];
				
				for (String t : avail) {
					
					if (t.equalsIgnoreCase(tag)) {
						
						DeluxeTag set = DeluxeTag.getLoadedTag(t);
						
						if (set != null) {
							
							set.setPlayerTag(target);
							
							plugin.saveTagIdentifier(target.getUniqueId().toString(), set.getIdentifier());
							
							DeluxeTags.msg(s, Lang.CMD_ADMIN_SET_SUCCESS.getConfigValue(new String[] {
								target.getName(), set.getIdentifier(), set.getDisplayTag()
							}));
							
								
							DeluxeTags.msg(target, Lang.CMD_ADMIN_SET_SUCCESS_TARGET.getConfigValue(new String[] {
								set.getIdentifier(), set.getDisplayTag(), "CONSOLE"
							}));
	
							return true;
						}
					}
				}
				
				DeluxeTags.msg(s, Lang.CMD_ADMIN_SET_FAIL.getConfigValue(new String[] {
					tag, target.getName()
				}));
				
				return true;
				
			}  else if (args[0].equalsIgnoreCase("clear")) {
				
				if (args.length != 2) {
					
					DeluxeTags.msg(s, Lang.CMD_ADMIN_CLEAR_INCORRECT_ARGS.getConfigValue(null));
					return true;
				}
				
				if (DeluxeTag.getLoadedTags() == null || DeluxeTag.getLoadedTags().isEmpty()) {
					
					DeluxeTags.msg(s, Lang.CMD_NO_TAGS_LOADED.getConfigValue(null));
					return true;
				}
				
				Player target = Bukkit.getPlayer(args[1]);
				
				if (target == null) {
					
					DeluxeTags.msg(s, Lang.CMD_TARGET_NOT_ONLINE.getConfigValue(new String[] {
						args[1]
					}));
					return true;
				}

				String tag = DeluxeTag.getPlayerTagIdentifier(target);
				
				if (tag == null || DeluxeTag.getPlayerDisplayTag(target).isEmpty()) {
					
					DeluxeTags.msg(s, Lang.CMD_ADMIN_CLEAR_NO_TAG_SET.getConfigValue(new String[] {
						target.getName()
					}));
					return true;
				}
				
				plugin.getDummy().setPlayerTag(target);
				plugin.removeSavedTag(target.getUniqueId().toString());
				
				DeluxeTags.msg(s, Lang.CMD_ADMIN_CLEAR_SUCCESS.getConfigValue(new String[] {
						target.getName()
				}));

					
				DeluxeTags.msg(target, Lang.CMD_ADMIN_CLEAR_SUCCESS_TARGET.getConfigValue(new String[] {
					"CONSOLE"
				}));
				
				return true;
				
			} else if (args[0].equalsIgnoreCase("reload")) {

				plugin.reloadConfig();
				plugin.saveConfig();
				DeluxeTag.unloadData();
				int loaded = plugin.getCfg().loadTags();
				
				DeluxeTags.setForceTags(plugin.getCfg().forceTags());
				
				plugin.getPlayerFile().reloadConfig();
				plugin.getPlayerFile().saveConfig();
				
				plugin.getLangFile().reloadConfig();
				plugin.getLangFile().saveConfig();
				plugin.loadMessages();
				
				
				plugin.reloadGUIOptions();
				
				for (Player online : Bukkit.getServer().getOnlinePlayers()) {
					
					if (!DeluxeTag.hasTagLoaded(online)) {
						
						String identifier = plugin.getSavedTagIdentifier(online.getUniqueId().toString());
						
						if (identifier != null 
								&& DeluxeTag.getLoadedTag(identifier) != null 
								&& DeluxeTag.getLoadedTag(identifier).hasTagPermission(online)) {
							
							DeluxeTag.getLoadedTag(identifier).setPlayerTag(online);
						} else {
						
							plugin.getDummy().setPlayerTag(online);
						}
					}
				}
				
				DeluxeTags.msg(s, Lang.CMD_ADMIN_RELOAD.getConfigValue(new String[] {
						String.valueOf(loaded)
				}));
				
				return true;
			
			
			} else {
				DeluxeTags.msg(s, Lang.CMD_INCORRECT_USAGE.getConfigValue(null));
			}
			return true;
		}
}
