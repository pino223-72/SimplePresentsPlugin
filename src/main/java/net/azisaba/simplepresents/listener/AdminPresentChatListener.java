package net.azisaba.simplepresents.listener;

import net.azisaba.simplepresents.SimplePresents;
import net.azisaba.simplepresents.model.PresentItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class AdminPresentChatListener implements Listener {

    private final SimplePresents plugin;

    public AdminPresentChatListener(SimplePresents plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        if (plugin.isAwaitingName(player.getUniqueId())) {
            event.setCancelled(true);

            String presentName = event.getMessage().trim();

            if (presentName.isEmpty()) {
                player.sendMessage(ChatColor.RED + "プレゼント名が無効です。もう一度入力してください。");
                return;
            }

            Inventory gui = plugin.getAdminGUI();
            List<PresentItem> items = new ArrayList<>();

            for (int i = 0; i < 9; i++) {
                ItemStack item = gui.getItem(i);
                if (item != null && item.getType() != Material.AIR) {
                    items.add(PresentItem.fromItemStack(item));
                }
            }

            plugin.savePresent(presentName, items);
            player.sendMessage(ChatColor.GREEN + "プレゼント「" + presentName + "」を保存しました！");

            plugin.setAwaitingName(player.getUniqueId(), false);
        }
    }
}
