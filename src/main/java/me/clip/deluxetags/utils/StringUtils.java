package me.clip.deluxetags.utils;

import org.bukkit.Color;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StringUtils {
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
