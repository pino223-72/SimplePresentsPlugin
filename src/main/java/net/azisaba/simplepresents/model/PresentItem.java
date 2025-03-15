package net.azisaba.simplepresents.model;

import com.shampaggon.crackshot.CSUtility;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.items.MythicItem;
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
    private String mythic;

    public PresentItem(String type, Material material, int amount, String crackshot, String mythic) {
        this.type = type;
        this.material = material;
        this.amount = amount;
        this.crackshot = crackshot;
        this.mythic = mythic;
    }

    // Deserialization method to convert the stored map to a PresentItem object
    public static PresentItem deserialize(Map<String, Object> map) {
        String type = (String) map.get("type");

        String materialName = (String) map.get("material");
        Material material = (materialName != null && Material.getMaterial(materialName) != null)
                ? Material.getMaterial(materialName)
                : Material.STONE; // デフォルト値（エラー防止）

        int amount = map.containsKey("amount") ? (int) map.get("amount") : 1;
        String crackshot = (String) map.get("crackshot");
        String mythic = (String) map.get("mythic");

        return new PresentItem(type, material, amount, crackshot, mythic);
    }

    // Serialization method to convert the PresentItem object to a map
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("type", type);
        map.put("material", material.toString());
        map.put("amount", amount);
        map.put("crackshot", crackshot);
        map.put("mythic", mythic);
        return map;
    }

    public void giveTo(Player player) {
        if (this.type.equals("VANILLA")) {
            ItemStack item = new ItemStack(this.material, this.amount);
            player.getInventory().addItem(item);
        } else if (this.type.equals("CRACKSHOT") && crackshot != null) {
            CSUtility cs = new CSUtility();
            cs.giveWeapon(player, crackshot, amount);
        } else if (this.type.equals("MYTHICMOBS") && mythic != null) {
            MythicItem mythicItem = MythicBukkit.inst().getItemManager().getItem(mythic).orElse(null);
            if (mythicItem != null) {
                player.getInventory().addItem(mythicItem.generateItemStack(amount));
            } else {
                player.sendMessage("§cMythicMobsアイテム '" + mythic + "' が見つかりませんでした！");
            }
        }
    }

    // ItemStack から PresentItem を作成
    public static PresentItem fromItemStack(ItemStack itemStack) {
        Material material = itemStack.getType();
        int amount = itemStack.getAmount();
        return new PresentItem("VANILLA", material, amount, null, null);
    }

    // ItemStack に変換
    public ItemStack toItemStack() {
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(type);
            item.setItemMeta(meta);
        }
        return item;
    }
}
