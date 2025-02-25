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
        if (label.equalsIgnoreCase("getpresent") && sender instanceof Player) {
            plugin.givePresent((Player) sender);
            return true;
        }

        if (label.equalsIgnoreCase("adminpresent") && sender instanceof Player) {
            ((Player) sender).openInventory(plugin.getAdminGUI());
            return true;
        }

        if (label.equalsIgnoreCase("clearpresents")) {
            plugin.getPresentItems().clear();
            plugin.savePresentItems();
            sender.sendMessage(ChatColor.GREEN + "プレゼントを削除しました！");
            return true;
        }

        return false;
    }
}
