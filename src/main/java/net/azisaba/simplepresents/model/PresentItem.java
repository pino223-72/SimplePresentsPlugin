package net.azisaba.simplepresents.model;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

@SerializableAs("PresentItem")
public class PresentItem implements ConfigurationSerializable {

    public enum ItemType {
        VANILLA, CRACKSHOT, MYTHICMOBS
    }

    private final ItemType type;
    private final ItemStack vanillaItem;
    private final String crackshotName;
    private final String mythicMobsName;

    // コンストラクタ（VANILLA用）
    public PresentItem(ItemStack vanillaItem) {
        this.type = ItemType.VANILLA;
        this.vanillaItem = vanillaItem;
        this.crackshotName = null;
        this.mythicMobsName = null;
    }

    // コンストラクタ（CRACKSHOT用）
    public PresentItem(String crackshotName) {
        this.type = ItemType.CRACKSHOT;
        this.vanillaItem = null;
        this.crackshotName = crackshotName;
        this.mythicMobsName = null;
    }

    // コンストラクタ（MYTHICMOBS用）
    public PresentItem(String mythicMobsName, boolean isMythic) {
        this.type = ItemType.MYTHICMOBS;
        this.vanillaItem = null;
        this.crackshotName = null;
        this.mythicMobsName = mythicMobsName;
    }

    public ItemType getType() {
        return type;
    }

    public ItemStack getVanillaItem() {
        return vanillaItem;
    }

    public String getCrackshotName() {
        return crackshotName;
    }

    public String getMythicMobsName() {
        return mythicMobsName;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("type", type.name());
        switch (type) {
            case VANILLA:
                map.put("itemstack", vanillaItem);
                break;
            case CRACKSHOT:
                map.put("crackshot", crackshotName);
                break;
            case MYTHICMOBS:
                map.put("mythic", mythicMobsName);
                break;
        }
        return map;
    }

    public static PresentItem deserialize(Map<String, Object> map) {
        ItemType type = ItemType.valueOf((String) map.get("type"));
        switch (type) {
            case VANILLA:
                return new PresentItem((ItemStack) map.get("itemstack"));
            case CRACKSHOT:
                return new PresentItem((String) map.get("crackshot"));
            case MYTHICMOBS:
                return new PresentItem((String) map.get("mythic"), true);
        }
        throw new IllegalArgumentException("Unknown PresentItem type: " + type);
    }
}
