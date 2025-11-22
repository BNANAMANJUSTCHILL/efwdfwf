package com.bnana;

import com.bnana.listeners.InventoryClickListener;
import com.bnana.commands.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class AvoidTPA extends JavaPlugin {

    private static AvoidTPA instance;
    private TPAStorage storage;
    private GUIManager guiManager;
    private ConfigManager configManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        configManager = new ConfigManager(this);
        storage = new TPAStorage(this, configManager);
        guiManager = new GUIManager(this, configManager);

        getCommand("tpa").setExecutor(new TPACommand(storage, this, configManager));
        getCommand("tpahere").setExecutor(new TPAHereCommand(storage, this, configManager));
        getCommand("tpaccept").setExecutor(new TPAcceptCommand(storage, this, configManager));
        getCommand("tpdeny").setExecutor(new TPADenyCommand(storage, configManager));
        getCommand("tpaguitoggle").setExecutor(new TPAGUIToggleCommand(storage, configManager));

        Bukkit.getPluginManager().registerEvents(new InventoryClickListener(storage, guiManager, configManager), this);

        getLogger().info("AvoidTPA enabled.");
    }

    @Override
    public void onDisable() {
        storage.savePrefs();
        getLogger().info("AvoidTPA disabled.");
    }

    public static AvoidTPA getInstance() {
        return instance;
    }

    public GUIManager getGuiManager() {
        return guiManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}
