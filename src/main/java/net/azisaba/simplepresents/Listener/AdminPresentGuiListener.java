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
        Inventory inv = event.getInventory();
        Player player = (Player) event.getWhoClicked();

        // `getTitle()` の代わりに `getView().getTitle()` を使用
        if (!event.getView().getTitle().equals(ChatColor.RED + "プレゼント設定")) {
            return;
        }

        int slot = event.getRawSlot();
        event.setCancelled(true); // GUI内のアイテムを勝手に動かせないようにする

        // 保存ブロック（エメラルドブロック）がクリックされたとき
        if (slot == 17) {
            player.sendMessage(ChatColor.YELLOW + "プレゼントの名前をチャットで入力してください！");
            plugin.setAwaitingName(player.getUniqueId(), true);
            player.closeInventory();
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Inventory inv = event.getInventory();

        // `getTitle()` の代わりに `getView().getTitle()` を使用
        if (!event.getView().getTitle().equals(ChatColor.RED + "プレゼント設定")) {
            return;
        }
    }

    public Inventory createAdminGUI() {
        Inventory gui = plugin.getServer().createInventory(null, 18, ChatColor.RED + "プレゼント設定");

        // 下の段（9マス）に灰色の板ガラスをセット
        ItemStack grayGlass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = grayGlass.getItemMeta();
        meta.setDisplayName(ChatColor.GRAY + " ");
        grayGlass.setItemMeta(meta);

        for (int i = 9; i < 17; i++) {
            gui.setItem(i, grayGlass);
        }

        // 右下のスロットに「保存する」ボタンを配置
        ItemStack saveItem = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta saveMeta = saveItem.getItemMeta();
        saveMeta.setDisplayName(ChatColor.GREEN + "保存する");
        saveItem.setItemMeta(saveMeta);
        gui.setItem(17, saveItem);

        return gui;
    }
}
