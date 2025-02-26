package net.azisaba.simplepresents;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PresentCommand implements CommandExecutor {
    private final SimplePresents plugin;

    public PresentCommand(SimplePresents plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "このコマンドはプレイヤーのみ使用できます！");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "使用方法: /presents <get|set|adminclear|reload|adminresetplayer|help|list>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "get":
                plugin.givePresent(player);
                break;
            case "set":
                player.openInventory(plugin.getAdminGUI());
                break;
            case "adminclear":
                plugin.clearPresents();
                sender.sendMessage(ChatColor.GREEN + "プレゼントデータをクリアしました！");
                break;
            case "reload":
                plugin.reloadConfig();
                sender.sendMessage(ChatColor.GREEN + "設定をリロードしました！");
                break;
            case "adminresetplayer":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "使用方法: /presents adminresetplayer <player>");
                    return true;
                }
                plugin.resetPlayerPresents(args[1]);
                sender.sendMessage(ChatColor.GREEN + "プレイヤー " + args[1] + " の受け取り履歴をリセットしました！");
                break;
            case "help":
                sender.sendMessage(ChatColor.GOLD + "/presents <get|set|adminclear|reload|adminresetplayer|help|list>");
                break;
            case "list":
                sender.sendMessage(ChatColor.GOLD + "現在のプレゼント一覧: " + plugin.getPresents().keySet());
                break;
            default:
                sender.sendMessage(ChatColor.RED + "不明なサブコマンドです！");
                break;
        }
        return true;
    }

}
