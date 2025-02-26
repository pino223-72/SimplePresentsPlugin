package net.azisaba.simplepresents.Listener;

import net.azisaba.simplepresents.SimplePresents;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Set;
import java.util.UUID;

public class AdminCommandPresentsListener implements Listener {
    private final SimplePresents plugin;
    private final Set<UUID> receivedPlayers; // 受け取り済みのプレイヤー

    public AdminCommandPresentsListener(SimplePresents plugin, Set<UUID> receivedPlayers) {
        this.plugin = plugin;
        this.receivedPlayers = receivedPlayers;
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        if (receivedPlayers.contains(playerId)) {
            player.sendMessage("あなたはすでにプレゼントを受け取っています！");
            event.setCancelled(true);
        }
    }
}
