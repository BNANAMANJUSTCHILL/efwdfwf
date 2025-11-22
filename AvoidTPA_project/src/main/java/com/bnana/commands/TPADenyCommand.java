package com.bnana.commands;

import com.bnana.ConfigManager;
import com.bnana.TPAStorage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class TPADenyCommand implements CommandExecutor {

    private final TPAStorage storage;
    private final ConfigManager configManager;

    public TPADenyCommand(TPAStorage storage, ConfigManager configManager) {
        this.storage = storage;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player target = (Player) sender;

        TPAStorage.Request req = storage.getRequest(target.getUniqueId());
        if (req == null) {
            target.sendMessage(configManager.getMessage("no-pending"));
            return true;
        }

        Player requester = Bukkit.getPlayer(req.requester);

        Map<String, String> replacements = new HashMap<>();
        replacements.put("target", target.getName());

        if (requester != null) {
            requester.sendMessage(configManager.getMessage("denied-requester", replacements));
        }

        target.sendMessage(configManager.getMessage("denied"));
        storage.removeRequest(target.getUniqueId());
        return true;
    }
}
