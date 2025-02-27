package net.azisaba.simplepresents.Listener;

import net.azisaba.simplepresents.SimplePresents;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
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
        if (!event.getView().getTitle().equals(ChatColor.RED + "プレゼント設定")) {
            return;
        }

        int slot = event.getRawSlot();

        // 下段（9〜16番）は灰色ガラス固定
        if (slot >= 9 && slot <= 16) {
            event.setCancelled(true);
        }

        // 右下の保存ボタン（17番）をクリックした場合
        if (slot == 17) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            player.sendMessage(ChatColor.YELLOW + "プレゼントの名前をチャットで入力してください！");
            player.closeInventory();
            plugin.setAwaitingName(player.getUniqueId(), true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getView().getTitle().equals(ChatColor.RED + "プレゼント設定")) {
            return;
        }
        // 必要ならクローズ時の処理を書く
    }

    public Inventory createAdminGUI() {
        Inventory gui = plugin.getServer().createInventory(null, 18, ChatColor.RED + "プレゼント設定");

        // 下段（9～16番）に灰色の板ガラスをセット
        ItemStack grayGlass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = grayGlass.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GRAY + " ");
            grayGlass.setItemMeta(meta);
        }

        for (int i = 9; i < 17; i++) {
            gui.setItem(i, grayGlass);
        }

        // 右下のスロット（17番）に「保存する」ボタンを配置
        ItemStack saveItem = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta saveMeta = saveItem.getItemMeta();
        if (saveMeta != null) {
            saveMeta.setDisplayName(ChatColor.GREEN + "保存する");
            saveItem.setItemMeta(saveMeta);
        }
        gui.setItem(17, saveItem);

        return gui;
    }
}
