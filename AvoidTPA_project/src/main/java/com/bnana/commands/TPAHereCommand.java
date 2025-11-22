package com.bnana.commands;

import com.bnana.AvoidTPA;
import com.bnana.ConfigManager;
import com.bnana.TPAStorage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class TPAHereCommand implements CommandExecutor {

    private final TPAStorage storage;
    private final AvoidTPA plugin;
    private final ConfigManager configManager;

    public TPAHereCommand(TPAStorage storage, AvoidTPA plugin, ConfigManager configManager) {
        this.storage = storage;
        this.plugin = plugin;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player p = (Player) sender;

        if (args.length != 1) {
            p.sendMessage(configManager.getMessage("usage-tpahere"));
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            p.sendMessage(configManager.getMessage("player-not-found"));
            return true;
        }

        if (target.equals(p)) {
            p.sendMessage(configManager.getMessage("cannot-request-self"));
            return true;
        }

        if (!storage.canSend(p.getUniqueId())) {
            p.sendMessage(configManager.getMessage("cooldown-wait"));
            return true;
        }

        storage.addRequest(target.getUniqueId(), p.getUniqueId(), true);
        storage.setSendCooldown(p.getUniqueId(), configManager.getSendCooldown());

        Map<String, String> replacements = new HashMap<>();
        replacements.put("target", target.getName());
        replacements.put("requester", p.getName());

        p.sendMessage(configManager.getMessage("tpahere-sent", replacements));
        target.sendMessage(configManager.getMessage("tpahere-received", replacements));

        if (storage.isGuiEnabled(target.getUniqueId())) {
            target.openInventory(plugin.getGuiManager().createTPAGui(target, p, true));
        } else {
            replacements.put("delay", String.valueOf(configManager.getRequestExpire()));
            target.sendMessage(configManager.getMessage("request-expire-seconds", replacements));
        }

        return true;
    }
}
