package com.bnana;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public class ConfigManager {

    private final AvoidTPA plugin;

    public ConfigManager(AvoidTPA plugin) {
        this.plugin = plugin;
    }

    public String getMessage(String key, Map<String, String> replacements) {
        FileConfiguration cfg = plugin.getConfig();
        String msg = cfg.getString("messages." + key, "");

        if (msg.contains("{prefix}")) {
            String prefix = cfg.getString("messages.prefix", "&5Avoid &9Tpa &7> &f");
            msg = msg.replace("{prefix}", prefix);
        }

        if (replacements != null) {
            for (Map.Entry<String, String> entry : replacements.entrySet()) {
                msg = msg.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }

        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    public String getMessage(String key) {
        return getMessage(key, null);
    }

    public long getTeleportDelay() {
        return plugin.getConfig().getLong("settings.teleport-delay-seconds", 5L);
    }

    public long getSendCooldown() {
        return plugin.getConfig().getLong("settings.send-cooldown-seconds", 30L);
    }

    public long getRequestExpire() {
        return plugin.getConfig().getLong("settings.request-expire-seconds", 30L);
    }

    public boolean isCancelOnMove() {
        return plugin.getConfig().getBoolean("settings.cancel-on-move", true);
    }
}
