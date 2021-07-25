package me.clip.deluxetags.gui;

import java.util.Arrays;
import java.util.List;

import com.cryptomorin.xseries.XMaterial;
import me.clip.deluxetags.DeluxeTags;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

public class GUIOptions {

	final List<String> ITEM_TYPES = Arrays.asList("tag_select_item", "divider_item", "has_tag_item", "no_tag_item", "exit_item", "next_page", "previous_page");
	private String menuName;
	private DisplayItem tagSelectItem;
	private DisplayItem dividerItem;
	private DisplayItem hasTagItem;
	private DisplayItem noTagItem;
	private DisplayItem exitItem;
	private DisplayItem nextPageItem;
	private DisplayItem previousPageItem;

	public GUIOptions(DeluxeTags plugin) {

		FileConfiguration c = plugin.getConfig();

		this.menuName = c.getString("gui.name");
		if (this.menuName == null) {
			this.menuName = "&6Available tags&f: &6%deluxetags_amount%";
		}

		Material material = null;
		String displayName;
		List<String> lore;
		short data;

		for (String type : ITEM_TYPES) {
			try {
				material = XMaterial.matchXMaterial(c.getString("gui." + type + ".material", "player_head").toUpperCase()).get().parseMaterial();
			} catch (Exception e) {
				switch (type) {
					case "tag_select_item":
						material = Material.NAME_TAG;
						break;
					case "divider_item":
						material = XMaterial.WHITE_STAINED_GLASS_PANE.parseMaterial();
						break;
					case "has_tag_item":
					case "no_tag_item":
						material = XMaterial.PLAYER_HEAD.parseMaterial();
						break;
					case "exit_item":
						material = Material.IRON_DOOR;
						break;
					case "next_page":
					case "previous_page":
						material = Material.PAPER;
				}
			}

			try {
				data = Short.parseShort(c.getString("gui." + type + ".data", "0"));
			} catch (Exception e) {
				data = 0;
			}

			displayName = c.getString("gui." + type + ".displayname");
			lore = c.getStringList("gui." + type + ".lore");

			switch (type) {
				case "tag_select_item":
					this.tagSelectItem = new DisplayItem(material, data, displayName, lore);
					break;
				case "divider_item":
					this.dividerItem = new DisplayItem(material, data, displayName, lore);
					break;
				case "has_tag_item":
					this.hasTagItem = new DisplayItem(material, data, displayName, lore);
					break;
				case "no_tag_item":
					this.noTagItem = new DisplayItem(material, data, displayName, lore);
					break;
				case "exit_item":
					this.exitItem = new DisplayItem(material, data, displayName, lore);
					break;
				case "next_page":
					this.nextPageItem = new DisplayItem(material, data, displayName, lore);
					break;
				case "previous_page":
					this.previousPageItem = new DisplayItem(material, data, displayName, lore);
					break;
			}

			material = null;
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

	public DisplayItem getNextPageItem() {
		return nextPageItem;
	}

	public DisplayItem getPreviousPageItem() {
		return previousPageItem;
	}

	public String getMenuName() {
		return menuName;
	}
}