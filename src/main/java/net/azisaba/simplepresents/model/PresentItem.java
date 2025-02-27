package net.azisaba.simplepresents.model;

import org.bukkit.Bukkit;
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
    private final ItemStack itemStack; // VANILLAの場合
    private final String identifier;   // CRACKSHOT・MYTHICMOBSの場合

    public PresentItem(ItemType type, ItemStack itemStack, String identifier) {
        this.type = type;
        this.itemStack = itemStack;
        this.identifier = identifier;
    }

    // ItemStackからVanillaアイテムとして作成
    public static PresentItem fromItemStack(ItemStack item) {
        return new PresentItem(ItemType.VANILLA, item, null);
    }

    // プレイヤーにアイテムを付与
    public void giveTo(Player player) {
        switch (type) {
            case VANILLA:
                player.getInventory().addItem(itemStack);
                break;
            case CRACKSHOT:
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "crackshot give " + player.getName() + " " + identifier);
                break;
            case MYTHICMOBS:
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mm items get " + identifier + " 1 " + player.getName());
                break;
        }
    }

    // シリアライズ処理 (YAML保存用)
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("type", type.name());
        if (type == ItemType.VANILLA) {
            map.put("itemstack", itemStack);
        } else {
            map.put("identifier", identifier);
        }
        return map;
    }

    // デシリアライズ処理 (YAML読み込み用)
    public static PresentItem deserialize(Map<String, Object> map) {
        ItemType type = ItemType.valueOf((String) map.get("type"));
        if (type == ItemType.VANILLA) {
            ItemStack itemStack = (ItemStack) map.get("itemstack");
            return new PresentItem(type, itemStack, null);
        } else {
            String identifier = (String) map.get("identifier");
            return new PresentItem(type, null, identifier);
        }
    }

    public ItemType getType() {
        return type;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public String getIdentifier() {
        return identifier;
    }
}
