package me.clip.deluxetags.config;

import com.cryptomorin.xseries.XMaterial;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

import me.clip.deluxetags.DeluxeTags;
import me.clip.deluxetags.gui.DisplayItem;
import me.clip.deluxetags.gui.ItemType;
import me.clip.deluxetags.gui.options.CustomModelDataComponent;
import me.clip.deluxetags.tags.DeluxeTag;
import me.clip.deluxetags.tags.DeluxeTagCategory;
import me.clip.deluxetags.utils.ItemUtils;
import me.clip.deluxetags.utils.StringUtils;
import me.clip.deluxetags.utils.VersionHelper;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        + "\nFormatting options:"
        + "\nlegacy_hex: false # Use '&#RRGGBB' instead of '#RRGGBB' for raw hex colors"
        + "\nuse_minimessage: false # Enable MiniMessage tags like <red> and <gradient:#ff0000:#00ff00>"
      + "\nMiniMessage example tag: '<gradient:#ff0000:#ffaa00><bold>[VIP]</bold></gradient>'"
        + "\n"
            + "\nCreate your tags using the following format:"
            + "\n"
            + "\ndeluxetags:"
            + "\n  VIP: "
            + "\n    order: 1"
            + "\n    category: general"
            + "\n    tag: '&7[&eVIP&7]'"
            + "\n    description: 'This tag is awarded by getting VIP'"
            + "\n"
            + "\nCreate your categories using the following format:"
            + "\n"
            + "\ncategories:"
            + "\n  general:"
            + "\n    order: 1"
            + "\n    item: NAME_TAG"
            + "\n    name: '&6General'"
            + "\n    lore:"
            + "\n      - '&7Click to view general tags'"
            + "\n    gui_name: '&6General tags'"
            + "\n"
            + "\nThe reserved 'all' category configures the automatic all-tags selector item."
            + "\n"
            + "\nPlaceholders for your chat plugin that supports PlaceholderAPI"
            + "\n"
            + "\n%deluxetags_identifier% - display the players active tag identifier"
            + "\n%deluxetags_tag% - display the players active tag"
            + "\n%deluxetags_description% - display the players active tag description"
            + "\n%deluxetags_amount% - display the amount of tags a player has access to");

    config.addDefault("force_tags", false);
    config.addDefault("check_updates", true);
    config.addDefault("legacy_hex", false);
    config.addDefault("use_minimessage", false);
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
    config.addDefault("gui.tag_slots", Collections.singletonList("0-35"));

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
    addDefaultUnlessAlternativePathExists("gui.divider_item.slots", Collections.singletonList("36-44"), "gui.divider_item.slot");

    // Has Tag item
    config.addDefault("gui.has_tag_item.material", "PLAYER_HEAD");
    config.addDefault("gui.has_tag_item.data", 0);
    config.addDefault("gui.has_tag_item.displayname", "&eCurrent tag&f: &6%deluxetags_identifier%");
    config.addDefault("gui.has_tag_item.lore",
        Arrays.asList("%deluxetags_tag%", "Click to remove your current tag"));
    addDefaultUnlessAlternativePathExists("gui.has_tag_item.slot", 49, "gui.has_tag_item.slots");

    // No Tag item
    config.addDefault("gui.no_tag_item.material", "PLAYER_HEAD");
    config.addDefault("gui.no_tag_item.data", 0);
    config.addDefault("gui.no_tag_item.displayname", "&cYou don't have a tag set!");
    config.addDefault("gui.no_tag_item.lore",
        Collections.singletonList("&7Click a tag above to select one!"));
    addDefaultUnlessAlternativePathExists("gui.no_tag_item.slot", 49,  "gui.no_tag_item.slots");

    // Exit item
    config.addDefault("gui.exit_item.material", "IRON_DOOR");
    config.addDefault("gui.exit_item.data", 0);
    config.addDefault("gui.exit_item.displayname", "&cClick to exit");
    config.addDefault("gui.exit_item.lore",
        Collections.singletonList("&7Exit the tags menu"));
    addDefaultUnlessAlternativePathExists("gui.exit_item.slots", Arrays.asList(48, 50), "gui.exit_item.slot");

    // Category Back item
    config.addDefault("gui.category_back_item.material", "ARROW");
    config.addDefault("gui.category_back_item.data", 0);
    config.addDefault("gui.category_back_item.displayname", "&6Back to categories");
    config.addDefault("gui.category_back_item.lore",
        Collections.singletonList("&7Return to category selection"));
    addDefaultUnlessAlternativePathExists("gui.category_back_item.slot", 47, "gui.category_back_item.slots");

    // Next Page item
    config.addDefault("gui.next_page.material", "PAPER");
    config.addDefault("gui.next_page.data", 0);
    config.addDefault("gui.next_page.displayname", "&6Next page: %page%");
    config.addDefault("gui.next_page.lore",
        Collections.singletonList("&7Move to the next page"));
    addDefaultUnlessAlternativePathExists("gui.next_page.slot", 53, "gui.next_page.slots");

    // Previous Page item
    config.addDefault("gui.previous_page.material", "PAPER");
    config.addDefault("gui.previous_page.data", 0);
    config.addDefault("gui.previous_page.displayname", "&6Previous page: %page%");
    config.addDefault("gui.previous_page.lore",
        Collections.singletonList("&7Move to the previous page"));
    addDefaultUnlessAlternativePathExists("gui.previous_page.slot", 45, "gui.previous_page.slots");

    // Category properties
    config.addDefault("categories.all.order", 0);
    config.addDefault("categories.all.item", "BOOK");
    config.addDefault("categories.all.name", "&6All Tags");
    config.addDefault("categories.all.lore",
        Collections.singletonList("&7Click to view all available tags"));
    config.addDefault("categories.all.gui_name", "&6All Tags");

    config.addDefault("categories.general.order", 1);
    config.addDefault("categories.general.item", "NAME_TAG");
    config.addDefault("categories.general.name", "&6General");
    config.addDefault("categories.general.lore",
        Collections.singletonList("&7Click to view general tags"));
    config.addDefault("categories.general.gui_name", "&6General tags");

    if (!config.contains("deluxetags")) {
      config.set("deluxetags.example.order", 1);
      config.set("deluxetags.example.category", DeluxeTagCategory.GENERAL_IDENTIFIER);
      config.set("deluxetags.example.tag", "&8[&bDeluxeTags&8]");
      config.set("deluxetags.example.description", "&cAwarded for using DeluxeTags!");
      config.set("deluxetags.example.permission", "deluxetags.tag.example");
    }

    migrateTagCategories();

    config.options().copyDefaults(true);
    plugin.saveConfig();
    plugin.reloadConfig();
    config = plugin.getConfig();
  }

  /**
   * Adds config path unless alternative path is set.
   * @param path path to set
   * @param value path value
   * @param alternativePath alternative path that means original path shouldn't be set
   */
  private void addDefaultUnlessAlternativePathExists(String path, Object value, String alternativePath) {
    if (!config.contains(alternativePath)) {
      config.addDefault(path, value);
    }
  }

  private void migrateTagCategories() {
    ConfigurationSection deluxetags = config.getConfigurationSection("deluxetags");
    if (deluxetags == null) {
      return;
    }

    for (String identifier : deluxetags.getKeys(false)) {
      String categoryPath = "deluxetags." + identifier + ".category";
      String category = config.getString(categoryPath);
      if (category == null || category.trim().isEmpty() || category.equalsIgnoreCase(DeluxeTagCategory.ALL_IDENTIFIER)) {
        config.set(categoryPath, DeluxeTagCategory.GENERAL_IDENTIFIER);
      }
    }
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

  public boolean useMiniMessage() {
    return config.getBoolean("use_minimessage", false);
  }

  public boolean loadTagOnJoin() {
    return config.getBoolean("load_tag_on_join");
  }

  public boolean forceTags() {
    return config.getBoolean("force_tags");
  }

  public List<Integer> getTagSlots() {
    // Conforms to existing code style but isn't as efficient as it could be due to processing on each call
    return loadSlots("gui.tag_slots", "gui.tag_slots");
  }

  public String getMenuName() {
    return config.getString("gui.name", "&6Available tags&f: &6%deluxetags_amount%");
  }

  public int getMenuSize() {
    return config.getInt("gui.size", 54);
  }

  public DisplayItem loadGuiItem(@NotNull final ItemType type) {
    Material material;
    String displayName;
    List<String> lore;
    List<Integer> slots;
    short data;

    String basePath = "gui." + type.name().toLowerCase();

    try {
      material = XMaterial.matchXMaterial(config.getString(basePath + ".material").toUpperCase()).get().parseMaterial();
    } catch (Exception e) {
      material = type.getFallbackMaterial();
    }

    try {
      data = Short.parseShort(config.getString(basePath + ".data", "0"));
    } catch (Exception e) {
      data = 0;
    }

    displayName = config.getString(basePath + ".displayname");
    lore = config.getStringList(basePath + ".lore");

    slots = loadSlots(basePath + ".slots", basePath + ".slot");

    ItemStack itemStack = ItemUtils.createItem(material, data, displayName, lore);
    ItemMeta itemMeta = itemStack.getItemMeta();

    // Sets Item Model
    if (VersionHelper.HAS_TOOLTIP_STYLE && config.isSet(basePath + ".item_model")) {
      NamespacedKey itemModel = NamespacedKey.fromString(config.getString(basePath + ".item_model"));
      if (itemModel != null) {
        itemMeta.setItemModel(itemModel);
      }
    }

    // Sets Model Data
    if (VersionHelper.IS_CUSTOM_MODEL_DATA && config.isSet(basePath + ".model_data")) {
      try {
        final int modelData = config.getInt(basePath + ".model_data");
        itemMeta.setCustomModelData(modelData);
      } catch (final Exception ignored) {
      }
    }

    // Sets Model Data Component
    if (VersionHelper.IS_CUSTOM_MODEL_DATA_COMPONENT && config.isConfigurationSection(basePath + ".model_data_component")) {
      CustomModelDataComponent configCustomModelDataComponent = CustomModelDataComponent.builder()
        .colors(config.getStringList(basePath + ".model_data_component.colors"))
        .flags(config.getStringList(basePath + ".model_data_component.flags"))
        .floats(config.getStringList(basePath + ".model_data_component.floats"))
        .strings(config.getStringList(basePath + ".model_data_component.strings"));

      itemMeta.setCustomModelDataComponent(parseCustomModelDataComponent(configCustomModelDataComponent, itemMeta.getCustomModelDataComponent()));
    }

    itemStack.setItemMeta(itemMeta);

    return material == null ? null : new DisplayItem(type, itemStack, slots);
  }

  public int loadCategories() {
    int loaded = 0;
    boolean loadedAllCategory = false;
    boolean loadedRealCategory = false;

    ConfigurationSection categories = config.getConfigurationSection("categories");
    Set<String> keys = categories != null ? categories.getKeys(false) : Collections.emptySet();

    for (String identifier : keys) {
      if (identifier.trim().isEmpty()) {
        continue;
      }

      String basePath = "categories." + identifier;
      boolean allCategory = identifier.equalsIgnoreCase(DeluxeTagCategory.ALL_IDENTIFIER);

      Material fallback = allCategory ? XMaterial.BOOK.parseMaterial() : XMaterial.NAME_TAG.parseMaterial();
      Material material = loadCategoryMaterial(config.getString(basePath + ".item"), fallback);
      String defaultName = allCategory ? "&6All Tags" : "&6" + identifier;
      String name = config.getString(basePath + ".name", defaultName);
      List<String> lore = config.getStringList(basePath + ".lore");
      String guiName = config.getString(basePath + ".gui_name", name);
      int order = config.getInt(basePath + ".order", allCategory ? 0 : loaded + 1);

      plugin.getTagsHandler().loadCategory(new DeluxeTagCategory(identifier, order, material, name, lore, guiName, allCategory));
      if (allCategory) {
        loadedAllCategory = true;
      } else {
        loadedRealCategory = true;
      }
      loaded++;
    }

    if (!loadedAllCategory) {
      plugin.getTagsHandler().loadCategory(new DeluxeTagCategory(
          DeluxeTagCategory.ALL_IDENTIFIER,
          0,
          XMaterial.BOOK.parseMaterial(),
          "&6All Tags",
          Collections.singletonList("&7Click to view all available tags"),
          "&6All Tags",
          true
      ));
      loaded++;
    }

    if (!loadedRealCategory) {
      plugin.getTagsHandler().loadCategory(new DeluxeTagCategory(
          DeluxeTagCategory.GENERAL_IDENTIFIER,
          1,
          XMaterial.NAME_TAG.parseMaterial(),
          "&6General",
          Collections.singletonList("&7Click to view general tags"),
          "&6General tags",
          false
      ));
      loaded++;
    }

    return loaded;
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

      String category = normalizeTagCategory(config.getString("deluxetags." + identifier + ".category", DeluxeTagCategory.GENERAL_IDENTIFIER));
      if (!category.equalsIgnoreCase(DeluxeTagCategory.GENERAL_IDENTIFIER) && !plugin.getTagsHandler().hasCategory(category)) {
        plugin.getLogger().log(Level.INFO, "Could not find category: " + category + " for tag: " + identifier + ". Using general instead.");
        category = DeluxeTagCategory.GENERAL_IDENTIFIER;
      }

      DeluxeTag t = new DeluxeTag(priority, identifier, tag, description, category);
      t.setPermission(config.getString("deluxetags." + identifier + ".permission", "deluxetags.tag." + identifier));
      plugin.getTagsHandler().loadTag(t);
      loaded++;
    }

    return loaded;
  }

  public void saveTag(DeluxeTag tag) {
    saveTag(tag.getPriority(), tag.getIdentifier(), tag.getDisplayTag(), tag.getDescription(), tag.getPermission(), tag.getCategory());
  }

  public void saveTag(int priority, String identifier, String tag, String description, String permission) {
    saveTag(priority, identifier, tag, description, permission, getSavedCategory(identifier));
  }

  public void saveTag(int priority, String identifier, String tag, String description, String permission, String category) {
    config.set("deluxetags." + identifier + ".order", priority);
    config.set("deluxetags." + identifier + ".category", normalizeTagCategory(category));
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

  private Material loadCategoryMaterial(String materialName, Material fallback) {
    if (fallback == null) {
      fallback = Material.NAME_TAG;
    }

    if (materialName == null || materialName.trim().isEmpty()) {
      return fallback;
    }

    try {
      Material material = XMaterial.matchXMaterial(materialName.toUpperCase(Locale.ROOT)).get().parseMaterial();
      return material == null ? fallback : material;
    } catch (Exception e) {
      return fallback;
    }
  }

  private String normalizeTagCategory(String category) {
    if (category == null || category.trim().isEmpty() || category.equalsIgnoreCase(DeluxeTagCategory.ALL_IDENTIFIER)) {
      return DeluxeTagCategory.GENERAL_IDENTIFIER;
    }

    return category;
  }

  private String getSavedCategory(String identifier) {
    return normalizeTagCategory(config.getString("deluxetags." + identifier + ".category", DeluxeTagCategory.GENERAL_IDENTIFIER));
  }

  private List<Integer> loadSlots(String slotsPath, String slotPath) {
    List<Integer> slotsList = new ArrayList<>();
    if (config.contains(slotsPath) && config.isList(slotsPath)) {
      List<String> confSlots = config.getStringList(slotsPath);
      for (String slot : confSlots) {
        String[] values = slot.split("-", 2);
        if (values.length == 2) {
          for (int i = Integer.parseInt(values[0]); i <= Integer.parseInt(values[1]); i++) {
            slotsList.add(i);
          }
        } else {
          slotsList.add(Integer.parseInt(slot));
        }
      }
    } else if (config.contains(slotPath) && config.isInt(slotPath)) {
      slotsList.add(config.getInt(slotPath));
    }

    return slotsList;
  }

  private @NotNull org.bukkit.inventory.meta.components.CustomModelDataComponent parseCustomModelDataComponent(
    @NotNull final CustomModelDataComponent unparsedComponent,
    @NotNull final org.bukkit.inventory.meta.components.CustomModelDataComponent component
  ) {
    if (!unparsedComponent.colors().isEmpty()) {
      final List<Color> colors = unparsedComponent.colors()
        .stream()
        .map(this::parseRGBColor)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
      component.setColors(colors);
    }

    if (!unparsedComponent.flags().isEmpty()) {
      final List<Boolean> flags = unparsedComponent.flags()
        .stream()
        .map(Boolean::parseBoolean)
        .collect(Collectors.toList());
      component.setFlags(flags);
    }

    if (!unparsedComponent.floats().isEmpty()) {
      final List<Float> floats = unparsedComponent.floats()
        .stream()
        .map(Float::parseFloat)
        .collect(Collectors.toList());
      component.setFloats(floats);
    }

    if (!unparsedComponent.strings().isEmpty()) {
      final List<String> strings = unparsedComponent.strings()
        .stream()
        .collect(Collectors.toList());
      component.setStrings(strings);
    }

    return component;
  }

  private @Nullable Color parseRGBColor(@NotNull final String input) {
    final Color color = StringUtils.parseRGBColor(input);
    if (color == null) {
      plugin.getLogger().warning("Invalid RGB color found: " + input);
    }
    return color;
  }
}
