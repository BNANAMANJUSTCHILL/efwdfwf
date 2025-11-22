package com.bnana;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GUIManager {

    private final AvoidTPA plugin;
    private final ConfigManager configManager;

    public GUIManager(AvoidTPA plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    public Inventory createTPAGui(Player target, Player requester, boolean isHere) {
        FileConfiguration cfg = plugin.getConfig();
        String title = cfg.getString("gui.title", "Avoid Tpa");
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.DARK_PURPLE + title);

        String fillerMatName = cfg.getString("gui.filler-material", "GRAY_STAINED_GLASS_PANE");
        Material fillerMat = Material.GRAY_STAINED_GLASS_PANE;
        try {
            fillerMat = Material.valueOf(fillerMatName.toUpperCase());
        } catch (Exception ignored) {}
        ItemStack filler = new ItemStack(fillerMat);
        for (int i = 0; i < 27; i++) inv.setItem(i, filler);

        int worldSlot = cfg.getInt("gui.world-slot", 11);
        int headSlot = cfg.getInt("gui.head-slot", 13);
        int acceptSlot = cfg.getInt("gui.accept-slot", 15);
        int denySlot = cfg.getInt("gui.deny-slot", 17);

        Material worldMat = pickMaterialForWorld(target.getWorld().getName());
        ItemStack world = new ItemStack(worldMat);
        ItemMeta wm = world.getItemMeta();
        String worldName = cfg.getString("gui.world-icon.name", "&b{world}");
        worldName = worldName.replace("{world}", target.getWorld().getName());
        wm.setDisplayName(ChatColor.translateAlternateColorCodes('&', worldName));
        List<String> worldLore = cfg.getStringList("gui.world-icon.lore");
        if (!worldLore.isEmpty()) {
            wm.setLore(worldLore.stream()
                .map(s -> ChatColor.translateAlternateColorCodes('&', s))
                .collect(Collectors.toList()));
        }
        wm.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        world.setItemMeta(wm);

        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta sm = (SkullMeta) head.getItemMeta();
        sm.setOwningPlayer(requester);
        String headName = cfg.getString("gui.player-head.name", "&9{player}");
        headName = headName.replace("{player}", requester.getName());
        sm.setDisplayName(ChatColor.translateAlternateColorCodes('&', headName));
        List<String> headLore = isHere
            ? cfg.getStringList("gui.player-head.lore-tpahere")
            : cfg.getStringList("gui.player-head.lore-tpa");
        if (!headLore.isEmpty()) {
            sm.setLore(headLore.stream()
                .map(s -> ChatColor.translateAlternateColorCodes('&', s))
                .collect(Collectors.toList()));
        }
        head.setItemMeta(sm);

        String acceptMatName = cfg.getString("gui.accept-button.material", "LIME_CONCRETE");
        Material acceptMat = Material.LIME_CONCRETE;
        try {
            acceptMat = Material.valueOf(acceptMatName.toUpperCase());
        } catch (Exception ignored) {}
        ItemStack accept = new ItemStack(acceptMat);
        ItemMeta am = accept.getItemMeta();
        String acceptName = cfg.getString("gui.accept-button.name", "&aConfirm");
        am.setDisplayName(ChatColor.translateAlternateColorCodes('&', acceptName));
        List<String> acceptLore = cfg.getStringList("gui.accept-button.lore");
        if (!acceptLore.isEmpty()) {
            am.setLore(acceptLore.stream()
                .map(s -> s.replace("{delay}", String.valueOf(configManager.getTeleportDelay())))
                .map(s -> ChatColor.translateAlternateColorCodes('&', s))
                .collect(Collectors.toList()));
        }
        accept.setItemMeta(am);

        String denyMatName = cfg.getString("gui.deny-button.material", "RED_CONCRETE");
        Material denyMat = Material.RED_CONCRETE;
        try {
            denyMat = Material.valueOf(denyMatName.toUpperCase());
        } catch (Exception ignored) {}
        ItemStack deny = new ItemStack(denyMat);
        ItemMeta dm = deny.getItemMeta();
        String denyName = cfg.getString("gui.deny-button.name", "&cCancel");
        dm.setDisplayName(ChatColor.translateAlternateColorCodes('&', denyName));
        List<String> denyLore = cfg.getStringList("gui.deny-button.lore");
        if (!denyLore.isEmpty()) {
            dm.setLore(denyLore.stream()
                .map(s -> ChatColor.translateAlternateColorCodes('&', s))
                .collect(Collectors.toList()));
        }
        deny.setItemMeta(dm);

        inv.setItem(worldSlot, world);
        inv.setItem(headSlot, head);
        inv.setItem(acceptSlot, accept);
        inv.setItem(denySlot, deny);

        return inv;
    }

    private Material pickMaterialForWorld(String worldName) {
        FileConfiguration cfg = plugin.getConfig();
        String matName = cfg.getString("world-icons." + worldName, null);
        if (matName != null) {
            try { return Material.valueOf(matName.toUpperCase()); } catch (Exception ignored) {}
        }
        switch (worldName.toLowerCase()) {
            case "world": return Material.GRASS_BLOCK;
            case "world_the_end": case "the_end": return Material.END_STONE;
            case "world_nether": case "the_nether": return Material.NETHERRACK;
            case "spawn": return Material.AMETHYST_BLOCK;
            default: return Material.BOOK;
        }
    }
}
