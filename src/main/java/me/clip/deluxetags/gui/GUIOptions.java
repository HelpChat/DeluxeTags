package me.clip.deluxetags.gui;

import me.clip.deluxetags.DeluxeTags;
import me.clip.deluxetags.config.TagConfig;
import org.bukkit.Material;

import java.util.List;

public class GUIOptions {
	private final String menuName;
	private int menuSize;
	private List<Integer> tagSlots;
	private Material tagVisibleMaterial;
	private short tagVisibleData;
	private DisplayItem dividerItem;
	private DisplayItem hasTagItem;
	private DisplayItem noTagItem;
	private DisplayItem exitItem;
	private DisplayItem categoryBackItem;
	private DisplayItem nextPageItem;
	private DisplayItem previousPageItem;

	public GUIOptions(DeluxeTags plugin) {
		TagConfig config = plugin.getCfg();
		menuName = config.getMenuName();

		menuSize = config.getMenuSize();
		// Validate menu size is 9, 18, 27, 36, 45 or 54
		if (menuSize > 54 || menuSize < 9 || menuSize % 9 != 0) {
			menuSize = 54;
		}

		tagSlots = config.getTagSlots();
		tagVisibleMaterial = config.getTagVisibleMaterial();
		tagVisibleData = config.getTagVisibleData();

		for (ItemType type : ItemType.getCached()) {
			switch (type) {
				case DIVIDER_ITEM:
					this.dividerItem = config.loadGuiItem(type);
					break;
				case HAS_TAG_ITEM:
					this.hasTagItem = config.loadGuiItem(type);
					break;
				case NO_TAG_ITEM:
					this.noTagItem = config.loadGuiItem(type);
					break;
				case EXIT_ITEM:
					this.exitItem = config.loadGuiItem(type);
					break;
				case CATEGORY_BACK_ITEM:
					this.categoryBackItem = config.loadGuiItem(type);
					break;
				case NEXT_PAGE:
					this.nextPageItem = config.loadGuiItem(type);
					break;
				case PREVIOUS_PAGE:
					this.previousPageItem = config.loadGuiItem(type);
					break;
			}
		}
	}

	public DisplayItem getDividerItem() {
		return dividerItem;
	}

	public Material getTagVisibleMaterial() {
		return tagVisibleMaterial;
	}

	public short getTagVisibleData() {
		return tagVisibleData;
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

	public DisplayItem getCategoryBackItem() {
		return categoryBackItem;
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

	public int getMenuSize() {
		return menuSize;
	}

	public List<Integer> getTagSlots() {
		return tagSlots;
	}
}
