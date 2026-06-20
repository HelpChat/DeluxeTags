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

  private static final String LEGACY_TAG_AVAILABILITY_PLACEHOLDER_PATH = "tag_availability_placeholder";
  private static final String TAG_AVAILABILITY_PLACEHOLDER_PATH = "gui.tag_availability_placeholder";
  private static final List<String> TOP_LEVEL_CONFIG_ORDER = Collections.unmodifiableList(Arrays.asList(
      "use_minimessage",
      "force_tags",
      "check_updates",
      "legacy_hex",
      "papi_chat",
      "format_chat",
      "load_tag_on_join",
      "gui",
      "categories",
      "deluxetags"
  ));

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
            + "\n    displayname: '&6Tag&f: &6%deluxetags_identifier%'"
            + "\n    description:"
            + "\n      - 'This tag is awarded by getting VIP'"
            + "\n      - '%deluxetags_available%'"
            + "\n    item: NAME_TAG"
            + "\n    data: 0"
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
            + "\n%deluxetags_amount% - display the amount of tags a player has access to"
            + "\n"
            + "\nPlaceholders for the tags GUI:"
            + "\n"
            + "\n%deluxetags_available% - display whether the player can select the displayed tag"
            + "\n%deluxetags_category_amount% - display the amount of tags the player has access to in the current category"
            + "\n"
            + "\nTag availability placeholder text:"
            + "\n"
            + "\ngui:"
            + "\n  tag_availability_placeholder:"
            + "\n    has_permission: '&aTag unlocked! Click to select'"
            + "\n    no_permission: '&cTag locked '");

    addDefault("use_minimessage", false);
    addDefault("force_tags", false);
    addDefault("check_updates", true);
    addDefault("legacy_hex", false);
    addDefault("papi_chat", true);
    addDefault("format_chat.enabled", false);
    addDefault("format_chat.format", "{deluxetags_tag} <%1$s> %2$s");
    if (config.isSet("force_tag_on_join")) {
      config.set("force_tag_on_join", null);
    }
    addDefault("load_tag_on_join", true);

    migrateTagAvailabilityPlaceholder(config);

    // GUI properties
    addDefault("gui.tag_availability_placeholder.has_permission", "&aTag unlocked! Click to select");
    addDefault("gui.tag_availability_placeholder.no_permission", "&cTag locked ");
    addDefault("gui.name", "&6Available tags&f: &6%deluxetags_amount%");
    addDefault("gui.size", 54);
    addDefault("gui.tag_slots", Collections.singletonList("0-35"));

    // Tag Visible item
    addDefault("gui.tag_visible_item.material", "BARRIER");
    addDefault("gui.tag_visible_item.data", 0);

    // Divider item
    addDefault("gui.divider_item.material", "BLACK_STAINED_GLASS_PANE");
    addDefault("gui.divider_item.data", 0);
    addDefault("gui.divider_item.displayname", "");
    addDefault("gui.divider_item.lore",
        Collections.emptyList());
    addDefaultUnlessAlternativePathExists("gui.divider_item.slots", Collections.singletonList("36-44"), "gui.divider_item.slot");

    // Has Tag item
    addDefault("gui.has_tag_item.material", "PLAYER_HEAD");
    addDefault("gui.has_tag_item.data", 0);
    addDefault("gui.has_tag_item.displayname", "&eCurrent tag&f: &6%deluxetags_identifier%");
    addDefault("gui.has_tag_item.lore",
        Arrays.asList("%deluxetags_tag%", "Click to remove your current tag"));
    addDefaultUnlessAlternativePathExists("gui.has_tag_item.slot", 49, "gui.has_tag_item.slots");

    // No Tag item
    addDefault("gui.no_tag_item.material", "PLAYER_HEAD");
    addDefault("gui.no_tag_item.data", 0);
    addDefault("gui.no_tag_item.displayname", "&cYou don't have a tag set!");
    addDefault("gui.no_tag_item.lore",
        Collections.singletonList("&7Click a tag above to select one!"));
    addDefaultUnlessAlternativePathExists("gui.no_tag_item.slot", 49,  "gui.no_tag_item.slots");

    // Exit item
    addDefault("gui.exit_item.material", "IRON_DOOR");
    addDefault("gui.exit_item.data", 0);
    addDefault("gui.exit_item.displayname", "&cClick to exit");
    addDefault("gui.exit_item.lore",
        Collections.singletonList("&7Exit the tags menu"));
    addDefaultUnlessAlternativePathExists("gui.exit_item.slots", Arrays.asList(48, 50), "gui.exit_item.slot");

    // Category Back item
    addDefault("gui.category_back_item.material", "ARROW");
    addDefault("gui.category_back_item.data", 0);
    addDefault("gui.category_back_item.displayname", "&6Back to categories");
    addDefault("gui.category_back_item.lore",
        Collections.singletonList("&7Return to category selection"));
    addDefaultUnlessAlternativePathExists("gui.category_back_item.slot", 47, "gui.category_back_item.slots");

    // Next Page item
    addDefault("gui.next_page.material", "PAPER");
    addDefault("gui.next_page.data", 0);
    addDefault("gui.next_page.displayname", "&6Next page: %page%");
    addDefault("gui.next_page.lore",
        Collections.singletonList("&7Move to the next page"));
    addDefaultUnlessAlternativePathExists("gui.next_page.slot", 53, "gui.next_page.slots");

    // Previous Page item
    addDefault("gui.previous_page.material", "PAPER");
    addDefault("gui.previous_page.data", 0);
    addDefault("gui.previous_page.displayname", "&6Previous page: %page%");
    addDefault("gui.previous_page.lore",
        Collections.singletonList("&7Move to the previous page"));
    addDefaultUnlessAlternativePathExists("gui.previous_page.slot", 45, "gui.previous_page.slots");

    // Category properties
    addDefault("categories.all.order", 0);
    addDefault("categories.all.item", "BOOK");
    addDefault("categories.all.name", "&6All Tags");
    addDefault("categories.all.lore",
        Collections.singletonList("&7Click to view all available tags"));
    addDefault("categories.all.gui_name", "&6All Tags");

    addDefault("categories.general.order", 1);
    addDefault("categories.general.item", "NAME_TAG");
    addDefault("categories.general.name", "&6General");
    addDefault("categories.general.lore",
        Collections.singletonList("&7Click to view general tags"));
    addDefault("categories.general.gui_name", "&6General tags");

    if (!config.contains("deluxetags")) {
      config.set("deluxetags.example.order", 1);
      config.set("deluxetags.example.category", DeluxeTagCategory.GENERAL_IDENTIFIER);
      config.set("deluxetags.example.tag", "&8[&bDeluxeTags&8]");
      config.set("deluxetags.example.displayname", DeluxeTag.DEFAULT_DISPLAY_NAME);
      config.set("deluxetags.example.description", Arrays.asList("&cAwarded for using DeluxeTags!", "%deluxetags_available%"));
      config.set("deluxetags.example.item", "NAME_TAG");
      config.set("deluxetags.example.data", 0);
      config.set("deluxetags.example.permission", "deluxetags.tag.example");
    }

    migrateTags(config);
    normalizeTopLevelOrder(config);
    applySectionComments(config);

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
    if (!config.isSet(alternativePath)) {
      addDefault(path, value);
    }
  }

  private void addDefault(String path, Object value) {
    config.addDefault(path, value);
    if (!config.isSet(path)) {
      config.set(path, value);
    }
  }

  static void applySectionComments(FileConfiguration config) {
    config.setComments("use_minimessage", Collections.singletonList("Main Options"));
    config.setComments("gui", Arrays.asList(null, "GUI layout and buttons"));
    config.setComments("categories", Arrays.asList(null, "Tag category menus"));
    config.setComments("deluxetags", Arrays.asList(null, "Tags"));
  }

  static void migrateTagAvailabilityPlaceholder(FileConfiguration config) {
    ConfigurationSection legacySection = config.getConfigurationSection(LEGACY_TAG_AVAILABILITY_PLACEHOLDER_PATH);
    if (legacySection == null) {
      if (config.isSet(LEGACY_TAG_AVAILABILITY_PLACEHOLDER_PATH)) {
        config.set(LEGACY_TAG_AVAILABILITY_PLACEHOLDER_PATH, null);
      }
      return;
    }

    for (String key : legacySection.getKeys(false)) {
      String newPath = TAG_AVAILABILITY_PLACEHOLDER_PATH + "." + key;
      if (!config.isSet(newPath)) {
        config.set(newPath, copyConfigValue(legacySection.get(key)));
      }
    }

    config.set(LEGACY_TAG_AVAILABILITY_PLACEHOLDER_PATH, null);
  }

  static void normalizeTopLevelOrder(FileConfiguration config) {
    Map<String, Object> values = new LinkedHashMap<>();
    for (String key : config.getKeys(false)) {
      values.put(key, copyConfigValue(config.get(key)));
    }

    if (values.isEmpty()) {
      return;
    }

    for (String key : new ArrayList<>(values.keySet())) {
      config.set(key, null);
    }

    Set<String> restoredKeys = new HashSet<>();
    for (String key : TOP_LEVEL_CONFIG_ORDER) {
      if (values.containsKey(key)) {
        restoreConfigValue(config, key, values.get(key));
        restoredKeys.add(key);
      }
    }

    for (Map.Entry<String, Object> entry : values.entrySet()) {
      if (!restoredKeys.contains(entry.getKey())) {
        restoreConfigValue(config, entry.getKey(), entry.getValue());
      }
    }
  }

  private static Object copyConfigValue(Object value) {
    if (value instanceof ConfigurationSection) {
      return copyConfigSection((ConfigurationSection) value);
    }

    return value;
  }

  private static Map<String, Object> copyConfigSection(ConfigurationSection section) {
    Map<String, Object> values = new LinkedHashMap<>();
    for (String key : section.getKeys(false)) {
      values.put(key, copyConfigValue(section.get(key)));
    }
    return values;
  }

  @SuppressWarnings("unchecked")
  private static void restoreConfigValue(ConfigurationSection section, String path, Object value) {
    if (value instanceof Map) {
      section.createSection(path, (Map<?, ?>) value);
      return;
    }

    section.set(path, value);
  }

  static void migrateTags(FileConfiguration config) {
    ConfigurationSection deluxetags = config.getConfigurationSection("deluxetags");
    if (deluxetags == null) {
      return;
    }

    String legacyDisplayName = getLegacyTagItemDisplayName(config);
    List<String> legacyLore = getLegacyTagItemLore(config);
    Material legacyMaterial = getLegacyTagItemMaterial(config);
    short legacyData = getLegacyTagItemData(config);

    for (String identifier : deluxetags.getKeys(false)) {
      String basePath = "deluxetags." + identifier;
      String categoryPath = basePath + ".category";
      String category = config.getString(categoryPath);
      if (category == null || category.trim().isEmpty() || category.equalsIgnoreCase(DeluxeTagCategory.ALL_IDENTIFIER)) {
        config.set(categoryPath, DeluxeTagCategory.GENERAL_IDENTIFIER);
      }

      String descriptionPath = basePath + ".description";
      List<String> description = loadTagDescriptionLines(config, basePath);
      if (!legacyLore.isEmpty()) {
        description = expandLegacyTagLore(legacyLore, description);
      } else if (!config.isList(descriptionPath)) {
        description = new ArrayList<>(description);
      }
      if (!config.isList(descriptionPath) || !legacyLore.isEmpty()) {
        config.set(descriptionPath, description);
      }

      if (!config.contains(basePath + ".displayname")) {
        config.set(basePath + ".displayname", legacyDisplayName);
      }

      if (!config.contains(basePath + ".item")) {
        config.set(basePath + ".item", legacyMaterial == null ? "NAME_TAG" : legacyMaterial.name());
      }

      if (!config.contains(basePath + ".data")) {
        config.set(basePath + ".data", (int) legacyData);
      }
    }

    removeLegacyTagItemDefaults(config);
  }

  private static void removeLegacyTagItemDefaults(FileConfiguration config) {
    Material tagVisibleMaterial = getTagVisibleMaterial(config);
    short tagVisibleData = getTagVisibleData(config);

    config.set("gui.tag_select_item", null);
    config.set("gui.tag_visible_item", null);
    config.set("gui.tag_visible_item.material", tagVisibleMaterial.name());
    config.set("gui.tag_visible_item.data", (int) tagVisibleData);
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

  public String getTagAvailabilityPlaceholder(boolean hasPermission) {
    String path = TAG_AVAILABILITY_PLACEHOLDER_PATH + (hasPermission ? ".has_permission" : ".no_permission");
    String legacyPath = LEGACY_TAG_AVAILABILITY_PLACEHOLDER_PATH + (hasPermission ? ".has_permission" : ".no_permission");
    String fallback = hasPermission ? "&aTag unlocked! Click to select" : "&cTag locked ";
    return config.getString(path, config.getString(legacyPath, fallback));
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

  public Material getTagVisibleMaterial() {
    return getTagVisibleMaterial(config);
  }

  private static Material getTagVisibleMaterial(FileConfiguration config) {
    return loadMaterial(config.getString("gui.tag_visible_item.material"), XMaterial.BARRIER.parseMaterial());
  }

  public short getTagVisibleData() {
    return getTagVisibleData(config);
  }

  private static short getTagVisibleData(FileConfiguration config) {
    return loadData(config, "gui.tag_visible_item.data", (short) 0);
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
      String basePath = "deluxetags." + identifier;
      String tag = config.getString(basePath + ".tag");
      if (tag == null) {
        plugin.getLogger().log(Level.INFO, "Could not load tag: " + identifier + " because it does not have a display set.");
        continue;
      }
      String description = loadTagDescription(basePath);
      String displayName = config.getString(basePath + ".displayname", DeluxeTag.DEFAULT_DISPLAY_NAME);

      if (!config.contains(basePath + ".order")) {
        plugin.getLogger().log(Level.INFO, "Could not load tag: " + identifier + " because it does not have an order set.");
        continue;
      }
      int priority = config.getInt(basePath + ".order");

      String category = normalizeTagCategory(config.getString(basePath + ".category", DeluxeTagCategory.GENERAL_IDENTIFIER));
      if (!category.equalsIgnoreCase(DeluxeTagCategory.GENERAL_IDENTIFIER) && !plugin.getTagsHandler().hasCategory(category)) {
        plugin.getLogger().log(Level.INFO, "Could not find category: " + category + " for tag: " + identifier + ". Using general instead.");
        category = DeluxeTagCategory.GENERAL_IDENTIFIER;
      }

      Material material = loadMaterial(config.getString(basePath + ".item"), XMaterial.NAME_TAG.parseMaterial());
      short data = loadData(basePath + ".data", (short) 0);

      DeluxeTag t = new DeluxeTag(priority, identifier, tag, description, category, displayName, material, data);
      t.setPermission(config.getString(basePath + ".permission", "deluxetags.tag." + identifier));
      plugin.getTagsHandler().loadTag(t);
      loaded++;
    }

    return loaded;
  }

  public void saveTag(DeluxeTag tag) {
    saveTag(tag.getPriority(), tag.getIdentifier(), tag.getDisplayTag(), tag.getDescription(), tag.getPermission(), tag.getCategory(), tag.getDisplayName(), tag.getMaterial(), tag.getData());
  }

  public void saveTag(int priority, String identifier, String tag, String description, String permission) {
    saveTag(priority, identifier, tag, description, permission, getSavedCategory(identifier), getSavedDisplayName(identifier), getSavedMaterial(identifier), getSavedData(identifier));
  }

  public void saveTag(int priority, String identifier, String tag, String description, String permission, String category) {
    saveTag(priority, identifier, tag, description, permission, category, getSavedDisplayName(identifier), getSavedMaterial(identifier), getSavedData(identifier));
  }

  public void saveTag(int priority, String identifier, String tag, String description, String permission, String category, String displayName, Material material, short data) {
    config.set("deluxetags." + identifier + ".order", priority);
    config.set("deluxetags." + identifier + ".category", normalizeTagCategory(category));
    config.set("deluxetags." + identifier + ".tag", tag);
    if (description == null) {
      description = "&fDescription for tag " + identifier;
    }
    if (material == null) {
      material = XMaterial.NAME_TAG.parseMaterial();
    }
    config.set("deluxetags." + identifier + ".displayname", displayName == null ? DeluxeTag.DEFAULT_DISPLAY_NAME : displayName);
    config.set("deluxetags." + identifier + ".description", serializeDescription(description));
    config.set("deluxetags." + identifier + ".item", material == null ? "NAME_TAG" : material.name());
    config.set("deluxetags." + identifier + ".data", (int) data);
    config.set("deluxetags." + identifier + ".permission", permission);
    plugin.saveConfig();
  }

  public void removeTag(String identifier) {
    config.set("deluxetags." + identifier, null);
    plugin.saveConfig();
  }

  private String loadTagDescription(String basePath) {
    return String.join("\n", loadTagDescriptionLines(basePath));
  }

  private List<String> loadTagDescriptionLines(String basePath) {
    return loadTagDescriptionLines(config, basePath);
  }

  private static List<String> loadTagDescriptionLines(FileConfiguration config, String basePath) {
    String descriptionPath = basePath + ".description";
    if (config.isList(descriptionPath)) {
      return new ArrayList<>(config.getStringList(descriptionPath));
    }

    return serializeDescription(config.getString(descriptionPath, "&f"));
  }

  private static List<String> serializeDescription(String description) {
    return Arrays.asList(description.replace("\r\n", "\n").replace("\r", "\n").split("\n", -1));
  }

  private static List<String> expandLegacyTagLore(List<String> legacyLore, List<String> description) {
    List<String> expanded = new ArrayList<>();
    String joinedDescription = String.join("\n", description);

    for (String line : legacyLore) {
      if (line.equals("%deluxetags_description%") || line.equals("{deluxetags_description}")) {
        expanded.addAll(description);
        continue;
      }

      if (line.contains("%deluxetags_description%") || line.contains("{deluxetags_description}")) {
        String expandedLine = line
            .replace("%deluxetags_description%", joinedDescription)
            .replace("{deluxetags_description}", joinedDescription);
        expanded.addAll(serializeDescription(expandedLine));
        continue;
      }

      expanded.add(line);
    }

    return expanded;
  }

  private static String getLegacyTagItemDisplayName(FileConfiguration config) {
    return config.getString("gui.tag_select_item.displayname", DeluxeTag.DEFAULT_DISPLAY_NAME);
  }

  private static List<String> getLegacyTagItemLore(FileConfiguration config) {
    if (!config.isList("gui.tag_select_item.lore")) {
      return Collections.emptyList();
    }

    return config.getStringList("gui.tag_select_item.lore");
  }

  private static Material getLegacyTagItemMaterial(FileConfiguration config) {
    return loadMaterial(config.getString("gui.tag_select_item.material"), XMaterial.NAME_TAG.parseMaterial());
  }

  private static short getLegacyTagItemData(FileConfiguration config) {
    return loadData(config, "gui.tag_select_item.data", (short) 0);
  }

  private String getSavedDisplayName(String identifier) {
    return config.getString("deluxetags." + identifier + ".displayname", DeluxeTag.DEFAULT_DISPLAY_NAME);
  }

  private Material getSavedMaterial(String identifier) {
    return loadMaterial(config.getString("deluxetags." + identifier + ".item"), XMaterial.NAME_TAG.parseMaterial());
  }

  private short getSavedData(String identifier) {
    return loadData("deluxetags." + identifier + ".data", (short) 0);
  }

  private static Material loadCategoryMaterial(String materialName, Material fallback) {
    return loadMaterial(materialName, fallback);
  }

  private static Material loadMaterial(String materialName, Material fallback) {
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

  private short loadData(String path, short fallback) {
    return loadData(config, path, fallback);
  }

  private static short loadData(FileConfiguration config, String path, short fallback) {
    try {
      if (config.isInt(path)) {
        return (short) config.getInt(path);
      }

      return Short.parseShort(config.getString(path, Short.toString(fallback)));
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
