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

    // 2行GUI＋下段ガラス＋保存ボタン
    public Inventory createAdminGUI() {
        Inventory gui = plugin.getServer().createInventory(null, 18, ChatColor.RED + "プレゼント設定");

        // 下段の左8スロットを灰色ガラスで埋める
        ItemStack grayGlass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta grayMeta = grayGlass.getItemMeta();
        if (grayMeta != null) {
            grayMeta.setDisplayName(" ");
            grayGlass.setItemMeta(grayMeta);
        }
        for (int i = 9; i < 17; i++) {
            gui.setItem(i, grayGlass);
        }

        // 右下に保存ボタン
        ItemStack saveItem = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta saveMeta = saveItem.getItemMeta();
        if (saveMeta != null) {
            saveMeta.setDisplayName(ChatColor.GREEN + "保存する");
            saveItem.setItemMeta(saveMeta);
        }
        gui.setItem(17, saveItem);

        return gui;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inv = event.getInventory();
        if (!inv.equals(plugin.getAdminGUI())) {
            return;
        }

        event.setCancelled(true);  // アイテム移動禁止
        Player player = (Player) event.getWhoClicked();

        if (event.getRawSlot() == 17) {  // 保存ボタン
            player.closeInventory();
            player.sendMessage(ChatColor.YELLOW + "プレゼント名をチャットに入力してください。");
            plugin.setAwaitingName(player.getUniqueId(), true);  // 名前入力待ち状態に設定
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().equals(plugin.getAdminGUI())) {
            plugin.setTempItems(event.getInventory().getContents());  // 一時保存
        }
    }
}
