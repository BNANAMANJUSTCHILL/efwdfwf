package com.bnana.listeners;

import com.bnana.AvoidTPA;
import com.bnana.ConfigManager;
import com.bnana.GUIManager;
import com.bnana.TPAStorage;
import com.bnana.commands.TPAcceptCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InventoryClickListener implements Listener {

    private final TPAStorage storage;
    private final GUIManager gui;
    private final ConfigManager configManager;

    public InventoryClickListener(TPAStorage storage, GUIManager gui, ConfigManager configManager) {
        this.storage = storage;
        this.gui = gui;
        this.configManager = configManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getView().getTitle() == null) return;
        String guiTitle = AvoidTPA.getInstance().getConfig().getString("gui.title", "Avoid Tpa");
        if (!e.getView().getTitle().startsWith(ChatColor.DARK_PURPLE + guiTitle)) return;

        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player clicker = (Player) e.getWhoClicked();
        ItemStack item = e.getCurrentItem();
        if (item == null || !item.hasItemMeta()) return;

        UUID targetUUID = clicker.getUniqueId();
        TPAStorage.Request req = storage.getRequest(targetUUID);
        if (req == null) {
            clicker.closeInventory();
            return;
        }

        String display = item.getItemMeta().getDisplayName();
        if (display == null) return;
        display = ChatColor.stripColor(display);

        if (display.equalsIgnoreCase("Confirm")) {
            Player requester = Bukkit.getPlayer(req.requester);
            if (requester == null) {
                clicker.sendMessage(configManager.getMessage("requester-offline"));
                storage.removeRequest(targetUUID);
                clicker.closeInventory();
                return;
            }

            clicker.closeInventory();
            TPAcceptCommand acceptCmd = new TPAcceptCommand(storage, AvoidTPA.getInstance(), configManager);
            acceptCmd.startTeleport(clicker, requester, req);
            return;
        }

        if (display.equalsIgnoreCase("Cancel") || display.equalsIgnoreCase("Deny")) {
            Player requester = Bukkit.getPlayer(req.requester);

            Map<String, String> replacements = new HashMap<>();
            replacements.put("target", clicker.getName());

            if (requester != null) {
                requester.sendMessage(configManager.getMessage("denied-requester", replacements));
            }

            clicker.sendMessage(configManager.getMessage("denied"));
            storage.removeRequest(targetUUID);
            clicker.closeInventory();
            return;
        }
    }
}
