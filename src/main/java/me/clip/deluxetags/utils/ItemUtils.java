package me.clip.deluxetags.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class ItemUtils {
  public static ItemStack createItem(Material material, short data, String name, List<String> lore) {
    if (material == null) {
      return null;
    }

    ItemStack item = new ItemStack(material, 1);
    if (data > 0) {
      item.setDurability(data);
    }

    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      if (name != null) {
        itemMeta.setDisplayName(name);
      }

      if (lore != null && !lore.isEmpty()) {
        itemMeta.setLore(lore);
      }
      item.setItemMeta(itemMeta);
    }

    return item;
  }

}
