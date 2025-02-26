package net.azisaba.simplepresents.Listener;

import net.azisaba.simplepresents.SimplePresents;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class AdminCommandPresentsListener implements Listener {
    private final SimplePresents plugin;
    private final Set<UUID> receivedPlayers;

    public AdminCommandPresentsListener(SimplePresents plugin, Set<UUID> receivedPlayers) {
        this.plugin = plugin;
        this.receivedPlayers = receivedPlayers;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(ChatColor.RED + "Adminプレゼント設定")) {
            if (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.EMERALD_BLOCK) {
                plugin.savePresentItems();
                event.getWhoClicked().sendMessage(ChatColor.GREEN + "プレゼントを保存しました！");
                event.setCancelled(true);
            }
        }
    }
}
