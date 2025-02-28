package net.azisaba.simplepresents;

import net.azisaba.simplepresents.command.PresentCommand;
import net.azisaba.simplepresents.listener.AdminPresentChatListener;
import net.azisaba.simplepresents.listener.AdminPresentGuiListener;
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

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

public class SimplePresents extends JavaPlugin {

    private final Map<String, Present> presents = new HashMap<>();
    private final Map<UUID, Set<String>> receivedPlayers = new HashMap<>();
    private final Map<UUID, Boolean> awaitingName = new HashMap<>();
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

        Bukkit.getPluginManager().registerEvents(new AdminPresentGuiListener(this), this);
        Bukkit.getPluginManager().registerEvents(new AdminPresentChatListener(this), this);

        getCommand("presents").setExecutor(new PresentCommand(this));
    }

    @Override
    public void onDisable() {
        saveReceivedPlayers();
        savePresentItems();
    }

    // プレゼント受け取り判定
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

    // プレゼントをプレイヤーに渡す
    public void givePresent(Player player) {
        UUID playerId = player.getUniqueId();
        LocalDate today = LocalDate.now();
        boolean receivedAny = false;

        for (String presentName : presents.keySet()) {
            ConfigurationSection presentSection = presentsConfig.getConfigurationSection("presents." + presentName);
            if (presentSection == null) continue;

            LocalDate start = LocalDate.parse(presentSection.getString("start"));
            LocalDate end = LocalDate.parse(presentSection.getString("end"));

            if (today.isBefore(start) || today.isAfter(end)) {
                continue;
            }

            Set<String> received = receivedPlayers.getOrDefault(playerId, new HashSet<>());
            if (received.contains(presentName)) {
                continue;
            }

            Present present = presents.get(presentName);
            if (present != null) {
                present.giveTo(player);

                // メッセージを送信
                String message = presentSection.getString("message", "プレゼント「" + presentName + "」を受け取りました！");
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));

                received.add(presentName);
                receivedPlayers.put(playerId, received);
                saveReceivedPlayers();

                receivedAny = true;
            }
        }

        if (!receivedAny) {
            player.sendMessage(ChatColor.RED + "現在受け取れるプレゼントはありません。");
        }
    }

    // プレゼントデータ読み込み
    public void loadPresentItems() {
        presentsFile = new File(getDataFolder(), "presents.yml");
        if (!presentsFile.exists()) {
            saveResource("presents.yml", false);
        }

        presentsConfig = YamlConfiguration.loadConfiguration(presentsFile);
        presents.clear();

        ConfigurationSection section = presentsConfig.getConfigurationSection("presents");
        if (section != null) {
            for (String name : section.getKeys(false)) {
                List<Map<?, ?>> itemsMap = section.getMapList("presents." + name + ".items");
                Present present = Present.deserialize(itemsMap);
                presents.put(name, present);
            }
        }
    }

    // プレゼントデータ保存
    public void savePresentItems() {
        for (Map.Entry<String, Present> entry : presents.entrySet()) {
            List<Map<String, Object>> serializedItems = entry.getValue().serialize();
            presentsConfig.set("presents." + entry.getKey() + ".items", serializedItems);
        }

        try {
            presentsConfig.save(presentsFile);
        } catch (IOException e) {
            getLogger().severe("presents.yml の保存に失敗しました！");
            e.printStackTrace();
        }
    }

    // プレイヤーごとの受け取り履歴読み込み
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

    // プレイヤーごとの受け取り履歴保存
    public void saveReceivedPlayers() {
        FileConfiguration config = getConfig();
        config.set("receivedPlayers", null);

        for (Map.Entry<UUID, Set<String>> entry : receivedPlayers.entrySet()) {
            config.set("receivedPlayers." + entry.getKey().toString(), new ArrayList<>(entry.getValue()));
        }

        saveConfig();
    }

    // プレゼント一覧表示
    public void showPresentList(Player player) {
        player.sendMessage(ChatColor.BLUE + "===SimplePresents プレゼント一覧===");

        if (presents.isEmpty()) {
            player.sendMessage(ChatColor.GRAY + "現在登録されているプレゼントはありません。");
        } else {
            for (String presentName : presents.keySet()) {
                player.sendMessage(ChatColor.AQUA + "- " + presentName);
            }
        }

        player.sendMessage(ChatColor.BLUE + "==========================");
    }

    // プレゼントを保存
    public void savePresent(String presentName, List<PresentItem> items) {
        Present present = new Present(items);
        presents.put(presentName, present);

        List<Map<String, Object>> serializedItems = present.serialize();
        presentsConfig.set("presents." + presentName + ".items", serializedItems);

        presentsConfig.set("presents." + presentName + ".start", "2025-01-01");
        presentsConfig.set("presents." + presentName + ".end", "2025-01-07");

        try {
            presentsConfig.save(presentsFile);
        } catch (IOException e) {
            getLogger().severe("プレゼントデータの保存に失敗しました: " + presentName);
            e.printStackTrace();
        }
    }

    public void resetPlayerPresents(String playerName) {
        Player player = Bukkit.getPlayer(playerName);
        if (player != null) {
            receivedPlayers.remove(player.getUniqueId());
            saveReceivedPlayers();
        }
    }

    public Map<String, Present> getPresents() {
        return presents;
    }

    public Inventory getAdminGUI() {
        return adminGUI;
    }

    public void createAdminGUI() {
        adminGUI = Bukkit.createInventory(null, 18, ChatColor.RED + "プレゼント設定");

        ItemStack grayGlass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        for (int i = 9; i < 17; i++) {
            adminGUI.setItem(i, grayGlass);
        }

        ItemStack save = new ItemStack(Material.EMERALD_BLOCK);
        save.getItemMeta().setDisplayName(ChatColor.GREEN + "保存する");
        adminGUI.setItem(17, save);
    }

    public void setAwaitingName(UUID uuid, boolean value) {
        awaitingName.put(uuid, value);
    }

    public boolean isAwaitingName(UUID uuid) {
        return awaitingName.getOrDefault(uuid, false);
    }
}
