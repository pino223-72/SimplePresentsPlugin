package net.azisaba.simplepresents.Listener;

import net.azisaba.simplepresents.SimplePresents;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class AdminPresentGuiListener implements Listener {
    private final SimplePresents plugin;

    public AdminPresentGuiListener(SimplePresents plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(ChatColor.RED + "管理者用プレゼント設定")) {
            if (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.EMERALD_BLOCK) {
                plugin.savePresentItems();
                event.getWhoClicked().sendMessage(ChatColor.GREEN + "プレゼントを保存しました！");
                event.setCancelled(true);
            }
        }
    }
}
