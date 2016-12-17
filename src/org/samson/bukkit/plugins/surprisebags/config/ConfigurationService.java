package org.samson.bukkit.plugins.surprisebags.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.samson.bukkit.plugins.surprisebags.SurpriseBags;
import org.samson.bukkit.plugins.surprisebags.config.ConfigurationIOError;
import org.samson.bukkit.plugins.surprisebags.config.InvalidParameterError;
import org.samson.bukkit.plugins.surprisebags.config.MissingTemplateException;

public class ConfigurationService {
    private final SurpriseBags plugin;
    private String dropLimitToWorld;
    private String dropLimitToArea;
    private boolean dropFromMobs;
    private boolean allowDropFromSpanwers;
    private boolean allowDropFromEggSpanwers;

    public ConfigurationService(SurpriseBags plugin) {
        this.plugin = plugin;
        this.readGlobals();
    }

    public synchronized Set<String> getBagList() {
        List listOfBags = this.plugin.getConfig().getStringList("Bags");
        String yamlFilename = "bags.yml";
        File file = new File(this.plugin.getDataFolder(), "bags.yml");
        YamlConfiguration yamlConfig = null;
        if (file.exists()) {
            yamlConfig = YamlConfiguration.loadConfiguration((File)file);
        } else {
            InputStream configurationStream = this.plugin.getResource("bags.yml");
            if (configurationStream != null) {
                this.plugin.saveResource("bags.yml", false);
                yamlConfig = YamlConfiguration.loadConfiguration((InputStream)configurationStream);
            } else {
                this.plugin.getLogger().warning("Cannot find bags file (update plugin?)");
            }
        }
        if (yamlConfig != null) {
            List customBagList = yamlConfig.getStringList("bags");
            listOfBags.addAll(customBagList);
        }
        HashSet<String> bagsSet = new HashSet<String>(listOfBags);
        return bagsSet;
    }

    public String getWorldLimitName() {
        return this.dropLimitToWorld;
    }

    public String getAreaLimitName() {
        return this.dropLimitToArea;
    }

    public YamlConfiguration getYamlConfigurationByBagId(String bagId) {
        String bagFile = this.getBagFileNameByBagId(bagId);
        if (bagFile != null) {
            return this.getConfigurationFromFile(bagFile);
        }
        return null;
    }

    public boolean isMobsDropsEnabled() {
        return this.dropFromMobs;
    }

    public synchronized void createNewBagConfig(String bagId, String bagName, double dropChance) throws ConfigurationIOError, MissingTemplateException {
        YamlConfiguration templateConfig = null;
        templateConfig = this.getTemplateYamlConfiguration();
        if (templateConfig != null) {
            this.saveNewBagToCustomBagsList(bagId);
            File bagFile = new File(this.plugin.getDataFolder(), bagId + ".yml");
            try {
                if (bagName != null && !StringUtils.isEmpty((String)bagName)) {
                    templateConfig.set("displayname", (Object)bagName);
                }
                templateConfig.set("drop-chance", (Object)dropChance);
                templateConfig.save(bagFile);
            }
            catch (IOException e) {
                throw new ConfigurationIOError(e.getMessage());
            }
        }
    }

    public boolean isAllowedDropFromSpawners() {
        return this.allowDropFromSpanwers;
    }

    private synchronized YamlConfiguration getConfigurationFromFile(String filename) {
        File file = new File(this.plugin.getDataFolder(), filename);
        YamlConfiguration yamlConfig = null;
        if (file.exists()) {
            yamlConfig = YamlConfiguration.loadConfiguration((File)file);
        } else {
            InputStream configurationStream = this.plugin.getResource(filename);
            if (configurationStream != null) {
                this.plugin.saveResource(filename, false);
                yamlConfig = YamlConfiguration.loadConfiguration((InputStream)configurationStream);
            } else {
                this.plugin.getLogger().warning("Cannot find configuration file by the name " + filename);
            }
        }
        return yamlConfig;
    }

    public synchronized void saveConfigurationToFile(YamlConfiguration yamlConfig, String bagId) throws ConfigurationIOError {
        File file = new File(this.plugin.getDataFolder(), bagId + ".yml");
        try {
            yamlConfig.save(file);
        }
        catch (IOException e) {
            throw new ConfigurationIOError(e.getMessage());
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private YamlConfiguration getTemplateYamlConfiguration() throws MissingTemplateException {
        YamlConfiguration templateConfig = null;
        File templateFile = new File(this.plugin.getDataFolder(), "template.yml");
        if (!templateFile.exists()) {
            InputStream configurationStream = this.plugin.getResource("template.yml");
            if (configurationStream == null) throw new MissingTemplateException("Template file does not exist. Cannot add new bags.");
            this.plugin.saveResource("template.yml", false);
            return YamlConfiguration.loadConfiguration((InputStream)configurationStream);
        }
        templateConfig = new YamlConfiguration();
        try {
            templateConfig.load(templateFile);
            return templateConfig;
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
            return templateConfig;
        }
        catch (IOException e) {
            e.printStackTrace();
            return templateConfig;
        }
        catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }
        return templateConfig;
    }

    private synchronized void saveNewBagToCustomBagsList(String bagId) throws ConfigurationIOError {
        List<String> bagList;
        YamlConfiguration bagListYamlConfig = new YamlConfiguration();
        File bagsFile = new File(this.plugin.getDataFolder(), "bags.yml");
        if (bagsFile.exists()) {
            try {
                bagListYamlConfig.load(bagsFile);
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            catch (InvalidConfigurationException e) {
                e.printStackTrace();
            }
            if (!bagListYamlConfig.isSet("bags")) {
                throw new ConfigurationIOError("Error in reading the bags list");
            }
        }
        if ((bagList = bagListYamlConfig.getStringList("bags")) == null) {
            bagList = new ArrayList<String>();
        }
        if (!bagList.contains(bagId)) {
            bagList.add(bagId);
            bagListYamlConfig.set("bags", bagList);
        }
        try {
            bagListYamlConfig.save(bagsFile);
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new ConfigurationIOError("Error in saving the bags list");
        }
    }

    private String getBagFileNameByBagId(String bagId) {
        if (StringUtils.isNotEmpty((String)bagId)) {
            return bagId + ".yml";
        }
        return null;
    }

    public synchronized void removeCustomBag(String bagId) throws ConfigurationIOError, InvalidParameterError {
        YamlConfiguration bagListYamlConfig = new YamlConfiguration();
        File bagsFile = new File(this.plugin.getDataFolder(), "bags.yml");
        if (bagsFile.exists()) {
            try {
                bagListYamlConfig.load(bagsFile);
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            catch (InvalidConfigurationException e) {
                e.printStackTrace();
            }
        }
        if (!bagListYamlConfig.isSet("bags")) {
            throw new ConfigurationIOError("Error in reading the bags list");
        }
        List customBagList = bagListYamlConfig.getStringList("bags");
        if (customBagList.contains(bagId)) {
            customBagList.remove(bagId);
            bagListYamlConfig.set("bags", (Object)customBagList);
            try {
                bagListYamlConfig.save(bagsFile);
            }
            catch (IOException e) {
                e.printStackTrace();
                throw new ConfigurationIOError("Error in removing the bag from the custom bags list. Please proceed manually.");
            }
        } else {
            throw new InvalidParameterError();
        }
    }

    public void readGlobals() {
        this.dropLimitToWorld = this.plugin.getConfig().getString("drop-limit-to-world");
        this.dropLimitToArea = this.plugin.getConfig().getString("drop-limit-to-area");
        this.dropFromMobs = this.plugin.getConfig().getBoolean("drop-from-mobs", true);
        this.allowDropFromSpanwers = this.plugin.getConfig().getBoolean("allow-drop-from-spawners", false);
        this.allowDropFromEggSpanwers = this.plugin.getConfig().getBoolean("allow-drop-from-eggs", false);
    }

    public boolean isAllowedDropFromEggSpawners() {
        return this.allowDropFromEggSpanwers;
    }
}