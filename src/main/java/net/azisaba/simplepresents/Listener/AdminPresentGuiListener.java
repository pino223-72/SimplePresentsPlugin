package net.azisaba.simplepresents.Listener;

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

        if (!player.getOpenInventory().getTitle().equals(ChatColor.RED + "プレゼント設定")) {
            return;
        }

        int slot = event.getRawSlot();

        // 下段の板ガラス・保存ボタンはクリック不可
        if (slot >= 9) {
            event.setCancelled(true);

            if (slot == 17) {
                // 保存ボタン押下時
                player.sendMessage(ChatColor.YELLOW + "プレゼント名をチャットで入力してください！");
                plugin.setAwaitingName(player.getUniqueId(), true);
                player.closeInventory();
            }
        }
    }

    public Inventory createAdminGUI() {
        Inventory gui = plugin.getServer().createInventory(null, 18, ChatColor.RED + "プレゼント設定");

        // 下段の灰色板ガラス設置
        ItemStack grayGlass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = grayGlass.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            grayGlass.setItemMeta(meta);
        }

        for (int i = 9; i < 17; i++) {
            gui.setItem(i, grayGlass);
        }

        // 右下に保存ボタン
        ItemStack saveButton = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta saveMeta = saveButton.getItemMeta();
        if (saveMeta != null) {
            saveMeta.setDisplayName(ChatColor.GREEN + "保存する");
            saveButton.setItemMeta(saveMeta);
        }
        gui.setItem(17, saveButton);

        return gui;
    }
}
