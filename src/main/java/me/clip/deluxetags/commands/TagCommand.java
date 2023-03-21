package me.clip.deluxetags.commands;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import me.clip.deluxetags.DeluxeTags;
import me.clip.deluxetags.config.Lang;
import me.clip.deluxetags.tags.DeluxeTag;
import me.clip.deluxetags.utils.MsgUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TagCommand implements CommandExecutor {

  private final DeluxeTags plugin;

  public TagCommand(DeluxeTags instance) {
    plugin = instance;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

    final Player player = sender instanceof Player ? (Player) sender : null;

    if (args.length == 0) {
      if (player == null) {

        String builder = "&8&m+----------------+\n" +
                "&5&lDeluxeTags &f&o" + plugin.getDescription().getVersion() + "\n" +
                "&7Created by &f&oextended_clip&7, &f&oGlare&7, &f&oBlitzOffline\n" +
                "Use /tags help for a list of commands\n" +
                "&8&m+----------------+";

        MsgUtils.msg(sender, builder);
        return true;
      }

      if (!player.hasPermission("deluxetags.gui")) {
        MsgUtils.msg(player, Lang.CMD_NO_PERMS.getConfigValue(new String[]{
            "deluxetags.gui"
        }));
        return true;
      }

      if (DeluxeTag.getLoadedTags() == null || DeluxeTag.getLoadedTags().isEmpty()) {
        MsgUtils.msg(player, Lang.CMD_NO_TAGS_LOADED.getConfigValue(null));
        return true;
      }

      if (!plugin.getGUIHandler().openMenu(player, 1)) {
        MsgUtils.msg(player, Lang.CMD_NO_TAGS_AVAILABLE.getConfigValue(null));
      }
      return true;

    } else if (args[0].equalsIgnoreCase("help")) {

      final StringBuilder builder = new StringBuilder();

      String color = Lang.CMD_HELP_COLOR.getConfigValue(null);

      builder.append("&8&m+----------------+").append("\n");
      builder.append(Lang.CMD_HELP_TITLE.getConfigValue(null)).append("\n");
      builder.append(" ").append("\n");

      final StringBuilder perPermissionBuilder = new StringBuilder();

      if (sender.hasPermission("deluxetags.gui")) {
        perPermissionBuilder.append(color).append("/tags").append("\n");
        perPermissionBuilder.append(Lang.CMD_HELP_TAGS.getConfigValue(null)).append("\n");
      }
      if (sender.hasPermission("deluxetags.list")) {
        perPermissionBuilder.append(color).append("/tags list (all/<playername>)").append("\n");
        perPermissionBuilder.append(Lang.CMD_HELP_LIST.getConfigValue(null)).append("\n");
      }
      if (sender.hasPermission("deluxetags.select")) {
        perPermissionBuilder.append(color).append("/tags select <tag>").append("\n");
        perPermissionBuilder.append(Lang.CMD_HELP_SELECT.getConfigValue(null)).append("\n");
      }
      if (sender.hasPermission("deluxetags.set")) {
        perPermissionBuilder.append(color).append("/tags set <player> <tag>").append("\n");
        perPermissionBuilder.append(Lang.CMD_HELP_ADMIN_SET.getConfigValue(null)).append("\n");
      }
      if (sender.hasPermission("deluxetags.clear")) {
        perPermissionBuilder.append(color).append("/tags clear <player>").append("\n");
        perPermissionBuilder.append(Lang.CMD_HELP_ADMIN_CLEAR.getConfigValue(null)).append("\n");
      }
      if (sender.hasPermission("deluxetags.create")) {
        perPermissionBuilder.append(color).append("/tags create <identifier> <tag>").append("\n");
        perPermissionBuilder.append(Lang.CMD_HELP_ADMIN_CREATE.getConfigValue(null)).append("\n");
      }
      if (sender.hasPermission("deluxetags.delete")) {
        perPermissionBuilder.append(color).append("/tags delete <identifier>").append("\n");
        perPermissionBuilder.append(Lang.CMD_HELP_ADMIN_DELETE.getConfigValue(null)).append("\n");
      }
      if (sender.hasPermission("deluxetags.setdescription")) {
        perPermissionBuilder.append(color).append("/tags setdesc <identifier> <tag description>").append("\n");
        perPermissionBuilder.append(Lang.CMD_HELP_ADMIN_SET_DESC.getConfigValue(null)).append("\n");
      }
      if (sender.hasPermission("deluxetags.setorder")) {
        perPermissionBuilder.append(color).append("/tags setorder <identifier> <order>").append("\n");
        perPermissionBuilder.append(Lang.CMD_HELP_ADMIN_SET_ORDER.getConfigValue(null)).append("\n");
      }
      if (sender.hasPermission("deluxetags.setdisplay")) {
        perPermissionBuilder.append(color).append("/tags setdisplay <identifier> <tag display>").append("\n");
        perPermissionBuilder.append(Lang.CMD_HELP_ADMIN_SET_DISPLAY.getConfigValue(null)).append("\n");
      }
      if (sender.hasPermission("deluxetags.reload")) {
        perPermissionBuilder.append(color).append("/tags reload").append("\n");
        perPermissionBuilder.append(Lang.CMD_HELP_RELOAD.getConfigValue(null)).append("\n");
      }
      if (sender.hasPermission("deluxetags.version")) {
        perPermissionBuilder.append(color).append("/tags version").append("\n");
        perPermissionBuilder.append(Lang.CMD_HELP_VERSION.getConfigValue(null)).append("\n");
      }


      if (perPermissionBuilder.length() == 0) {
        MsgUtils.msg(sender, Lang.CMD_NO_PERMS.getConfigValue(new String[]{
            "deluxetags.gui"
        }));
        return true;
      }

      builder.append(perPermissionBuilder);
      builder.append("&8&m+----------------+");

      MsgUtils.msg(sender, builder.toString());
      return true;

    } else if (args[0].equalsIgnoreCase("version")) {

      if (!sender.hasPermission("deluxetags.version")) {
        MsgUtils.msg(sender, Lang.CMD_NO_PERMS.getConfigValue(new String[]{
                "deluxetags.version"
        }));
        return true;
      }

      MsgUtils.msg(
              sender,
              "&8&m+----------------+\n"+
                      "&5&lDeluxeTags &f&o" + plugin.getDescription().getVersion() + "\n" +
                      "&7Created by &f&oextended_clip&7, &f&oGlare&7, &f&oBlitzOffline\n" +
                      "&8&m+----------------+");
      return true;

    } else if (args[0].equalsIgnoreCase("list")) {
      if (args.length == 1) {
        if (!sender.hasPermission("deluxetags.list")) {
          MsgUtils.msg(sender, Lang.CMD_NO_PERMS.getConfigValue(new String[]{
              "deluxetags.list"
          }));
          return true;
        }

        if (DeluxeTag.getLoadedTags() == null || DeluxeTag.getLoadedTags().isEmpty()) {
          MsgUtils.msg(sender, Lang.CMD_NO_TAGS_LOADED.getConfigValue(null));
          return true;
        }

        Collection<DeluxeTag> tags;

        if (player == null) {
          tags = DeluxeTag.getLoadedTags();
        } else {
          tags = DeluxeTag.getLoadedTags().stream().filter(tag -> tag.hasTagPermission(player)).collect(Collectors.toList());
        }

        if (tags.isEmpty()) {
          MsgUtils.msg(sender, Lang.CMD_TAG_LIST_FAIL.getConfigValue(null));
          return true;
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (DeluxeTag tag : tags) {
          stringBuilder
              .append("&f")
              .append(tag.getIdentifier())
              .append("&7:&f")
              .append(tag.getDisplayTag(player))
              .append("&a, ");
        }

        String tagsDisplay = stringBuilder.substring(0, stringBuilder.length()-2).trim();
        String amount = String.valueOf(tags.size());
        MsgUtils.msg(sender, Lang.CMD_TAG_LIST.getConfigValue(new String[]{
            amount, tagsDisplay
        }));

      } else if (args[1].equalsIgnoreCase("all")) {
        if (!sender.hasPermission("deluxetags.list.all")) {
          MsgUtils.msg(sender, Lang.CMD_NO_PERMS.getConfigValue(new String[]{
              "deluxetags.list.all"
          }));
          return true;
        }

        if (DeluxeTag.getLoadedTags() == null || DeluxeTag.getLoadedTags().isEmpty()) {
          MsgUtils.msg(sender, Lang.CMD_NO_TAGS_LOADED.getConfigValue(null));
          return true;
        }

        final Collection<DeluxeTag> tags = DeluxeTag.getLoadedTags();
        StringBuilder stringBuilder = new StringBuilder();
        for (DeluxeTag tag : tags) {
          stringBuilder
              .append("&f")
              .append(tag.getIdentifier())
              .append("&7:&f")
              .append(tag.getDisplayTag(player))
              .append("&a, ");
        }

        String tagsDisplay = stringBuilder.length() <= 2 ? "" : stringBuilder.substring(0, stringBuilder.length()-2).trim();
        String amount = String.valueOf(tags.size());
        MsgUtils.msg(sender, Lang.CMD_TAG_LIST_ALL.getConfigValue(new String[]{
            amount, tagsDisplay
        }));

        return true;

      } else {
        if (!sender.hasPermission("deluxetags.list.player")) {
          MsgUtils.msg(sender, Lang.CMD_NO_PERMS.getConfigValue(new String[]{
              "deluxetags.list.player"
          }));
          return true;
        }

        if (DeluxeTag.getLoadedTags() == null || DeluxeTag.getLoadedTags().isEmpty()) {
          MsgUtils.msg(sender, Lang.CMD_NO_TAGS_LOADED.getConfigValue(null));
          return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
          MsgUtils.msg(sender, Lang.CMD_TARGET_NOT_ONLINE.getConfigValue(new String[]{
              args[1]
          }));
          return true;
        }

        Collection<DeluxeTag> tags = DeluxeTag.getLoadedTags().stream().filter(tag -> tag.hasTagPermission(target)).collect(Collectors.toList());
        StringBuilder stringBuilder = new StringBuilder();

        if (tags.isEmpty()) {
          MsgUtils.msg(sender, Lang.CMD_TAG_LIST_FAIL_TARGET.getConfigValue(new String[]{
              args[1]
          }));
          return true;
        }

        for (DeluxeTag tag : tags) {
          stringBuilder
              .append("&f")
              .append(tag.getIdentifier())
              .append("&7:&f")
              .append(tag.getDisplayTag(target))
              .append("&a, ");
        }

        String tagsDisplay = stringBuilder.length() <= 2 ? "" : stringBuilder.substring(0, stringBuilder.length()-2).trim();
        String amount = String.valueOf(tags.size());
        MsgUtils.msg(sender, Lang.CMD_TAG_LIST_TARGET.getConfigValue(new String[]{
            target.getName(), amount, tagsDisplay
        }));
      }
      return true;

    } else if (args[0].equalsIgnoreCase("select")) {
      if (player == null) {
        MsgUtils.msg(sender, "&4This command can only be used in game!");
        return true;
      }

      if (!player.hasPermission("deluxetags.select")) {
        MsgUtils.msg(player, Lang.CMD_NO_PERMS.getConfigValue(new String[]{
            "deluxetags.select"
        }));
        return true;
      }

      if (args.length != 2) {
        MsgUtils.msg(player, Lang.CMD_TAG_SEL_INCORRECT.getConfigValue(null));
        return true;
      }

      if (DeluxeTag.getLoadedTags() == null || DeluxeTag.getLoadedTags().isEmpty()) {
        MsgUtils.msg(player, Lang.CMD_NO_TAGS_LOADED.getConfigValue(null));
        return true;
      }

      List<String> availIdentifiers = DeluxeTag.getAvailableTagIdentifiers(player);
      if (availIdentifiers == null || availIdentifiers.isEmpty()) {
        MsgUtils.msg(player, Lang.CMD_NO_TAGS_AVAILABLE.getConfigValue(null));
        return true;
      }

      String identifier = args[1];
      for (String availIdentifier : availIdentifiers) {
        if (!availIdentifier.equalsIgnoreCase(identifier)) {
          continue;
        }
        DeluxeTag tag = DeluxeTag.getLoadedTag(availIdentifier);
        if (tag == null) {
          continue;
        }
        if (tag.setPlayerTag(player)) {
          plugin.saveTagIdentifier(player.getUniqueId().toString(), tag.getIdentifier());
          MsgUtils.msg(sender, Lang.CMD_TAG_SEL_SUCCESS.getConfigValue(new String[]{
              tag.getIdentifier(), tag.getDisplayTag(player)}));
        } else {
          MsgUtils.msg(sender, Lang.CMD_TAG_SEL_FAIL_SAMETAG.getConfigValue(new String[]{
              tag.getIdentifier(), tag.getDisplayTag(player)}));
        }
        return true;
      }

      MsgUtils.msg(player, Lang.CMD_TAG_SEL_FAIL_INVALID.getConfigValue(new String[]{
          identifier
      }));
      return true;

    } else if (args[0].equalsIgnoreCase("create")) {
      if (!sender.hasPermission("deluxetags.create")) {
        MsgUtils.msg(sender, Lang.CMD_NO_PERMS.getConfigValue(new String[]{
            "deluxetags.create"
        }));
        return true;
      }

      if (args.length < 3) {
        MsgUtils.msg(sender, Lang.CMD_ADMIN_CREATE_TAG_INCORRECT.getConfigValue(null));
        return true;
      }

      String identifier = args[1];
      if (DeluxeTag.getLoadedTag(identifier) != null) {
        MsgUtils.msg(sender, Lang.CMD_ADMIN_CREATE_TAG_FAIL.getConfigValue(new String[]{
            identifier
        }));
        return true;
      }

      String tagDisplay = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
      if (tagDisplay.isEmpty()) {
        return true;
      }

      if (tagDisplay.endsWith("_")) {
        tagDisplay = tagDisplay.substring(0, tagDisplay.length() - 1) + " ";
      }

      int priority = DeluxeTag.getLoadedTagsAmount() + 1;
      Set<Integer> priorities = DeluxeTag.getLoadedPriorities();
      if (priorities != null && !priorities.isEmpty()) {
        for (int i = 1; i <= priorities.size() + 1; i++) {
          if (priorities.contains(i)) continue;
          priority = i;
          break;
        }
      }

      DeluxeTag tag = new DeluxeTag(priority, identifier, tagDisplay, "");
      tag.load();
      plugin.getCfg().saveTag(priority, identifier, tagDisplay, "&f", "deluxetags.tag." + identifier);
      MsgUtils.msg(sender, Lang.CMD_ADMIN_CREATE_TAG_SUCCESS.getConfigValue(new String[]{
          identifier, tagDisplay
      }));

    } else if (args[0].equalsIgnoreCase("delete")) {
      if (!sender.hasPermission("deluxetags.delete")) {
        MsgUtils.msg(sender, Lang.CMD_NO_PERMS.getConfigValue(new String[]{
            "deluxetags.delete"
        }));
        return true;
      }

      if (args.length != 2) {
        MsgUtils.msg(sender, Lang.CMD_ADMIN_DELETE_TAG_INCORRECT.getConfigValue(null));
        return true;
      }

      String identifier = args[1];
      DeluxeTag tag = DeluxeTag.getLoadedTag(identifier);
      if (tag == null) {
        MsgUtils.msg(sender, Lang.CMD_ADMIN_DELETE_TAG_FAIL.getConfigValue(new String[]{
            identifier
        }));
        return true;
      }

      List<String> remove = tag.removeActivePlayers();
      if (remove != null && !remove.isEmpty()) {
        plugin.removeSavedTags(remove);
      }

      if (tag.unload()) {
        plugin.getCfg().removeTag(identifier);
        MsgUtils.msg(sender, Lang.CMD_ADMIN_DELETE_TAG_SUCCESS.getConfigValue(new String[]{
            identifier
        }));
        return true;
      }

    } else if (args[0].equalsIgnoreCase("setdesc") || args[0].equalsIgnoreCase("setdescription")) {
      if (!sender.hasPermission("deluxetags.setdescription")) {
        MsgUtils.msg(sender, Lang.CMD_NO_PERMS.getConfigValue(new String[]{
            "deluxetags.setdescription"
        }));
        return true;
      }

      if (args.length < 3) {
        MsgUtils.msg(sender, Lang.CMD_ADMIN_SET_DESCRIPTION_INCORRECT.getConfigValue(null));
        return true;
      }

      String identifier = args[1];
      DeluxeTag tag = DeluxeTag.getLoadedTag(identifier);
      if (tag == null) {
        MsgUtils.msg(sender, Lang.CMD_ADMIN_SET_DESCRIPTION_FAIL.getConfigValue(new String[]{
            identifier
        }));
        return true;
      }

      String desc = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
      if (desc.endsWith("_")) {
        desc = desc.substring(0, desc.length() - 1) + " ";
      }

      tag.setDescription(desc);
      plugin.getCfg().saveTag(tag.getPriority(), tag.getIdentifier(), tag.getDisplayTag(),
          tag.getDescription(), tag.getPermission());
      MsgUtils.msg(sender, Lang.CMD_ADMIN_SET_DESCRIPTION_SUCCESS.getConfigValue(new String[]{
          identifier, tag.getDisplayTag(player), desc
      }));
      return true;

    } else if (args[0].equalsIgnoreCase("setorder")) {
      if (!sender.hasPermission("deluxetags.setorder")) {
        MsgUtils.msg(sender, Lang.CMD_NO_PERMS.getConfigValue(new String[]{
            "deluxetags.setorder"
        }));
        return true;
      }

      if (args.length < 3) {
        MsgUtils.msg(sender, Lang.CMD_ADMIN_SET_ORDER_INCORRECT.getConfigValue(null));
        return true;
      }

      DeluxeTag tag = DeluxeTag.getLoadedTag(args[1]);
      if (tag == null) {
        MsgUtils.msg(sender, Lang.CMD_ADMIN_SET_ORDER_FAIL.getConfigValue(new String[]{
            args[1]
        }));
        return true;
      }

      int priority;
      try {
        priority = Integer.parseInt(args[2]);
      } catch (NumberFormatException ex) {
        MsgUtils.msg(sender, Lang.CMD_ADMIN_SET_ORDER_NOT_A_NUMBER.getConfigValue(new String[]{
            args[2]
        }));
        return true;
      }

      if (DeluxeTag.getLoadedPriorities().contains(priority)) {
        MsgUtils.msg(sender, Lang.CMD_ADMIN_SET_ORDER_ALREADY_EXISTS.getConfigValue(new String[]{
            args[2]
        }));
        return true;
      }

      tag.unload();
      tag.setPriority(priority);
      plugin.getCfg().saveTag(tag.getPriority(), tag.getIdentifier(), tag.getDisplayTag(),
          tag.getDescription(), tag.getPermission());
      tag.load();
      MsgUtils.msg(sender, Lang.CMD_ADMIN_SET_ORDER_SUCCESS.getConfigValue(new String[]{
          args[2], args[1]
      }));
      return true;
    } else if (args[0].equalsIgnoreCase("setdisplay")) {
      if (!sender.hasPermission("deluxetags.setdisplay")) {
        MsgUtils.msg(sender, Lang.CMD_NO_PERMS.getConfigValue(new String[]{
            "deluxetags.setdisplay"
        }));
        return true;
      }

      if (args.length < 3) {
        MsgUtils.msg(sender, Lang.CMD_ADMIN_SET_DISPLAY_INCORRECT.getConfigValue(null));
        return true;
      }

      String identifier = args[1];
      DeluxeTag tag = DeluxeTag.getLoadedTag(identifier);
      if (tag == null) {
        MsgUtils.msg(sender, Lang.CMD_ADMIN_SET_DISPLAY_FAIL.getConfigValue(new String[]{
            identifier
        }));
        return true;
      }

      String tagDisplay = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
      if (tagDisplay.isEmpty()) {
        return true;
      }

      if (tagDisplay.endsWith("_")) {
        tagDisplay = tagDisplay.substring(0, tagDisplay.length() - 1) + " ";
      }

      tag.setDisplayTag(tagDisplay);
      plugin.getCfg().saveTag(tag.getPriority(), tag.getIdentifier(), tag.getDisplayTag(),
          tag.getDescription(), tag.getPermission());
      MsgUtils.msg(sender, Lang.CMD_ADMIN_SET_DISPLAY_SUCCESS.getConfigValue(new String[]{
          identifier, tag.getDisplayTag()
      }));
      return true;

    } else if (args[0].equalsIgnoreCase("set")) {
      if (!sender.hasPermission("deluxetags.set")) {
        MsgUtils.msg(sender, Lang.CMD_NO_PERMS.getConfigValue(new String[]{
            "deluxetags.set"
        }));
        return true;
      }

      if (args.length != 3) {
        MsgUtils.msg(sender, Lang.CMD_ADMIN_SET_INCORRECT_ARGS.getConfigValue(null));
        return true;
      }

      if (DeluxeTag.getLoadedTags() == null || DeluxeTag.getLoadedTags().isEmpty()) {
        MsgUtils.msg(sender, Lang.CMD_NO_TAGS_LOADED.getConfigValue(null));
        return true;
      }

      Player target = Bukkit.getPlayer(args[1]);
      if (target == null) {
        MsgUtils.msg(sender, Lang.CMD_TARGET_NOT_ONLINE.getConfigValue(new String[]{
            args[1]
        }));
        return true;
      }

      List<String> availIdentifiers = DeluxeTag.getAvailableTagIdentifiers(target);
      if (availIdentifiers == null || availIdentifiers.isEmpty()) {
        MsgUtils.msg(sender, Lang.CMD_ADMIN_SET_NO_TAGS.getConfigValue(new String[]{
            target.getName()
        }));
        return true;
      }

      String identifier = args[2];
      for (String availIdentifier : availIdentifiers) {
        if (!availIdentifier.equalsIgnoreCase(identifier)) {
          continue;
        }
        DeluxeTag tag = DeluxeTag.getLoadedTag(availIdentifier);
        if (tag == null) {
          continue;
        }
        tag.setPlayerTag(target);
        plugin.saveTagIdentifier(target.getUniqueId().toString(), tag.getIdentifier());
        MsgUtils.msg(sender, Lang.CMD_ADMIN_SET_SUCCESS.getConfigValue(new String[]{
            target.getName(), tag.getIdentifier(), tag.getDisplayTag(target)
        }));
        if (target != sender) {
          MsgUtils.msg(target, Lang.CMD_ADMIN_SET_SUCCESS_TARGET.getConfigValue(new String[]{
              tag.getIdentifier(), tag.getDisplayTag(target), sender.getName()
          }));
        }
        return true;
      }

      MsgUtils.msg(sender, Lang.CMD_ADMIN_SET_FAIL.getConfigValue(new String[]{
          identifier, target.getName()
      }));
      return true;

    } else if (args[0].equalsIgnoreCase("clear")) {
      if (!sender.hasPermission("deluxetags.clear")) {
        MsgUtils.msg(sender, Lang.CMD_NO_PERMS.getConfigValue(new String[]{
            "deluxetags.clear"
        }));
        return true;
      }

      if (args.length != 2) {
        MsgUtils.msg(sender, Lang.CMD_ADMIN_CLEAR_INCORRECT_ARGS.getConfigValue(null));
        return true;
      }

      if (DeluxeTag.getLoadedTags() == null || DeluxeTag.getLoadedTags().isEmpty()) {
        MsgUtils.msg(sender, Lang.CMD_NO_TAGS_LOADED.getConfigValue(null));
        return true;
      }

      Player target = Bukkit.getPlayer(args[1]);
      if (target == null) {
        MsgUtils.msg(sender, Lang.CMD_TARGET_NOT_ONLINE.getConfigValue(new String[]{
            args[1]
        }));
        return true;
      }

      String tag = DeluxeTag.getPlayerTagIdentifier(target);
      if (tag == null || DeluxeTag.getPlayerDisplayTag(target).isEmpty()) {
        MsgUtils.msg(sender, Lang.CMD_ADMIN_CLEAR_NO_TAG_SET.getConfigValue(new String[]{
            target.getName()
        }));
        return true;
      }

      plugin.getDummy().setPlayerTag(target);
      plugin.removeSavedTag(target.getUniqueId().toString());

      MsgUtils.msg(sender, Lang.CMD_ADMIN_CLEAR_SUCCESS.getConfigValue(new String[]{
          target.getName()
      }));

      if (target != sender) {
        MsgUtils.msg(target, Lang.CMD_ADMIN_CLEAR_SUCCESS_TARGET.getConfigValue(new String[]{
            sender.getName()
        }));
      }
      return true;

    } else if (args[0].equalsIgnoreCase("reload")) {

      if (!sender.hasPermission("deluxetags.reload")) {
        MsgUtils.msg(sender, Lang.CMD_NO_PERMS.getConfigValue(new String[]{
            "deluxetags.reload"
        }));
        return true;
      }

      plugin.reloadConfig();
      plugin.saveConfig();
      plugin.getCfg().reload();

      DeluxeTag.unloadData();
      int loaded = plugin.getCfg().loadTags();

      MsgUtils.setPattern(plugin.getCfg().legacyHex());
      if (plugin.getCfg().legacyHex()) {
        plugin.getLogger().info("Using legacy hex colors format: &#aaFF00");
      } else {
        plugin.getLogger().info("Using standard hex colors format: #aaFF00");
      }

      plugin.getPlayerFile().reloadConfig();
      plugin.getPlayerFile().saveConfig();

      plugin.getLangFile().reloadConfig();
      plugin.getLangFile().saveConfig();
      plugin.loadMessages();

      plugin.reloadGUIOptions();

      for (Player online : Bukkit.getServer().getOnlinePlayers()) {
        if (DeluxeTag.hasTagLoaded(online)) {
          continue;
        }
        String identifier = plugin.getSavedTagIdentifier(online.getUniqueId().toString());
        if (identifier == null) {
          plugin.getDummy().setPlayerTag(online);
          continue;
        }
        DeluxeTag loadedTag = DeluxeTag.getLoadedTag(identifier);
        if (loadedTag != null && loadedTag.hasTagPermission(online)) {
          loadedTag.setPlayerTag(online);
        } else {
          plugin.getDummy().setPlayerTag(online);
        }
      }

      MsgUtils.msg(sender, Lang.CMD_ADMIN_RELOAD.getConfigValue(new String[]{
          String.valueOf(loaded)
      }));
      return true;

    } else {
      MsgUtils.msg(sender, Lang.CMD_INCORRECT_USAGE.getConfigValue(null));
    }
    return true;
  }
}