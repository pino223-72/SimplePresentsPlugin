package net.azisaba.simplepresents.model;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;

public class PresentItem {
    private ItemStack item;

    public PresentItem(ItemStack item) {
        this.item = item;
    }

    // ItemStackを取得
    public ItemStack getItem() {
        return item;
    }

    // デシリアライズメソッド：MapからPresentItemを復元する
    public static PresentItem deserialize(Map<String, Object> map) {
        String type = (String) map.get("type");

        // Material が null の場合にエラーハンドリング
        String materialName = (String) map.get("material");
        Material material = (materialName != null && Material.getMaterial(materialName) != null)
                ? Material.getMaterial(materialName)
                : Material.STONE; // デフォルト値を設定

        // その後、CrackShotやMythicMobsなどの処理を行います。
        // ...
    }


    // シリアライズメソッド：PresentItemをMapに変換
    public Map<String, Object> serialize() {
        return item.serialize();
    }

    // ItemStackからPresentItemを生成するメソッド
    public static PresentItem fromItemStack(ItemStack itemStack) {
        return new PresentItem(itemStack);
    }

    // PresentItemをItemStackに変換
    public ItemStack toItemStack() {
        return this.item;
    }

    public void giveTo(Player player) {
    }
}
