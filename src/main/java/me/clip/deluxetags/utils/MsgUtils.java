package me.clip.deluxetags.utils;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MsgUtils {
    private static final Pattern HEX_PATTERN = Pattern.compile("&(#[a-f0-9]{6})", Pattern.CASE_INSENSITIVE);

    public static String color(String input) {

        //Hex Support for 1.16.1+
        Matcher m = HEX_PATTERN.matcher(input);
        try {
            net.md_5.bungee.api.ChatColor.class.getDeclaredMethod("of", String.class);
            while (m.find()) {
                input = input.replace(m.group(), net.md_5.bungee.api.ChatColor.of(m.group(1)).toString());
            }
        } catch (Exception e) {
            while (m.find()) {
                input = input.replace(m.group(), "");
            }
        }

        return ChatColor.translateAlternateColorCodes('&', input);
    }

    public static void msg(CommandSender s, String msg) {
        s.sendMessage(color(msg));
    }
}
