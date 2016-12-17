package org.samson.bukkit.plugins.surprisebags;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.EntityType;

public class MobNameToEntityMapper {
    private static final Map<String, EntityType> CREATURES;

    public EntityType getEntityTypeByCreatureName(String name) {
        return CREATURES.get(name.toLowerCase());
    }

    static {
        HashMap<String, EntityType> creatureSetter = new HashMap<String, EntityType>();
        creatureSetter.put("creeper", EntityType.CREEPER);
        creatureSetter.put("zombie", EntityType.ZOMBIE);
        creatureSetter.put("giant", EntityType.GIANT);
        creatureSetter.put("skeleton", EntityType.SKELETON);
        creatureSetter.put("skele", EntityType.SKELETON);
        creatureSetter.put("pigzombie", EntityType.PIG_ZOMBIE);
        creatureSetter.put("wither", EntityType.WITHER);
        creatureSetter.put("dragon", EntityType.ENDER_DRAGON);
        creatureSetter.put("enderdragon", EntityType.ENDER_DRAGON);
        creatureSetter.put("enderman", EntityType.ENDERMAN);
        creatureSetter.put("ghast", EntityType.GHAST);
        creatureSetter.put("slime", EntityType.SLIME);
        creatureSetter.put("spider", EntityType.SPIDER);
        creatureSetter.put("cavespider", EntityType.CAVE_SPIDER);
        creatureSetter.put("witch", EntityType.WITCH);
        CREATURES = Collections.unmodifiableMap(creatureSetter);
    }
}