package me.clip.deluxetags.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class TagGUI {
	
	private static HashMap<String, TagGUI> inGUI;
	
	private Inventory inventory;
	
	private String displayName;
	
	private int slots;
	
	private int page;
	
	private HashMap<Integer, String> tags;
	
	private HashMap<Integer, ItemStack> items;
	
	public TagGUI(String displayName, int page){
		this.displayName = displayName;
		this.items = new HashMap<Integer, ItemStack>();
		this.page = page;
		this.tags = new HashMap<Integer, String>();
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
		if(this.items.values().contains(item)) {
			return true;
		}	
		return false;
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
		
		this.inventory = Bukkit.createInventory(null, slots, ChatColor.translateAlternateColorCodes('&', displayName));
		
		for(Integer slot : this.items.keySet()){
			inventory.setItem(slot, this.items.get(slot));
		}
		player.openInventory(this.inventory);
		
		if (inGUI == null) {
			inGUI = new HashMap<String, TagGUI>();
		}
		
		inGUI.put(player.getName(), this);
	}
	
	public static ItemStack createItem(Material mat, short data, int amount, String displayName, List<String> lore) {
		if (mat == null) {
			return null;
		}
		ItemStack i = new ItemStack(mat, amount);
		if (data > 0) {
			i.setDurability(data);
		}
		ItemMeta im = i.getItemMeta();
		if (displayName != null) {
			im.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
		}
		if (lore != null && !lore.isEmpty()) {
			List<String> temp = new ArrayList<String>();
			for (String line : lore) {
				temp.add(ChatColor.translateAlternateColorCodes('&', line));
			}
			im.setLore(temp);
		}
		i.setItemMeta(im);
		return i;
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

	public HashMap<Integer, String> getTags() {
		return tags;
	}

	public void setTags(HashMap<Integer, String> tags) {
		this.tags = tags;
	}
	
}
