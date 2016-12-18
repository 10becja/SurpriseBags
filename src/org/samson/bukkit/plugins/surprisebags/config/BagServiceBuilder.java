package org.samson.bukkit.plugins.surprisebags.config;

import java.util.List;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.samson.bukkit.plugins.surprisebags.ItemStackColorTranslator;
import org.samson.bukkit.plugins.surprisebags.SurpriseBags;
import org.samson.bukkit.plugins.surprisebags.bag.Bag;
import org.samson.bukkit.plugins.surprisebags.service.BagService;

public class BagServiceBuilder {
    private final SurpriseBags plugin;
    private final ConfigurationService configService;
    private final Set<String> listOfBags;

    public BagServiceBuilder(SurpriseBags plugin, ConfigurationService configService, Set<String> listOfBags) {
        this.plugin = plugin;
        this.configService = configService;
        this.listOfBags = listOfBags;
    }

    public BagService build() {
        BagService bagService = new BagService();
        for (String bagId : this.listOfBags) {
            YamlConfiguration bagConfig = this.configService.getYamlConfigurationByBagId(bagId);
            if (bagConfig != null) {
                List<String> limitToMobsList = bagConfig.getStringList("limit-mob");
                String materialName = bagConfig.getString("material");
                Material material = null;
                if (materialName != null) {
                    material = Material.getMaterial((String)materialName);
                }
                if (material == null) {
                    material = Material.CHEST;
                }
                String displayName = bagConfig.getString("displayname", bagId);
                Bag bagItem = new Bag(material, bagId);
                bagItem.setBagDisplayName(ChatColor.translateAlternateColorCodes((char)'&', (String)displayName));
                String additionalLore = bagConfig.getString("openmsg");
                if (additionalLore != null) {
                    bagItem.setAdditionalLoreDescription(bagConfig.getString("openmsg"));
                }
                bagItem.setDropChance(bagConfig.getDouble("drop-chance", 0.0));
                bagItem.setFailureChance(bagConfig.getDouble("failure-chance", 0.0));
                List<String> failureLines = bagConfig.getStringList("failure-lines");
                for (String line : failureLines) {
                    bagItem.addFailureLIne(line);
                }
                ItemStackColorTranslator itemStackTranslator = new ItemStackColorTranslator();
                List<?> itemsInBag = bagConfig.getList("items");
                if (itemsInBag != null) {
                    for (Object item : itemsInBag) {
                        if (!(item instanceof ItemStack)) continue;
                        ItemStack itemStack = (ItemStack)item;
                        itemStackTranslator.translateItemNameColors(itemStack);
                        bagItem.addItemToBag(itemStack);
                    }
                }
                if (limitToMobsList != null) {
                    for (String mob : limitToMobsList) {
                        EntityType ent = this.plugin.getCreatureMapper().getEntityTypeByCreatureName(mob);
                        if (ent == null) continue;
                        bagItem.addLimitMob(ent);
                    }
                }
                bagService.addItem(bagId, bagItem);
                continue;
            }
            this.plugin.getLogger().warning("Could not load config for bag type " + bagId);
        }
        return bagService;
    }
}