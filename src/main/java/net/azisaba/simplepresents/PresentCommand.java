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
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "このコマンドはプレイヤーのみ実行できます。");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "使用方法: /presents <subcommand>");
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "get":
                if (plugin.canReceivePresent(player)) {
                    plugin.givePresent(player);
                } else {
                    player.sendMessage(ChatColor.RED + "現在、受け取れるプレゼントはありません！");
                }
                return true;

            case "adminset":
                if (!player.hasPermission("simplepresents.admin")) {
                    player.sendMessage(ChatColor.RED + "このコマンドを使用する権限がありません！");
                    return true;
                }
                player.openInventory(plugin.getAdminGUI());
                return true;

            case "adminclear":
                if (!player.hasPermission("simplepresents.admin")) {
                    player.sendMessage(ChatColor.RED + "このコマンドを使用する権限がありません！");
                    return true;
                }
                plugin.clearPresents();
                sender.sendMessage(ChatColor.GREEN + "すべてのプレゼントデータをクリアしました！");
                return true;

            case "adminreload":
                if (!player.hasPermission("simplepresents.admin")) {
                    player.sendMessage(ChatColor.RED + "このコマンドを使用する権限がありません！");
                    return true;
                }
                plugin.loadPresentItems();
                plugin.loadReceivedPlayers();
                sender.sendMessage(ChatColor.GREEN + "プレゼントデータをリロードしました！");
                return true;

            case "adminresetplayer":
                if (!player.hasPermission("simplepresents.admin")) {
                    player.sendMessage(ChatColor.RED + "このコマンドを使用する権限がありません！");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "使用方法: /presents adminresetplayer <player>");
                    return true;
                }
                plugin.resetPlayerPresents(args[1]);
                sender.sendMessage(ChatColor.GREEN + args[1] + " の受け取り履歴をリセットしました！");
                return true;

            default:
                sender.sendMessage(ChatColor.RED + "未知のサブコマンドです！ 使用方法: /presents <subcommand>");
                return false;
        }
    }
}
