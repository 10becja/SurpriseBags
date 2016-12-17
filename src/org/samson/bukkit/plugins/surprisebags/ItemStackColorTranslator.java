package org.samson.bukkit.plugins.surprisebags;

import java.util.List;
import java.util.ListIterator;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.samson.bukkit.plugins.surprisebags.ColorCodeTranslator;

public class ItemStackColorTranslator {
    public void translateItemNameColors(ItemStack itemStack) {
        MinecraftColorCodeTranslator translator = new MinecraftColorCodeTranslator();
        this.applyOnDisplayName(itemStack, translator);
        this.applyOnLoreStrings(itemStack, translator);
    }

    public void convertItemStackToSafeColorCharacter(ItemStack itemStack) {
        ANSIColorCodeTranslator translator = new ANSIColorCodeTranslator();
        this.applyOnDisplayName(itemStack, translator);
        this.applyOnLoreStrings(itemStack, translator);
    }

    private void applyOnDisplayName(ItemStack itemStack, ColorCodeTranslator translator) {
        ItemMeta itemMeta;
        if (itemStack.hasItemMeta() && (itemMeta = itemStack.getItemMeta()).hasDisplayName()) {
            String itemDisplayName = itemMeta.getDisplayName();
            String translatedDisplayName = translator.translate(itemDisplayName);
            itemMeta.setDisplayName(translatedDisplayName);
            itemStack.setItemMeta(itemMeta);
        }
    }

    private void applyOnLoreStrings(ItemStack itemStack, ColorCodeTranslator translator) {
        ItemMeta itemMeta;
        if (itemStack.hasItemMeta() && (itemMeta = itemStack.getItemMeta()).hasLore()) {
            List loreList = itemMeta.getLore();
            ListIterator<String> iterator = loreList.listIterator();
            while (iterator.hasNext()) {
                String loreString = (String)iterator.next();
                iterator.set(translator.translate(loreString));
            }
            itemMeta.setLore(loreList);
            itemStack.setItemMeta(itemMeta);
        }
    }

    private class MinecraftColorCodeTranslator
    implements ColorCodeTranslator {
        private MinecraftColorCodeTranslator() {
        }

        @Override
        public String translate(String name) {
            return ChatColor.translateAlternateColorCodes((char)'&', (String)name);
        }
    }

    private class ANSIColorCodeTranslator
    implements ColorCodeTranslator {
        private ANSIColorCodeTranslator() {
        }

        @Override
        public String translate(String name) {
            return StringUtils.replaceChars((String)name, (char)'\u00a7', (char)'&');
        }
    }

}