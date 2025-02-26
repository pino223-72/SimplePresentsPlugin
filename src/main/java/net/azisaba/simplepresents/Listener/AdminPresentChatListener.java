package net.azisaba.simplepresents.Listener;

import net.azisaba.simplepresents.SimplePresents;
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
import java.util.UUID;

public class AdminPresentChatListener implements Listener {
    private final SimplePresents plugin;

    public AdminPresentChatListener(SimplePresents plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        // プレゼントの名前入力を待っているか確認
        if (!plugin.isAwaitingName(playerId)) {
            return;
        }

        event.setCancelled(true); // チャットのメッセージを送信しない

        String presentName = event.getMessage().trim();

        // 名前が無効な場合
        if (presentName.isEmpty()) {
            player.sendMessage(ChatColor.RED + "無効な名前です！もう一度 /presents set を実行してください。");
            plugin.setAwaitingName(playerId, false);
            return;
        }

        // GUI からアイテムを取得
        Inventory inv = plugin.getAdminGUI();
        List<ItemStack> items = new ArrayList<>();

        for (int i = 0; i < 9; i++) { // 上の9スロットから取得
            ItemStack item = inv.getItem(i);
            if (item != null && item.getType() != Material.AIR) {
                items.add(item);
            }
        }

        // アイテムが入っていない場合はエラー
        if (items.isEmpty()) {
            player.sendMessage(ChatColor.RED + "アイテムが設定されていません！もう一度 /presents set を実行してください。");
            plugin.setAwaitingName(playerId, false);
            return;
        }

        // プレゼントを保存
        plugin.savePresent(presentName, items);
        player.sendMessage(ChatColor.GREEN + "プレゼント「" + presentName + "」が保存されました！");

        // 待機状態を解除
        plugin.setAwaitingName(playerId, false);
    }
}
