package me.clip.deluxetags.gui;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import me.clip.deluxetags.DeluxeTags;


public class GUIOptions {
	
	final List<String> ITEM_TYPES = Arrays.asList(new String[] { 
			"tag_select_item", "divider_item", "has_tag_item", "no_tag_item", "exit_item" });
	
	private String menuName;
	
	private DisplayItem tagSelectItem;
	
	private DisplayItem dividerItem;
	
	private DisplayItem hasTagItem;
	
	private DisplayItem noTagItem;
	
	private DisplayItem exitItem;
	
	public GUIOptions(DeluxeTags i) {

		FileConfiguration c = i.getConfig();

		this.menuName = c.getString("gui.name");

		if (this.menuName == null) {
			this.menuName = "&6Available tags&f: &6%deluxetags_amount%";
		}

		Material mat = null;
		short data = 0;
		String display = null;
		List<String> lore = null;

		for (String type : ITEM_TYPES) {

			try {
				mat = Material.getMaterial(c.getString(
						"gui." + type + ".material").toUpperCase());
			} catch (Exception e) {
				if (type.equals("tag_select_item")) {
					mat = Material.NAME_TAG;
				} else if (type.equals("divider_item")) {
					mat = Material.STAINED_GLASS_PANE;
				} else if (type.equals("has_tag_item")) {
					mat = Material.SKULL_ITEM;
				} else if (type.equals("no_tag_item")) {
					mat = Material.SKULL_ITEM;
				} else if (type.equals("exit_item")) {
					mat = Material.IRON_DOOR;
				}
			}

			try {
				data = Short.parseShort(c.getString("gui." + type + ".data"));
			} catch (Exception e) {
				data = 0;
			}

			display = c.getString("gui." + type + ".displayname");

			lore = c.getStringList("gui." + type + ".lore");

			if (type.equals("tag_select_item")) {
				this.tagSelectItem = new DisplayItem(mat, data, display, lore);
			} else if (type.equals("divider_item")) {
				this.dividerItem = new DisplayItem(mat, data, display, lore);
			} else if (type.equals("has_tag_item")) {
				this.hasTagItem = new DisplayItem(mat, data, display, lore);
			} else if (type.equals("no_tag_item")) {
				this.noTagItem = new DisplayItem(mat, data, display, lore);
			} else if (type.equals("exit_item")) {
				this.exitItem = new DisplayItem(mat, data, display, lore);
			}

			mat = null;
			data = 0;
			display = null;
			lore = null;
		}

	}

	public DisplayItem getTagSelectItem() {
		return tagSelectItem;
	}

	public DisplayItem getDividerItem() {
		return dividerItem;
	}

	public DisplayItem getHasTagItem() {
		return hasTagItem;
	}

	public DisplayItem getNoTagItem() {
		return noTagItem;
	}

	public DisplayItem getExitItem() {
		return exitItem;
	}
	
	public String getMenuName() {
		return menuName;
	}

}
