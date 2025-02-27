package net.azisaba.simplepresents;

import net.azisaba.simplepresents.Listener.AdminPresentChatListener;
import net.azisaba.simplepresents.Listener.AdminPresentGuiListener;
import net.azisaba.simplepresents.PresentCommand;
import net.azisaba.simplepresents.model.PresentItem;
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

    public boolean canReceivePresent(Player player) {
        LocalDate today = LocalDate.now();
        UUID playerId = player.getUniqueId();

        for (String presentName : presents.keySet()) {
            ConfigurationSection section = presentsConfig.getConfigurationSection("presents." + presentName);
            if (section == null) continue;

            LocalDate start = LocalDate.parse(section.getString("start"));
            LocalDate end = LocalDate.parse(section.getString("end"));

            if (!today.isBefore(start) && !today.isAfter(end)) {
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
            ConfigurationSection section = presentsConfig.getConfigurationSection("presents." + presentName);
            if (section == null) continue;

            LocalDate start = LocalDate.parse(section.getString("start"));
            LocalDate end = LocalDate.parse(section.getString("end"));

            if (today.isBefore(start) || today.isAfter(end)) continue;

            Set<String> received = receivedPlayers.getOrDefault(playerId, new HashSet<>());
            if (received.contains(presentName)) continue;

            for (PresentItem item : presents.get(presentName)) {
                item.giveTo(player);
            }

            received.add(presentName);
            receivedPlayers.put(playerId, received);
            saveReceivedPlayers();

            player.sendMessage(ChatColor.GREEN + "プレゼント「" + presentName + "」を受け取りました！");
        }
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

    public void resetPlayerPresents(String playerName) {
        Player player = Bukkit.getPlayer(playerName);
        if (player == null) return;

        receivedPlayers.remove(player.getUniqueId());
        saveReceivedPlayers();
    }

    public Map<String, List<PresentItem>> getPresents() {
        return presents;
    }

    public Inventory getAdminGUI() {
        return adminGUI;
    }

    public void createAdminGUI() {
        adminGUI = Bukkit.createInventory(null, 18, ChatColor.RED + "プレゼント設定");

        ItemStack grayGlass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        for (int i = 9; i < 17; i++) adminGUI.setItem(i, grayGlass);

        ItemStack save = new ItemStack(Material.EMERALD_BLOCK);
        save.getItemMeta().setDisplayName(ChatColor.GREEN + "保存する");
        adminGUI.setItem(17, save);
    }

    public void clearPresents() {
        presents.clear();
        savePresentItems();
    }

    public void setAwaitingName(UUID uuid, boolean value) {
        awaitingName.put(uuid, value);
    }

    public boolean isAwaitingName(UUID uuid) {
        return awaitingName.getOrDefault(uuid, false);
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
        config.set("receivedPlayers", null); // 一度クリアして再保存

        for (Map.Entry<UUID, Set<String>> entry : receivedPlayers.entrySet()) {
            config.set("receivedPlayers." + entry.getKey().toString(), new ArrayList<>(entry.getValue()));
        }

        saveConfig();
    }

    public void showPresentList(Player player) {
        player.sendMessage(ChatColor.BLUE + "===SimplePresents プレゼント一覧===");

        if (presents.isEmpty()) {
            player.sendMessage(ChatColor.GRAY + "現在、登録されているプレゼントはありません。");
        } else {
            for (String presentName : presents.keySet()) {
                player.sendMessage(ChatColor.AQUA + "- " + presentName);
            }
        }

        player.sendMessage(ChatColor.BLUE + "==========================");
    }

    public void savePresent(String presentName, List<PresentItem> items) {
        presents.put(presentName, items); // メモリ上に保存

        // presents.ymlにも保存
        List<Map<String, Object>> serializedItems = new ArrayList<>();
        for (PresentItem item : items) {
            serializedItems.add(item.serialize());
        }

        presentsConfig.set("presents." + presentName + ".items", serializedItems);
        presentsConfig.set("presents." + presentName + ".start", "2025-01-01"); // 仮の日付
        presentsConfig.set("presents." + presentName + ".end", "2025-1-3");   // 仮の日付

        try {
            presentsConfig.save(presentsFile);
        } catch (IOException e) {
            getLogger().severe("プレゼントデータの保存に失敗しました: " + presentName);
            e.printStackTrace();
        }
    }


}
