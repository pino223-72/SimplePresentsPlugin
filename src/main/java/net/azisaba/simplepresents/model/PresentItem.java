package net.azisaba.simplepresents.model;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;

public class PresentItem {
    private String type;
    private Material material;
    private int amount;
    private String crackshot;

    public PresentItem(String type, Material material, int amount, String crackshot) {
        this.type = type;
        this.material = material;
        this.amount = amount;
        this.crackshot = crackshot;
    }

    // Deserialization method
    public static PresentItem deserialize(Map<String, Object> map) {
        String type = (String) map.get("type");
        String materialName = (String) map.get("material");
        Material material = (materialName != null && Material.getMaterial(materialName) != null)
                ? Material.getMaterial(materialName)
                : Material.STONE; // Default value
        int amount = map.containsKey("amount") ? (int) map.get("amount") : 1;
        String crackshot = (String) map.get("crackshot");

        return new PresentItem(type, material, amount, crackshot);
    }

    // Serialization method
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("type", type);
        map.put("material", material.toString());
        map.put("amount", amount);
        map.put("crackshot", crackshot);
        return map;
    }

    public static PresentItem fromItemStack(ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            String type = meta.getDisplayName(); // Use display name as type
            Material material = itemStack.getType();
            int amount = itemStack.getAmount();
            return new PresentItem(type, material, amount, null);
        }
        return null;
    }

    public ItemStack toItemStack() {
        ItemStack itemStack = new ItemStack(material, amount);
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(type);
            itemStack.setItemMeta(meta);
        }
        return itemStack;
    }

    public void giveTo(Player player) {
        if (this.type.equals("VANILLA")) {
            ItemStack item = new ItemStack(material, amount);
            player.getInventory().addItem(item);
        } else if (this.type.equals("CRACKSHOT")) {
            // CrackShot API を使用してアイテムを付与
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "crackshot give " + player.getName() + " " + crackshot);
        }
    }

    // Getters
    public String getType() {
        return type;
    }

    public Material getMaterial() {
        return material;
    }

    public int getAmount() {
        return amount;
    }

    public String getCrackshot() {
        return crackshot;
    }
}
