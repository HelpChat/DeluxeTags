package me.clip.deluxetags.gui;

import java.util.*;

import me.clip.deluxetags.DeluxeTags;
import me.clip.deluxetags.config.Lang;
import me.clip.deluxetags.tags.DeluxeTag;
import me.clip.deluxetags.tags.DeluxeTagCategory;
import me.clip.deluxetags.utils.ItemUtils;
import me.clip.deluxetags.utils.MsgUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GUIHandler implements Listener {

    private final DeluxeTags plugin;

    public GUIHandler(DeluxeTags identifier) {
        this.plugin = identifier;
    }

    private void sms(Player p, String message) {
        for (String line : MsgUtils.color(message).split("\\\\n")) {
            p.sendMessage(line);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();

        if (!TagGUI.hasGUI(p)) {
            return;
        }

        TagGUI gui = TagGUI.getGUI(p);
        if (gui == null) {
            return;
        }

        e.setCancelled(true);

        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || clicked.getType().equals(Material.AIR)) {
            return;
        }

        int slot = e.getRawSlot();

        ItemType clickedItemType = gui.getClickedItemType(slot);
        switch (clickedItemType) {
            case UNKNOWN:
                break;
            case EXIT_ITEM:
                TagGUI.close(p);
                p.closeInventory();
                break;
            case NO_TAG_ITEM:
                TagGUI.close(p);
                p.closeInventory();
                break;
            case NEXT_PAGE:
                if (gui.isCategoryMenu()) {
                    openCategoryMenu(p, gui.getPage()+1);
                } else {
                    openTagMenu(p, gui.getCategoryIdentifier(), gui.getPage()+1);
                }
                break;
            case DIVIDER_ITEM:
                break;
            case CATEGORY_BACK_ITEM:
                openCategoryMenu(p, 1);
                break;
            case HAS_TAG_ITEM:
                final DeluxeTag currentTag = plugin.getTagsHandler().getPlayerActiveTag(p);
                if (currentTag == null || currentTag.getDisplayTag(p).isEmpty() || plugin.getTagsHandler().isUsingDefaultTag(p) || plugin.getTagsHandler().isUsingForcedTag(p)) {
                    p.updateInventory();
                    return;
                }

                TagGUI.close(p);
                p.closeInventory();

                plugin.getTagsHandler().setPlayerTag(p, plugin.getDummyTag());
                plugin.removeSavedTag(p.getUniqueId().toString());

                sms(p, Lang.GUI_TAG_DISABLED.getConfigValue(null));
                p.updateInventory();
                break;
            case PREVIOUS_PAGE:
                if (gui.isCategoryMenu()) {
                    openCategoryMenu(p, gui.getPage()-1);
                } else {
                    openTagMenu(p, gui.getCategoryIdentifier(), gui.getPage()-1);
                }
                break;
            case TAG_SELECT_ITEM:
                if (gui.isCategoryMenu()) {
                    Map<Integer, String> categories;
                    try {
                        categories = gui.getCategories();
                    } catch (NullPointerException ex) {
                        TagGUI.close(p);
                        p.closeInventory();
                        return;
                    }

                    if (categories.isEmpty()) {
                        TagGUI.close(p);
                        p.closeInventory();
                        return;
                    }

                    String categoryId = categories.get(slot);
                    if (categoryId == null || categoryId.isEmpty()) {
                        TagGUI.close(p);
                        p.closeInventory();
                        return;
                    }

                    openTagMenu(p, categoryId, 1);
                    break;
                }

                Map<Integer, String> tags;
                try {
                    tags = gui.getTags();
                } catch (NullPointerException ex) {
                    TagGUI.close(p);
                    p.closeInventory();
                    return;
                }

                if (tags.isEmpty()) {
                    TagGUI.close(p);
                    p.closeInventory();
                    return;
                }

                String id = tags.get(slot);
                if (id == null || id.isEmpty()) {
                    TagGUI.close(p);
                    p.closeInventory();
                    return;
                }

                DeluxeTag selectedTag = plugin.getTagsHandler().getTagByIdentifier(id);
                if (selectedTag == null) {
                    return;
                }

                if (!p.hasPermission(selectedTag.getPermission())) {
                    sms(p, Lang.CMD_NO_PERMS.getConfigValue(new String[]{
                      "deluxetags.tag." + id
                    }));
                    TagGUI.close(p);
                    p.closeInventory();
                    return;
                }

                if (!plugin.getTagsHandler().setPlayerTag(p, selectedTag)) {
                    return;
                }

                TagGUI.close(p);
                p.closeInventory();

                selectedTag = plugin.getTagsHandler().getPlayerActiveTag(p);
                final String displayName = selectedTag == null ? "" : selectedTag.getDisplayTag(p);

                sms(p, Lang.GUI_TAG_SELECTED.getConfigValue(new String[]{id, displayName}));

                plugin.saveTagIdentifier(p.getUniqueId().toString(), id);
                break;
            case TAG_VISIBLE_ITEM:
                break;
            default:
                break;
        }

    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player)) {
            return;
        }

        Player p = (Player) e.getPlayer();
        if (TagGUI.hasGUI(p)) {
            TagGUI.close(p);
        }
    }

    public boolean openMenu(Player p, int page) {
        List<DeluxeTagCategory> visibleCategories = plugin.getTagsHandler().getPlayerVisibleCategories(p);
        if (visibleCategories.isEmpty()) {
            return false;
        }

        if (visibleCategories.size() == 1) {
            return openTagMenu(p, visibleCategories.get(0).getIdentifier(), page);
        }

        return openCategoryMenu(p, page);
    }

    private boolean openCategoryMenu(Player p, int page) {
        List<DeluxeTagCategory> categories = plugin.getTagsHandler().getPlayerSelectableCategories(p);
        if (categories.isEmpty()) {
            return false;
        }

        GUIOptions options = plugin.getGuiOptions();

        List<Integer> tagSlots = options.getTagSlots();
        if (tagSlots.isEmpty()) {
            return false;
        }

        int pages = (int) Math.ceil(categories.size() / (double) tagSlots.size());
        page = clampPage(page, pages);
        boolean hasNextPage = page < pages;

        String title = prepareTitle(p, options.getMenuName(), page, hasNextPage);
        int menuSlots = options.getMenuSize();

        TagGUI gui = new TagGUI(title, page).setSlots(menuSlots);
        gui.setCategoryMenu(true);

        List<DeluxeTagCategory> pageCategories = getPageItems(categories, page, tagSlots.size());
        int categoryIndex = 0;
        Map<Integer, String> categorySlots = new HashMap<>();
        for (int tagSlot : tagSlots) {
            if (categoryIndex >= pageCategories.size()) {
                break;
            }

            DeluxeTagCategory category = pageCategories.get(categoryIndex);
            categorySlots.put(tagSlot, category.getIdentifier());

            DisplayItem categoryDisplayItem = new DisplayItem(
                ItemType.TAG_SELECT_ITEM,
                ItemUtils.createItem(category.getMaterial(), (short) 0, category.getName(), category.getLore()),
                Collections.singletonList(tagSlot)
            );
            ItemMeta categoryItemMeta = categoryDisplayItem.getItemStack().getItemMeta();
            if (categoryItemMeta != null) {
                categoryItemMeta.setDisplayName(plugin.setPlaceholders(p, replacePageNumbers(categoryDisplayItem.getName(), page, hasNextPage), null));
                categoryItemMeta.setLore(processLore(categoryDisplayItem.getLore(), p, null, page, hasNextPage));
                categoryDisplayItem.getItemStack().setItemMeta(categoryItemMeta);
            }
            gui.addDisplayItem(categoryDisplayItem);

            categoryIndex++;
        }
        gui.setCategories(categorySlots);

        addStaticMenuItems(gui, p, page, hasNextPage);
        gui.setPage(page);
        gui.openInventory(p);
        return true;
    }

    private boolean openTagMenu(Player p, String categoryIdentifier, int page) {
        if (categoryIdentifier == null || categoryIdentifier.isEmpty()) {
            categoryIdentifier = DeluxeTagCategory.ALL_IDENTIFIER;
        }

        if (!canOpenCategory(p, categoryIdentifier)) {
            return false;
        }

        List<String> ids = plugin.getTagsHandler().getPlayerVisibleTagIdentifiers(p, categoryIdentifier);
        if (ids.isEmpty()) {
            return false;
        }

        GUIOptions options = plugin.getGuiOptions();

        List<Integer> tagSlots = options.getTagSlots();
        if (tagSlots.isEmpty()) {
            return false;
        }

        int pages = (int) Math.ceil(ids.size() / (double) tagSlots.size());
        page = clampPage(page, pages);
        boolean hasNextPage = page < pages;

        DeluxeTagCategory category = plugin.getTagsHandler().getCategoryByIdentifier(categoryIdentifier);
        String title = prepareTitle(p, category != null ? category.getGuiName() : options.getMenuName(), page, hasNextPage);

        int menuSlots = options.getMenuSize();

        TagGUI gui = new TagGUI(title, page).setSlots(menuSlots);
        gui.setCategoryIdentifier(categoryIdentifier);

        ids = getPageItems(ids, page, tagSlots.size());

        int idIndex = 0;
        Map<Integer, String> tags = new HashMap<>();
        for (int tagSlot: tagSlots) {
            if (idIndex >= ids.size()) {
                break;
            }

            String id = ids.get(idIndex);

            tags.put(tagSlot, id);
            DeluxeTag tag = plugin.getTagsHandler().getTagByIdentifier(id);
            if (tag == null) {
                tag = plugin.getDummyTag();
            }

            // Adds Tag Item to Menu
            DisplayItem tagDisplayItem = new DisplayItem(tag.hasPermissionToUse(p) ? options.getTagSelectItem() : options.getTagVisibleItem());
            ItemMeta tagItemMeta = tagDisplayItem.getItemStack().getItemMeta();
            if (tagItemMeta != null) {
                tagItemMeta.setDisplayName(plugin.setPlaceholders(p, replacePageNumbers(tagDisplayItem.getName(), page, hasNextPage), tag));
                tagItemMeta.setLore(processLore(tagDisplayItem.getLore(), p, tag, page, hasNextPage));
                tagDisplayItem.getItemStack().setItemMeta(tagItemMeta);
            }
            tagDisplayItem.setSlots(Collections.singletonList(tagSlot));
            gui.addDisplayItem(tagDisplayItem);

            idIndex++;
        }
        gui.setTags(tags);

        addStaticMenuItems(gui, p, page, hasNextPage);
        gui.setPage(page);
        gui.openInventory(p);
        return true;
    }

    private void addStaticMenuItems(TagGUI gui, Player p, int page, boolean hasNextPage) {
        GUIOptions options = plugin.getGuiOptions();

        // Adds Divider Item to Menu
        DisplayItem dividerDisplayItem = new DisplayItem(options.getDividerItem());
        ItemMeta dividerItemMeta = dividerDisplayItem.getItemStack().getItemMeta();
        if (dividerItemMeta != null) {
            dividerItemMeta.setDisplayName(plugin.setPlaceholders(p, replacePageNumbers(dividerDisplayItem.getName(), page, hasNextPage), null));
            dividerItemMeta.setLore(processLore(dividerDisplayItem.getLore(), p, null, page, hasNextPage));
            dividerDisplayItem.getItemStack().setItemMeta(dividerItemMeta);
        }
        gui.addDisplayItem(dividerDisplayItem);

        // Gets Tag Item that represents Player's tag status (Tag equipped or not equipped)
        final DeluxeTag currentTag = plugin.getTagsHandler().getPlayerActiveTag(p);
        DisplayItem currentTagItem;

        if (currentTag == null || currentTag.getIdentifier().isEmpty()) {
            currentTagItem = options.getNoTagItem();
        } else {
            currentTagItem = options.getHasTagItem();
        }

        // Adds Info Item to Menu
        DisplayItem infoDisplayItem = new DisplayItem(currentTagItem);
        ItemMeta infoItemMeta = infoDisplayItem.getItemStack().getItemMeta();
        if (infoItemMeta != null) {
            infoItemMeta.setDisplayName(plugin.setPlaceholders(p, replacePageNumbers(infoDisplayItem.getName(), page, hasNextPage), null));
            infoItemMeta.setLore(processLore(infoDisplayItem.getLore(), p, null, page, hasNextPage));
            infoDisplayItem.getItemStack().setItemMeta(infoItemMeta);
        }
        gui.addDisplayItem(infoDisplayItem);

        // Adds Exit Item to Menu
        DisplayItem exitDisplayItem = new DisplayItem(options.getExitItem());
        ItemMeta exitItemMeta = exitDisplayItem.getItemStack().getItemMeta();
        if (exitItemMeta != null) {
            exitItemMeta.setDisplayName(plugin.setPlaceholders(p, replacePageNumbers(exitDisplayItem.getName(), page, hasNextPage), null));
            exitItemMeta.setLore(processLore(exitDisplayItem.getLore(), p, null, page, hasNextPage));
            exitDisplayItem.getItemStack().setItemMeta(exitItemMeta);
        }
        gui.addDisplayItem(exitDisplayItem);

        boolean showCategoryBack = !gui.isCategoryMenu() && shouldShowCategorySelector(p);
        if (showCategoryBack) {
            DisplayItem categoryBackDisplayItem = new DisplayItem(options.getCategoryBackItem());
            ItemMeta categoryBackItemMeta = categoryBackDisplayItem.getItemStack().getItemMeta();
            if (categoryBackItemMeta != null) {
                categoryBackItemMeta.setDisplayName(plugin.setPlaceholders(p, replacePageNumbers(categoryBackDisplayItem.getName(), page, hasNextPage), null));
                categoryBackItemMeta.setLore(processLore(categoryBackDisplayItem.getLore(), p, null, page, hasNextPage));
                categoryBackDisplayItem.getItemStack().setItemMeta(categoryBackItemMeta);
            }
            gui.addDisplayItem(categoryBackDisplayItem);
        }

        if (page > 1) {
            // Adds Previous Page Item to Menu
            DisplayItem previousPageDisplayItem = new DisplayItem(options.getPreviousPageItem());
            ItemMeta previousPageItemMeta = previousPageDisplayItem.getItemStack().getItemMeta();
            if (previousPageItemMeta != null) {
                previousPageItemMeta.setDisplayName(plugin.setPlaceholders(p, replacePageNumbers(previousPageDisplayItem.getName(), page, hasNextPage), null));
                previousPageItemMeta.setLore(processLore(previousPageDisplayItem.getLore(), p, null, page, hasNextPage));
                previousPageDisplayItem.getItemStack().setItemMeta(previousPageItemMeta);
            }
            gui.addDisplayItem(previousPageDisplayItem);
        }

        if (hasNextPage) {
            // Adds Next Page Item to Menu
            DisplayItem nextPageDisplayItem = new DisplayItem(options.getNextPageItem());
            ItemMeta nextPageItemMeta = nextPageDisplayItem.getItemStack().getItemMeta();
            if (nextPageItemMeta != null) {
                nextPageItemMeta.setDisplayName(plugin.setPlaceholders(p, replacePageNumbers(nextPageDisplayItem.getName(), page, hasNextPage), null));
                nextPageItemMeta.setLore(processLore(nextPageDisplayItem.getLore(), p, null, page, hasNextPage));
                nextPageDisplayItem.getItemStack().setItemMeta(nextPageItemMeta);
            }
            gui.addDisplayItem(nextPageDisplayItem);
        }

    }

    private boolean canOpenCategory(Player player, String categoryIdentifier) {
        if (categoryIdentifier.equalsIgnoreCase(DeluxeTagCategory.ALL_IDENTIFIER)) {
            return plugin.getTagsHandler().getPlayerVisibleCategories(player).size() >= 2;
        }

        for (DeluxeTagCategory category : plugin.getTagsHandler().getPlayerVisibleCategories(player)) {
            if (category.getIdentifier().equalsIgnoreCase(categoryIdentifier)) {
                return true;
            }
        }

        return false;
    }

    private boolean shouldShowCategorySelector(Player player) {
        return plugin.getTagsHandler().getPlayerVisibleCategories(player).size() >= 2;
    }

    private int clampPage(int page, int pages) {
        if (pages <= 0) {
            return 1;
        }

        if (page < 1) {
            return 1;
        }

        return Math.min(page, pages);
    }

    private String prepareTitle(Player player, String title, int page, boolean hasNextPage) {
        if (title == null) {
            title = "";
        }

        title = replacePageNumbers(plugin.setPlaceholders(player, title, null), page, hasNextPage);
        if (title.length() > 32) {
            title = title.substring(0, 31);
        }

        return title;
    }

    private <T> List<T> getPageItems(List<T> items, int page, int pageSize) {
        int startIndex = (page * pageSize) - pageSize;
        int endIndex = Math.min(startIndex + pageSize, items.size());
        if (startIndex < 0 || startIndex >= items.size()) {
            return Collections.emptyList();
        }

        return items.subList(startIndex, endIndex);
    }

    private String replacePageNumbers(String line, int page, boolean hasNextPage) {
        if (page <= 0) {
            return line;
        }

        line = line
            .replace("%previous_page%", page == 1 ? "" : Integer.toString(page  -1))
            .replace("{previous_page}", page == 1 ? "" : Integer.toString(page  -1))
            .replace("%current_page%", Integer.toString(page))
            .replace("{current_page}", Integer.toString(page))
            .replace("%next_page%", hasNextPage ? Integer.toString(page + 1) : "")
            .replace("{next_page}", hasNextPage ? Integer.toString(page + 1) : "");

        return line;
    }

    private List<String> processLore(List<String> originalLore, Player player, DeluxeTag tag, int page, boolean hasNextPage) {
        List<String> processedLore = null;

        if (originalLore != null && !originalLore.isEmpty()) {
            processedLore = new ArrayList<>();
            for (String line : originalLore) {
                line = replacePageNumbers(plugin.setPlaceholders(player, line, tag), page, hasNextPage);
                if (line.contains("\n")) {
                    processedLore.addAll(Arrays.asList(line.split("\n")));
                } else {
                    processedLore.add(line);
                }
            }
        }

        return processedLore;
    }
}
