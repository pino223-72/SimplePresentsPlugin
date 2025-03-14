package net.azisaba.simplepresents.model;

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

        // Materialの取得時にnullチェックを強化
        String materialName = (String) map.get("material");
        Material material = (materialName != null && Material.getMaterial(materialName) != null)
                ? Material.getMaterial(materialName)
                : Material.STONE; // Default value for material if not found

        int amount = map.containsKey("amount") ? (int) map.get("amount") : 1; // Default amount if not specified
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

    // Method to create ItemStack from PresentItem
    public ItemStack toItemStack() {
        ItemStack itemStack = new ItemStack(material, amount);
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(type); // Example display name based on the type of the present
            itemStack.setItemMeta(meta);
        }
        return itemStack;
    }

    public void giveTo(Player player) {
        if (this.type.equals("VANILLA")) {
            // VANILLA アイテムの処理
            ItemStack item = new ItemStack(this.material, this.amount);
            player.getInventory().addItem(item);
        } else if (this.type.equals("CRACKSHOT")) {
            // CRACKSHOT アイテムの処理
            if (crackshot != null) {
                // CrackShotのアイテム処理コード（例: アイテムをプレイヤーに与える）
                // 例えば、CrackShotAPIなどを使ってSniperRifleを付与するコードをここに書く
            }
        } else if (this.type.equals("MYTHICMOBS")) {
            // MythicMobsのアイテム処理コード
            if (mythic != null) {
                // MythicMobsのアイテム処理（例: MythicMobsAPIを使用）
                // mythicアイテムをプレイヤーに与える処理を追加
            }
        }
        // 他のタイプの処理が必要な場合は追加します
    }

    // Getters and setters for the fields
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
