package net.azisaba.simplepresents.model;

import com.shampaggon.crackshot.CSUtility;
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
    private String mythic;

    // コンストラクタ
    public PresentItem(String type, Material material, int amount, String crackshot, String mythic) {
        this.type = type;
        this.material = material;
        this.amount = amount;
        this.crackshot = crackshot;
        this.mythic = mythic;
    }

    // MapをPresentItemに変換するメソッド (Deserialization)
    public static PresentItem deserialize(Map<String, Object> map) {
        String type = (String) map.get("type");

        // Materialの取得
        String materialName = (String) map.get("material");
        Material material = (materialName != null && Material.getMaterial(materialName) != null)
                ? Material.getMaterial(materialName)
                : Material.STONE; // Default value for material

        // Amount（数量）の取得、指定がなければデフォルトで1
        int amount = map.containsKey("amount") ? (int) map.get("amount") : 1;
        String crackshot = (String) map.get("crackshot");
        String mythic = (String) map.get("mythic");

        return new PresentItem(type, material, amount, crackshot, mythic);
    }

    // PresentItemをMapに変換するメソッド (Serialization)
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("type", type);
        map.put("material", material.toString());
        map.put("amount", amount);
        map.put("crackshot", crackshot);
        map.put("mythic", mythic);
        return map;
    }

    // ItemStackをPresentItemに変換するメソッド
    public static PresentItem fromItemStack(ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            String type = meta.getDisplayName(); // プレゼントのタイプを名前として使用
            Material material = itemStack.getType();
            int amount = itemStack.getAmount();
            return new PresentItem(type, material, amount, null, null); // crackshot や mythic は null で構いません
        }
        return null;
    }

    // PresentItemをItemStackに変換するメソッド
    public ItemStack toItemStack() {
        ItemStack itemStack = new ItemStack(material, amount);
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(type); // アイテム名をタイプに設定
            itemStack.setItemMeta(meta);
        }
        return itemStack;
    }

    // プレイヤーにアイテムを与える処理
    public void giveTo(Player player) {
        if (this.type.equals("VANILLA")) {
            // バニラアイテム
            ItemStack item = new ItemStack(material, amount);
            player.getInventory().addItem(item);
        } else if (this.type.equals("CRACKSHOT")) {
            // CrackShotの武器
            if (crackshot != null) {
                CSUtility cs = new CSUtility();
                cs.giveWeapon(player, crackshot, amount);
            }
        } else if (this.type.equals("MYTHICMOBS")) {
            // MythicMobs のアイテム
            if (mythic != null) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mm items get " + mythic + " " + amount + " " + player.getName());
            }
        }
    }

    // Getterメソッド
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
