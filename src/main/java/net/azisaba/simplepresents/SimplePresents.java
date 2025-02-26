package net.azisaba.simplepresents;

import net.azisaba.simplepresents.Listener.AdminCommandPresentsListener;
import net.azisaba.simplepresents.Listener.AdminPresentChatListener;
import net.azisaba.simplepresents.Listener.AdminPresentGuiListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

public class SimplePresents extends JavaPlugin {
    private Map<String, List<ItemStack>> presents = new HashMap<>();
    private Map<UUID, Set<String>> receivedPlayers = new HashMap<>();
    private File presentsFile;
    private FileConfiguration presentsConfig;
    private Inventory adminGUI;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadReceivedPlayers();
        loadPresentItems();
        createAdminGUI();

        getLogger().info("SimplePresents has been enabled!");

        // 受け取り済みプレイヤーのUUIDリストを取得
        Set<UUID> receivedUUIDs = receivedPlayers.keySet();

        // イベントリスナー登録（エラー修正済み）
        Bukkit.getPluginManager().registerEvents(new AdminCommandPresentsListener(this, receivedUUIDs), this);
        Bukkit.getPluginManager().registerEvents(new AdminPresentGuiListener(this), this);
        Bukkit.getPluginManager().registerEvents(new AdminPresentChatListener(this), this);

        // コマンド登録
        if (getCommand("presents") != null) {
            getCommand("presents").setExecutor(new PresentCommand(this));
        } else {
            getLogger().severe("コマンド 'presents' が登録されていません！plugin.yml を確認してください！");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
    }


    @Override
    public void onDisable() {
        saveReceivedPlayers();
        savePresentItems();
    }

    public boolean canReceivePresent(Player player) {
        LocalDate today = LocalDate.now();
        UUID playerId = player.getUniqueId();

        for (String presentName : presents.keySet()) {
            ConfigurationSection presentSection = presentsConfig.getConfigurationSection("presents." + presentName);
            if (presentSection == null) continue;

            String startStr = presentSection.getString("start");
            String endStr = presentSection.getString("end");

            if (startStr == null || endStr == null) continue; // エラー防止

            LocalDate startDate = LocalDate.parse(startStr);
            LocalDate endDate = LocalDate.parse(endStr);

            if (!today.isBefore(startDate) && !today.isAfter(endDate)) {
                Set<String> received = receivedPlayers.getOrDefault(playerId, new HashSet<>());
                if (!received.contains(presentName)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void givePresent(Player player) {
        UUID playerId = player.getUniqueId();
        LocalDate today = LocalDate.now();

        for (String presentName : presents.keySet()) {
            ConfigurationSection presentSection = presentsConfig.getConfigurationSection("presents." + presentName);
            if (presentSection == null) continue;

            String startStr = presentSection.getString("start");
            String endStr = presentSection.getString("end");

            if (startStr == null || endStr == null) continue;

            LocalDate startDate = LocalDate.parse(startStr);
            LocalDate endDate = LocalDate.parse(endStr);

            if (today.isBefore(startDate) || today.isAfter(endDate)) {
                continue;
            }

            Set<String> received = receivedPlayers.getOrDefault(playerId, new HashSet<>());
            if (received.contains(presentName)) {
                player.sendMessage(ChatColor.RED + "すでに「" + presentName + "」を受け取っています！");
                continue;
            }

            for (ItemStack item : presents.get(presentName)) {
                player.getInventory().addItem(item);
            }

            received.add(presentName);
            receivedPlayers.put(playerId, received);
            saveReceivedPlayers();

            player.sendMessage(ChatColor.GREEN + "プレゼント「" + presentName + "」を受け取りました！");
        }
    }

    void loadPresentItems() {
        presentsFile = new File(getDataFolder(), "presents.yml");
        if (!presentsFile.exists()) {
            saveResource("presents.yml", false);
        }

        presentsConfig = YamlConfiguration.loadConfiguration(presentsFile);
        presents.clear();

        ConfigurationSection presentsSection = presentsConfig.getConfigurationSection("presents");
        if (presentsSection != null) {
            for (String presentName : presentsSection.getKeys(false)) {
                List<ItemStack> itemList = (List<ItemStack>) presentsConfig.getList("presents." + presentName + ".items");
                if (itemList != null) {
                    presents.put(presentName, itemList);
                }
            }
        }
    }

    public void savePresentItems() {
        presentsConfig.set("presents", presents);
        try {
            presentsConfig.save(presentsFile);
        } catch (IOException e) {
            getLogger().severe("プレゼントデータの保存中にエラーが発生しました。");
            e.printStackTrace();
        }
    }

    private void loadReceivedPlayers() {
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

    private void saveReceivedPlayers() {
        FileConfiguration config = getConfig();
        config.set("receivedPlayers", null);

        for (Map.Entry<UUID, Set<String>> entry : receivedPlayers.entrySet()) {
            config.set("receivedPlayers." + entry.getKey().toString(), new ArrayList<>(entry.getValue()));
        }

        saveConfig();
    }

    public Inventory getAdminGUI() {
        return adminGUI;
    }

    private void createAdminGUI() {
        adminGUI = Bukkit.createInventory(null, 9, ChatColor.RED + "管理者用プレゼント設定");
    }

    public void clearPresents() {
        presents.clear();
        receivedPlayers.clear();
        savePresentItems();
        saveReceivedPlayers();
        getLogger().info("すべてのプレゼントデータを削除しました！");
    }
}
