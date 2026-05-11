package me.clip.deluxetags.config;

import com.cryptomorin.xseries.XMaterial;

import java.util.*;
import java.util.logging.Level;

import me.clip.deluxetags.DeluxeTags;
import me.clip.deluxetags.gui.DisplayItem;
import me.clip.deluxetags.gui.ItemType;
import me.clip.deluxetags.tags.DeluxeTag;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public class TagConfig {

  DeluxeTags plugin;
  FileConfiguration config;

  public TagConfig(DeluxeTags instance) {
    plugin = instance;
    config = plugin.getConfig();
  }

  public void reload() {
    plugin.reloadConfig();
    plugin.saveConfig();
    config = plugin.getConfig();
  }

  public void loadDefConfig() {

    config.options().header(
        "DeluxeTags version: " + plugin.getDescription().getVersion() + " Main Configuration"
            + "\n"
            + "\nCreate your tags using the following format:"
            + "\n"
            + "\ndeluxetags:"
            + "\n  VIP: "
            + "\n    order: 1"
            + "\n    tag: '&7[&eVIP&7]'"
            + "\n    description: 'This tag is awarded by getting VIP'"
            + "\n"
            + "\nPlaceholders for your chat plugin that supports PlaceholderAPI (Including DeluxeChat)"
            + "\n"
            + "\n%deluxetags_identifier% - display the players active tag identifier"
            + "\n%deluxetags_tag% - display the players active tag"
            + "\n%deluxetags_description% - display the players active tag description"
            + "\n%deluxetags_amount% - display the amount of tags a player has access to"
            + "\n"
            + "\nPlaceholders for your essentials/chat handling formats config:"
            + "\n"
            + "\n{deluxetags_identifier} - display the players active tag identifier"
            + "\n{deluxetags_tag} - display the players active tag"
            + "\n{deluxetags_description} - display the players active tag description"
            + "\n{deluxetags_amount} - display the amount of tags a player has access to");

    config.addDefault("force_tags", false);
    config.addDefault("check_updates", true);
    config.addDefault("legacy_hex", false);
    config.addDefault("papi_chat", true);
    config.addDefault("format_chat.enabled", false);
    config.addDefault("format_chat.format", "{deluxetags_tag} <%1$s> %2$s");
    if (config.contains("force_tag_on_join")) {
      config.set("force_tag_on_join", null);
    }
    config.addDefault("load_tag_on_join", true);
    // GUI properties
    config.addDefault("gui.name", "&6Available tags&f: &6%deluxetags_amount%");
    config.addDefault("gui.size", 54);
    config.addDefault("gui.tagSlots", Collections.singletonList("0-35"));
    // Tag Select item
    config.addDefault("gui.tag_select_item.material", "NAME_TAG");
    config.addDefault("gui.tag_select_item.data", 0);
    config.addDefault("gui.tag_select_item.displayname", "&6Tag&f: &6%deluxetags_identifier%");
    config.addDefault("gui.tag_select_item.lore",
        Arrays.asList("%deluxetags_tag%", "%deluxetags_description%"));
    // Tag Visible item
    config.addDefault("gui.tag_visible_item.material", "NAME_TAG");
    config.addDefault("gui.tag_visible_item.data", 0);
    config.addDefault("gui.tag_visible_item.displayname", "&6Tag&f: &6%deluxetags_identifier%");
    config.addDefault("gui.tag_visible_item.lore",
        Arrays.asList("%deluxetags_tag%", "%deluxetags_description%", "&7You can see this tag but you can't select it!"));
    // Divider item
    config.addDefault("gui.divider_item.material", "BLACK_STAINED_GLASS_PANE");
    config.addDefault("gui.divider_item.data", 0);
    config.addDefault("gui.divider_item.displayname", "");
    config.addDefault("gui.divider_item.lore",
        Collections.emptyList());
    config.addDefault("gui.divider_item.slots", Collections.singletonList("36-44"));
    // Has Tag item
    config.addDefault("gui.has_tag_item.material", "PLAYER_HEAD");
    config.addDefault("gui.has_tag_item.data", 0);
    config.addDefault("gui.has_tag_item.displayname", "&eCurrent tag&f: &6%deluxetags_identifier%");
    config.addDefault("gui.has_tag_item.lore",
        Arrays.asList("%deluxetags_tag%", "Click to remove your current tag"));
    config.addDefault("gui.has_tag_item.slot", 49);
    // No Tag item
    config.addDefault("gui.no_tag_item.material", "PLAYER_HEAD");
    config.addDefault("gui.no_tag_item.data", 0);
    config.addDefault("gui.no_tag_item.displayname", "&cYou don't have a tag set!");
    config.addDefault("gui.no_tag_item.lore",
        Collections.singletonList("&7Click a tag above to select one!"));
    config.addDefault("gui.no_tag_item.slot", 49);
    // Exit item
    config.addDefault("gui.exit_item.material", "IRON_DOOR");
    config.addDefault("gui.exit_item.data", 0);
    config.addDefault("gui.exit_item.displayname", "&cClick to exit");
    config.addDefault("gui.exit_item.lore",
        Collections.singletonList("&7Exit the tags menu"));
    config.addDefault("gui.exit_item.slots", Arrays.asList(48, 50));
    // Next Page item
    config.addDefault("gui.next_page.material", "PAPER");
    config.addDefault("gui.next_page.data", 0);
    config.addDefault("gui.next_page.displayname", "&6Next page: %page%");
    config.addDefault("gui.next_page.lore",
        Collections.singletonList("&7Move to the next page"));
    config.addDefault("gui.next_page.slot", 53);
    // Previous Page item
    config.addDefault("gui.previous_page.material", "PAPER");
    config.addDefault("gui.previous_page.data", 0);
    config.addDefault("gui.previous_page.displayname", "&6Previous page: %page%");
    config.addDefault("gui.previous_page.lore",
        Collections.singletonList("&7Move to the previous page"));
    config.addDefault("gui.previous_page.slot", 45);

    if (!config.contains("deluxetags")) {
      config.set("deluxetags.example.order", 1);
      config.set("deluxetags.example.tag", "&8[&bDeluxeTags&8]");
      config.set("deluxetags.example.description", "&cAwarded for using DeluxeTags!");
      config.set("deluxetags.example.permission", "deluxetags.tag.example");
    }

    config.options().copyDefaults(true);
    plugin.saveConfig();
    plugin.reloadConfig();
    config = plugin.getConfig();
  }

  public boolean formatChat() {
    return config.getBoolean("format_chat.enabled");
  }

  public String chatFormat() {
    String format = config.getString("format_chat.format");
    return format != null ? format : "{deluxetags_tag} <%1$s> %2$s";
  }

  public boolean checkUpdates() {
    return config.getBoolean("check_updates");
  }

  public boolean papiChat() {
    return config.getBoolean("papi_chat");
  }

  public boolean legacyHex() {
    return config.getBoolean("legacy_hex", false);
  }

  public boolean loadTagOnJoin() {
    return config.getBoolean("load_tag_on_join");
  }

  public boolean forceTags() {
    return config.getBoolean("force_tags");
  }

  public String getMenuName() {
    return config.getString("gui.name", "&6Available tags&f: &6%deluxetags_amount%");
  }

  public int getMenuSize() {
    return config.getInt("gui.size", 54);
  }

  public DisplayItem loadGuiItem(ItemType type) {
    Material material;
    String displayName;
    List<String> lore;
    List<Integer> slots;
    short data;

    String basePath = "gui." + type.name().toLowerCase();

    try {
      material = XMaterial.matchXMaterial(config.getString("gui." + type.name().toLowerCase() + ".material").toUpperCase()).get().parseMaterial();
    } catch (Exception e) {
      material = type.getFallbackMaterial();
    }

    try {
      data = Short.parseShort(config.getString("gui." + type.name().toLowerCase() + ".data", "0"));
    } catch (Exception e) {
      data = 0;
    }

    displayName = config.getString("gui." + type.name().toLowerCase() + ".displayname");
    lore = config.getStringList("gui." + type.name().toLowerCase() + ".lore");

    slots = new ArrayList<>();
    if (config.contains("gui." + type.name().toLowerCase() + ".slots") && config.isList("gui." + type.name().toLowerCase() + ".slots")) {
      List<String> confSlots = config.getStringList("gui." + type.name().toLowerCase() + ".slots");
      for (String slot : confSlots) {
        String[] values = slot.split("-", 2);
        if (values.length == 2) {
          for (int i = Integer.parseInt(values[0]); i <= Integer.parseInt(values[1]); i++) {
            slots.add(i);
          }
        } else {
          slots.add(Integer.parseInt(slot));
        }
      }
    } else if (config.contains("gui." + type.name().toLowerCase() + ".slot") && config.isInt("gui." + type.name().toLowerCase() + ".slot")) {
      slots.add(config.getInt("gui." + type.name().toLowerCase() + ".slot"));
    }

    return material == null ? null : new DisplayItem(material, data, displayName, lore, slots);
  }

  public int loadTags() {
    int loaded = 0;

    ConfigurationSection deluxetags = config.getConfigurationSection("deluxetags");
    if (deluxetags == null) {
      return loaded;
    }

    Set<String> keys = deluxetags.getKeys(false);
    if (keys.isEmpty()) {
      return loaded;
    }

    for (String identifier : keys) {
      String tag = config.getString("deluxetags." + identifier + ".tag");
      if (tag == null) {
        plugin.getLogger().log(Level.INFO, "Could not load tag: " + identifier + " because it does not have a display set.");
        continue;
      }
      String description;

      if (config.isList("deluxetags." + identifier + ".description")) {
        description = String.join("\n", config.getStringList("deluxetags." + identifier + ".description"));
      } else {
        description = config.getString("deluxetags." + identifier + ".description", "&f");
      }

      if (!config.contains("deluxetags." + identifier + ".order")) {
        plugin.getLogger().log(Level.INFO, "Could not load tag: " + identifier + " because it does not have an order set.");
        continue;
      }
      int priority = config.getInt("deluxetags." + identifier + ".order");

      DeluxeTag t = new DeluxeTag(priority, identifier, tag, description);
      t.setPermission(config.getString("deluxetags." + identifier + ".permission", "deluxetags.tag." + identifier));
      plugin.getTagsHandler().loadTag(t);
      loaded++;
    }

    return loaded;
  }

  public void saveTag(DeluxeTag tag) {
    saveTag(tag.getPriority(), tag.getIdentifier(), tag.getDisplayTag(), tag.getDescription(), tag.getPermission());
  }

  public void saveTag(int priority, String identifier, String tag, String description, String permission) {
    config.set("deluxetags." + identifier + ".order", priority);
    config.set("deluxetags." + identifier + ".tag", tag);
    if (description == null) {
      description = "&fDescription for tag " + identifier;
    }
    config.set("deluxetags." + identifier + ".description", description);
    config.set("deluxetags." + identifier + ".permission", permission);
    plugin.saveConfig();
  }

  public void removeTag(String identifier) {
    config.set("deluxetags." + identifier, null);
    plugin.saveConfig();
  }
}
