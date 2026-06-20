package me.clip.deluxetags.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.Test;

public class TagConfigTest {

  @Test
  public void normalizesMigratedConfigTopLevelOrder() {
    YamlConfiguration config = new YamlConfiguration();
    config.set("deluxetags.vip.order", 1);
    config.set("deluxetags.vip.tag", "&6VIP");
    config.set("categories.general.order", 1);
    config.set("gui.name", "&6Tags");
    config.set("load_tag_on_join", true);
    config.set("format_chat.enabled", false);
    config.set("format_chat.format", "{deluxetags_tag} <%1$s> %2$s");
    config.set("papi_chat", true);
    config.set("legacy_hex", false);
    config.set("check_updates", true);
    config.set("force_tags", false);
    config.set("use_minimessage", false);
    config.set("tag_availability_placeholder.has_permission", "&aAllowed");
    config.set("tag_availability_placeholder.no_permission", "&cNope");
    config.set("custom_section.enabled", true);

    TagConfig.migrateTagAvailabilityPlaceholder(config);
    TagConfig.normalizeTopLevelOrder(config);

    assertEquals(Arrays.asList(
        "use_minimessage",
        "force_tags",
        "check_updates",
        "legacy_hex",
        "papi_chat",
        "format_chat",
        "load_tag_on_join",
        "gui",
        "categories",
        "deluxetags",
        "custom_section"
    ), new ArrayList<>(config.getKeys(false)));
    assertFalse(config.isSet("tag_availability_placeholder"));
    assertEquals("&aAllowed", config.getString("gui.tag_availability_placeholder.has_permission"));
    assertEquals("&cNope", config.getString("gui.tag_availability_placeholder.no_permission"));
  }

  @Test
  public void keepsExistingGuiAvailabilityPlaceholderDuringLegacyMigration() {
    YamlConfiguration config = new YamlConfiguration();
    config.set("gui.tag_availability_placeholder.has_permission", "&aCurrent");
    config.set("tag_availability_placeholder.has_permission", "&aLegacy");

    TagConfig.migrateTagAvailabilityPlaceholder(config);

    assertEquals("&aCurrent", config.getString("gui.tag_availability_placeholder.has_permission"));
    assertFalse(config.isSet("tag_availability_placeholder"));
  }

  @Test
  public void migratesLegacyTagItemConfigIntoEachTag() {
    YamlConfiguration config = new YamlConfiguration();
    config.set("gui.tag_select_item.material", "DIAMOND");
    config.set("gui.tag_select_item.data", 3);
    config.set("gui.tag_select_item.displayname", "&6Legacy tag &f%deluxetags_identifier%");
    config.set("gui.tag_select_item.lore", Arrays.asList(
        "%deluxetags_tag%",
        "%deluxetags_description%",
        "&7Click to select"
    ));
    config.set("gui.tag_visible_item.material", "BARRIER");
    config.set("gui.tag_visible_item.data", 0);
    config.set("gui.tag_visible_item.displayname", "&cLocked legacy item");
    config.set("gui.tag_visible_item.lore", Arrays.asList("&7You can see this tag"));
    config.set("deluxetags.vip.order", 1);
    config.set("deluxetags.vip.category", "all");
    config.set("deluxetags.vip.tag", "&6VIP");
    config.set("deluxetags.vip.description", "&7Legacy description");

    TagConfig.migrateTags(config);

    assertEquals("general", config.getString("deluxetags.vip.category"));
    assertEquals("&6Legacy tag &f%deluxetags_identifier%", config.getString("deluxetags.vip.displayname"));
    assertEquals(Arrays.asList(
        "%deluxetags_tag%",
        "&7Legacy description",
        "&7Click to select"
    ), config.getStringList("deluxetags.vip.description"));
    assertEquals("DIAMOND", config.getString("deluxetags.vip.item"));
    assertEquals(3, config.getInt("deluxetags.vip.data"));
    assertFalse(config.isSet("gui.tag_select_item"));
    assertEquals("BARRIER", config.getString("gui.tag_visible_item.material"));
    assertEquals(0, config.getInt("gui.tag_visible_item.data"));
    assertFalse(config.isSet("gui.tag_visible_item.displayname"));
    assertFalse(config.isSet("gui.tag_visible_item.lore"));
  }

  @Test
  public void addsSectionCommentsToSavedConfig() {
    YamlConfiguration config = new YamlConfiguration();
    config.set("use_minimessage", false);
    config.set("gui.name", "&6Tags");
    config.set("categories.general.order", 1);
    config.set("deluxetags.vip.order", 1);

    TagConfig.applySectionComments(config);

    String yaml = config.saveToString();
    assertTrue(yaml.contains("# Main Options\nuse_minimessage: false"));
    assertTrue(yaml.contains("use_minimessage: false\n\n# GUI layout and buttons\ngui:"));
    assertTrue(yaml.contains("  name: '&6Tags'\n\n# Tag category menus\ncategories:"));
    assertTrue(yaml.contains("  order: 1\n\n# Tags\ndeluxetags:"));
  }
}
