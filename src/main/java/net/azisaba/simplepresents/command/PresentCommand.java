package net.azisaba.simplepresents.command;

import net.azisaba.simplepresents.SimplePresents;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class PresentCommand implements CommandExecutor {
    private final SimplePresents plugin;

    public PresentCommand(SimplePresents plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "このコマンドはプレイヤーのみ実行できます！");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "使用方法: /presents <サブコマンド>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "get":
                plugin.givePresent(player, presentName);
                return true;

            case "set":
                player.openInventory(plugin.getAdminGUI());
                return true;

            case "list":
                plugin.showPresentList(player);
                return true;

            case "adminresetplayer":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "使用方法: /presents adminresetplayer <プレイヤー名>");
                    return true;
                }
                plugin.resetPlayerPresents(args[1]);
                player.sendMessage(ChatColor.GREEN + "プレイヤー " + args[1] + " の受け取り履歴をリセットしました。");
                return true;

            case "reload":
                plugin.reloadConfig();
                plugin.loadPresentItems();
                plugin.loadReceivedPlayers();
                player.sendMessage(ChatColor.GREEN + "設定をリロードしました。");
                return true;

            case "help":
                plugin.showHelpMessage(player);
                return true;

            default:
                player.sendMessage(ChatColor.RED + "不明なサブコマンドです。/presents help を使用してください。");
                return true;
        }
    }
}
