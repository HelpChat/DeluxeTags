package me.clip.deluxetags.gui;

import java.util.*;

import me.clip.deluxetags.DeluxeTags;
import me.clip.deluxetags.config.Lang;
import me.clip.deluxetags.tags.DeluxeTag;
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
                openMenu(p, gui.getPage()+1);
                break;
            case DIVIDER_ITEM:
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
                openMenu(p, gui.getPage()-1);
                break;
            case TAG_SELECT_ITEM:
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
        List<String> ids = plugin.getTagsHandler().getPlayerVisibleTagIdentifiers(p);
        if (ids.isEmpty()) {
            return false;
        }

        GUIOptions options = plugin.getGuiOptions();

        List<Integer> tagSlots = options.getTagSlots();

        int pages = (int) Math.ceil(ids.size() / (double) tagSlots.size());
        boolean hasNextPage = page < pages;

        String title = options.getMenuName();
        title = replacePageNumbers(plugin.setPlaceholders(p, title, null), page, hasNextPage);
        if (title.length() > 32) {
            title = title.substring(0, 31);
        }

        int menuSlots = options.getMenuSize();

        TagGUI gui = new TagGUI(title, page).setSlots(menuSlots);

        if (page > 1 && page <= pages) {
            ids = ids.subList((tagSlots.size() * page) - tagSlots.size(), ids.size());
        }

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
        ItemMeta exitItemMeta = dividerDisplayItem.getItemStack().getItemMeta();
        if (exitItemMeta != null) {
            exitItemMeta.setDisplayName(plugin.setPlaceholders(p, replacePageNumbers(exitDisplayItem.getName(), page, hasNextPage), null));
            exitItemMeta.setLore(processLore(exitDisplayItem.getLore(), p, null, page, hasNextPage));
            exitDisplayItem.getItemStack().setItemMeta(exitItemMeta);
        }
        gui.addDisplayItem(exitDisplayItem);

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

        gui.setPage(page);
        gui.openInventory(p);
        return true;
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