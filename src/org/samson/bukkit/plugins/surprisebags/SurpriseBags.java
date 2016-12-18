package org.samson.bukkit.plugins.surprisebags;

import java.util.Set;

import org.bukkit.World;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.samson.bukkit.plugins.surprisebags.bag.BagController;
import org.samson.bukkit.plugins.surprisebags.config.BagServiceBuilder;
import org.samson.bukkit.plugins.surprisebags.config.ConfigurationIOError;
import org.samson.bukkit.plugins.surprisebags.config.ConfigurationService;
import org.samson.bukkit.plugins.surprisebags.config.InvalidParameterError;
import org.samson.bukkit.plugins.surprisebags.config.MissingTemplateException;
import org.samson.bukkit.plugins.surprisebags.service.BagService;

import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class SurpriseBags extends JavaPlugin {
    private final SurpriseBagsCommandExecutor commandExecutor;
    private final TabCompleter commandCompleter;
    private final SurpriseBagsEventListener eventListener;
    private final MobNameToEntityMapper creatureMapper;
    private final ConfigurationService configService;
    private BagService bagService;
    private BagController bagController;

    public SurpriseBags() {
        this.commandExecutor = new SurpriseBagsCommandExecutor(this);
        this.commandCompleter = new SurpriseBagsCommandCompleter(this);
        this.eventListener = new SurpriseBagsEventListener(this);
        this.creatureMapper = new MobNameToEntityMapper();
        this.configService = new ConfigurationService(this);
    }

    public void onDisable() {
    }

    public void onEnable() {
        this.saveDefaultConfig();
        PluginManager pm = this.getServer().getPluginManager();
        Set<String> listOfBags = this.configService.getBagList();
        this.bagService = new BagServiceBuilder(this, this.configService, listOfBags).build();
        this.bagController = new BagController(this, this.bagService);
        this.getCommand("surprisebags").setExecutor((CommandExecutor)this.commandExecutor);
        this.getCommand("surprisebags").setTabCompleter(this.commandCompleter);
        pm.registerEvents((Listener)this.eventListener, (Plugin)this);
    }

    public BagService getBagService() {
        return this.bagService;
    }

    public void reloadPlugin() {
        this.reloadConfig();
        this.configService.readGlobals();
        this.bagService.clearItems();
        Set<String> listOfBags = this.configService.getBagList();
        this.bagService = new BagServiceBuilder(this, this.configService, listOfBags).build();
        this.bagController = new BagController(this, this.bagService);
    }

    public BagController getBagController() {
        return this.bagController;
    }

    public boolean canMobDrop(CreatureSpawnEvent.SpawnReason spawnReason, LivingEntity entity) {
        boolean canMobDrop = false;
        if (this.configService.isMobsDropsEnabled() && this.mobPassSpawnReason(spawnReason) && this.mobPassWorldTest(entity) && this.mobPassAreaTest(entity)) {
            canMobDrop = true;
        }
        return canMobDrop;
    }

    public MobNameToEntityMapper getCreatureMapper() {
        return this.creatureMapper;
    }

    public void addCustomBag(String bagId, String bagName, double dropChance) throws ConfigurationIOError, MissingTemplateException {
        this.configService.createNewBagConfig(bagId, bagName, dropChance);
    }

    public ConfigurationService getConfigService() {
        return this.configService;
    }

    public void removeCustomBag(String bagId) throws ConfigurationIOError, InvalidParameterError {
        this.configService.removeCustomBag(bagId);
    }

    private boolean mobPassSpawnReason(CreatureSpawnEvent.SpawnReason spawnReason) {
        boolean allowSpawnerDrops = this.configService.isAllowedDropFromSpawners();
        boolean allowSpawnerEggDrops = this.configService.isAllowedDropFromEggSpawners();
        if (!(spawnReason == CreatureSpawnEvent.SpawnReason.NATURAL || allowSpawnerDrops && spawnReason == CreatureSpawnEvent.SpawnReason.SPAWNER || allowSpawnerEggDrops && spawnReason == CreatureSpawnEvent.SpawnReason.SPAWNER_EGG)) {
            return false;
        }
        return true;
    }

    private boolean mobPassWorldTest(LivingEntity entity) {
        String limitedWorld = this.configService.getWorldLimitName();
        if (limitedWorld != null && !entity.getWorld().getName().equalsIgnoreCase(limitedWorld)) {
            return false;
        }
        return true;
    }

    private boolean mobPassAreaTest(LivingEntity entity) {
        boolean isMobInArea;
        isMobInArea = false;
        String limitedArea = this.configService.getAreaLimitName();
        if (limitedArea != null) {
            try {
                RegionManager regionManager = WGBukkit.getRegionManager((World)entity.getWorld());
                if (regionManager != null) {
                    ApplicableRegionSet wgRegions = regionManager.getApplicableRegions(entity.getLocation());
                    for (ProtectedRegion protectedRegion : wgRegions) {
                        if (!protectedRegion.getId().equals(limitedArea)) continue;
                        isMobInArea = true;
                    }
                }
            }
            catch (NoClassDefFoundError e) {
                this.getLogger().warning("limited area configuration is set, but worldguard is not enabled!");
            }
        } else {
            isMobInArea = true;
        }
        return isMobInArea;
    }
}
