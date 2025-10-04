package me.clip.deluxetags.placeholders;

import me.clip.deluxetags.DeluxeTags;
import me.clip.deluxetags.tags.DeluxeTag;
import me.clip.deluxetags.utils.MsgUtils;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TagPlaceholders extends PlaceholderExpansion {
    DeluxeTags plugin;

    public TagPlaceholders(DeluxeTags plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "deluxetags";
    }

    @Override
    public @NotNull String getAuthor() {
        return plugin.getDescription().getAuthors().get(0);
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer offlinePlayer, String params) {
        if (params.startsWith("tag_")) {
            DeluxeTag tag = plugin.getTagsHandler().getTagByIdentifier(params.substring(4));
            if (tag == null) {
                return "invalid tag";
            }
            return MsgUtils.color(tag.getDisplayTag(offlinePlayer));
        }

        if (params.startsWith("description_")) {
            DeluxeTag tag = plugin.getTagsHandler().getTagByIdentifier(params.substring(12));
            if (tag == null) {
                return "invalid tag";
            }
            return MsgUtils.color(tag.getDescription(offlinePlayer));
        }

        if (params.startsWith("order_")) {
            DeluxeTag tag = plugin.getTagsHandler().getTagByIdentifier(params.substring(6));
            if (tag == null) {
                return "invalid tag";
            }
            return String.valueOf(tag.getPriority());
        }

        if (offlinePlayer == null || offlinePlayer.getPlayer() == null) {
            return "";
        }

        Player player = offlinePlayer.getPlayer();

        if (params.startsWith("has_tag_")) {
            DeluxeTag tag = plugin.getTagsHandler().getTagByIdentifier(params.substring(8));
            if (tag == null) {
                return "invalid tag";
            }
            return tag.hasPermissionToUse(player) ? PlaceholderAPIPlugin.booleanTrue() : PlaceholderAPIPlugin.booleanFalse();
        }

        final DeluxeTag tag = plugin.getTagsHandler().getPlayerActiveTag(player);

        switch (params) {
            case "amount":
                return String.valueOf(plugin.getTagsHandler().getPlayerAvailableTagIdentifiers(player).size());
            case "order":
                if (tag == null) {
                    return "";
                }

                final int priority = tag.getPriority();
                return priority != -1 ? String.valueOf(priority) : "";
            case "description":
                if (tag == null) {
                    return "";
                }
                return MsgUtils.color(tag.getDescription(player));
            case "identifier":
                if (tag == null) {
                    return "";
                }
                return MsgUtils.color(tag.getIdentifier());
            case "tag":
                if (tag == null) {
                    return "";
                }
                return MsgUtils.color(tag.getDisplayTag(player));
        }

        return null;
    }
}