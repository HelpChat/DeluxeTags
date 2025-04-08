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

            DeluxeTag tag = plugin.getTagsHandler().getLoadedTag(id);
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

            sms(p, Lang.GUI_TAG_SELECTED.getConfigValue(new String[]{
                id, plugin.getTagsHandler().getPlayerDisplayTag(p)
            }));

            plugin.saveTagIdentifier(p.getUniqueId().toString(), id);

        } else if (slot == 48 || slot == 50) {
            TagGUI.close(p);
            p.closeInventory();

        } else if (slot == 49) {
            if (plugin.getTagsHandler().getPlayerDisplayTag(p).isEmpty()) {
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
        List<String> ids = plugin.getTagsHandler().getAllVisibleTagIdentifiers(p);
        if (ids == null || ids.isEmpty()) {
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
            DeluxeTag tag = plugin.getTagsHandler().getLoadedTag(id);
            if (tag == null) {
                tag = plugin.getDummyTag();
            }

            if (tag.hasPermissionToUse(p)) {
                gui.setItem(
                    count,
                    TagGUI.createItem(
                        options.getTagSelectItem().getMaterial(),
                        options.getTagSelectItem().getData(),
                        1,
                        plugin.setPlaceholders(p, replacePageNumbers(options.getTagSelectItem().getName(), page, hasNextPage), tag),
                        processLore(options.getTagSelectItem().getLore(), p, tag, page, hasNextPage)
                    )
                );
            } else {
                gui.setItem(
                    count,
                    TagGUI.createItem(
                        options.getTagVisibleItem().getMaterial(),
                        options.getTagVisibleItem().getData(),
                        1,
                        plugin.setPlaceholders(p, replacePageNumbers(options.getTagVisibleItem().getName(), page, hasNextPage), tag),
                        processLore(options.getTagVisibleItem().getLore(), p, tag, page, hasNextPage)
                    )
                );
            }
            count++;
        }
        gui.setTags(tags);

        ItemStack divider = TagGUI.createItem(
            options.getDividerItem().getMaterial(),
            options.getDividerItem().getData(),
            1,
            plugin.setPlaceholders(p, replacePageNumbers(options.getDividerItem().getName(), page, hasNextPage), null),
            processLore(options.getDividerItem().getLore(), p, null, page, hasNextPage)
        );
        for (int b = 36; b < 45; b++) {
            gui.setItem(b, divider);
        }

        String currentTag = plugin.getTagsHandler().getPlayerTagIdentifier(p);
        DisplayItem currentTagItem;

        if (currentTag == null || currentTag.isEmpty()) {
            currentTagItem = options.getNoTagItem();
        } else {
            currentTagItem = options.getHasTagItem();
        }

        ItemStack info = TagGUI.createItem(
            currentTagItem.getMaterial(),
            currentTagItem.getData(),
            1,
            plugin.setPlaceholders(p, replacePageNumbers(currentTagItem.getName(), page, hasNextPage), null),
            processLore(currentTagItem.getLore(), p, null, page, hasNextPage)
        );
        gui.setItem(49, info);

        ItemStack exit = TagGUI.createItem(
            options.getExitItem().getMaterial(),
            options.getExitItem().getData(),
            1,
            plugin.setPlaceholders(p, replacePageNumbers(options.getExitItem().getName(), page, hasNextPage), null),
            processLore(options.getExitItem().getLore(), p, null, page, hasNextPage)
        );
        gui.setItem(48, exit);
        gui.setItem(50, exit);

        if (page > 1) {
            ItemStack previousPage = TagGUI.createItem(
                options.getPreviousPageItem().getMaterial(),
                options.getPreviousPageItem().getData(),
                1,
                plugin.setPlaceholders(p, replacePageNumbers(options.getPreviousPageItem().getName().replace("%page%", String.valueOf(page-1)), page, hasNextPage), null),
                processLore(options.getPreviousPageItem().getLore(), p, null, page, hasNextPage)
            );
            gui.setItem(45, previousPage);
        }

        if (hasNextPage) {
            ItemStack nextPage = TagGUI.createItem(
                options.getNextPageItem().getMaterial(),
                options.getNextPageItem().getData(),
                1,
                plugin.setPlaceholders(p, replacePageNumbers(options.getNextPageItem().getName().replace("%page%", String.valueOf(page+1)), page, true), null),
                processLore(options.getNextPageItem().getLore(), p, null, page, true)
            );
            gui.setItem(53, nextPage);
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