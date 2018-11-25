package com.everneth.emi.events;

import co.aikar.idb.DB;
import co.aikar.idb.DbRow;
import com.everneth.emi.Utils;
import com.everneth.emi.models.Motd;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

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
    private List<DbRow> rows;

    public JoinEvent(Plugin plugin)
    {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        Player player = event.getPlayer();

        rows = new ArrayList<DbRow>();
        motdList = new ArrayList<Motd>();
        futureList = DB.getResultsAsync("SELECT motd_id, player_id, message, ministry_name FROM motds\n" +
                "INNER JOIN ministries ON motds.ministry_id = ministries.ministry_id");
        try {
            rows = futureList.get();
        }
        catch (Exception e)
        {
            System.out.print(e.getMessage());
        }
        buildMotdList(rows);

        for(Motd motd : motdList)
        {
            if(motd.name.equals("interior") && (!motd.getMessage().equals("")))
            {
                player.sendMessage(Utils.color(INT_INTRO + motd.getMessage()));
            }
            else if(motd.name.equals("competition") && !motd.getMessage().equals(""))
            {
                player.sendMessage(Utils.color(COMP_INTRO + motd.getMessage()));
            }
            else if(motd.name.equals("communications") && !motd.getMessage().equals(""))
            {
                player.sendMessage(Utils.color(COMM_INTRO + motd.getMessage()));
            }
        }
    }

    private void buildMotdList(List<DbRow> rows)
    {
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
