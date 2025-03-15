package net.azisaba.simplepresents.model;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.items.MythicItem;
import com.shampaggon.crackshot.CSUtility;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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

    // Deserialization method
    public static PresentItem deserialize(Map<String, Object> map) {
        String type = (String) map.get("type");

        String materialName = (String) map.get("material");
        Material material = (materialName != null && Material.getMaterial(materialName) != null)
                ? Material.getMaterial(materialName)
                : Material.STONE;

        int amount = map.containsKey("amount") ? (int) map.get("amount") : 1;
        String crackshot = (String) map.get("crackshot");
        String mythic = (String) map.get("mythic");

        return new PresentItem(type, material, amount, crackshot, mythic);
    }

    // Serialization method
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("type", type);
        map.put("material", material.toString());
        map.put("amount", amount);
        map.put("crackshot", crackshot);
        map.put("mythic", mythic);
        return map;
    }

    public static PresentItem fromItemStack(ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            String type = meta.getDisplayName();
            Material material = itemStack.getType();
            int amount = itemStack.getAmount();
            return new PresentItem(type, material, amount, null, null);
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
        if ("VANILLA".equals(this.type)) {
            ItemStack item = new ItemStack(material, amount);
            player.getInventory().addItem(item);
            player.sendMessage("§a通常アイテム " + material + " を " + amount + " 個受け取りました！");
        } else if ("CRACKSHOT".equals(this.type) && crackshot != null) {
            CSUtility cs = new CSUtility();
            cs.giveWeapon(player, crackshot, amount);
            player.sendMessage("§bCrackShot武器 " + crackshot + " を受け取りました！");
        } else if ("MYTHICMOBS".equals(this.type) && mythic != null) {
            Optional<MythicItem> mythicItem = MythicBukkit.inst().getItemManager().getItem(mythic);
            if (mythicItem.isPresent()) {
                player.getInventory().addItem(mythicItem.get().generateItemStack(amount));
                player.sendMessage("§dMythicMobsアイテム " + mythic + " を受け取りました！");
            } else {
                player.sendMessage("§cMythicMobsアイテム '" + mythic + "' が見つかりませんでした！");
            }
        } else {
            player.sendMessage("§c無効なアイテムタイプです！");
        }
    }

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

    public String getMythic() {
        return mythic;
    }
}
