package org.samson.bukkit.plugins.surprisebags;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.samson.bukkit.plugins.surprisebags.bag.Bag;
import org.samson.bukkit.plugins.surprisebags.config.ConfigurationIOError;
import org.samson.bukkit.plugins.surprisebags.config.InvalidParameterError;
import org.samson.bukkit.plugins.surprisebags.config.MissingTemplateException;
import org.samson.bukkit.plugins.surprisebags.service.BagService;

public class SurpriseBagsCommandExecutor
implements CommandExecutor {
    private SurpriseBags plugin;

    public SurpriseBagsCommandExecutor(SurpriseBags plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("surprisebags") && args.length > 0) {
            return this.dispatchSubCommad(command, args[0], sender, args);
        }
        return false;
    }

    private boolean dispatchSubCommad(Command command, String subcommand, CommandSender sender, String[] args) {
        if (subcommand.equalsIgnoreCase("version")) {
            sender.sendMessage((Object)ChatColor.GREEN + this.plugin.getName() + " version " + this.plugin.getDescription().getVersion());
            return true;
        }
        if (subcommand.equalsIgnoreCase("spawn")) {
            if (args.length > 1) {
                this.handleSpawnCommand(sender, args);
            } else {
                sender.sendMessage((Object)ChatColor.RED + "Usage: " + command.getName() + " spawn <bag> [count] [player]");
            }
            return true;
        }
        if (subcommand.equalsIgnoreCase("list")) {
            this.handleListCommand(sender);
            return true;
        }
        if (subcommand.equalsIgnoreCase("reload")) {
            this.plugin.reloadPlugin();
            sender.sendMessage((Object)ChatColor.GREEN + this.plugin.getName() + " reloaded! ");
            return true;
        }
        if (subcommand.equalsIgnoreCase("edit")) {
            if (args.length > 1) {
                this.handleEditCommand(sender, args);
            } else {
                sender.sendMessage((Object)ChatColor.RED + "Usage: " + command.getName() + " edit <bag> ");
            }
            return true;
        }
        if (subcommand.equalsIgnoreCase("save")) {
            if (args.length > 1) {
                this.handleSaveCommand(sender, args);
            } else {
                sender.sendMessage((Object)ChatColor.RED + "Usage: " + command.getName() + " save <bag> ");
            }
            return true;
        }
        if (subcommand.equalsIgnoreCase("addbag")) {
            if (args.length > 1) {
                this.handleAddBagCommand(sender, args);
            } else {
                sender.sendMessage((Object)ChatColor.RED + "Usage: " + command.getName() + " addbag <bag-id> [name] [drop-chance]");
            }
            return true;
        }
        if (subcommand.equalsIgnoreCase("removebag")) {
            if (args.length > 1) {
                this.handleRemoveBagCommand(sender, args);
            } else {
                sender.sendMessage((Object)ChatColor.RED + "Usage: " + command.getName() + " removebag <bag-id>");
            }
            return true;
        }
        return false;
    }

    private void handleRemoveBagCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("surprisebags.edit")) {
            sender.sendMessage((Object)ChatColor.RED + "You do not have permission to remove bags.");
            return;
        }
        String bagId = args[1];
        if (StringUtils.isAsciiPrintable((String)bagId)) {
            BagService bagService = this.plugin.getBagService();
            Bag bag = bagService.getBagById(bagId);
            if (bag != null) {
                Inventory bagInventory = bag.getInventory();
                if (bagInventory.getViewers().size() > 0) {
                    sender.sendMessage((Object)ChatColor.RED + "Error: Other admins are currently editing the bag. Cannot remove it.");
                } else {
                    this.removeCustomBag(sender, bagId);
                }
            } else {
                sender.sendMessage((Object)ChatColor.RED + "Could not find a bag with the id " + bagId);
            }
        } else {
            sender.sendMessage((Object)ChatColor.RED + "The id " + bagId + " is not a legal bag id!");
        }
    }

    private void removeCustomBag(CommandSender sender, String bagId) {
        boolean isOK = false;
        try {
            this.plugin.removeCustomBag(bagId);
            isOK = true;
        }
        catch (ConfigurationIOError e) {
            sender.sendMessage((Object)ChatColor.RED + "Could not update the configuration files (bags.yml exists?)!");
        }
        catch (InvalidParameterError e) {
            sender.sendMessage((Object)ChatColor.RED + "Could not remove this bag (reload and try again, or check config.yml for the list of old bags)");
        }
        if (isOK) {
            sender.sendMessage((Object)ChatColor.GREEN + "The bag " + bagId + " wad removed!");
            sender.sendMessage((Object)ChatColor.GREEN + "Run /sbag reload to apply the changes.");
        } else {
            this.plugin.getLogger().warning("Attempt to remove the bag " + bagId + " failed.");
        }
    }

    private void handleAddBagCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("surprisebags.edit")) {
            sender.sendMessage((Object)ChatColor.RED + "You do not have permission to add new bags.");
            return;
        }
        String bagId = args[1];
        if (StringUtils.isAsciiPrintable((String)bagId)) {
            BagService bagService = this.plugin.getBagService();
            Bag bag = bagService.getBagById(bagId);
            if (bag == null) {
                String bagName = null;
                if (args.length > 2) {
                    bagName = StringUtils.replaceChars((String)args[2], (char)'_', (char)' ');
                }
                double dropChance = 0.0;
                if (args.length > 3) {
                    try {
                        dropChance = Double.parseDouble(args[3]);
                    }
                    catch (NumberFormatException e) {
                        sender.sendMessage((Object)ChatColor.RED + "The drop chance you specified is not a legal number. Using 0 drop chance for now.");
                    }
                    if (dropChance > 1.0) {
                        sender.sendMessage((Object)ChatColor.RED + "The drop chance you specified cannot be greater than 1. Using 0 drop chance for now.");
                        dropChance = 0.0;
                    }
                }
                this.addNewBag(sender, bagId, bagName, dropChance);
            } else {
                sender.sendMessage((Object)ChatColor.RED + "A bag with the id " + bagId + " already exists!");
            }
        } else {
            sender.sendMessage((Object)ChatColor.RED + "The id " + bagId + " is not a legal bag id!");
        }
    }

    private void addNewBag(CommandSender sender, String bagId, String bagName, double dropChance) {
        boolean isOK = false;
        try {
            this.plugin.addCustomBag(bagId, bagName, dropChance);
            isOK = true;
        }
        catch (ConfigurationIOError e) {
            sender.sendMessage((Object)ChatColor.RED + "Could not write configuration files!");
            e.printStackTrace();
        }
        catch (MissingTemplateException e) {
            sender.sendMessage((Object)ChatColor.RED + "Could not write configuration files!");
            e.printStackTrace();
        }
        if (isOK) {
            sender.sendMessage((Object)ChatColor.GREEN + "An empty bag with the id " + bagId + " wad added!");
            sender.sendMessage((Object)ChatColor.GREEN + "Run /sbag reload to apply the changes.");
        }
    }

    private void handleSpawnCommand(CommandSender sender, String[] args) {
        String bagId = args[1];
        BagService bagService = this.plugin.getBagService();
        Bag bag = bagService.getBagById(bagId);
        if (bag != null) {
            Player player = null;
            if (args.length > 3) {
                String playerName = args[3];
                player = this.plugin.getServer().getPlayer(playerName);
                if (player == null || !player.isOnline()) {
                    sender.sendMessage((Object)ChatColor.RED + "Player " + playerName + " could not be found!");
                }
            } else if (sender instanceof Player) {
                player = (Player)sender;
            }
            if (player != null) {
                int itemCount;
                String bagItemCount;
                ItemStack bagItemStack = bag.getItemStack();
                if (args.length > 2 && StringUtils.isNumeric((String)(bagItemCount = args[2])) && (itemCount = Integer.parseInt(bagItemCount)) <= bagItemStack.getMaxStackSize() && itemCount > 0) {
                    bagItemStack.setAmount(itemCount);
                }
                if (player.getInventory().firstEmpty() == -1) {
                    sender.sendMessage((Object)ChatColor.RED + "The player's inventory is full!");
                } else {
                    player.getInventory().addItem(new ItemStack[]{bagItemStack});
                    sender.sendMessage((Object)ChatColor.GREEN + "Giving " + player.getName() + " a " + bagId + " bag!");
                }
            }
        } else {
            sender.sendMessage((Object)ChatColor.RED + "Cannot find bag by the name: " + bagId);
        }
    }

    private void handleListCommand(CommandSender sender) {
        BagService bagService = this.plugin.getBagService();
        Set<String> items = bagService.getBagIdList();
        if (items != null) {
            String itemListMsg = StringUtils.join(items, (char)' ');
            sender.sendMessage((Object)ChatColor.GREEN + " Available bags: ");
            sender.sendMessage(itemListMsg);
        }
    }

    private void handleSaveCommand(CommandSender sender, String[] args) {
        String bagId = args[1];
        if (!sender.hasPermission("surprisebags.edit")) {
            sender.sendMessage((Object)ChatColor.RED + "You do not have permission to edit the bags.");
            return;
        }
        BagService bagService = this.plugin.getBagService();
        Bag bag = bagService.getBagById(bagId);
        if (bag != null) {
            YamlConfiguration yamlConfig = this.plugin.getConfigService().getYamlConfigurationByBagId(bagId);
            ArrayList<ItemStack> newList = new ArrayList<ItemStack>();
            Inventory inventory = bag.getInventory();
            ItemStack[] inventoryContents = inventory.getContents();
            ItemStackColorTranslator itemStackTranslator = new ItemStackColorTranslator();
            for (int i = 0; i < inventoryContents.length; ++i) {
                if (inventoryContents[i] == null) continue;
                itemStackTranslator.convertItemStackToSafeColorCharacter(inventoryContents[i]);
                newList.add(inventoryContents[i]);
            }
            yamlConfig.set("items", newList);
            try {
                this.plugin.getConfigService().saveConfigurationToFile(yamlConfig, bagId);
                sender.sendMessage((Object)ChatColor.GREEN + "Bag " + bagId + " saved.");
                sender.sendMessage((Object)ChatColor.GREEN + "Please reload the plugin to make effective.");
            }
            catch (ConfigurationIOError e) {
                this.plugin.getLogger().warning("Cannot save config file for bag " + bagId + ". Error: " + e.getMessage());
                sender.sendMessage((Object)ChatColor.RED + "Could not save the bag. Please review the server logs.");
            }
        } else {
            sender.sendMessage((Object)ChatColor.RED + "Cannot find bag by the name: " + bagId);
        }
    }

    private void handleEditCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("surprisebags.edit")) {
            sender.sendMessage((Object)ChatColor.RED + "You do not have permission to edit the bags.");
            return;
        }
        String bagId = args[1];
        BagService bagService = this.plugin.getBagService();
        Bag bag = bagService.getBagById(bagId);
        if (bag != null) {
            List<ItemStack> items = bag.getListOfItems();
            if (items != null) {
                if (sender instanceof Player) {
                    Player player = (Player)sender;
                    player.sendMessage((Object)ChatColor.GREEN + "You are now editing the " + bagId + " bag.");
                    player.sendMessage((Object)ChatColor.GREEN + "To save the changes, please use /sbag save " + bagId);
                    Inventory bagInventory = bag.getInventory();
                    if (bagInventory.getViewers().size() > 0) {
                        player.sendMessage((Object)ChatColor.RED + "Warning: Other admins are currently editing the bag. ");
                    }
                    player.openInventory(bagInventory);
                } else {
                    for (ItemStack item : items) {
                        sender.sendMessage(item.toString());
                    }
                }
            }
        } else {
            sender.sendMessage((Object)ChatColor.RED + "Cannot find bag by the name: " + bagId);
        }
    }
}