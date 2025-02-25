package net.azisaba.simplepresents;

import net.azisaba.simplepresents.Listener.AdminCommandPresentsListener;
import net.azisaba.simplepresents.Listener.AdminPresentGuiListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class SimplePresents extends JavaPlugin implements Listener {
    private Set<UUID> receivedPlayers = new HashSet<>();
    private List<ItemStack> presentItems = new ArrayList<>();
    private Inventory adminGUI;
    private File presentsFile;
    private FileConfiguration presentsConfig;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadReceivedPlayers();
        loadPresentItems();
        createAdminGUI();

        //プラグインが読み込まれない; ;
        getLogger().info("SimplePresents has been enabled!");

        // イベントリスナー登録
        Bukkit.getPluginManager().registerEvents(new AdminCommandPresentsListener(this, receivedPlayers), this);
        Bukkit.getPluginManager().registerEvents(new AdminPresentGuiListener(this), this);

        // コマンド登録
        getCommand("getpresent").setExecutor(new PresentCommand(this));
        getCommand("reloadpresents").setExecutor(new PresentCommand(this));
        getCommand("adminpresent").setExecutor(new PresentCommand(this));
        getCommand("clearpresents").setExecutor(new PresentCommand(this));
        getCommand("resetpresent").setExecutor(new PresentCommand(this));
    }

    @Override
    public void onDisable() {
        saveReceivedPlayers();
        savePresentItems();
    }

    private void createAdminGUI() {
        adminGUI = Bukkit.createInventory(null, 9, ChatColor.RED + "管理者用プレゼント設定");
        ItemStack saveItem = new ItemStack(Material.EMERALD_BLOCK);
        saveItem.getItemMeta().setDisplayName(ChatColor.GREEN + "保存");
        adminGUI.setItem(8, saveItem);
    }

    public Inventory getAdminGUI() {
        return adminGUI;
    }

    public List<ItemStack> getPresentItems() {
        return presentItems;
    }

    public void savePresentItems() {
        if (presentItems == null) {
            getLogger().warning("プレゼントデータ (presentItems) が null です。保存をスキップします。");
            return;
        }

        if (presentsConfig == null || presentsFile == null) {
            getLogger().warning("presentsConfig または presentsFile が null です。保存をスキップします。");
            return;
        }

        // プレゼントデータを保存
        presentsConfig.set("presents", presentItems);

        try {
            presentsConfig.save(presentsFile);
        } catch (IOException e) {
            getLogger().severe("プレゼントデータの保存中にエラーが発生しました。");
            e.printStackTrace();
        }
    }


    public void loadPresentItems() {
        presentsFile = new File(getDataFolder(), "presents.yml");

        // ファイルが存在しない場合はデフォルトのものをコピー
        if (!presentsFile.exists()) {
            saveResource("presents.yml", false);
        }

        // 設定ファイルをロード
        presentsConfig = YamlConfiguration.loadConfiguration(presentsFile);

        // "presents" セクションが存在するかチェック
        ConfigurationSection presentsSection = presentsConfig.getConfigurationSection("presents");

        if (presentsSection != null) {
            presentItems = new ArrayList<>();

            for (String key : presentsSection.getKeys(false)) {
                Object item = presentsSection.get(key);

                if (item instanceof ItemStack) {
                    presentItems.add((ItemStack) item);
                } else {
                    getLogger().warning("Invalid item found in presents.yml: " + key);
                }
            }
        } else {
            presentItems = new ArrayList<>();
        }
    }


    private void loadReceivedPlayers() {
        FileConfiguration config = getConfig();
        receivedPlayers.clear();
        for (String uuid : config.getStringList("receivedPlayers")) {
            receivedPlayers.add(UUID.fromString(uuid));
        }
    }

    private void saveReceivedPlayers() {
        getConfig().set("receivedPlayers", new ArrayList<>(receivedPlayers));
        saveConfig();
    }

    public void givePresent(Player player) {
        if (receivedPlayers.contains(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "すでにプレゼントを受け取っています！");
            return;
        }

        for (ItemStack item : presentItems) {
            player.getInventory().addItem(item);
        }

        receivedPlayers.add(player.getUniqueId());
        saveReceivedPlayers();
        player.sendMessage(ChatColor.GREEN + "プレゼントを受け取りました！");
    }
}
