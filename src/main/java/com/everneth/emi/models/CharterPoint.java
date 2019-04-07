package com.everneth.emi.models;

import co.aikar.idb.DB;
import co.aikar.idb.DbRow;
import com.everneth.emi.EMI;
import com.sun.xml.internal.ws.util.CompletedFuture;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class CharterPoint {
    private Player issuer;
    private Player recipient;
    private String reason;
    private int amount;

    public CharterPoint(Player issuer, Player recipient, String reason, int amount)
    {
        this.issuer = issuer;
        this.recipient = recipient;
        this.reason = reason;
        this.amount = amount;
    }

    public Player getIssuer() {
        return issuer;
    }

    public void setIssuer(Player issuer) {
        this.issuer = issuer;
    }

    public Player getRecipient() {
        return recipient;
    }

    public void setRecipient(Player recipient) {
        this.recipient = recipient;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public long IssuePoint(CharterPoint charterPoint)
    {
        DbRow issuer = getPlayerRow(charterPoint.getIssuer().getUniqueId());
        DbRow recipient = getPlayerRow(charterPoint.getRecipient().getUniqueId());
        Date now = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try {
            return DB.executeInsert("INSERT INTO charter_points (issued_to, reason, amount, issued_by, date_issued) " +
                    "VALUES (?,?,?,?,?)",
                    recipient.getInt("player_id"),
                    charterPoint.getReason(),
                    charterPoint.getAmount(),
                    issuer.getInt("player_id"),
                    format.format(now));
        }
        catch (SQLException e)
        {
            System.out.println(e.getMessage());
            return 0;
        }
    }

    public List<CharterPoint> getAllPoints(String name)
    {
        DbRow recipient = getPlayerRow(name);

        List<DbRow> recordsList = new ArrayList<DbRow>();

        try {
            recordsList = DB.getResultsAsync("SELECT * FROM charter_points WHERE issued_to = ? AND date_expired = ?",
                    recipient.getInt("player_id"),
                    null).get();
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }

        List<CharterPoint> pointsList = new ArrayList<CharterPoint>();
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(recipient.getString("player_uuid")));

        for(DbRow record : recordsList)
        {
            pointsList.add(new CharterPoint(null, offlinePlayer.getPlayer(), record.getString("reason"), record.getInt("amount")));
        }
        return pointsList;
    }

    public DbRow getCharterPoint()
    {

    }

    private DbRow getPlayerRow(UUID uuid)
    {
        CompletableFuture<DbRow> futurePlayer;
        DbRow player = new DbRow();
        futurePlayer = DB.getFirstRowAsync("SELECT * FROM players WHERE player_uuid = ?", uuid.toString());
        try {
            player = futurePlayer.get();
        }
        catch (Exception e)
        {
            EMI.getPlugin().getLogger().info(e.getMessage());
        }
        return player;
    }

    private DbRow getPlayerRow(int id)
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

    private DbRow getPlayerRow(String playerName)
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
}
