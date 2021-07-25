package me.clip.deluxetags.placeholders;

import me.clip.deluxetags.DeluxeTags;
import me.clip.deluxetags.tags.DeluxeTag;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class TagPlaceholders extends PlaceholderExpansion {
    DeluxeTags plugin;

    public TagPlaceholders(DeluxeTags plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "deluxetags";
    }

    @Override
    public String getAuthor() {
        return plugin.getDescription().getAuthors().get(0);
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer offlinePlayer, String params) {
        if (params.startsWith("tag_")) {
            DeluxeTag tag = DeluxeTag.getLoadedTag(params.replace("tag_", ""));
            if (tag == null) {
                return "invalid tag";
            }
            return tag.getDisplayTag();
        }

        if (params.startsWith("description_")) {
            DeluxeTag tag = DeluxeTag.getLoadedTag(params.replace("description_", ""));
            if (tag == null) {
                return "invalid tag";
            }
            return tag.getDescription();
        }

        if (offlinePlayer == null || offlinePlayer.getPlayer() == null) {
            return "";
        }
        Player player = offlinePlayer.getPlayer();

        if (params.startsWith("has_tag_")) {
            DeluxeTag tag = DeluxeTag.getLoadedTag(params.replace("has_tag_", ""));
            if (tag == null) {
                return "invalid tag";
            }
            return tag.hasTagPermission(player) ? PlaceholderAPIPlugin.booleanTrue() : PlaceholderAPIPlugin.booleanFalse();
        }

        switch (params) {
            case "description":
                return DeluxeTag.getPlayerTagDescription(player);
            case "identifier":
                return DeluxeTag.getPlayerTagIdentifier(player) != null ? DeluxeTag.getPlayerTagIdentifier(player) : "";
            case "amount":
                return DeluxeTag.getAvailableTagIdentifiers(player) != null ? String.valueOf(DeluxeTag.getAvailableTagIdentifiers(player).size()) : "0";
            case "tag":
                return DeluxeTag.getPlayerDisplayTag(player);
        }
        return null;
    }
}