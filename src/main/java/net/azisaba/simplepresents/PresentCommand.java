package net.azisaba.simplepresents;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PresentCommand implements CommandExecutor {
    private final SimplePresents plugin;

    public PresentCommand(SimplePresents plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "このコマンドはプレイヤーのみ使用できます。");
            return true;
        }

        Player player = (Player) sender;

        switch (label.toLowerCase()) {
            case "getpresent":
                if (plugin.canReceivePresent(player)) {
                    plugin.givePresent(player);
                } else {
                    player.sendMessage(ChatColor.RED + "現在、受け取れるプレゼントはありません！");
                }
                return true;

            case "adminpresent":
                player.openInventory(plugin.getAdminGUI());
                return true;

            case "clearpresents":
                plugin.clearPresents();
                sender.sendMessage(ChatColor.GREEN + "すべてのプレゼントデータをクリアしました！");
                return true;

            default:
                sender.sendMessage(ChatColor.RED + "未知のコマンドです！");
                return false;
        }
    }
}
