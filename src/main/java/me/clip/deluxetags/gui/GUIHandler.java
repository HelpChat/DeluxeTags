package me.clip.deluxetags.gui;

import java.util.Arrays;
import me.clip.deluxetags.DeluxeTag;
import me.clip.deluxetags.DeluxeTags;
import me.clip.deluxetags.Lang;
import me.clip.deluxetags.utils.MsgUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GUIHandler implements Listener {

    private DeluxeTags plugin;

    public GUIHandler(DeluxeTags i) {
        plugin = i;
    }

    private void sms(Player p, String msg) {
        p.sendMessage(MsgUtils.color(msg));
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {

        Player p = (Player) e.getWhoClicked();

        if (!TagGUI.hasGUI(p)) {
            return;
        }

        TagGUI gui = TagGUI.getGUI(p);

        e.setCancelled(true);

        ItemStack clicked = e.getCurrentItem();

        if (clicked == null || clicked.getType().equals(Material.AIR)) {
            return;
        }

        int slot = e.getRawSlot();

        if (slot < 36) {

            if (clicked.getType() != null && !clicked.getType().equals(Material.AIR)) {

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



                if (DeluxeTag.getLoadedTag(id) != null && DeluxeTag.getLoadedTag(id).setPlayerTag(p)) {
                    TagGUI.close(p);
                    p.closeInventory();

                    sms(p, Lang.GUI_TAG_SELECTED.getConfigValue(new String[]{
                            id, DeluxeTag.getPlayerDisplayTag(p)
                    }));

                    plugin.saveTagIdentifier(p.getUniqueId().toString(), id);
                }
            }

        } else if (slot == 48 || slot == 50) {

            TagGUI.close(p);
            p.closeInventory();

        } else if (slot == 49) {

            if (!DeluxeTag.getPlayerDisplayTag(p).isEmpty()) {

                TagGUI.close(p);

                p.closeInventory();

                plugin.getDummy().setPlayerTag(p);

                plugin.removeSavedTag(p.getUniqueId().toString());

                sms(p, Lang.GUI_TAG_DISABLED.getConfigValue(null));
            }

            p.updateInventory();

        } else if (slot == 45 || slot == 53) {

            if (clicked.hasItemMeta() && clicked.getItemMeta().hasDisplayName()) {

                String name;

                if (slot == 45) {
                    name = clicked.getItemMeta().getDisplayName().replace("Back to page ", "");
                } else {
                    name = clicked.getItemMeta().getDisplayName().replace("Forward to page ", "");
                }

                int page;

                try {

                    page = Integer.parseInt(name);

                } catch (Exception ex) {

                    TagGUI.close(p);

                    p.closeInventory();

                    sms(p, Lang.GUI_PAGE_ERROR.getConfigValue(null));
                    return;
                }

                openMenu(p, page);
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {

        if (e.getPlayer() instanceof Player) {

            Player p = (Player) e.getPlayer();

            if (TagGUI.hasGUI(p)) {

                TagGUI.close(p);
            }
        }
    }


    public boolean openMenu(Player p, int page) {

        List<String> ids = DeluxeTag.getAvailableTagIdentifiers(p);

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

        int pages = 1;

        if (ids.size() > 36) {

            pages = ids.size() / 36;

            if (ids.size() % 36 > 0) {
                pages++;
            }
        }

        if (page > 1 && page <= pages) {

            int start = (36 * page) - 36;

            ids = ids.subList(start, ids.size());
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

            String display = options.getTagSelectItem().getName();

            display = DeluxeTags.setPlaceholders(p, display, tag);

            List<String> tmp = null;

            List<String> orig = options.getTagSelectItem().getLore();
            if (orig != null && !orig.isEmpty()) {
                tmp = new ArrayList<>();
                for (String line : orig) {
                    line = DeluxeTags.setPlaceholders(p, line, tag);
                    if (line.contains("\n")) {
                        tmp.addAll(Arrays.asList(line.split("\n")));
                    } else {
                        tmp.add(line);
                    }
                }
            }
            gui.setItem(count, TagGUI.createItem(options.getTagSelectItem().getMaterial(), options.getTagSelectItem().getData(), 1, display, tmp));

            count++;
        }

        gui.setTags(tags);

        //divider

        String display = options.getDividerItem().getName();
        display = MsgUtils.color(DeluxeTags.setPlaceholders(p, display, null));
        List<String> tmp = null;

        List<String> orig = options.getDividerItem().getLore();
        if (orig != null && !orig.isEmpty()) {
            tmp = new ArrayList<>();
            for (String line : orig) {
                line = DeluxeTags.setPlaceholders(p, line, null);
                if (line.contains("\n")) {
                    tmp.addAll(Arrays.asList(line.split("\n")));
                } else {
                    tmp.add(line);
                }
                tmp.add(line);
            }
        }

        ItemStack divider = TagGUI.createItem(options.getDividerItem().getMaterial(), options.getDividerItem().getData(), 1, display, tmp);

        for (int b = 36; b < 45; b++) {

            gui.setItem(b, divider);
        }

        //info

        String current = DeluxeTag.getPlayerTagIdentifier(p);

        DisplayItem item;

        if (current == null || current.isEmpty()) {
            item = options.getNoTagItem();
        } else {
            item = options.getHasTagItem();
        }

        ItemStack info = new ItemStack(item.getMaterial(), 1, item.getData());

        ItemMeta meta = info.getItemMeta();

        meta.setDisplayName(DeluxeTags.setPlaceholders(p, item.getName(), null));

        if (item.getLore() != null) {

            List<String> infoTmp;

            List<String> infoLore = item.getLore();

            if (infoLore != null && !infoLore.isEmpty()) {

                infoTmp = new ArrayList<>();

                for (String line : infoLore) {
                    line = DeluxeTags.setPlaceholders(p, line, null);
                    if (line.contains("\n")) {
                        tmp.addAll(Arrays.asList(line.split("\n")));
                    } else {
                        tmp.add(line);
                    }
                    infoTmp.add(line);
                }

                meta.setLore(infoTmp);
            }
        }

        info.setItemMeta(meta);

        gui.setItem(49, info);

        String exitDisplay = DeluxeTags.setPlaceholders(p, options.getExitItem().getName(), null);

        List<String> exitLore = options.getExitItem().getLore();

        List<String> exitTmp = null;

        if (exitLore != null && !exitLore.isEmpty()) {

            exitTmp = new ArrayList<>();

            for (String line : exitLore) {
                line = DeluxeTags.setPlaceholders(p, line, null);
                exitTmp.add(line);
            }
        }

        ItemStack exit = TagGUI.createItem(options.getExitItem().getMaterial(), options.getExitItem().getData(), 1, exitDisplay, exitTmp);

        gui.setItem(48, exit);
        gui.setItem(50, exit);

        //NOT CONFIGURABLE YET
        if (page > 1) {
            gui.setItem(45, TagGUI.createItem(Material.PAPER, (short) 0, 1, "Back to page " + (page - 1), null));
        }

        if (page < pages) {
            gui.setItem(53, TagGUI.createItem(Material.PAPER, (short) 0, 1, "Forward to page " + (page + 1), null));
        }

        gui.openInventory(p);
        return true;
    }

}
