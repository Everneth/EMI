package com.everneth.emi.events;

import co.aikar.idb.DB;
import co.aikar.idb.DbRow;

import com.everneth.emi.managers.MotdManager;
import com.everneth.emi.managers.ReportManager;
import com.everneth.emi.Utils;
import com.everneth.emi.EMI;
import com.everneth.emi.models.EMIPlayer;
import com.everneth.emi.models.Motd;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 *     Class: JoinEvent
 *     Author: Faceman (@TptMike)
 *     Purpose: Handle player joins and broadcast MOTDs set by the ministries
 *
 */

public class JoinEvent implements Listener {


    private final String INT_INTRO = "&7[&dMINT&7] ";
    private final String COMP_INTRO = "&7[&cCOMP&7] ";
    private final String COMM_INTRO = "&7[&9COMM&7] ";
    private final Plugin plugin;

    private List<Motd> motdList;
    private CompletableFuture<List<DbRow>> futureList;
    private CompletableFuture<DbRow> playerObjectFuture;
    private DbRow playerRow;
    private List<DbRow> rows;

    public JoinEvent(Plugin plugin)
    {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        Player player = event.getPlayer();
        DbRow playerRow = EMIPlayer.getPlayerRow(player.getUniqueId());

        // Did we find anything?
        if (playerRow == null) {
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
        else if (!playerRow.getString("player_name").equals(player.getName())
                && playerRow.getString("player_uuid").equals(player.getUniqueId().toString()))
        {
            //Record found, name mismatch. Update the record with the players current name.
            DB.executeUpdateAsync(
                  "UPDATE players SET player_name = ? WHERE player_uuid = ?",
                  player.getName(),
                  player.getUniqueId().toString()
            );
        }
        else if (playerRow.getString("alt_name") != null
                && !playerRow.getString("alt_name").equals(player.getName())
                && playerRow.getString("alt_uuid").equals(player.getUniqueId().toString()))
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

        ReportManager rm = ReportManager.getReportManager();
        if(rm.hasActiveReport(player.getUniqueId()))
        {
            int numMissed = rm.messagesMissed(player.getUniqueId());
            if(numMissed > 0)
            {
                player.sendMessage(Utils.color("&c[!]&f You have &6" + numMissed + " &fmissed messages on your report. Please use &c/getreplies&f to view them."));
            }
        }
    }
}