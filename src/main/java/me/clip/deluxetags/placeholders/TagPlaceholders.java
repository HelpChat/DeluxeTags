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
            DeluxeTag tag = plugin.getTagsHandler().getLoadedTag(params.replace("tag_", ""));
            if (tag == null) {
                return "invalid tag";
            }
            return MsgUtils.color(tag.getDisplayTag(offlinePlayer));
        }

        if (params.startsWith("description_")) {
            DeluxeTag tag = plugin.getTagsHandler().getLoadedTag(params.replace("description_", ""));
            if (tag == null) {
                return "invalid tag";
            }
            return MsgUtils.color(tag.getDescription(offlinePlayer));
        }

        if (params.startsWith("order_")) {
            DeluxeTag tag = plugin.getTagsHandler().getLoadedTag(params.replace("order_", ""));
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
            DeluxeTag tag = plugin.getTagsHandler().getLoadedTag(params.replace("has_tag_", ""));
            if (tag == null) {
                return "invalid tag";
            }
            return tag.hasPermissionToUse(player) ? PlaceholderAPIPlugin.booleanTrue() : PlaceholderAPIPlugin.booleanFalse();
        }

        switch (params) {
            case "order":
                return plugin.getTagsHandler().getPlayerTagPriority(player) != -1 ? String.valueOf(plugin.getTagsHandler().getPlayerTagPriority(player)) : "";
            case "description":
                return MsgUtils.color(plugin.getTagsHandler().getPlayerTagDescription(player));
            case "identifier":
                return plugin.getTagsHandler().getPlayerTagIdentifier(player) != null ? MsgUtils.color(plugin.getTagsHandler().getPlayerTagIdentifier(player)) : "";
            case "amount":
                return plugin.getTagsHandler().getAvailableTagIdentifiers(player) != null ? String.valueOf(plugin.getTagsHandler().getAvailableTagIdentifiers(player).size()) : "0";
            case "tag":
                return MsgUtils.color(plugin.getTagsHandler().getPlayerDisplayTag(player));
        }
        return null;
    }
}