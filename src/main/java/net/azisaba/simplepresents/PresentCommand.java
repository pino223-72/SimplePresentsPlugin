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
            sender.sendMessage(ChatColor.RED + "このコマンドはプレイヤーのみ実行できます。");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "使用方法: /presents <subcommand> (管理者は /presents help で一覧表示)");
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "help":
                if (!player.hasPermission("simplepresents.admin")) {
                    player.sendMessage(ChatColor.RED + "このコマンドを使用する権限がありません！");
                    return true;
                }
                player.sendMessage(ChatColor.GOLD + "=== [管理者] SimplePresents コマンド一覧 ===");
                player.sendMessage(ChatColor.YELLOW + "/presents get - プレゼントを受け取る");
                player.sendMessage(ChatColor.YELLOW + "/presents set - プレゼントを設定");
                player.sendMessage(ChatColor.YELLOW + "/presents list - 設定されたプレゼント一覧を表示");
                player.sendMessage(ChatColor.YELLOW + "/presents adminclear - 全プレゼントデータを削除");
                player.sendMessage(ChatColor.YELLOW + "/presents reload - 設定をリロード");
                player.sendMessage(ChatColor.YELLOW + "/presents adminresetplayer <player> - 指定プレイヤーの履歴をリセット");
                return true;

            case "list":
                if (!player.hasPermission("simplepresents.admin")) {
                    player.sendMessage(ChatColor.RED + "このコマンドを使用する権限がありません！");
                    return true;
                }
                Map<String, List<ItemStack>> presents = new HashMap<>();
                if (presents.isEmpty()) {
                    player.sendMessage(ChatColor.RED + "現在、設定されているプレゼントはありません。");
                } else {
                    player.sendMessage(ChatColor.GOLD + "=== 現在のプレゼント一覧 ===");
                    for (String name : presents.keySet()) {
                        player.sendMessage(ChatColor.YELLOW + "- " + name);
                    }
                }
                return true;

            case "get":
                if (plugin.canReceivePresent(player)) {
                    plugin.givePresent(player);
                } else {
                    player.sendMessage(ChatColor.RED + "現在、受け取れるプレゼントはありません！");
                }
                return true;

            case "set":
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

            case "reload":
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
                sender.sendMessage(ChatColor.RED + "未知のサブコマンドです！ 使用方法: /presents help でコマンド一覧を確認 (管理者のみ)");
                return false;
        }
    }
}
