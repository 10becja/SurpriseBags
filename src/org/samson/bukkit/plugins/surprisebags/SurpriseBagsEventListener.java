package org.samson.bukkit.plugins.surprisebags;

import java.util.List;
import java.util.Map;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.samson.bukkit.plugins.surprisebags.bag.BagController;

public class SurpriseBagsEventListener
implements Listener {
    private static final double LOOTING_CHANCE_INCREASE_PER_LEVEL = 0.05;
    private SurpriseBags plugin;

    public SurpriseBagsEventListener(SurpriseBags plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        List<?> itemLore;
        String loreFirstLine;
        ItemStack item;
        if ((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) && (item = event.getItem()) != null && item.hasItemMeta() && item.getItemMeta().hasLore() && (itemLore = item.getItemMeta().getLore()).size() > 0 && !(loreFirstLine = (String)itemLore.get(0)).isEmpty()) {
            Player player = event.getPlayer();
            BagController bagController = plugin.getBagController();
            boolean success = bagController.handleBagOpen(player, item, loreFirstLine);
            if (success) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority=EventPriority.HIGH, ignoreCancelled=true)
    public void onCreatureSpawnEvent(CreatureSpawnEvent event) {
        LivingEntity entity = event.getEntity();
        CreatureSpawnEvent.SpawnReason spawnReason = event.getSpawnReason();
        if (entity instanceof Monster && plugin.canMobDrop(spawnReason, entity)) {
            entity.setMetadata("mightdropbag", (MetadataValue)new FixedMetadataValue(plugin, (Object)true));
        }
    }

    @EventHandler(priority=EventPriority.HIGH, ignoreCancelled=true)
    public void onEntityDeathEvent(EntityDeathEvent event) {
        LivingEntity deadEntity = event.getEntity();
        Player player = deadEntity.getKiller();
        if (player != null && player.isOnline() && deadEntity.hasMetadata("mightdropbag")) {
            double dropChance = plugin.getConfig().getDouble("drop-chance", 0.0);
            double rand = Math.random();
            boolean isDropping = false;
            if (rand < dropChance) {
                isDropping = true;
            } else {
                ItemStack itemInHand = player.getItemInHand();
                Map<Enchantment, Integer> enchantments = itemInHand.getEnchantments();
                Integer level = enchantments.get(Enchantment.LOOT_BONUS_MOBS);
                if (plugin.getConfig().getBoolean("looting-sensitive", false) && itemInHand != null && level != null) {
                    dropChance *= 1.0 + (double)level.intValue() * LOOTING_CHANCE_INCREASE_PER_LEVEL;
                }
                if (rand < dropChance) {
                    isDropping = true;
                }
            }
            if (isDropping) {
                BagController bagController = this.plugin.getBagController();
                List<ItemStack> bagDrops = bagController.getBagDrops(deadEntity);
                List<ItemStack> existingDrops = event.getDrops();
                existingDrops.addAll(bagDrops);
            }
        }
    }
}