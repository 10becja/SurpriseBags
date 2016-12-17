package org.samson.bukkit.plugins.surprisebags.bag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.samson.bukkit.plugins.surprisebags.SurpriseBags;
import org.samson.bukkit.plugins.surprisebags.bag.Bag;
import org.samson.bukkit.plugins.surprisebags.service.BagService;

public class BagController {
    private final SurpriseBags plugin;
    private final BagService bagService;

    public BagController(SurpriseBags plugin, BagService bagService) {
        this.plugin = plugin;
        this.bagService = bagService;
    }

    public boolean handleBagOpen(Player player, ItemStack item, String loreFirstLine) {
        Bag bag = this.bagService.getBagById(loreFirstLine);
        if (bag != null) {
            double rand;
            if (!player.hasPermission("surprisebags.openbag")) {
                player.sendMessage(ChatColor.RED + " You do not have permission to open this bag!");
                return true;
            }
            boolean failure = false;
            double failureChance = bag.getFailureChance();
            if (failureChance != 0.0 && (rand = Math.random()) < failureChance) {
                failure = true;
                PotionEffect effect = this.getRandomNegativePotionEffect();
                player.addPotionEffect(effect);
                if (bag.hasRandomFailureLines()) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)bag.getRandomFailureLine()));
                }
                player.getWorld().playEffect(player.getLocation(), Effect.POTION_BREAK, 0);
            }
            PlayerInventory playerInventory = player.getInventory();
            if (failure || playerInventory.firstEmpty() != -1) {
                if (!failure) {
                    ItemStack surprise = bag.pickRandomItem();
                    String surpriseDisplayString = this.getSurpriseDisplayString(surprise);
                    if (surprise != null) {
                        playerInventory.addItem(new ItemStack[]{surprise});
                        player.sendMessage(ChatColor.DARK_GREEN + "You got " + surpriseDisplayString);
                        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.8f, 0.4f);
                    } else {
                        player.sendMessage(ChatColor.RED + " The bag was empty!");
                    }
                    if (this.plugin.getConfig().getBoolean("openingspymessage", true)) {
                        Command.broadcastCommandMessage((CommandSender)player, (String)(ChatColor.DARK_GREEN + "[SBAG] " + ChatColor.GRAY + player.getName() + " opened a " + loreFirstLine + ". Got " + surpriseDisplayString), (boolean)player.isOp());
                    }
                }
                item.setAmount(item.getAmount() - 1);
                playerInventory.setItemInHand(item);
                player.updateInventory();
            } else {
                player.sendMessage(ChatColor.RED + " No empty slots in your inventory!");
            }
            return true;
        }
        return false;
    }

    private String getSurpriseDisplayString(ItemStack surprise) {
        String surpriseDisplayString = "";
        if (surprise != null) {
            if (surprise.hasItemMeta() && surprise.getItemMeta().hasDisplayName()) {
                surpriseDisplayString = surprise.getItemMeta().getDisplayName();
            } else if (surprise.getType() == Material.ENCHANTED_BOOK && surprise.hasItemMeta()) {
                EnchantmentStorageMeta bookMeta = (EnchantmentStorageMeta)surprise.getItemMeta();
                Map<Enchantment, Integer> enchants = bookMeta.getStoredEnchants();
                Entry<Enchantment, Integer> enchantEntry = enchants.entrySet().iterator().next();
                Enchantment enchant = (Enchantment)enchantEntry.getKey();
                surpriseDisplayString = enchant.getName() + " " + enchantEntry.getValue() + " book";
            } else if (surprise.getType() == Material.POTION) {
                Potion potion = Potion.fromDamage((int)(surprise.getDurability() & 63));
                surpriseDisplayString = potion.getType().toString() + (potion.isSplash() ? " splash" : "") + " potion";
            } else {
                Map<Enchantment, Integer> enchants = surprise.getEnchantments();
                if (!enchants.isEmpty()) {
                    Entry<Enchantment, Integer> enchantEntry = enchants.entrySet().iterator().next();
                    Enchantment enchant = (Enchantment)enchantEntry.getKey();
                    surpriseDisplayString = enchant.getName() + " " + enchantEntry.getValue() + " ";
                }
                surpriseDisplayString = surpriseDisplayString + surprise.getType().toString();
            }
        }
        if (surpriseDisplayString == "") {
            surpriseDisplayString = "nothing";
        }
        return surpriseDisplayString;
    }

    public List<ItemStack> getBagDrops(LivingEntity deadEntity) {
        ArrayList<ItemStack> drops = new ArrayList<ItemStack>();
        List<Bag> bagItems = this.bagService.getRandomBags(deadEntity);
        for (Bag bagItem : bagItems) {
            ItemStack drop = bagItem.getItemStack();
            if (drop == null) continue;
            drops.add(drop);
        }
        return drops;
    }

    private PotionEffect getRandomNegativePotionEffect() {
        ArrayList<PotionEffectType> effectTypeList = new ArrayList<PotionEffectType>();
        effectTypeList.add(PotionEffectType.BLINDNESS);
        effectTypeList.add(PotionEffectType.CONFUSION);
        effectTypeList.add(PotionEffectType.POISON);
        effectTypeList.add(PotionEffectType.SLOW);
        int randomEffectTypeIndex = (int)Math.floor(Math.random() * (double)effectTypeList.size());
        PotionEffectType randomEffectType = (PotionEffectType)effectTypeList.get(randomEffectTypeIndex);
        int randomDuration = 20 * (3 + (int)(Math.random() * 10.0));
        int randomAmplifier = (int)Math.floor(Math.random() * 4.0);
        return new PotionEffect(randomEffectType, randomDuration, randomAmplifier);
    }
}