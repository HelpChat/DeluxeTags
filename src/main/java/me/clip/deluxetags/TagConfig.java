package me.clip.deluxetags;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import org.bukkit.configuration.file.FileConfiguration;

public class TagConfig {

  DeluxeTags plugin;

  FileConfiguration config;

  public TagConfig(DeluxeTags i) {
    plugin = i;
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
            + "\nPlaceholders for your DeluxeChat formats config:"
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
    config.addDefault("deluxe_chat", true);
    config.addDefault("format_chat.enabled", true);
    config.addDefault("format_chat.format", "{deluxetags_tag} <%1$s> %2$s");
    if (config.contains("force_tag_on_join")) {
      config.set("force_tag_on_join", null);
    }
    config.addDefault("load_tag_on_join", true);
    config.addDefault("gui.name", "&6Available tags&f: &6%deluxetags_amount%");
    config.addDefault("gui.tag_select_item.material", "NAME_TAG");
    config.addDefault("gui.tag_select_item.data", 0);
    config.addDefault("gui.tag_select_item.displayname", "&6Tag&f: &6%deluxetags_identifier%");
    config.addDefault("gui.tag_select_item.lore",
        Arrays.asList("%deluxetags_tag%", "%deluxetags_description%"));
    config.addDefault("gui.divider_item.material", "STAINED_GLASS_PANE");
    config.addDefault("gui.divider_item.data", 15);
    config.addDefault("gui.divider_item.displayname", "");
    config.addDefault("gui.divider_item.lore", Collections.emptyList());
    config.addDefault("gui.has_tag_item.material", "SKULL_ITEM");
    config.addDefault("gui.has_tag_item.data", 3);
    config.addDefault("gui.has_tag_item.displayname", "&eCurrent tag&f: &6%deluxetags_identifier%");
    config.addDefault("gui.has_tag_item.lore",
        Arrays.asList("%deluxetags_tag%", "Click to remove your current tag"));
    config.addDefault("gui.no_tag_item.material", "SKULL_ITEM");
    config.addDefault("gui.no_tag_item.data", 3);
    config.addDefault("gui.no_tag_item.displayname", "&cYou don't have a tag set!");
    config.addDefault("gui.no_tag_item.lore",
        Collections.singletonList("&7Click a tag above to select one!"));
    config.addDefault("gui.exit_item.material", "IRON_DOOR");
    config.addDefault("gui.exit_item.data", 0);
    config.addDefault("gui.exit_item.displayname", "&cClick to exit");
    config.addDefault("gui.exit_item.lore", Collections.singletonList("&7Exit the tags menu"));

    if (!config.contains("deluxetags")) {
      config.set("deluxetags.example.order", 1);
      config.set("deluxetags.example.tag", "&8[&bDeluxeTags&8]");
      config.set("deluxetags.example.description", "&cAwarded for using DeluxeTags!");
      config.set("deluxetags.example.permission", "deluxetags.tag.example");
    }

    config.options().copyDefaults(true);
    plugin.saveConfig();
    plugin.reloadConfig();
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

  public boolean deluxeChat() {
    return config.getBoolean("deluxe_chat");
  }

  public boolean loadTagOnJoin() {
    return config.getBoolean("load_tag_on_join");
  }

  public boolean forceTags() {
    return config.getBoolean("force_tags");
  }

  public int loadTags() {

    FileConfiguration c = plugin.getConfig();

    int loaded = 0;

    if (!c.contains("deluxetags")) {
      return loaded;
    }

    Set<String> keys = c.getConfigurationSection("deluxetags").getKeys(false);

    if (keys != null && !keys.isEmpty()) {

      Iterator<String> it = keys.iterator();

      int count = 1;

      boolean save = false;

      while (it.hasNext()) {

        String identifier = it.next();

        String tag = c.getString("deluxetags." + identifier + ".tag");

        String desc;

        if (c.isList("deluxetags." + identifier + ".description")) {
           desc = String.join("\n", c.getStringList("deluxetags." + identifier + ".description"));
        } else {
          desc = c.getString("deluxetags." + identifier + ".description", "&f");
        }

        int priority = count;

        count++;

        if (!c.contains("deluxetags." + identifier + ".order")) {
          c.set("deluxetags." + identifier + ".order", priority);
          save = true;
        } else {
          priority = c.getInt("deluxetags." + identifier + ".order");
        }

        DeluxeTag t = new DeluxeTag(priority, identifier, tag, desc);
        t.setPermission(c.getString("deluxetags." + identifier + ".permission", "deluxetags.tag." + identifier));
        t.load();
        loaded++;
      }

      if (save) {
        plugin.saveConfig();
        plugin.reloadConfig();
      }
    }

    return loaded;
  }

  public void saveTag(DeluxeTag tag) {
    saveTag(tag.getPriority(), tag.getIdentifier(), tag.getDisplayTag(), tag.getDescription(), tag.getPermission());
  }

  public void saveTag(int priority, String identifier, String tag, String description, String permission) {
    FileConfiguration c = plugin.getConfig();
    c.set("deluxetags." + identifier + ".order", priority);
    c.set("deluxetags." + identifier + ".tag", tag);
    if (description == null) {
      description = "&fDescription for tag " + identifier;
    }
    c.set("deluxetags." + identifier + ".description", description);
    c.set("deluxetags." + identifier + ".permission", permission);
    plugin.saveConfig();
  }

  public void removeTag(String identifier) {
    FileConfiguration c = plugin.getConfig();
    c.set("deluxetags." + identifier, null);
    plugin.saveConfig();
  }
}
