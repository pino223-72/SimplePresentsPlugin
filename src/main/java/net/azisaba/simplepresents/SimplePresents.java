package net.azisaba.simplepresents;

import net.azisaba.simplepresents.listener.AdminPresentGuiListener;
import net.azisaba.simplepresents.listener.AdminPresentChatListener;
import net.azisaba.simplepresents.command.PresentCommand;
import net.azisaba.simplepresents.model.PresentItem;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

public class SimplePresents extends JavaPlugin {

    private final Map<String, List<PresentItem>> presents = new HashMap<>();
    private final Map<UUID, Set<String>> receivedPlayers = new HashMap<>();
    private File presentsFile;
    private FileConfiguration presentsConfig;
    private Inventory adminGUI;
    private final Map<UUID, Boolean> awaitingName = new HashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadReceivedPlayers();
        loadPresentItems();
        createAdminGUI();

        getLogger().info("SimplePresents has been enabled!");

        Bukkit.getPluginManager().registerEvents(new AdminPresentGuiListener(this), this);
        Bukkit.getPluginManager().registerEvents(new AdminPresentChatListener(this), this);

        getCommand("presents").setExecutor(new PresentCommand(this));
    }

    @Override
    public void onDisable() {
        saveReceivedPlayers();
        savePresentItems();
    }

    public void givePresent(Player player, String presentName) {
        UUID playerId = player.getUniqueId();

        if (!presents.containsKey(presentName)) {
            player.sendMessage(ChatColor.RED + "プレゼント「" + presentName + "」は存在しません。");
            return;
        }

        ConfigurationSection presentSection = presentsConfig.getConfigurationSection("presents." + presentName);
        if (presentSection == null) {
            player.sendMessage(ChatColor.RED + "プレゼントデータが見つかりません。");
            return;
        }

        LocalDate today = LocalDate.now();
        LocalDate startDate = LocalDate.parse(presentSection.getString("start"));
        LocalDate endDate = LocalDate.parse(presentSection.getString("end"));

        if (today.isBefore(startDate) || today.isAfter(endDate)) {
            player.sendMessage(ChatColor.RED + "このプレゼントは現在受け取ることができません。");
            return;
        }

        Set<String> received = receivedPlayers.getOrDefault(playerId, new HashSet<>());
        if (received.contains(presentName)) {
            player.sendMessage(ChatColor.RED + "このプレゼントはすでに受け取っています。");
            return;
        }

        for (PresentItem item : presents.get(presentName)) {
            item.giveTo(player);
        }

        String message = presentSection.getString("message", "プレゼント「" + presentName + "」を受け取りました！");
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));

        received.add(presentName);
        receivedPlayers.put(playerId, received);
        saveReceivedPlayers();
    }

    public void loadPresentItems() {
        presentsFile = new File(getDataFolder(), "presents.yml");
        if (!presentsFile.exists()) saveResource("presents.yml", false);

        presentsConfig = YamlConfiguration.loadConfiguration(presentsFile);
        presents.clear();

        ConfigurationSection section = presentsConfig.getConfigurationSection("presents");
        if (section != null) {
            for (String name : section.getKeys(false)) {
                List<Map<?, ?>> itemsMap = section.getMapList(name + ".items");
                List<PresentItem> items = new ArrayList<>();
                for (Map<?, ?> map : itemsMap) {
                    items.add(PresentItem.deserialize((Map<String, Object>) map));
                }
                presents.put(name, items);
            }
        }
    }

    public void savePresentItems() {
        for (Map.Entry<String, List<PresentItem>> entry : presents.entrySet()) {
            List<Map<String, Object>> serializedItems = new ArrayList<>();
            for (PresentItem item : entry.getValue()) {
                serializedItems.add(item.serialize());
            }
            presentsConfig.set("presents." + entry.getKey() + ".items", serializedItems);
        }

        try {
            presentsConfig.save(presentsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadReceivedPlayers() {
        FileConfiguration config = getConfig();
        receivedPlayers.clear();

        ConfigurationSection playersSection = config.getConfigurationSection("receivedPlayers");
        if (playersSection != null) {
            for (String uuid : playersSection.getKeys(false)) {
                List<String> receivedList = playersSection.getStringList(uuid);
                receivedPlayers.put(UUID.fromString(uuid), new HashSet<>(receivedList));
            }
        }
    }

    public void saveReceivedPlayers() {
        FileConfiguration config = getConfig();
        config.set("receivedPlayers", null);

        for (Map.Entry<UUID, Set<String>> entry : receivedPlayers.entrySet()) {
            config.set("receivedPlayers." + entry.getKey().toString(), new ArrayList<>(entry.getValue()));
        }

        saveConfig();
    }

    public void createAdminGUI() {
        adminGUI = Bukkit.createInventory(null, 18, ChatColor.RED + "プレゼント設定");
    }

    public Inventory getAdminGUI() {
        return adminGUI;
    }

    public void resetPlayerPresents(String playerName) {
        Player player = Bukkit.getPlayer(playerName);
        if (player == null) return;

        receivedPlayers.remove(player.getUniqueId());
        saveReceivedPlayers();
    }
}
