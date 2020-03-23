package me.clip.deluxetags.utils;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class MsgUtils {
    public static String color(String input) {
        return ChatColor.translateAlternateColorCodes('&', input);
    }

    public static void msg(CommandSender s, String msg) {
        s.sendMessage(color(msg));
    }
}
