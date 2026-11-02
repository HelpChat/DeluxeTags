package me.clip.deluxetags.utils;

import java.util.List;
import me.clip.deluxetags.tags.DeluxeTag;
import org.bukkit.Color;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StringUtils {
  public static String replaceLore(String input, @Nullable DeluxeTag tag, List<String> defaultLore) {
    if (!input.contains("%deluxetags_lore%") && !input.contains("{deluxetags_lore}")) {
      return input;
    }
    String lore = tag == null ? "" : String.join("\n", tag.getLore() == null ? defaultLore : tag.getLore());
    // A lore entry cannot expand itself through PlaceholderAPI.
    lore = lore.replace("%deluxetags_lore%", "").replace("{deluxetags_lore}", "");
    return input.replace("%deluxetags_lore%", lore).replace("{deluxetags_lore}", lore);
  }

  @Nullable
  public static Color parseRGBColor(@NotNull final String input) {
    final String[] parts = input.split(",");
    try {
      return Color.fromRGB(
        Integer.parseInt(parts[0].trim()),
        Integer.parseInt(parts[1].trim()),
        Integer.parseInt(parts[2].trim())
      );
    } catch (final Exception exception) {
      return null;
    }
  }
}
