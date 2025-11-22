package com.bnana.commands;

import com.bnana.AvoidTPA;
import com.bnana.ConfigManager;
import com.bnana.TPAStorage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TPAcceptCommand implements CommandExecutor {

    private final TPAStorage storage;
    private final AvoidTPA plugin;
    private final ConfigManager configManager;
    private final Map<UUID, TeleportData> pendingTeleports = new HashMap<>();

    public TPAcceptCommand(TPAStorage storage, AvoidTPA plugin, ConfigManager configManager) {
        this.storage = storage;
        this.plugin = plugin;
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
        if (requester == null) {
            target.sendMessage(configManager.getMessage("requester-offline"));
            storage.removeRequest(target.getUniqueId());
            return true;
        }

        if (storage.isGuiEnabled(target.getUniqueId())) {
            target.openInventory(plugin.getGuiManager().createTPAGui(target, requester, req.isHere));
            return true;
        }

        startTeleport(target, requester, req);
        return true;
    }

    public void startTeleport(Player target, Player requester, TPAStorage.Request req) {
        long delay = configManager.getTeleportDelay();
        Map<String, String> replacements = new HashMap<>();
        replacements.put("delay", String.valueOf(delay));
        replacements.put("target", target.getName());

        target.sendMessage(configManager.getMessage("accepted", replacements));
        requester.sendMessage(configManager.getMessage("accepted-requester", replacements));

        if (configManager.isCancelOnMove()) {
            Location startLoc = requester.getLocation().clone();
            TeleportData data = new TeleportData(req, startLoc);
            pendingTeleports.put(requester.getUniqueId(), data);

            BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                if (!requester.isOnline() || !target.isOnline()) {
                    cancelTeleport(requester.getUniqueId());
                    return;
                }

                Location currentLoc = requester.getLocation();
                if (startLoc.getBlockX() != currentLoc.getBlockX() ||
                    startLoc.getBlockY() != currentLoc.getBlockY() ||
                    startLoc.getBlockZ() != currentLoc.getBlockZ()) {
                    requester.sendMessage(configManager.getMessage("movement-cancelled"));
                    target.sendMessage(configManager.getMessage("movement-cancelled"));
                    cancelTeleport(requester.getUniqueId());
                }
            }, 0L, 5L);

            data.task = task;
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            TPAStorage.Request now = storage.getRequest(target.getUniqueId());
            if (now == null || !now.equals(req)) return;

            Player t = Bukkit.getPlayer(target.getUniqueId());
            Player r = Bukkit.getPlayer(req.requester);
            if (t == null || r == null) return;

            if (configManager.isCancelOnMove()) {
                TeleportData data = pendingTeleports.remove(r.getUniqueId());
                if (data == null) return;
                if (data.task != null) data.task.cancel();
            }

            Map<String, String> tpReplacements = new HashMap<>();
            if (req.isHere) {
                r.teleport(t.getLocation());
                tpReplacements.put("target", t.getName());
                r.sendMessage(configManager.getMessage("teleported-to", tpReplacements));
            } else {
                r.teleport(t.getLocation());
                tpReplacements.put("target", t.getName());
                r.sendMessage(configManager.getMessage("teleported-to", tpReplacements));
            }

            t.sendMessage(configManager.getMessage("teleport-complete"));
            storage.removeRequest(target.getUniqueId());
        }, delay * 20L);
    }

    private void cancelTeleport(UUID uuid) {
        TeleportData data = pendingTeleports.remove(uuid);
        if (data != null && data.task != null) {
            data.task.cancel();
        }
        Player p = Bukkit.getPlayer(uuid);
        if (p != null) {
            storage.removeRequest(data.request.target);
        }
    }

    private static class TeleportData {
        TPAStorage.Request request;
        Location startLocation;
        BukkitTask task;

        TeleportData(TPAStorage.Request request, Location startLocation) {
            this.request = request;
            this.startLocation = startLocation;
        }
    }
}
