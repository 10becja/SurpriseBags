package org.samson.bukkit.plugins.surprisebags.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.samson.bukkit.plugins.surprisebags.bag.Bag;

public class BagService {
    private Map<String, Bag> bags = new HashMap<String, Bag>();

    public Bag getBagById(String id) {
        return this.bags.get(id);
    }

    public void addItem(String id, Bag bagItem) {
        this.bags.put(id, bagItem);
    }

    public Set<String> getBagIdList() {
        return this.bags.keySet();
    }

    public void clearItems() {
        this.bags.clear();
    }

    public List<Bag> getRandomBags(LivingEntity deadEntity) {
        ArrayList<Bag> randomList = new ArrayList<Bag>();
        Set<Map.Entry<String, Bag>> bagEntrySet = this.bags.entrySet();
        for (Map.Entry<String, Bag> entry : bagEntrySet) {
            double rand;
            double dropChance;
            Bag bag = entry.getValue();
            if (!bag.canBeDropFromMob(deadEntity.getType()) || (rand = Math.random()) >= (dropChance = bag.getDropChance())) continue;
            randomList.add(bag);
        }
        return randomList;
    }
}