package com.bnana.commands;

import com.bnana.ConfigManager;
import com.bnana.TPAStorage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TPAGUIToggleCommand implements CommandExecutor {

    private final TPAStorage storage;
    private final ConfigManager configManager;

    public TPAGUIToggleCommand(TPAStorage storage, ConfigManager configManager) {
        this.storage = storage;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player p = (Player) sender;
        UUID id = p.getUniqueId();

        boolean enabled = storage.isGuiEnabled(id);
        storage.setGuiEnabled(id, !enabled);

        Map<String, String> replacements = new HashMap<>();
        replacements.put("status", !enabled ? "§aENABLED" : "§cDISABLED");

        p.sendMessage(configManager.getMessage("gui-toggled", replacements));
        return true;
    }
}
