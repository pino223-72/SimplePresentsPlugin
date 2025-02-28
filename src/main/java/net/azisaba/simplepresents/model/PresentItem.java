package net.azisaba.simplepresents.model;

import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class PresentItem {

    private final ItemType type;
    private final ItemStack itemStack;
    private final String crackshot;
    private final String mythic;

    public enum ItemType {
        VANILLA,
        CRACKSHOT,
        MYTHICMOBS
    }

    public PresentItem(ItemStack itemStack) {
        this.type = ItemType.VANILLA;
        this.itemStack = itemStack;
        this.crackshot = null;
        this.mythic = null;
    }

    public PresentItem(String crackshot) {
        this.type = ItemType.CRACKSHOT;
        this.itemStack = null;
        this.crackshot = crackshot;
        this.mythic = null;
    }

    public PresentItem(String mythic, boolean isMythic) {
        this.type = ItemType.MYTHICMOBS;
        this.itemStack = null;
        this.crackshot = null;
        this.mythic = mythic;
    }

    public void giveTo(Player player) {
        switch (type) {
            case VANILLA:
                player.getInventory().addItem(itemStack);
                break;
            case CRACKSHOT:
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "crackshot give " + player.getName() + " " + crackshot);
                break;
            case MYTHICMOBS:
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mm items give " + player.getName() + " " + mythic);
                break;
        }
    }

    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("type", type.name());

        switch (type) {
            case VANILLA:
                map.put("itemstack", itemStack);
                break;
            case CRACKSHOT:
                map.put("crackshot", crackshot);
                break;
            case MYTHICMOBS:
                map.put("mythic", mythic);
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
        throw new IllegalArgumentException("不明なアイテムタイプ: " + type);
    }
}
