package com.everneth.EMI.events;

import co.aikar.idb.DB;
import co.aikar.idb.DbRow;
import com.everneth.EMI.models.Motd;
import me.botsko.prism.wands.InspectorWand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class JoinEvent implements Listener {

    private List<Motd> motdList;
    private CompletableFuture<List<DbRow>> futureList;
    private List<DbRow> rows;

    public JoinEvent()
    {
        rows = new ArrayList<DbRow>();
        motdList = new ArrayList<Motd>();
        futureList = DB.getResultsAsync("SELECT * FROM motds").toCompletableFuture();
        futureList.complete(rows);
        buildMotdList(rows);
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        Player p = event.getPlayer();
        if(p.hasPermission("emi.mint.motd"))
        {
            List<Motd> result = motdList.stream().filter(motd -> Objects.equals(motd.getId(), 3)).collect(Collectors.toList());
            p.sendMessage(result.get(0).getMessage());
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
                    row.get("isPublic")));
        }
    }
}
