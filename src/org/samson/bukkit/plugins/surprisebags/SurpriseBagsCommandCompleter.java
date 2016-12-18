package org.samson.bukkit.plugins.surprisebags;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class SurpriseBagsCommandCompleter
implements TabCompleter {
    private final SurpriseBags plugin;

    public SurpriseBagsCommandCompleter(SurpriseBags plugin) {
        this.plugin = plugin;
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        ArrayList<String> bagStringList = new ArrayList<String>();
        if (args.length == 2 && (args[0].equalsIgnoreCase("spawn") || args[0].equalsIgnoreCase("edit") || args[0].equalsIgnoreCase("save") || args[0].equalsIgnoreCase("removebag"))) {
            Set<String> bagsIds = this.plugin.getBagService().getBagIdList();
            String prefix = args[1];
            for (String bagId : bagsIds) {
                if (!bagId.toLowerCase().startsWith(prefix.toLowerCase())) continue;
                bagStringList.add(bagId);
            }
        }
        return bagStringList;
    }
}