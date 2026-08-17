package me.clip.deluxetags.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

        if (slot < 36) {
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

            DeluxeTag tag = plugin.getTagsHandler().getTagByIdentifier(id);
            if (tag == null) {
                return;
            }

            if (!p.hasPermission(tag.getPermission())) {
                sms(p, Lang.CMD_NO_PERMS.getConfigValue(new String[]{
                    "deluxetags.tag." + id
                }));
                TagGUI.close(p);
                p.closeInventory();
                return;
            }

            if (!plugin.getTagsHandler().setPlayerTag(p, tag)) {
                return;
            }

            TagGUI.close(p);
            p.closeInventory();

            tag = plugin.getTagsHandler().getPlayerActiveTag(p);
            final String displayName = tag == null ? "" : tag.getDisplayTag(p);

            sms(p, Lang.GUI_TAG_SELECTED.getConfigValue(new String[]{id, displayName}));

            plugin.saveTagIdentifier(p.getUniqueId().toString(), id);

        } else if (slot == 48 || slot == 50) {
            TagGUI.close(p);
            p.closeInventory();

        } else if (slot == 49) {
            final DeluxeTag tag = plugin.getTagsHandler().getPlayerActiveTag(p);
            if (tag == null || tag.getDisplayTag(p).isEmpty() || plugin.getTagsHandler().isUsingDefaultTag(p) || plugin.getTagsHandler().isUsingForcedTag(p)) {
                p.updateInventory();
                return;
            }

            TagGUI.close(p);
            p.closeInventory();

            plugin.getTagsHandler().setPlayerTag(p, plugin.getDummyTag());
            plugin.removeSavedTag(p.getUniqueId().toString());

            sms(p, Lang.GUI_TAG_DISABLED.getConfigValue(null));
            p.updateInventory();

        } else if (slot == 45) {
            openMenu(p, gui.getPage()-1);

        } else if (slot == 53) {
            openMenu(p, gui.getPage()+1);
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

        int pages = (int) Math.ceil(ids.size() / 36d);
        boolean hasNextPage = page < pages;

        String title = options.getMenuName();
        title = replacePageNumbers(plugin.setPlaceholders(p, title, null), page, hasNextPage);
        if (title.length() > 32) {
            title = title.substring(0, 31);
        }

        TagGUI gui = new TagGUI(title, page).setSlots(54);

        if (page > 1 && page <= pages) {
            ids = ids.subList((36 * page) - 36, ids.size());
        }

        int count = 0;
        Map<Integer, String> tags = new HashMap<>();
        for (String id : ids) {
            if (count >= 36) {
                break;
            }

            tags.put(count, id);
            DeluxeTag tag = plugin.getTagsHandler().getTagByIdentifier(id);
            if (tag == null) {
                tag = plugin.getDummyTag();
            }

            DisplayItem tagItem = tag.hasPermissionToUse(p) ? options.getTagSelectItem() : options.getTagVisibleItem();
            ItemStack tagStack = buildItem(tagItem, p, tag, page, hasNextPage);
            if (tagStack == null) {
                // tag_select_item / tag_visible_item is essential: fall back to a name tag so the
                // menu always lists the available tags even if the item is misconfigured/disabled.
                tagStack = TagGUI.createItem(
                    ItemType.TAG_SELECT_ITEM.getFallbackMaterial(), (short) 0, 1,
                    plugin.setPlaceholders(p, "&6%deluxetags_identifier%", tag), null
                );
            }
            gui.setItem(count, tagStack);
            count++;
        }
        gui.setTags(tags);

        // The divider/filler row (slots 36-44) is optional. Set gui.divider_item.material to AIR
        // or NONE (or remove the section) to leave it empty instead of crashing the menu.
        ItemStack divider = buildItem(options.getDividerItem(), p, null, page, hasNextPage);
        if (divider != null) {
            for (int b = 36; b < 45; b++) {
                gui.setItem(b, divider);
            }
        }

        final DeluxeTag currentTag = plugin.getTagsHandler().getPlayerActiveTag(p);
        DisplayItem currentTagItem =
            (currentTag == null || currentTag.getIdentifier().isEmpty())
                ? options.getNoTagItem()
                : options.getHasTagItem();

        ItemStack info = buildItem(currentTagItem, p, null, page, hasNextPage);
        if (info != null) {
            gui.setItem(49, info);
        }

        ItemStack exit = buildItem(options.getExitItem(), p, null, page, hasNextPage);
        if (exit != null) {
            gui.setItem(48, exit);
            gui.setItem(50, exit);
        }

        DisplayItem prevItem = options.getPreviousPageItem();
        if (page > 1 && prevItem != null && prevItem.getMaterial() != null) {
            String prevName = prevItem.getName() == null ? "" : prevItem.getName().replace("%page%", String.valueOf(page - 1));
            ItemStack previousPage = TagGUI.createItem(
                prevItem.getMaterial(),
                prevItem.getData(),
                1,
                plugin.setPlaceholders(p, replacePageNumbers(prevName, page, hasNextPage), null),
                processLore(prevItem.getLore(), p, null, page, hasNextPage)
            );
            gui.setItem(45, previousPage);
        }

        DisplayItem nextItem = options.getNextPageItem();
        if (hasNextPage && nextItem != null && nextItem.getMaterial() != null) {
            String nextName = nextItem.getName() == null ? "" : nextItem.getName().replace("%page%", String.valueOf(page + 1));
            ItemStack nextPage = TagGUI.createItem(
                nextItem.getMaterial(),
                nextItem.getData(),
                1,
                plugin.setPlaceholders(p, replacePageNumbers(nextName, page, true), null),
                processLore(nextItem.getLore(), p, null, page, true)
            );
            gui.setItem(53, nextPage);
        }

        gui.setPage(page);
        gui.openInventory(p);
        return true;
    }

    /**
     * Builds an {@link ItemStack} from a configurable {@link DisplayItem}, applying placeholders to
     * the (configurable) display name and lore. Returns {@code null} when the item is disabled
     * (material set to AIR/NONE or removed from config), in which case the caller leaves the slot empty.
     */
    private ItemStack buildItem(DisplayItem item, Player p, DeluxeTag tag, int page, boolean hasNextPage) {
        if (item == null || item.getMaterial() == null) {
            return null;
        }

        String name = item.getName() == null ? "" : item.getName();
        return TagGUI.createItem(
            item.getMaterial(),
            item.getData(),
            1,
            plugin.setPlaceholders(p, replacePageNumbers(name, page, hasNextPage), tag),
            processLore(item.getLore(), p, tag, page, hasNextPage)
        );
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