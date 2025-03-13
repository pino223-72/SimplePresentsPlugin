package net.azisaba.simplepresents.listener;

import net.azisaba.simplepresents.SimplePresents;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class AdminPresentGuiListener implements Listener {

    private final SimplePresents plugin;

    public AdminPresentGuiListener(SimplePresents plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inv = event.getInventory();
        Player player = (Player) event.getWhoClicked();

        // GUIのタイトルチェック
        if (!event.getView().getTitle().equals(ChatColor.RED + "プレゼント設定")) {
            return;
        }

        int slot = event.getRawSlot();

        // 下段9〜17のクリックを禁止
        if (slot >= 9 && slot <= 17) {
            event.setCancelled(true);
        }

        // 保存ボタン（17）がクリックされたとき
        if (slot == 17) {
            player.sendMessage(ChatColor.YELLOW + "プレゼントの名前をチャットで入力してください！");
            player.closeInventory();
            plugin.setAwaitingName(player.getUniqueId(), true);
        }
    }
}
