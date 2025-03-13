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
    public void givePresent(Player player) {
        UUID playerId = player.getUniqueId();
        LocalDate today = LocalDate.now();
        boolean receivedAny = false;

        for (String presentName : presents.keySet()) {
            List<PresentItem> presentItems = presents.get(presentName);  // List<PresentItem> で取得

            ConfigurationSection presentSection = presentsConfig.getConfigurationSection("presents." + presentName);
            if (presentSection == null) continue;

            String startStr = presentSection.getString("start");
            String endStr = presentSection.getString("end");

            if (startStr == null || endStr == null) {
                getLogger().warning("プレゼント " + presentName + " の期間設定が不正です。");
                continue;
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate startDate = LocalDate.parse(startStr, formatter);
            LocalDate endDate = LocalDate.parse(endStr, formatter);

            // 期間外ならスキップ
            if (today.isBefore(startDate) || today.isAfter(endDate)) {
                continue;
            }

            // 受け取り済みならスキップ
            Set<String> received = receivedPlayers.getOrDefault(playerId, new HashSet<>());
            if (received.contains(presentName)) {
                continue;
            }

            // PresentItemを使ってプレゼントを渡す
            for (PresentItem presentItem : presentItems) {
                presentItem.giveTo(player);  // 例えば、PresentItemクラスのgiveToメソッドを使用してアイテムを渡す
            }

            // メッセージを送信
            String message = presentSection.getString("message", "プレゼント「" + presentName + "」を受け取りました！");
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));

            // 受け取り履歴に保存
            received.add(presentName);
            receivedPlayers.put(playerId, received);
            saveReceivedPlayers();

            receivedAny = true;
        }

        if (!receivedAny) {
            player.sendMessage(ChatColor.RED + "現在受け取れるプレゼントはありません。");
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
            presentsConfig.set("presents." + entry.getKey() + ".items", serializedItems);
        }

        try {
            presentsConfig.save(presentsFile);
        } catch (IOException e) {
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
        presents.put(presentName, items); // メモリ上に保存

        List<Map<String, Object>> serializedItems = new ArrayList<>();
        for (PresentItem item : items) {
            serializedItems.add(item.serialize());
        }

        presentsConfig.set("presents." + presentName + ".items", serializedItems);
        presentsConfig.set("presents." + presentName + ".start", "2025-01-01"); // 仮の日付
        presentsConfig.set("presents." + presentName + ".end", "2025-1-03");   // 仮の日付

        try {
            presentsConfig.save(presentsFile);
        } catch (IOException e) {
            getLogger().severe("プレゼントデータの保存に失敗しました: " + presentName);
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
