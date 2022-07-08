package com.everneth.emi.events;

import co.aikar.idb.DB;
import com.everneth.emi.EMI;
import com.everneth.emi.Utils;
import com.everneth.emi.managers.MotdManager;
import com.everneth.emi.models.EMIPlayer;
import com.everneth.emi.models.Motd;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

import java.sql.SQLException;

/**
 *     Class: JoinEvent
 *     Author: Faceman (@TptMike)
 *     Purpose: Handle player joins and broadcast MOTDs set by the ministries
 *
 */

public class JoinEvent implements Listener {
    private final Plugin plugin;

    public JoinEvent(Plugin plugin)
    {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        Player player = event.getPlayer();
        EMIPlayer playerRow = EMIPlayer.getEmiPlayer(player.getUniqueId());

        // Did we find anything?
        if (playerRow.isEmpty()) {
            //No records returned. Add player to database.
            try {
                DB.executeInsert(
                        "INSERT INTO players (player_name, player_uuid) VALUES(?,?)",
                        player.getName(),
                        player.getUniqueId().toString()
                );
            } catch (SQLException e) {
                EMI.getPlugin().getLogger().warning(e.getMessage());
            }
        }
        else if (!playerRow.getName().equals(player.getName())
                && playerRow.getUuid().toString().equals(player.getUniqueId().toString()))
        {
            //Record found, name mismatch. Update the record with the players current name.
            DB.executeUpdateAsync(
                  "UPDATE players SET player_name = ? WHERE player_uuid = ?",
                  player.getName(),
                  player.getUniqueId().toString()
            );
        }
        else if (playerRow.getAltName() != null
                && !playerRow.getAltName().equals(player.getName())
                && playerRow.getAltUuid().equals(player.getUniqueId().toString()))
        {
            // Name mismatch on the alt account, update it.
            DB.executeUpdateAsync("UPDATE players SET alt_name = ? WHERE player_uuid = ?",
                    player.getName(),
                    player.getUniqueId().toString());
        }

        // Display MOTDs to player upon login.
        MotdManager motdManager = MotdManager.getMotdManager();
        for(Motd motd : motdManager.getMotds().values())
        {
            player.sendMessage(Utils.color(motd.displayMotd()));
        }
    }
}