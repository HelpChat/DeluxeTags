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

            DeluxeTag tag = DeluxeTag.getLoadedTag(id);
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

            if (!tag.setPlayerTag(p)) {
                return;
            }

            TagGUI.close(p);
            p.closeInventory();

            sms(p, Lang.GUI_TAG_SELECTED.getConfigValue(new String[]{
                id, DeluxeTag.getPlayerDisplayTag(p)
            }));

            plugin.saveTagIdentifier(p.getUniqueId().toString(), id);

        } else if (slot == 48 || slot == 50) {
            TagGUI.close(p);
            p.closeInventory();

        } else if (slot == 49) {
            if (DeluxeTag.getPlayerDisplayTag(p).isEmpty()) {
                p.updateInventory();
                return;
            }

            TagGUI.close(p);
            p.closeInventory();

            plugin.getDummy().setPlayerTag(p);
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
        List<String> ids = DeluxeTag.getAllVisibleTagIdentifiers(p);
        if (ids == null || ids.isEmpty()) {
            return false;
        }

        GUIOptions options = plugin.getGuiOptions();

        String title = options.getMenuName();
        title = DeluxeTags.setPlaceholders(p, title, null);
        if (title.length() > 32) {
            title = title.substring(0, 31);
        }

        TagGUI gui = new TagGUI(title, page).setSlots(54);

        int pages = (int) Math.ceil(ids.size() / 36d);
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
            DeluxeTag tag = DeluxeTag.getLoadedTag(id);
            if (tag == null) {
                tag = new DeluxeTag(1, "", "", "");
            }

            gui.setItem(
                count,
                TagGUI.createItem(
                    options.getTagSelectItem().getMaterial(),
                    options.getTagSelectItem().getData(),
                    1,
                    DeluxeTags.setPlaceholders(p, options.getTagSelectItem().getName(), tag),
                    processLore(options.getTagSelectItem().getLore(), p, tag)
                )
            );
            count++;
        }
        gui.setTags(tags);

        ItemStack divider = TagGUI.createItem(
            options.getDividerItem().getMaterial(),
            options.getDividerItem().getData(),
            1,
            DeluxeTags.setPlaceholders(p, options.getDividerItem().getName(), null),
            processLore(options.getDividerItem().getLore(), p, null)
        );
        for (int b = 36; b < 45; b++) {
            gui.setItem(b, divider);
        }

        String currentTag = DeluxeTag.getPlayerTagIdentifier(p);
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
            DeluxeTags.setPlaceholders(p, currentTagItem.getName(), null),
            processLore(currentTagItem.getLore(), p, null)
        );
        gui.setItem(49, info);

        ItemStack exit = TagGUI.createItem(
            options.getExitItem().getMaterial(),
            options.getExitItem().getData(),
            1,
            DeluxeTags.setPlaceholders(p, options.getExitItem().getName(), null),
            processLore(options.getExitItem().getLore(), p, null)
        );
        gui.setItem(48, exit);
        gui.setItem(50, exit);

        if (page > 1) {
            ItemStack previousPage = TagGUI.createItem(
                options.getPreviousPageItem().getMaterial(),
                options.getPreviousPageItem().getData(),
                1,
                DeluxeTags.setPlaceholders(p, options.getPreviousPageItem().getName().replace("%page%", String.valueOf(page-1)), null),
                processLore(options.getPreviousPageItem().getLore(), p, null)
            );
            gui.setItem(45, previousPage);
        }

        if (page < pages) {
            ItemStack nextPage = TagGUI.createItem(
                options.getNextPageItem().getMaterial(),
                options.getNextPageItem().getData(),
                1,
                DeluxeTags.setPlaceholders(p, options.getNextPageItem().getName().replace("%page%", String.valueOf(page+1)), null),
                processLore(options.getNextPageItem().getLore(), p, null)
            );
            gui.setItem(53, nextPage);
        }

        gui.setPage(page);
        gui.openInventory(p);
        return true;
    }

    private List<String> processLore(List<String> originalLore, Player player, DeluxeTag tag) {
        List<String> processedLore = null;

        if (originalLore != null && !originalLore.isEmpty()) {
            processedLore = new ArrayList<>();
            for (String line : originalLore) {
                line = DeluxeTags.setPlaceholders(player, line, tag);
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