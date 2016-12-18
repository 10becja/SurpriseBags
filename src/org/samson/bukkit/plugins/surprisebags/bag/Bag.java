package org.samson.bukkit.plugins.surprisebags.bag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Bag
implements InventoryHolder {
	public static final int INVENTORY_SIZE = 54;
	private final Material bagItemMaterial;
	private final String id;
	private String bagName = null;
	private String additionalLoreDescription;
	private List<ItemStack> itemsInBag = new ArrayList<ItemStack>();
	private int totalItemCount = 0;
	private double dropChance = 0.0;
	private double failureChance = 0.0;
	private List<String> failureLines = new ArrayList<String>();
	private List<EntityType> mobLimitList = new ArrayList<EntityType>();
	private final Inventory inventory;

	public Bag(Material bagItemMaterial, String id) {
		this.bagItemMaterial = bagItemMaterial;
		this.id = id;
		this.inventory = Bukkit.createInventory(this, 54, id);
	}

	public String getBagDisplayName() {
		return this.bagName;
	}

	public void setBagDisplayName(String bagName) {
		this.bagName = bagName;
	}

	public String getAdditionalLoreDescription() {
		return this.additionalLoreDescription;
	}

	public void setAdditionalLoreDescription(String additionalLoreDescription) {
		this.additionalLoreDescription = additionalLoreDescription;
	}

	public Material getBagItemMaterial() {
		return this.bagItemMaterial;
	}

	public String getBagId() {
		return this.id;
	}

	public void addItemToBag(ItemStack item) {
		this.itemsInBag.add(item);
		if (this.inventory.firstEmpty() != -1) {
			this.inventory.addItem(new ItemStack[]{item});
		}
		this.totalItemCount += item.getAmount();
	}

	public void setDropChance(double dropChance) {
		this.dropChance = dropChance;
	}

	public void setFailureChance(double failureChance) {
		this.failureChance = failureChance;
	}

	public double getDropChance() {
		return this.dropChance;
	}

	public double getFailureChance() {
		return this.failureChance;
	}

	public ItemStack getItemStack() {
		ItemStack bagItemStack = new ItemStack(this.bagItemMaterial);
		ItemMeta bagItemMeta = bagItemStack.getItemMeta();
		if (this.bagName != null) {
			bagItemMeta.setDisplayName(this.bagName);
		}
		ArrayList<String> loreList = new ArrayList<String>();
		loreList.add(this.id);
		if (this.additionalLoreDescription != null) {
			loreList.add(ChatColor.translateAlternateColorCodes('&', this.additionalLoreDescription));
		}
		bagItemMeta.setLore(loreList);
		bagItemStack.setItemMeta(bagItemMeta);
		return bagItemStack;
	}

	public ItemStack pickRandomItem() {
		ItemStack randomItem = null;
		if (this.totalItemCount > 0) {
			double ran = Math.random();
			int ranSlot = (int)Math.floor(ran * (double)this.totalItemCount);
			int counter = 0;
			for (ItemStack itemInBag : this.itemsInBag) {
				if (ranSlot < counter + itemInBag.getAmount()) {
					randomItem = new ItemStack(itemInBag);
					randomItem.setAmount(1);
					break;
				}
				counter += itemInBag.getAmount();
			}
		}
		return randomItem;
	}

	public List<ItemStack> getListOfItems() {
		return Collections.unmodifiableList(this.itemsInBag);
	}

	public void addFailureLIne(String line) {
		this.failureLines.add(line);
	}

	public String getRandomFailureLine() {
		String randomLine = "";
		if (this.failureLines.size() > 0) {
			int failureLineIndex = (int)Math.floor(Math.random() * (double)this.failureLines.size());
			randomLine = this.failureLines.get(failureLineIndex);
		}
		return randomLine;
	}

	public boolean hasRandomFailureLines() {
		return this.failureLines.size() > 0;
	}

	public Inventory getInventory() {
		return this.inventory;
	}

	public void addLimitMob(EntityType mob) {
		this.mobLimitList.add(mob);
	}

	public boolean canBeDropFromMob(EntityType deadEntity) {
		if (this.mobLimitList.size() > 0) {
			return this.mobLimitList.contains((Object)deadEntity);
		}
		return true;
	}
}