package com.everneth.emi.services;

import co.aikar.idb.DB;
import co.aikar.idb.DbRow;
import com.everneth.emi.EMI;
import com.everneth.emi.utils.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WhitelistService {
    private static WhitelistService service;
    private ArrayList<String> whitelistedPlayers = new ArrayList<>();

    public static WhitelistService getService() {
        if (service == null) {
            service = new WhitelistService();
        }
        return service;
    }

    public void addToWhitelistTemporarily(String name) {
        EMI.getPlugin().getServer().getScheduler().callSyncMethod(EMI.getPlugin(), () ->
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "whitelist add " + name));
        whitelistedPlayers.add(name);
        // Create a BukkitScheduler and execute the whitelist removal check after 5 minutes
        BukkitScheduler scheduler = EMI.getPlugin().getServer().getScheduler();
        scheduler.scheduleSyncDelayedTask(EMI.getPlugin(), new Runnable() {
            @Override
            public void run() {
                DbRow playerRow = PlayerUtils.getPlayerRow(name);
                // If row or discord_id is null, the player did not sync and needs to be removed from the whitelist
                if (playerRow == null || playerRow.getLong("discord_id") == null) {
                    EMI.getPlugin().getServer().getScheduler().callSyncMethod(EMI.getPlugin(), () ->
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "whitelist remove " + name));
                    whitelistedPlayers.remove(name);
                    DB.executeUpdateAsync("DELETE FROM players WHERE player_name = ?", name);
                }
            }
        }, 20L * 60 * 5);
    }

    public void removeAllFromWhitelist() {
        BukkitScheduler scheduler = EMI.getPlugin().getServer().getScheduler();
        for (String username : whitelistedPlayers) {
            scheduler.callSyncMethod(EMI.getPlugin(), () ->
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "whitelist remove " + username));
            DB.executeUpdateAsync("DELETE FROM players WHERE player_name = ?", username);
        }
        
        whitelistedPlayers = new ArrayList<>();
    }

    public boolean isWhitelisted(String name) {
        return whitelistedPlayers.contains(name);
    }
}
