package me.clip.deluxetags.gui;

import java.util.ArrayList;
import java.util.List;

import me.clip.deluxetags.utils.ItemUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public class DisplayItem {

	private ItemType type;
	private ItemStack itemStack;
	private List<Integer> slots;

	public DisplayItem(@NotNull final ItemType type, @NotNull final ItemStack itemStack, @NotNull final List<Integer> slots) {
		this.type = type;
		this.itemStack = itemStack;
		this.slots = slots;
	}

	public DisplayItem(@NotNull final DisplayItem displayItem) {
		this(
			displayItem.getType(),
			displayItem.getItemStack().clone(),
			new ArrayList<>(displayItem.getSlots())
		);
	}

	public @NotNull ItemType getType() {
		return type;
	}

	public void setType(ItemType type) {
		this.type = type;
	}

	public ItemStack getItemStack() {
		return itemStack;
	}

	public void setItemStack(ItemStack itemStack) {
		this.itemStack = itemStack;
	}

	public Material getMaterial() {
		return this.itemStack != null ? this.itemStack.getType() : null;
	}

	public void setMaterial(Material material) {
		if (this.itemStack != null) this.itemStack.setType(material);
	}

	public short getData() {
		return this.itemStack != null ? this.itemStack.getDurability() : 0;
	}

	public void setData(short data) {
		if (this.itemStack != null) this.itemStack.setDurability(data);
	}

	public String getName() {
		if (this.itemStack == null) return null;

		ItemMeta itemMeta = this.itemStack.getItemMeta();

		return itemMeta != null ? itemMeta.getDisplayName() : null;
	}

	public void setName(String name) {
		if (this.itemStack == null) return;

		ItemMeta itemMeta = this.itemStack.getItemMeta();

		if (itemMeta != null) {
			itemMeta.setDisplayName(name);
		}

		this.itemStack.setItemMeta(itemMeta);
	}

	public List<String> getLore() {
		if (this.itemStack == null) return null;

		ItemMeta itemMeta = this.itemStack.getItemMeta();

		return itemMeta != null ? itemMeta.getLore() : null;
	}

	public void setLore(List<String> lore) {
		if (this.itemStack == null) return;

		ItemMeta itemMeta = this.itemStack.getItemMeta();

		if (itemMeta != null) {
			itemMeta.setLore(lore);
		}

		this.itemStack.setItemMeta(itemMeta);
	}

	public List<Integer> getSlots() {
		return slots;
	}

	public void setSlots(List<Integer> slots) {
		this.slots = slots;
	}
}
