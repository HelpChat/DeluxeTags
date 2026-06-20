package me.clip.deluxetags.gui;

import java.util.*;

import me.clip.deluxetags.utils.MsgUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class TagGUI {
	
	private static HashMap<String, TagGUI> inGUI;
	private Map<Integer, String> tags;
	private Map<Integer, String> categories;

	private Inventory inventory;
	private final Map<Integer, DisplayItem> items;
	private final String displayName;
	private String categoryIdentifier;
	private boolean categoryMenu;
	private int slots;
	private int page;
	
	public TagGUI(String displayName, int page){
		this.displayName = displayName;
		this.items = new HashMap<>();
		this.page = page;
		this.tags = new HashMap<>();
		this.categories = new HashMap<>();
	}
	
	public int getInventorySize(){
		return this.slots;
	}
	
	public String getInventoryName(){
		return this.displayName;
	}
 
	public TagGUI clear() {
		this.items.clear();
		this.tags = null;
		this.categories = null;
		this.categoryIdentifier = null;
		this.categoryMenu = false;
		return this;
	}
	
	public boolean contains(DisplayItem item) {
		return this.items.containsValue(item);
	}

	public TagGUI addDisplayItem(DisplayItem item) {
		for (Integer slot : item.getSlots()) {
			this.items.put(slot, item);
		}
		return this;
	}

	public TagGUI setSlots(int slots){
		this.slots = slots;
		return this;
	}

	public void openInventory(Player player){
		this.inventory = Bukkit.createInventory(null, slots, MsgUtils.color(displayName));
		
		for(Integer slot : this.items.keySet()){
			if (slot < 0 || slot >= slots) {
				continue;
			}

			inventory.setItem(slot, this.items.get(slot).getItemStack());
		}
		player.openInventory(this.inventory);
		
		if (inGUI == null) {
			inGUI = new HashMap<>();
		}
		
		inGUI.put(player.getName(), this);
	}
	
	public static boolean hasGUI(Player p) {
		if (inGUI == null) {
			return false;
		}
		
		return inGUI.containsKey(p.getName()) && inGUI.get(p.getName()) != null;
	}

	public static TagGUI getGUI(Player p) {
		if (!hasGUI(p)) {
			return null;
		}

		return inGUI.get(p.getName());
	}
	
	public static boolean close(Player p) {
		if (!hasGUI(p)) {
			return false;
		}
		
		getGUI(p).clear();
		inGUI.remove(p.getName());
		return true;
	}

	public ItemType getClickedItemType(int slot) {
		DisplayItem clickedDisplayItem = this.items.get(slot);

		return clickedDisplayItem != null ? clickedDisplayItem.getType() : ItemType.UNKNOWN;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}
	
	public static void unload() {
		inGUI = null;
	}

	public Map<Integer, String> getTags() {
		return tags;
	}

	public void setTags(Map<Integer, String> tags) {
		this.tags = tags;
	}

	public Map<Integer, String> getCategories() {
		return categories;
	}

	public void setCategories(Map<Integer, String> categories) {
		this.categories = categories;
	}

	public String getCategoryIdentifier() {
		return categoryIdentifier;
	}

	public void setCategoryIdentifier(String categoryIdentifier) {
		this.categoryIdentifier = categoryIdentifier;
	}

	public boolean isCategoryMenu() {
		return categoryMenu;
	}

	public void setCategoryMenu(boolean categoryMenu) {
		this.categoryMenu = categoryMenu;
	}
}
