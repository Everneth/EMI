package com.everneth.emi.events;

import co.aikar.idb.DB;
import co.aikar.idb.DbRow;

import com.everneth.emi.ReportManager;
import com.everneth.emi.Utils;
import com.everneth.emi.EMI;
import com.everneth.emi.models.Motd;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

import java.sql.SQLException;
import java.util.ArrayList;
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
    private CompletableFuture<DbRow> playerOjbectFuture;
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

        //Check if the player exists in the EMI database
        //Query the database and put it in a future
        playerOjbectFuture = DB.getFirstRowAsync(
                "SELECT * FROM players WHERE player_uuid = ?",
                player.getUniqueId().toString()
        );
        try {
            //Try to get the row from the future and put it into a DbRow object
            playerRow = playerOjbectFuture.get();
        }
        catch (Exception e)
        {
            //Something went wrong, record the error. This should never happen.
            EMI.getPlugin().getLogger().warning(e.getMessage());
        }
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
        else if(!playerRow.getString("player_name").equals(player.getName())
                && playerRow.getString("player_uuid").equals(player.getUniqueId().toString()))
        {
          //Record found, name mismatch. Update the record with the players current name.
          DB.executeUpdateAsync(
                  "UPDATE players SET player_name = ? WHERE player_uuid = ?",
                  player.getName(),
                  player.getUniqueId().toString()
          );
        }

        // Prepare for futures to be turned into a list of DbRows
        rows = new ArrayList<DbRow>();
        motdList = new ArrayList<Motd>();
        futureList = DB.getResultsAsync("SELECT motd_id, player_id, message, ministry_name FROM motds\n" +
                "INNER JOIN ministries ON motds.ministry_id = ministries.ministry_id");
        // get the results from the future
        try {
            rows = futureList.get();
        }
        catch (Exception e)
        {
            System.out.print(e.getMessage());
        }
        // process it into an iterable list.
        buildMotdList(rows);

        // Send a message tot he player with all active MOTDs
        for(Motd motd : motdList)
        {
            if(motd.getName().equals("interior") && !(motd.getMessage() == null))
            {
                player.sendMessage(Utils.color(INT_INTRO + motd.getMessage()));
            }
            else if(motd.getName().equals("competition") && !(motd.getMessage() == null))
            {
                player.sendMessage(Utils.color(COMP_INTRO + motd.getMessage()));
            }
            else if(motd.getName().equals("communications") && !(motd.getMessage() == null))
            {
                player.sendMessage(Utils.color(COMM_INTRO + motd.getMessage()));
            }
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

    private void buildMotdList(List<DbRow> rows)
    {
        // get what we need and create an ArrayList of Motd objects
        for(DbRow row : rows)
        {
            this.motdList.add(new Motd(
                    row.getInt("motd_id"),
                    row.getInt("player_id"),
                    row.getString("message"),
                    row.getString("ministry_name")));
        }
    }
}