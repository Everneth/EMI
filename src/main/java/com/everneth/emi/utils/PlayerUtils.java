package com.everneth.emi.utils;

import co.aikar.idb.DB;
import co.aikar.idb.DbRow;
import com.everneth.emi.EMI;
import com.everneth.emi.models.CharterPoint;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PlayerUtils {
    public static DbRow getPlayerRow(String playerName)
    {
        CompletableFuture<DbRow> futurePlayer;
        DbRow player = new DbRow();
        futurePlayer = DB.getFirstRowAsync("SELECT * FROM players WHERE player_name = ?", playerName);
        try {
            player = futurePlayer.get();
        }
        catch (Exception e)
        {
            EMI.getPlugin().getLogger().info(e.getMessage());
        }
        return player;
    }

    public static List<CharterPoint> getAllPoints(String name, boolean includeExpired)
    {
        DbRow recipient = getPlayerRow(name);

        List<DbRow> recordsList = new ArrayList<DbRow>();

        try {
            if(includeExpired) {
                recordsList = DB.getResultsAsync("SELECT * FROM charter_points WHERE issued_to = ?",
                        recipient.getInt("player_id")).get();
            }
            else
            {
                recordsList = DB.getResultsAsync("SELECT * FROM charter_points WHERE issued_to = ? AND date_expired > CURDATE() AND expunged = 0",
                        recipient.getInt("player_id")).get();
            }
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }

        List<CharterPoint> pointsList = new ArrayList<>();
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(recipient.getString("player_uuid")));

        for(DbRow record : recordsList)
        {
            pointsList.add(new CharterPoint(null, offlinePlayer.getPlayer(), record.getString("reason"), record.getInt("amount")));
        }
        return pointsList;
    }

    public static CharterPoint getOnePoint(int id)
    {

        DbRow record = new DbRow();
        try {
            record = DB.getFirstRowAsync("SELECT * FROM charter_points WHERE charter_point_id = ?", id).get();
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        if(record.isEmpty())
        {
            return null;
        }
        else
        {
            DbRow issuer = getPlayerRow(record.getInt("issued_by"));
            DbRow recipient = getPlayerRow(record.getInt("issued_to"));

            return new CharterPoint(
                    Bukkit.getOfflinePlayer(UUID.fromString(issuer.getString("player_uuid"))).getPlayer(),
                    Bukkit.getOfflinePlayer(UUID.fromString(recipient.getString("player_uuid"))).getPlayer(),
                    record.getString("reason"),
                    record.getInt("amount")
            );
        }
    }
    public static DbRow getPlayerRow(int id)
    {
        CompletableFuture<DbRow> futurePlayer;
        DbRow player = new DbRow();
        futurePlayer = DB.getFirstRowAsync("SELECT * FROM players WHERE player_id = ?", id);
        try {
            player = futurePlayer.get();
        }
        catch (Exception e)
        {
            EMI.getPlugin().getLogger().info(e.getMessage());
        }
        return player;
    }
}
