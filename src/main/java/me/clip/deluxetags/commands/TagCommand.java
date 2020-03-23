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

public class TagCommand implements CommandExecutor {

  private DeluxeTags plugin;

  private TagConsoleCommand console;

  public TagCommand(DeluxeTags i) {
    plugin = i;
    console = new TagConsoleCommand(i);
  }

  @Override
  public boolean onCommand(CommandSender s, Command c, String label,
      String[] args) {

    if (!(s instanceof Player)) {

      return console.onCommand(s, c, label, args);
    }

    Player p = (Player) s;

    if (args.length == 0) {

      if (!p.hasPermission("deluxetags.gui")) {

        DeluxeTags.msg(p, Lang.CMD_NO_PERMS.getConfigValue(new String[]{
            "deluxetags.gui"
        }));
        return true;
      }

      if (DeluxeTag.getLoadedTags() == null || DeluxeTag.getLoadedTags().isEmpty()) {

        DeluxeTags.msg(s, Lang.CMD_NO_TAGS_LOADED.getConfigValue(null));
        return true;
      }

      if (!plugin.getGUIHandler().openMenu(p, 1)) {

        DeluxeTags.msg(s, Lang.CMD_NO_TAGS_AVAILABLE.getConfigValue(null));
      }

      return true;

    } else if (args[0].equalsIgnoreCase("help")) {

      String color = Lang.CMD_HELP_COLOR.getConfigValue(null);
      DeluxeTags.msg(s, "&8&m+----------------+");
      DeluxeTags.msg(s, Lang.CMD_HELP_TITLE.getConfigValue(null));
      DeluxeTags.msg(s, " ");
      if (p.hasPermission("deluxetags.gui")) {
        DeluxeTags.msg(s, color + "/tags");
        DeluxeTags.msg(s, Lang.CMD_HELP_TAGS.getConfigValue(null));
      }
      if (p.hasPermission("deluxetags.list")) {
        DeluxeTags.msg(s, color + "/tags list (all/<playername>)");
        DeluxeTags.msg(s, Lang.CMD_HELP_LIST.getConfigValue(null));
      }
      if (p.hasPermission("deluxetags.select")) {
        DeluxeTags.msg(s, color + "/tags select <tag>");
        DeluxeTags.msg(s, Lang.CMD_HELP_SELECT.getConfigValue(null));
      }
      if (p.hasPermission("deluxetags.set")) {
        DeluxeTags.msg(s, color + "/tags set <player> <tag>");
        DeluxeTags.msg(s, Lang.CMD_HELP_ADMIN_SET.getConfigValue(null));
      }
      if (p.hasPermission("deluxetags.clear")) {
        DeluxeTags.msg(s, color + "/tags clear <player>");
        DeluxeTags.msg(s, Lang.CMD_HELP_ADMIN_CLEAR.getConfigValue(null));
      }
      if (p.hasPermission("deluxetags.create")) {
        DeluxeTags.msg(s, color + "/tags create <identifier> <tag>");
        DeluxeTags.msg(s, Lang.CMD_HELP_ADMIN_CREATE.getConfigValue(null));
      }
      if (p.hasPermission("deluxetags.delete")) {
        DeluxeTags.msg(s, color + "/tags delete <identifier>");
        DeluxeTags.msg(s, Lang.CMD_HELP_ADMIN_DELETE.getConfigValue(null));
      }
      if (p.hasPermission("deluxetags.setdescription")) {
        DeluxeTags.msg(s, color + "/tags setdesc <identifier> <tag description>");
        DeluxeTags.msg(s, Lang.CMD_HELP_ADMIN_SET_DESC.getConfigValue(null));
      }
      if (p.hasPermission("deluxetags.reload")) {
        DeluxeTags.msg(s, color + "/tags reload");
        DeluxeTags.msg(s, Lang.CMD_HELP_RELOAD.getConfigValue(null));
      }
      DeluxeTags.msg(s, color + "/tags version");
      DeluxeTags.msg(s, Lang.CMD_HELP_VERSION.getConfigValue(null));
      DeluxeTags.msg(s, "&8&m+----------------+");
      return true;

    } else if (args[0].equalsIgnoreCase("version")) {

      DeluxeTags.msg(s, "&8&m+----------------+");
      DeluxeTags.msg(s, "&5&lDeluxeTags &f&o" + plugin.getDescription().getVersion());
      DeluxeTags.msg(s, "&7Created by &f&oextended_clip");
      DeluxeTags.msg(s, "&8&m+----------------+");

      return true;

    } else if (args[0].equalsIgnoreCase("list")) {

      if (args.length == 1) {

        if (!p.hasPermission("deluxetags.list")) {

          DeluxeTags.msg(p, Lang.CMD_NO_PERMS.getConfigValue(new String[]{
              "deluxetags.list"
          }));
          return true;
        }

        if (DeluxeTag.getLoadedTags() == null || DeluxeTag.getLoadedTags().isEmpty()) {

          DeluxeTags.msg(s, Lang.CMD_NO_TAGS_LOADED.getConfigValue(null));
          return true;
        }

        List<String> t = DeluxeTag.getAvailableTagIdentifiers(p);

        String tags = t.toString().replace("[", "&7").replace(",", "&a,&7").replace("]", "");

        String amount = String.valueOf(t.size());

        DeluxeTags.msg(p, Lang.CMD_TAG_LIST.getConfigValue(new String[]{
            amount, tags
        }));
      } else if (args[1].equalsIgnoreCase("all")) {

        if (!p.hasPermission("deluxetags.list.all")) {

          DeluxeTags.msg(p, Lang.CMD_NO_PERMS.getConfigValue(new String[]{
              "deluxetags.list.all"
          }));
          return true;
        }

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

        DeluxeTags.msg(p, Lang.CMD_TAG_LIST_ALL.getConfigValue(new String[]{
            amount, tags
        }));

        return true;

      } else {

        if (!p.hasPermission("deluxetags.list.player")) {

          DeluxeTags.msg(p, Lang.CMD_NO_PERMS.getConfigValue(new String[]{
              "deluxetags.list.player"
          }));
          return true;
        }

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

        DeluxeTags.msg(p, Lang.CMD_TAG_LIST_TARGET.getConfigValue(new String[]{
            target.getName(), amount, tags
        }));
      }

      return true;

    } else if (args[0].equalsIgnoreCase("select")) {

      if (!p.hasPermission("deluxetags.select")) {
        DeluxeTags.msg(p, Lang.CMD_NO_PERMS.getConfigValue(new String[]{
            "deluxetags.select"
        }));
        return true;
      }

      if (args.length != 2) {

        DeluxeTags.msg(p, Lang.CMD_TAG_SEL_INCORRECT.getConfigValue(null));
        return true;
      }

      if (DeluxeTag.getLoadedTags() == null || DeluxeTag.getLoadedTags().isEmpty()) {

        DeluxeTags.msg(s, Lang.CMD_NO_TAGS_LOADED.getConfigValue(null));
        return true;
      }

      List<String> avail = DeluxeTag.getAvailableTagIdentifiers(p);

      if (avail == null || avail.isEmpty()) {

        DeluxeTags.msg(s, Lang.CMD_NO_TAGS_AVAILABLE.getConfigValue(null));
        return true;
      }

      String tag = args[1];

      for (String t : avail) {

        if (t.equalsIgnoreCase(tag)) {

          DeluxeTag set = DeluxeTag.getLoadedTag(t);

          if (set != null) {

            if (set.setPlayerTag(p)) {

              plugin.saveTagIdentifier(p.getUniqueId().toString(), set.getIdentifier());

              DeluxeTags.msg(s, Lang.CMD_TAG_SEL_SUCCESS.getConfigValue(new String[]{
                  set.getIdentifier(), set.getDisplayTag()}));
            } else {
              DeluxeTags.msg(s, Lang.CMD_TAG_SEL_FAIL_SAMETAG.getConfigValue(new String[]{
                  set.getIdentifier(), set.getDisplayTag()}));
            }

            return true;
          }
        }
      }

      DeluxeTags.msg(s, Lang.CMD_TAG_SEL_FAIL_INVALID.getConfigValue(new String[]{
          tag
      }));
      return true;

    } else if (args[0].equalsIgnoreCase("create")) {

      if (!p.hasPermission("deluxetags.create")) {

        DeluxeTags.msg(p, Lang.CMD_NO_PERMS.getConfigValue(new String[]{
            "deluxetags.create"
        }));
        return true;
      }

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

        plugin.getCfg().saveTag(priority, id, tag, "&f", "deluxetags.tag." + id);

        DeluxeTags.msg(s, Lang.CMD_ADMIN_CREATE_TAG_SUCCESS.getConfigValue(new String[]{
            id, tag
        }));

      }

      return true;

    } else if (args[0].equalsIgnoreCase("delete")) {

      if (!p.hasPermission("deluxetags.delete")) {

        DeluxeTags.msg(p, Lang.CMD_NO_PERMS.getConfigValue(new String[]{
            "deluxetags.delete"
        }));
        return true;
      }

      if (args.length != 2) {

        DeluxeTags.msg(p, Lang.CMD_ADMIN_DELETE_TAG_INCORRECT.getConfigValue(null));
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

          tag = null;

          DeluxeTags.msg(p, Lang.CMD_ADMIN_DELETE_TAG_SUCCESS.getConfigValue(new String[]{
              id
          }));

          return true;
        }
      }

      DeluxeTags.msg(p, Lang.CMD_ADMIN_DELETE_TAG_FAIL.getConfigValue(new String[]{
          id
      }));

      return true;

    } else if (args[0].equalsIgnoreCase("setdesc") || args[0].equalsIgnoreCase("setdescription")) {

      if (!p.hasPermission("deluxetags.setdescription")) {

        DeluxeTags.msg(p, Lang.CMD_NO_PERMS.getConfigValue(new String[]{
            "deluxetags.setdescription"
        }));
        return true;
      }

      if (args.length < 3) {

        DeluxeTags.msg(s, Lang.CMD_ADMIN_SET_DESCRIPTION_INCORRECT.getConfigValue(null));
        return true;
      }

      String id = args[1];

      if (DeluxeTag.getLoadedTag(id) == null) {
        DeluxeTags.msg(s, Lang.CMD_ADMIN_SET_DESCRIPTION_FAIL.getConfigValue(new String[]{
            id
        }));
        return true;
      }

      DeluxeTag tag = DeluxeTag.getLoadedTag(id);

      String desc = StringUtils.join(Arrays.copyOfRange(args, 2, args.length), " ");

      if (desc.endsWith("_")) {
        desc = desc.substring(0, desc.length() - 1) + " ";
      }

      tag.setDescription(desc);

      plugin.getCfg().saveTag(tag.getPriority(), tag.getIdentifier(), tag.getDisplayTag(),
          tag.getDescription(), tag.getPermission());

      DeluxeTags.msg(s, Lang.CMD_ADMIN_SET_DESCRIPTION_SUCCESS.getConfigValue(new String[]{
          id, tag.getDisplayTag(), desc
      }));

      return true;

    } else if (args[0].equalsIgnoreCase("set")) {

      if (!p.hasPermission("deluxetags.set")) {

        DeluxeTags.msg(p, Lang.CMD_NO_PERMS.getConfigValue(new String[]{
            "deluxetags.set"
        }));
        return true;
      }

      if (args.length != 3) {

        DeluxeTags.msg(p, Lang.CMD_ADMIN_SET_INCORRECT_ARGS.getConfigValue(null));
        return true;
      }

      if (DeluxeTag.getLoadedTags() == null || DeluxeTag.getLoadedTags().isEmpty()) {

        DeluxeTags.msg(s, Lang.CMD_NO_TAGS_LOADED.getConfigValue(null));
        return true;
      }

      Player target = Bukkit.getPlayer(args[1]);

      if (target == null) {

        DeluxeTags.msg(s, Lang.CMD_TARGET_NOT_ONLINE.getConfigValue(new String[]{
            args[1]
        }));
        return true;
      }

      List<String> avail = DeluxeTag.getAvailableTagIdentifiers(target);

      if (avail == null || avail.isEmpty()) {

        DeluxeTags.msg(s, Lang.CMD_ADMIN_SET_NO_TAGS.getConfigValue(new String[]{
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

            DeluxeTags.msg(s, Lang.CMD_ADMIN_SET_SUCCESS.getConfigValue(new String[]{
                target.getName(), set.getIdentifier(), set.getDisplayTag()
            }));

            if (target != p) {

              DeluxeTags.msg(target, Lang.CMD_ADMIN_SET_SUCCESS_TARGET.getConfigValue(new String[]{
                  set.getIdentifier(), set.getDisplayTag(), p.getName()
              }));
            }
            return true;
          }
        }
      }

      DeluxeTags.msg(s, Lang.CMD_ADMIN_SET_FAIL.getConfigValue(new String[]{
          tag, target.getName()
      }));
      return true;

    } else if (args[0].equalsIgnoreCase("clear")) {

      if (!p.hasPermission("deluxetags.clear")) {

        DeluxeTags.msg(p, Lang.CMD_NO_PERMS.getConfigValue(new String[]{
            "deluxetags.clear"
        }));
        return true;
      }

      if (args.length != 2) {

        DeluxeTags.msg(p, Lang.CMD_ADMIN_CLEAR_INCORRECT_ARGS.getConfigValue(null));
        return true;
      }

      if (DeluxeTag.getLoadedTags() == null || DeluxeTag.getLoadedTags().isEmpty()) {

        DeluxeTags.msg(s, Lang.CMD_NO_TAGS_LOADED.getConfigValue(null));
        return true;
      }

      Player target = Bukkit.getPlayer(args[1]);

      if (target == null) {

        DeluxeTags.msg(s, Lang.CMD_TARGET_NOT_ONLINE.getConfigValue(new String[]{
            args[1]
        }));
        return true;
      }

      String tag = DeluxeTag.getPlayerTagIdentifier(target);

      if (tag == null || DeluxeTag.getPlayerDisplayTag(target).isEmpty()) {

        DeluxeTags.msg(s, Lang.CMD_ADMIN_CLEAR_NO_TAG_SET.getConfigValue(new String[]{
            target.getName()
        }));
        return true;
      }

      plugin.getDummy().setPlayerTag(target);
      plugin.removeSavedTag(target.getUniqueId().toString());

      DeluxeTags.msg(s, Lang.CMD_ADMIN_CLEAR_SUCCESS.getConfigValue(new String[]{
          target.getName()
      }));

      if (target != p) {

        DeluxeTags.msg(target, Lang.CMD_ADMIN_CLEAR_SUCCESS_TARGET.getConfigValue(new String[]{
            p.getName()
        }));
      }
      return true;

    } else if (args[0].equalsIgnoreCase("reload")) {

      if (!p.hasPermission("deluxetags.reload")) {
        DeluxeTags.msg(p, Lang.CMD_NO_PERMS.getConfigValue(new String[]{
            "deluxetags.reload"
        }));
        return true;
      }

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

      DeluxeTags.msg(p, Lang.CMD_ADMIN_RELOAD.getConfigValue(new String[]{
          String.valueOf(loaded)
      }));

      return true;


    } else {
      DeluxeTags.msg(s, Lang.CMD_INCORRECT_USAGE.getConfigValue(null));
    }
    return true;
  }
}
