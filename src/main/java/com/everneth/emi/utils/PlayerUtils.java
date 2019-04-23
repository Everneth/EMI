package com.everneth.emi.utils;

import co.aikar.idb.DB;
import co.aikar.idb.DbRow;
import com.everneth.emi.EMI;
import com.everneth.emi.models.CharterPoint;
import com.everneth.emi.models.EMIPlayer;
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

    public static List<DbRow> getAllPoints(String name)
    {
        DbRow recipient = getPlayerRow(name);

        List<DbRow> recordsList = new ArrayList<DbRow>();
        try {
                recordsList = DB.getResultsAsync("SELECT charter_point_id, p1.player_name as 'issued_to', p1.player_uuid as 'recipient_uuid', p2.player_name as 'issued_by', p2.player_uuid as 'issuer_uuid', reason, amount, date_issued, date_expired, expunged FROM charter_points c INNER JOIN\n" +
                                "players p1 ON c.issued_to = p1.player_id\n" +
                                "JOIN players p2 ON c.issued_by = p2.player_id WHERE issued_to = ?",
                        recipient.getInt("player_id")).get();
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }

        return recordsList;
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

            EMIPlayer issuerPlayer = new EMIPlayer(
                    issuer.getString("player_uuid"),
                    issuer.getString("player_name"),
                    issuer.getInt("player_id")
            );
            EMIPlayer recipientPlayer = new EMIPlayer(
                    recipient.getString("player_uuid"),
                    recipient.getString("player_name"),
                    recipient.getInt("player_id")
            );
            return new CharterPoint(
                    issuerPlayer,
                    recipientPlayer,
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
