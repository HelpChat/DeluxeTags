package me.clip.deluxetags.gui;

import me.clip.deluxetags.DeluxeTags;
import me.clip.deluxetags.config.TagConfig;

public class GUIOptions {
	private final String menuName;
	private DisplayItem tagSelectItem;
	private DisplayItem dividerItem;
	private DisplayItem hasTagItem;
	private DisplayItem noTagItem;
	private DisplayItem exitItem;
	private DisplayItem nextPageItem;
	private DisplayItem previousPageItem;

	public GUIOptions(DeluxeTags plugin) {
		TagConfig config = plugin.getCfg();
		menuName = config.loadMenuName();

		for (ItemType type : ItemType.getCached()) {
			switch (type) {
				case TAG_SELECT_ITEM:
					this.tagSelectItem = config.loadGuiItem(type);
					break;
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
				case NEXT_PAGE:
					this.nextPageItem = config.loadGuiItem(type);
					break;
				case PREVIOUS_PAGE:
					this.previousPageItem = config.loadGuiItem(type);
					break;
			}
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