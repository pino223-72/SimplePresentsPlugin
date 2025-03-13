package net.azisaba.simplepresents.model;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class PresentItem implements ConfigurationSerializable {

    public enum ItemType {
        VANILLA, CRACKSHOT, MYTHICMOBS
    }

    private final ItemType type;
    private final ItemStack vanillaItem;
    private final String crackshotGun;
    private final String mythicItem;

    // コンストラクタ（Vanillaアイテム用）
    public PresentItem(ItemStack item) {
        this.type = ItemType.VANILLA;
        this.vanillaItem = item;
        this.crackshotGun = null;
        this.mythicItem = null;
    }

    // コンストラクタ（CrackShotアイテム用）
    public PresentItem(String crackshotGun) {
        this.type = ItemType.CRACKSHOT;
        this.vanillaItem = null;
        this.crackshotGun = crackshotGun;
        this.mythicItem = null;
    }

    // コンストラクタ（MythicMobsアイテム用）
    public PresentItem(String mythicItem, boolean isMythic) {
        this.type = ItemType.MYTHICMOBS;
        this.vanillaItem = null;
        this.crackshotGun = null;
        this.mythicItem = mythicItem;
    }

    public ItemType getType() {
        return type;
    }

    public ItemStack getVanillaItem() {
        return vanillaItem;
    }

    public String getCrackshotGun() {
        return crackshotGun;
    }

    public String getMythicItem() {
        return mythicItem;
    }

    public static PresentItem fromItemStack(ItemStack item) {
        return new PresentItem(item);
    }

    public void giveTo(Player player) {
        switch (type) {
            case VANILLA:
                player.getInventory().addItem(vanillaItem);
                break;
            case CRACKSHOT:
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "crackshot give " + player.getName() + " " + crackshotGun);
                break;
            case MYTHICMOBS:
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mm items get " + mythicItem + " 1 " + player.getName());
                break;
        }
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
                map.put("crackshot", crackshotGun);
                break;
            case MYTHICMOBS:
                map.put("mythic", mythicItem);
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
            default:
                throw new IllegalArgumentException("Invalid PresentItem type: " + type);
        }
    }
}
