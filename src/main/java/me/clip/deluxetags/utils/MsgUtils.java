package me.clip.deluxetags.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;

public class MsgUtils {
    private static final Pattern LEGACY_HEX_PATTERN = Pattern.compile("&(#[a-f0-9]{6})", Pattern.CASE_INSENSITIVE);
    private static final Pattern HEX_PATTERN = Pattern.compile("(#[a-f0-9]{6})", Pattern.CASE_INSENSITIVE);
    private static Pattern PATTERN = HEX_PATTERN;

    public static void setPattern(boolean legacy) {
        PATTERN = legacy ? LEGACY_HEX_PATTERN : HEX_PATTERN;
    }

    public static String color(String input) {

        //Hex Support for 1.16.1+
        Matcher m = PATTERN.matcher(input);
        try {
            ChatColor.class.getDeclaredMethod("of", String.class);
            while (m.find()) {
                input = input.replace(m.group(), ChatColor.of(m.group(1)).toString());
            }
        } catch (Exception e) {
            while (m.find()) {
                input = input.replace(m.group(), "");
            }
        }

        return ChatColor.translateAlternateColorCodes('&', input);
    }

    public static void msg(CommandSender s, String message) {
        for (String line : color(message).split("\\\\n")) {
            s.sendMessage(line);
        }
    }
}
