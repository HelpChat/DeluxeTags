package me.clip.deluxetags.commands;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import me.clip.deluxetags.DeluxeTags;
import me.clip.deluxetags.storage.Selection;
import me.clip.deluxetags.config.Lang;
import me.clip.deluxetags.tags.DeluxeTag;
import me.clip.deluxetags.utils.MsgUtils;
import me.clip.deluxetags.utils.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TagCommand implements CommandExecutor {

  private final DeluxeTags plugin;

  public TagCommand(DeluxeTags instance) {
    plugin = instance;
  }

  @Override
  public boolean onCommand(@NotNull final CommandSender sender,
                           @NotNull final Command command,
                           @NotNull final String label,
                           final String[] args) {

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

      if (!player.hasPermission(Permissions.GUI)) {
        MsgUtils.msg(player, Lang.CMD_NO_PERMS.getConfigValue(new String[]{
            Permissions.GUI,
        }));
        return true;
      }

      if (plugin.getTagsHandler().getAllTags().isEmpty()) {
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

      if (sender.hasPermission(Permissions.GUI)) {
        perPermissionBuilder.append(color).append("/tags").append("\n");
        perPermissionBuilder.append(Lang.CMD_HELP_TAGS.getConfigValue(null)).append("\n");
      }
      if (sender.hasPermission(Permissions.LIST)) {
        perPermissionBuilder.append(color).append("/tags list (all/<playername>)").append("\n");
        perPermissionBuilder.append(Lang.CMD_HELP_LIST.getConfigValue(null)).append("\n");
      }
      if (sender.hasPermission(Permissions.SELECT)) {
        perPermissionBuilder.append(color).append("/tags select <tag>").append("\n");
        perPermissionBuilder.append(Lang.CMD_HELP_SELECT.getConfigValue(null)).append("\n");
      }
      if (sender.hasPermission(Permissions.SET)) {
        perPermissionBuilder.append(color).append("/tags set <player> <tag>").append("\n");
        perPermissionBuilder.append(Lang.CMD_HELP_ADMIN_SET.getConfigValue(null)).append("\n");
      }
      if (sender.hasPermission(Permissions.CLEAR)) {
        perPermissionBuilder.append(color).append("/tags clear <player>").append("\n");
        perPermissionBuilder.append(Lang.CMD_HELP_ADMIN_CLEAR.getConfigValue(null)).append("\n");
      }
      if (sender.hasPermission(Permissions.CREATE)) {
        perPermissionBuilder.append(color).append("/tags create <identifier> <tag>").append("\n");
        perPermissionBuilder.append(Lang.CMD_HELP_ADMIN_CREATE.getConfigValue(null)).append("\n");
      }
      if (sender.hasPermission(Permissions.DELETE)) {
        perPermissionBuilder.append(color).append("/tags delete <identifier>").append("\n");
        perPermissionBuilder.append(Lang.CMD_HELP_ADMIN_DELETE.getConfigValue(null)).append("\n");
      }
      if (sender.hasPermission(Permissions.SET_DESCRIPTION)) {
        perPermissionBuilder.append(color).append("/tags setdesc <identifier> <tag description>").append("\n");
        perPermissionBuilder.append(Lang.CMD_HELP_ADMIN_SET_DESC.getConfigValue(null)).append("\n");
      }
      if (sender.hasPermission(Permissions.SET_ORDER)) {
        perPermissionBuilder.append(color).append("/tags setorder <identifier> <order>").append("\n");
        perPermissionBuilder.append(Lang.CMD_HELP_ADMIN_SET_ORDER.getConfigValue(null)).append("\n");
      }
      if (sender.hasPermission(Permissions.SET_DISPLAY)) {
        perPermissionBuilder.append(color).append("/tags setdisplay <identifier> <tag display>").append("\n");
        perPermissionBuilder.append(Lang.CMD_HELP_ADMIN_SET_DISPLAY.getConfigValue(null)).append("\n");
      }
      if (sender.hasPermission(Permissions.RELOAD)) {
        perPermissionBuilder.append(color).append("/tags reload").append("\n");
        perPermissionBuilder.append(Lang.CMD_HELP_RELOAD.getConfigValue(null)).append("\n");
      }
      if (sender.hasPermission(Permissions.VERSION)) {
        perPermissionBuilder.append(color).append("/tags version").append("\n");
        perPermissionBuilder.append(Lang.CMD_HELP_VERSION.getConfigValue(null)).append("\n");
      }


      if (perPermissionBuilder.length() == 0) {
        MsgUtils.msg(sender, Lang.CMD_NO_PERMS.getConfigValue(new String[]{
            Permissions.GUI
        }));
        return true;
      }

      builder.append(perPermissionBuilder);
      builder.append("&8&m+----------------+");

      MsgUtils.msg(sender, builder.toString());
      return true;

    } else if (args[0].equalsIgnoreCase("version")) {

      if (!sender.hasPermission(Permissions.VERSION)) {
        MsgUtils.msg(sender, Lang.CMD_NO_PERMS.getConfigValue(new String[]{
                Permissions.VERSION
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
        if (!sender.hasPermission(Permissions.LIST)) {
          MsgUtils.msg(sender, Lang.CMD_NO_PERMS.getConfigValue(new String[]{
              Permissions.LIST
          }));
          return true;
        }

        if (plugin.getTagsHandler().getAllTags().isEmpty()) {
          MsgUtils.msg(sender, Lang.CMD_NO_TAGS_LOADED.getConfigValue(null));
          return true;
        }

        Collection<DeluxeTag> tags;

        if (player == null) {
          tags = plugin.getTagsHandler().getAllTags();
        } else {
          tags = plugin.getTagsHandler().getAllTags().stream().filter(tag -> tag.hasPermissionToUse(player)).collect(Collectors.toList());
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
        if (!sender.hasPermission(Permissions.LIST_ALL)) {
          MsgUtils.msg(sender, Lang.CMD_NO_PERMS.getConfigValue(new String[]{
              Permissions.LIST_ALL
          }));
          return true;
        }

        if (plugin.getTagsHandler().getAllTags().isEmpty()) {
          MsgUtils.msg(sender, Lang.CMD_NO_TAGS_LOADED.getConfigValue(null));
          return true;
        }

        final Collection<DeluxeTag> tags = plugin.getTagsHandler().getAllTags();
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
        if (!sender.hasPermission(Permissions.LIST_PLAYER)) {
          MsgUtils.msg(sender, Lang.CMD_NO_PERMS.getConfigValue(new String[]{
              Permissions.LIST_PLAYER
          }));
          return true;
        }

        if (plugin.getTagsHandler().getAllTags().isEmpty()) {
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

        Collection<DeluxeTag> tags = plugin.getTagsHandler().getAllTags().stream().filter(tag -> tag.hasPermissionToUse(target)).collect(Collectors.toList());
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

      if (!player.hasPermission(Permissions.SELECT)) {
        MsgUtils.msg(player, Lang.CMD_NO_PERMS.getConfigValue(new String[]{
            Permissions.SELECT
        }));
        return true;
      }

      if (args.length != 2) {
        MsgUtils.msg(player, Lang.CMD_TAG_SEL_INCORRECT.getConfigValue(null));
        return true;
      }

      if (plugin.getTagsHandler().getAllTags().isEmpty()) {
        MsgUtils.msg(player, Lang.CMD_NO_TAGS_LOADED.getConfigValue(null));
        return true;
      }

      List<String> availIdentifiers = plugin.getTagsHandler().getPlayerAvailableTagIdentifiers(player);
      if (availIdentifiers.isEmpty()) {
        MsgUtils.msg(player, Lang.CMD_NO_TAGS_AVAILABLE.getConfigValue(null));
        return true;
      }

      String identifier = args[1];
      for (String availIdentifier : availIdentifiers) {
        if (!availIdentifier.equalsIgnoreCase(identifier)) {
          continue;
        }
        DeluxeTag tag = plugin.getTagsHandler().getTagByIdentifier(availIdentifier);
        if (tag == null) {
          continue;
        }
        if (plugin.getTagsHandler().getPlayerActiveTag(player) != tag) {
          plugin.selectTag(player, tag.getIdentifier(), sender, () ->
              MsgUtils.msg(sender, Lang.CMD_TAG_SEL_SUCCESS.getConfigValue(new String[]{
                  tag.getIdentifier(), tag.getDisplayTag(player)})));
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
      if (!sender.hasPermission(Permissions.CREATE)) {
        MsgUtils.msg(sender, Lang.CMD_NO_PERMS.getConfigValue(new String[]{
            Permissions.CREATE
        }));
        return true;
      }

      if (args.length < 3) {
        MsgUtils.msg(sender, Lang.CMD_ADMIN_CREATE_TAG_INCORRECT.getConfigValue(null));
        return true;
      }

      String identifier = args[1];
      if (plugin.getTagsHandler().getTagByIdentifier(identifier) != null) {
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

      int priority = plugin.getTagsHandler().getLoadedTagsAmount() + 1;
      Set<Integer> priorities = plugin.getTagsHandler().getLoadedPriorities();
      if (!priorities.isEmpty()) {
        for (int i = 1; i <= priorities.size() + 1; i++) {
          if (priorities.contains(i)) continue;
          priority = i;
          break;
        }
      }

      DeluxeTag tag = new DeluxeTag(priority, identifier, tagDisplay, "");
      plugin.getTagsHandler().loadTag(tag);
      plugin.getCfg().saveTag(priority, identifier, tagDisplay, "&f", "deluxetags.tag." + identifier);
      MsgUtils.msg(sender, Lang.CMD_ADMIN_CREATE_TAG_SUCCESS.getConfigValue(new String[]{
          identifier, tagDisplay
      }));

    } else if (args[0].equalsIgnoreCase("delete")) {
      if (!sender.hasPermission(Permissions.DELETE)) {
        MsgUtils.msg(sender, Lang.CMD_NO_PERMS.getConfigValue(new String[]{
           Permissions.DELETE
        }));
        return true;
      }

      if (args.length != 2) {
        MsgUtils.msg(sender, Lang.CMD_ADMIN_DELETE_TAG_INCORRECT.getConfigValue(null));
        return true;
      }

      String identifier = args[1];
      DeluxeTag tag = plugin.getTagsHandler().getTagByIdentifier(identifier);
      if (tag == null) {
        MsgUtils.msg(sender, Lang.CMD_ADMIN_DELETE_TAG_FAIL.getConfigValue(new String[]{
            identifier
        }));
        return true;
      }

      List<UUID> remove = plugin.getTagsHandler().removeActivePlayers(tag);

      if (plugin.getTagsHandler().unloadTag(tag)) {
        plugin.getCfg().removeTag(identifier);
        if (!plugin.isMySqlStorage()) plugin.removeSavedTags(remove);
        if (remove != null) {
          for (UUID uuid : remove) {
            Player affected = Bukkit.getPlayer(uuid);
            if (affected != null) plugin.getTagsHandler().updateTagForPlayer(affected);
          }
        }
        MsgUtils.msg(sender, Lang.CMD_ADMIN_DELETE_TAG_SUCCESS.getConfigValue(new String[]{
            identifier
        }));
        return true;
      }

    } else if (args[0].equalsIgnoreCase("setdesc") || args[0].equalsIgnoreCase("setdescription")) {
      if (!sender.hasPermission(Permissions.SET_DESCRIPTION)) {
        MsgUtils.msg(sender, Lang.CMD_NO_PERMS.getConfigValue(new String[]{
            Permissions.SET_DESCRIPTION
        }));
        return true;
      }

      if (args.length < 3) {
        MsgUtils.msg(sender, Lang.CMD_ADMIN_SET_DESCRIPTION_INCORRECT.getConfigValue(null));
        return true;
      }

      String identifier = args[1];
      DeluxeTag tag = plugin.getTagsHandler().getTagByIdentifier(identifier);
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
      if (!sender.hasPermission(Permissions.SET_ORDER)) {
        MsgUtils.msg(sender, Lang.CMD_NO_PERMS.getConfigValue(new String[]{
            Permissions.SET_ORDER
        }));
        return true;
      }

      if (args.length < 3) {
        MsgUtils.msg(sender, Lang.CMD_ADMIN_SET_ORDER_INCORRECT.getConfigValue(null));
        return true;
      }

      DeluxeTag tag = plugin.getTagsHandler().getTagByIdentifier(args[1]);
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

      if (plugin.getTagsHandler().getLoadedPriorities().contains(priority)) {
        MsgUtils.msg(sender, Lang.CMD_ADMIN_SET_ORDER_ALREADY_EXISTS.getConfigValue(new String[]{
            args[2]
        }));
        return true;
      }

      plugin.getTagsHandler().unloadTag(tag);
      tag.setPriority(priority);
      plugin.getCfg().saveTag(tag.getPriority(), tag.getIdentifier(), tag.getDisplayTag(),
          tag.getDescription(), tag.getPermission());
      plugin.getTagsHandler().loadTag(tag);
      MsgUtils.msg(sender, Lang.CMD_ADMIN_SET_ORDER_SUCCESS.getConfigValue(new String[]{
          args[2], args[1]
      }));
      return true;
    } else if (args[0].equalsIgnoreCase(Permissions.SET_DISPLAY)) {
      if (!sender.hasPermission("deluxetags.setdisplay")) {
        MsgUtils.msg(sender, Lang.CMD_NO_PERMS.getConfigValue(new String[]{
            Permissions.SET_DISPLAY
        }));
        return true;
      }

      if (args.length < 3) {
        MsgUtils.msg(sender, Lang.CMD_ADMIN_SET_DISPLAY_INCORRECT.getConfigValue(null));
        return true;
      }

      String identifier = args[1];
      DeluxeTag tag = plugin.getTagsHandler().getTagByIdentifier(identifier);
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
      if (!sender.hasPermission(Permissions.SET)) {
        MsgUtils.msg(sender, Lang.CMD_NO_PERMS.getConfigValue(new String[]{
            Permissions.SET
        }));
        return true;
      }

      if (args.length != 3) {
        MsgUtils.msg(sender, Lang.CMD_ADMIN_SET_INCORRECT_ARGS.getConfigValue(null));
        return true;
      }

      if (plugin.getTagsHandler().getAllTags().isEmpty()) {
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

      List<String> availIdentifiers = plugin.getTagsHandler().getPlayerAvailableTagIdentifiers(target);
      if (availIdentifiers.isEmpty()) {
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
        DeluxeTag tag = plugin.getTagsHandler().getTagByIdentifier(availIdentifier);
        if (tag == null) {
          continue;
        }
        plugin.selectTag(target, tag.getIdentifier(), sender, () -> {
          MsgUtils.msg(sender, Lang.CMD_ADMIN_SET_SUCCESS.getConfigValue(new String[]{
              target.getName(), tag.getIdentifier(), tag.getDisplayTag(target)
          }));
          if (target != sender) {
            MsgUtils.msg(target, Lang.CMD_ADMIN_SET_SUCCESS_TARGET.getConfigValue(new String[]{
                tag.getIdentifier(), tag.getDisplayTag(target), sender.getName()
            }));
          }
        });
        return true;
      }

      MsgUtils.msg(sender, Lang.CMD_ADMIN_SET_FAIL.getConfigValue(new String[]{
          identifier, target.getName()
      }));
      return true;

    } else if (args[0].equalsIgnoreCase("clear")) {
      if (!sender.hasPermission(Permissions.CLEAR)) {
        MsgUtils.msg(sender, Lang.CMD_NO_PERMS.getConfigValue(new String[]{
            Permissions.CLEAR
        }));
        return true;
      }

      if (args.length != 2) {
        MsgUtils.msg(sender, Lang.CMD_ADMIN_CLEAR_INCORRECT_ARGS.getConfigValue(null));
        return true;
      }

      if (plugin.getTagsHandler().getAllTags().isEmpty()) {
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

      final DeluxeTag tag = plugin.getTagsHandler().getPlayerActiveTag(target);
      if (tag == null || tag.getDisplayTag(target).isEmpty()) {
        MsgUtils.msg(sender, Lang.CMD_ADMIN_CLEAR_NO_TAG_SET.getConfigValue(new String[]{
            target.getName()
        }));
        return true;
      }

      plugin.selectTag(target, Selection.NO_TAG, sender, () -> {
        MsgUtils.msg(sender, Lang.CMD_ADMIN_CLEAR_SUCCESS.getConfigValue(new String[]{target.getName()}));
        if (target != sender) {
          MsgUtils.msg(target, Lang.CMD_ADMIN_CLEAR_SUCCESS_TARGET.getConfigValue(new String[]{sender.getName()}));
        }
      });
      return true;

    } else if (args[0].equalsIgnoreCase("reload")) {

      if (!sender.hasPermission(Permissions.RELOAD)) {
        MsgUtils.msg(sender, Lang.CMD_NO_PERMS.getConfigValue(new String[]{
            Permissions.RELOAD
        }));
        return true;
      }

      plugin.reloadConfig();
      plugin.saveConfig();
      plugin.getCfg().reload();

      plugin.getTagsHandler().unloadData();
      plugin.getCfg().loadCategories();
      int loaded = plugin.getCfg().loadTags();

      plugin.reloadFormattingOptions();

      plugin.getLangFile().reloadConfig();
      plugin.getLangFile().saveConfig();
      plugin.loadMessages();

      plugin.reloadGUIOptions();

      plugin.reloadSelectionStorage(sender);

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
