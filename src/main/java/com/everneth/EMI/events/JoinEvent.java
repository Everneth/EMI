package com.everneth.EMI.events;

import co.aikar.idb.DB;
import co.aikar.idb.DbRow;
import com.everneth.EMI.models.Motd;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

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


    private final String INT_INTRO = ChatColor.GRAY + "[" + ChatColor.LIGHT_PURPLE + "INT" + ChatColor.GRAY + "] ";
    private final String COMP_INTRO = ChatColor.GRAY + "[" + ChatColor.RED + "COMP" + ChatColor.GRAY + "] ";
    private final String COMM_INTRO = ChatColor.GRAY + "[" + ChatColor.BLUE + "COMM" + ChatColor.GRAY + "] ";

    private List<Motd> motdList;
    private CompletableFuture<List<DbRow>> futureList;
    private List<DbRow> rows;

    public JoinEvent()
    {
        rows = new ArrayList<DbRow>();
        motdList = new ArrayList<Motd>();
        futureList = DB.getResultsAsync("SELECT message, ministry_name FROM motds\n" +
                "INNER JOIN ministries ON motds.ministry_id = ministries.ministry_id").toCompletableFuture();
        futureList.complete(rows);
        buildMotdList(rows);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        Player p = event.getPlayer();

        for(Motd motd : motdList)
        {
            if(motd.name.equals("interior") && !motd.getMessage().equals(""))
            {
                p.sendMessage(INT_INTRO + motd.getMessage());
            }
            else if(motd.name.equals("competition") && !motd.getMessage().equals(""))
            {
                p.sendMessage(COMP_INTRO + motd.getMessage());
            }
            else if(motd.name.equals("communications") && !motd.getMessage().equals(""))
            {
                p.sendMessage(COMM_INTRO + motd.getMessage());
            }
        }
    }

    private void buildMotdList(List<DbRow> rows)
    {
        for(DbRow row : rows)
        {
            this.motdList.add(new Motd(
                    row.getInt("id"),
                    row.getInt("player_id"),
                    row.getString("message"),
                    row.getString("ministry_name"),
                    row.get("isPublic")));
        }
    }
}
