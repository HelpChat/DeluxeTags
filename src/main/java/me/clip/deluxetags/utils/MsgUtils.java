package me.clip.deluxetags.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;

public class MsgUtils {
    private static final Pattern LEGACY_HEX_PATTERN = Pattern.compile("&(#[a-f0-9]{6})", Pattern.CASE_INSENSITIVE);
    private static final Pattern HEX_PATTERN = Pattern.compile("(#[a-f0-9]{6})", Pattern.CASE_INSENSITIVE);
    private static final Pattern ANGLE_TAG_PATTERN = Pattern.compile("(?<!\\\\)<[^<>\\r\\n]+>");
    private static final Pattern DIRECT_HEX_TAG_PATTERN = Pattern.compile("#[a-f0-9]{6}", Pattern.CASE_INSENSITIVE);
    private static final Set<String> KNOWN_MINI_MESSAGE_TAGS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
        "black", "dark_blue", "dark_green", "dark_aqua", "dark_red", "dark_purple", "gold",
        "gray", "grey", "dark_gray", "dark_grey", "blue", "green", "aqua", "red",
        "light_purple", "yellow", "white", "color", "colour", "c", "shadow", "bold", "b",
        "italic", "em", "i", "underlined", "u", "strikethrough", "st", "obfuscated", "obf",
        "reset", "click", "hover", "keybind", "key", "lang", "tr", "translate", "lang_or",
        "tr_or", "translate_or", "insert", "rainbow", "gradient", "transition", "font",
        "newline", "br", "selector", "sel", "score", "nbt", "data", "pride", "sprite", "head"
    )));
    private static final boolean SUPPORTS_HEX_COLORS = supportsHexColors();
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer MINI_MESSAGE_SERIALIZER = SUPPORTS_HEX_COLORS
        ? LegacyComponentSerializer.builder()
            .character('\u00A7')
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build()
        : LegacyComponentSerializer.legacySection();
    private static Pattern PATTERN = HEX_PATTERN;
    private static boolean miniMessage;

    public static void setPattern(boolean legacy) {
        PATTERN = legacy ? LEGACY_HEX_PATTERN : HEX_PATTERN;
    }

    public static void setMiniMessage(boolean enabled) {
        miniMessage = enabled;
    }

    public static String color(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        if (miniMessage && containsMiniMessageTag(input)) {
            input = colorMiniMessage(input);
        }

        //Hex Support for 1.16.1+
        Matcher m = PATTERN.matcher(input);
        if (SUPPORTS_HEX_COLORS) {
            while (m.find()) {
                input = input.replace(m.group(), ChatColor.of(m.group(1)).toString());
            }
        } else {
            while (m.find()) {
                input = input.replace(m.group(), "");
            }
        }

        return ChatColor.translateAlternateColorCodes('&', input);
    }

    private static String colorMiniMessage(String input) {
        try {
            return MINI_MESSAGE_SERIALIZER.serialize(MINI_MESSAGE.deserialize(escapeUnsupportedTags(input)));
        } catch (RuntimeException ignored) {
            return input;
        }
    }

    private static boolean containsMiniMessageTag(String input) {
        Matcher matcher = ANGLE_TAG_PATTERN.matcher(input);
        while (matcher.find()) {
            if (isMiniMessageTag(matcher.group())) {
                return true;
            }
        }

        return false;
    }

    private static String escapeUnsupportedTags(String input) {
        Matcher matcher = ANGLE_TAG_PATTERN.matcher(input);
        StringBuffer escaped = new StringBuffer();

        while (matcher.find()) {
            String tag = matcher.group();
            String replacement = isMiniMessageTag(tag) ? tag : "\\" + tag;
            matcher.appendReplacement(escaped, Matcher.quoteReplacement(replacement));
        }

        matcher.appendTail(escaped);
        return escaped.toString();
    }

    private static boolean isMiniMessageTag(String tag) {
        String content = tag.substring(1, tag.length() - 1).trim();
        if (content.isEmpty()) {
            return false;
        }

        if (content.startsWith("/") || content.startsWith("!")) {
            content = content.substring(1).trim();
        }

        if (content.isEmpty()) {
            return false;
        }

        String name = content.split(":", 2)[0].toLowerCase(Locale.ROOT);
        return DIRECT_HEX_TAG_PATTERN.matcher(name).matches() || KNOWN_MINI_MESSAGE_TAGS.contains(name);
    }

    private static boolean supportsHexColors() {
        try {
            ChatColor.class.getDeclaredMethod("of", String.class);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    public static void msg(CommandSender s, String message) {
        for (String line : color(message).split("\\\\n")) {
            s.sendMessage(line);
        }
    }
}
