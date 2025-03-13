package net.azisaba.simplepresents;

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

        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "使用方法: /presents <get|set|list|adminresetplayer|reload|help>");
            return true;
        }

        if (!(sender instanceof Player) && args[0].equalsIgnoreCase("get")) {
            sender.sendMessage(ChatColor.RED + "このコマンドはプレイヤーのみ実行できます。");
            return true;
        }

        Player player = sender instanceof Player ? (Player) sender : null;

        switch (args[0].toLowerCase()) {
            case "get":
                plugin.givePresent(player);
                break;

            case "set":
                if (player != null && player.hasPermission("simplepresents.admin")) {
                    player.openInventory(plugin.getAdminGUI());
                } else {
                    sender.sendMessage(ChatColor.RED + "このコマンドを使用する権限がありません。");
                }
                break;

            case "list":
                plugin.showPresentList((Player) sender);
                break;

            case "adminresetplayer":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "使用方法: /presents adminresetplayer <player>");
                    return true;
                }
                plugin.resetPlayerPresents(args[1]);
                break;

            case "reload":
                plugin.reloadConfig();
                plugin.loadPresentItems();
                plugin.loadReceivedPlayers();
                sender.sendMessage(ChatColor.GREEN + "設定をリロードしました。");
                break;

            case "help":
                showHelp(sender);
                break;

            default:
                sender.sendMessage(ChatColor.RED + "不明なコマンドです。/presents help でコマンド一覧を確認できます。");
                break;
        }

        return true;
    }

    private void showHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.BLUE + "===SimplePresents HELP===");
        sender.sendMessage(ChatColor.AQUA + "サブコマンド一覧");
        sender.sendMessage(ChatColor.AQUA + "get" + ChatColor.WHITE + " - プレゼントを受け取ります");
        sender.sendMessage(ChatColor.AQUA + "set" + ChatColor.WHITE + " - プレゼントを設定します");
        sender.sendMessage(ChatColor.AQUA + "list" + ChatColor.WHITE + " - 設定されているプレゼント一覧を表示します");
        sender.sendMessage(ChatColor.AQUA + "adminresetplayer <player>" + ChatColor.WHITE + " - プレイヤーのプレゼント受け取り履歴をリセットします");
        sender.sendMessage(ChatColor.AQUA + "reload" + ChatColor.WHITE + " - 設定をリロードします");
        sender.sendMessage(ChatColor.AQUA + "help" + ChatColor.WHITE + " - このヘルプを表示します");
        sender.sendMessage(ChatColor.BLUE + "=====================");
    }
}
