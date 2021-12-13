package me.clip.deluxetags.gui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

	private Inventory inventory;
	private final Map<Integer, ItemStack> items;
	private final String displayName;
	private int slots;

	private int page;

	
	public TagGUI(String displayName, int page){
		this.displayName = displayName;
		this.items = new HashMap<>();
		this.page = page;
		this.tags = new HashMap<>();
	}
	
	public int getInventorySize(){
		return this.slots;
	}
	
	public String getInventoryName(){
		return this.displayName;
	}
 
	public TagGUI clear(){
		this.items.clear();
		this.tags = null;
		return this;
	}
	
	public boolean contains(ItemStack item){
		return this.items.containsValue(item);
	}
	
	public TagGUI setItem(int slot, ItemStack item){
		items.put(slot, item);
		return this;
	}

	public TagGUI setSlots(int slots){
		this.slots = slots;
		return this;
	}

	public void openInventory(Player player){
		this.inventory = Bukkit.createInventory(null, slots, MsgUtils.color(displayName));
		
		for(Integer slot : this.items.keySet()){
			inventory.setItem(slot, this.items.get(slot));
		}
		player.openInventory(this.inventory);
		
		if (inGUI == null) {
			inGUI = new HashMap<>();
		}
		
		inGUI.put(player.getName(), this);
	}

	public static ItemStack createItem(Material mat, short data, int amount, String displayName, List<String> lore) {
		if (mat == null) {
			return null;
		}
		ItemStack item = new ItemStack(mat, amount);
		if (data > 0) {
			item.setDurability(data);
		}
		ItemMeta itemMeta = item.getItemMeta();
		if (itemMeta == null) {
			return null;
		}
		if (displayName != null) {
			itemMeta.setDisplayName(displayName);
		}
		if (lore != null && !lore.isEmpty()) {
			itemMeta.setLore(lore);
		}
		item.setItemMeta(itemMeta);
		return item;
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
}