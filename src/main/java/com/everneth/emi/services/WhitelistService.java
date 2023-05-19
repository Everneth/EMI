package com.everneth.emi.services;

import co.aikar.idb.DB;
import com.everneth.emi.EMI;
import com.everneth.emi.models.EMIPlayer;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.HashMap;

public class WhitelistService {
    private static WhitelistService service;
    private HashMap<Long,String> whitelistRequests = new HashMap<>();

    public static WhitelistService getService() {
        if (service == null) {
            service = new WhitelistService();
        }
        return service;
    }

    public void addToWhitelistTemporarily(long discordId, String name) {
        EMI.getPlugin().getServer().getScheduler().callSyncMethod(EMI.getPlugin(), () ->
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "whitelist add " + name));

        // If put returns some value that indicates the user has attempted to whitelist multiple accounts
        String oldUsername = whitelistRequests.put(discordId, name);
        if (oldUsername != null) {
            EMI.getPlugin().getServer().getScheduler().callSyncMethod(EMI.getPlugin(), () ->
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "whitelist remove " + oldUsername));
        }
        // Create a BukkitScheduler and execute the whitelist removal check after 5 minutes
        BukkitScheduler scheduler = EMI.getPlugin().getServer().getScheduler();
        scheduler.scheduleSyncDelayedTask(EMI.getPlugin(), new Runnable() {
            @Override
            public void run() {
                EMIPlayer player = EMIPlayer.getEmiPlayer(name);
                // If row or discord_id is null, the player did not sync and needs to be removed from the whitelist
                if (player.isEmpty() || player.getDiscordId() == 0) {
                    EMI.getPlugin().getServer().getScheduler().callSyncMethod(EMI.getPlugin(), () ->
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "whitelist remove " + name));
                    whitelistRequests.remove(discordId);
                }
            }
        }, 20L * 60 * 5);
    }

    public void removeAllFromWhitelist() {
        BukkitScheduler scheduler = EMI.getPlugin().getServer().getScheduler();
        for (String username : whitelistRequests.values()) {
            scheduler.callSyncMethod(EMI.getPlugin(), () ->
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "whitelist remove " + username));
            DB.executeUpdateAsync("DELETE FROM players WHERE player_name = ?", username);
        }

        whitelistRequests = new HashMap<>();
    }

    public boolean isWhitelisted(String name) {
        return whitelistRequests.containsValue(name);
    }
}
