package me.clip.deluxetags.gui;

import java.util.Arrays;
import java.util.List;

import com.cryptomorin.xseries.XMaterial;
import me.clip.deluxetags.DeluxeTags;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;


public class GUIOptions {
	
	final List<String> ITEM_TYPES = Arrays.asList("tag_select_item", "divider_item", "has_tag_item", "no_tag_item", "exit_item");
	
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
				switch (type) {
					case "tag_select_item":
						mat = Material.NAME_TAG;
						break;
					case "divider_item":
						mat = XMaterial.WHITE_STAINED_GLASS_PANE.parseMaterial();
						break;
					case "has_tag_item":
					case "no_tag_item":
						mat = XMaterial.PLAYER_HEAD.parseMaterial();
						break;
					case "exit_item":
						mat = Material.IRON_DOOR;
						break;
				}
			}

			try {
				data = Short.parseShort(c.getString("gui." + type + ".data"));
			} catch (Exception e) {
				data = 0;
			}

			display = c.getString("gui." + type + ".displayname");

			lore = c.getStringList("gui." + type + ".lore");

			switch (type) {
				case "tag_select_item":
					this.tagSelectItem = new DisplayItem(mat, data, display, lore);
					break;
				case "divider_item":
					this.dividerItem = new DisplayItem(mat, data, display, lore);
					break;
				case "has_tag_item":
					this.hasTagItem = new DisplayItem(mat, data, display, lore);
					break;
				case "no_tag_item":
					this.noTagItem = new DisplayItem(mat, data, display, lore);
					break;
				case "exit_item":
					this.exitItem = new DisplayItem(mat, data, display, lore);
					break;
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
