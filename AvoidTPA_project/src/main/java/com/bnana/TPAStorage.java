package com.bnana;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class TPAStorage {

    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final Map<UUID, Request> pending = new HashMap<>();
    private final Map<UUID, Long> sendCooldown = new HashMap<>();
    private final Map<UUID, Boolean> guiPrefs = new HashMap<>();

    private final File prefsFile;
    private final FileConfiguration prefsCfg;

    public TPAStorage(JavaPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        prefsFile = new File(plugin.getDataFolder(), "players.yml");
        if (!prefsFile.exists()) {
            prefsFile.getParentFile().mkdirs();
            try { prefsFile.createNewFile(); } catch (IOException ignored) {}
        }
        prefsCfg = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(prefsFile);
        // Load prefs
        for (String k : prefsCfg.getKeys(false)) {
            try {
                guiPrefs.put(UUID.fromString(k), prefsCfg.getBoolean(k, true));
            } catch (Exception ignored) {}
        }
    }

    public boolean canSend(UUID sender) {
        long now = System.currentTimeMillis();
        return sendCooldown.getOrDefault(sender, 0L) <= now;
    }

    public void setSendCooldown(UUID sender, long seconds) {
        sendCooldown.put(sender, System.currentTimeMillis() + seconds * 1000L);
    }

    public void savePrefs() {
        for (Map.Entry<UUID, Boolean> e : guiPrefs.entrySet()) {
            prefsCfg.set(e.getKey().toString(), e.getValue());
        }
        try {
            prefsCfg.save(prefsFile);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public boolean isGuiEnabled(UUID player) {
        return guiPrefs.getOrDefault(player, true);
    }

    public void setGuiEnabled(UUID player, boolean enabled) {
        guiPrefs.put(player, enabled);
    }

    public void addRequest(UUID target, UUID requester, boolean isHere) {
        Request r = new Request(target, requester, System.currentTimeMillis(), isHere);
        pending.put(target, r);

        long expireSec = configManager.getRequestExpire();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Request now = pending.get(target);
            if (now != null && now.equals(r)) {
                pending.remove(target);
                Player t = Bukkit.getPlayer(target);
                Player req = Bukkit.getPlayer(requester);

                Map<String, String> replacements = new HashMap<>();
                replacements.put("target", t != null ? t.getName() : "(offline)");

                if (t != null) t.sendMessage(configManager.getMessage("request-expired"));
                if (req != null) req.sendMessage(configManager.getMessage("request-expired-requester", replacements));
            }
        }, expireSec * 20L);
    }

    public Request getRequest(UUID target) {
        return pending.get(target);
    }

    public void removeRequest(UUID target) {
        pending.remove(target);
    }

    public static class Request {
        public final UUID target;
        public final UUID requester;
        public final long createdAt;
        public final boolean isHere;

        public Request(UUID target, UUID requester, long createdAt, boolean isHere) {
            this.target = target;
            this.requester = requester;
            this.createdAt = createdAt;
            this.isHere = isHere;
        }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Request)) return false;
            Request r = (Request)o;
            return Objects.equals(target, r.target) && Objects.equals(requester, r.requester) && createdAt==r.createdAt;
        }
    }
}
