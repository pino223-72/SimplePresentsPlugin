package net.azisaba.simplepresents;

import net.azisaba.simplepresents.listener.AdminPresentGuiListener;
import net.azisaba.simplepresents.listener.AdminPresentChatListener;
import net.azisaba.simplepresents.command.PresentCommand;
import net.azisaba.simplepresents.model.Present;
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
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;

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

    // プレゼントを受け取れるか確認
    public boolean canReceivePresent(Player player) {
        LocalDate today = LocalDate.now();
        UUID playerId = player.getUniqueId();

        for (String presentName : presents.keySet()) {
            ConfigurationSection presentSection = presentsConfig.getConfigurationSection("presents." + presentName);
            if (presentSection == null) continue;

            LocalDate startDate = LocalDate.parse(presentSection.getString("start"));
            LocalDate endDate = LocalDate.parse(presentSection.getString("end"));

            if (!today.isBefore(startDate) && !today.isAfter(endDate)) {
                Set<String> received = receivedPlayers.getOrDefault(playerId, new HashSet<>());
                if (!received.contains(presentName)) {
                    return true;
                }
            }
        }
        return false;
    }

    // プレゼントを渡す
    public void givePresent(Player player, String presentName) {
        UUID playerId = player.getUniqueId();
        LocalDate today = LocalDate.now();

        ConfigurationSection presentSection = presentsConfig.getConfigurationSection("presents." + presentName);
        if (presentSection == null) {
            player.sendMessage(ChatColor.RED + "プレゼント '" + presentName + "' は存在しません！");
            return;
        }

        String startStr = presentSection.getString("start");
        String endStr = presentSection.getString("end");

        if (startStr == null || endStr == null) {
            player.sendMessage(ChatColor.RED + "プレゼント '" + presentName + "' の日付設定が無効です！");
            return;
        }

        LocalDate startDate = LocalDate.parse(startStr);
        LocalDate endDate = LocalDate.parse(endStr);

        if (today.isBefore(startDate) || today.isAfter(endDate)) {
            player.sendMessage(ChatColor.RED + "プレゼント '" + presentName + "' は現在受け取れません！");
            return;
        }

        Set<String> received = receivedPlayers.getOrDefault(playerId, new HashSet<>());
        if (received.contains(presentName)) {
            player.sendMessage(ChatColor.RED + "あなたはすでに '" + presentName + "' を受け取っています！");
            return;
        }

        List<PresentItem> presentItems = presents.get(presentName);
        if (presentItems == null || presentItems.isEmpty()) {
            player.sendMessage(ChatColor.RED + "プレゼント '" + presentName + "' にアイテムが設定されていません！");
            return;
        }

        for (PresentItem item : presentItems) {
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
                    items.add(PresentItem.deserialize((Map<String, Object>) map)); // 修正されたdeserializeメソッドを呼び出す
                }
                presents.put(name, items);
            }
        }
    }


    // プレゼントアイテムをファイルに保存
    public void savePresentItems() {
        for (Map.Entry<String, List<PresentItem>> entry : presents.entrySet()) {
            List<Map<String, Object>> serializedItems = new ArrayList<>();

            for (PresentItem item : entry.getValue()) {
                serializedItems.add(item.serialize());
            }

            // プレゼントの基本情報を保存
            presentsConfig.set("presents." + entry.getKey() + ".items", serializedItems);

            // 既存の設定（start, end, message, etc.）をそのまま保持する
            ConfigurationSection section = presentsConfig.getConfigurationSection("presents." + entry.getKey());
            if (section != null) {
                presentsConfig.set("presents." + entry.getKey() + ".start", section.getString("start", "2025-01-01"));
                presentsConfig.set("presents." + entry.getKey() + ".end", section.getString("end", "2025-12-31"));
                presentsConfig.set("presents." + entry.getKey() + ".message", section.getString("message", "プレゼントを受け取りました！"));
                presentsConfig.set("presents." + entry.getKey() + ".Predictive_Conversion", section.getBoolean("Predictive_Conversion", true));
                presentsConfig.set("presents." + entry.getKey() + ".Passed_at_login", section.getBoolean("Passed_at_login", true));
            }
        }

        try {
            presentsConfig.save(presentsFile);
        } catch (IOException e) {
            getLogger().severe("プレゼントデータの保存中にエラーが発生しました！");
            e.printStackTrace();
        }
    }


    // プレイヤーが受け取ったプレゼント履歴をリセット
    public void resetPlayerPresents(String playerName) {
        Player player = Bukkit.getPlayer(playerName);
        if (player == null) return;

        receivedPlayers.remove(player.getUniqueId());
        saveReceivedPlayers();
    }

    // プレゼントリストを取得
    public Map<String, List<PresentItem>> getPresents() {
        return presents;
    }

    public void setAwaitingName(UUID uuid, boolean value) {
        awaitingName.put(uuid, value);
    }

    // プレゼントの受け取りを待っているか確認
    public boolean isAwaitingName(UUID uuid) {
        return awaitingName.getOrDefault(uuid, false);
    }

    // プレゼントを保存
    public void savePresent(String presentName, List<PresentItem> items) {
        presents.put(presentName, items); // メモリに保存

        List<Map<String, Object>> serializedItems = new ArrayList<>();
        for (PresentItem item : items) {
            serializedItems.add(item.serialize());
        }

        presentsConfig.set("presents." + presentName + ".items", serializedItems);
        presentsConfig.set("presents." + presentName + ".start", "2025-01-01"); // 仮のデフォルト日付
        presentsConfig.set("presents." + presentName + ".end", "2025-12-31");   // 仮のデフォルト日付
        presentsConfig.set("presents." + presentName + ".message", "プレゼント「" + presentName + "」を受け取りました！");
        presentsConfig.set("presents." + presentName + ".Predictive_Conversion", true);
        presentsConfig.set("presents." + presentName + ".Passed_at_login", true);

        try {
            presentsConfig.save(presentsFile);
        } catch (IOException e) {
            getLogger().severe("プレゼント " + presentName + " の保存中にエラーが発生しました！");
            e.printStackTrace();
        }
    }


    // プレゼント履歴の保存
    public void saveReceivedPlayers() {
        FileConfiguration config = getConfig();
        config.set("receivedPlayers", null); // 一度クリアして再保存

        for (Map.Entry<UUID, Set<String>> entry : receivedPlayers.entrySet()) {
            config.set("receivedPlayers." + entry.getKey().toString(), new ArrayList<>(entry.getValue()));
        }

        saveConfig();
    }

    // プレイヤーが受け取ったプレゼント履歴をロード
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

    // 管理者用GUIを作成
    public void createAdminGUI() {
        adminGUI = Bukkit.createInventory(null, 18, ChatColor.RED + "プレゼント設定");

        ItemStack grayGlass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        for (int i = 9; i < 17; i++) adminGUI.setItem(i, grayGlass);

        ItemStack save = new ItemStack(Material.EMERALD_BLOCK);
        save.getItemMeta().setDisplayName(ChatColor.GREEN + "保存する");
        adminGUI.setItem(17, save);
    }

    // 管理者用プレゼントGUIを取得
    public Inventory getAdminGUI() {
        return adminGUI;
    }

    // プレゼントリストを表示
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

    // ヘルプメッセージを表示
    public void showHelpMessage(Player player) {
        player.sendMessage(ChatColor.BLUE + "===SimplePresents HELP===");
        player.sendMessage(ChatColor.AQUA + "/presents get - プレゼントを受け取ります");
        player.sendMessage(ChatColor.AQUA + "/presents set - プレゼントを設定します");
        player.sendMessage(ChatColor.AQUA + "/presents list - プレゼントの一覧を表示します");
        player.sendMessage(ChatColor.AQUA + "/presents adminresetplayer <player> - プレイヤーのプレゼント受け取り履歴をリセットします");
        player.sendMessage(ChatColor.AQUA + "/presents reload - 設定をリロードします");
        player.sendMessage(ChatColor.AQUA + "/presents help - ヘルプを表示します");
        player.sendMessage(ChatColor.BLUE + "==========================");
    }
}
