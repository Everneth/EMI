package com.everneth.emi.models;

import co.aikar.idb.DB;
import co.aikar.idb.DbRow;
import com.everneth.emi.EMI;
import org.bukkit.*;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
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

    public void enforceCharter()
    {
        List<CharterPoint> pointsList = getAllPoints(this.recipient.getPlayer().getName());
        int points = 0;
        for(CharterPoint point : pointsList)
        {
            points += point.getAmount();
        }

        Player player = pointsList.get(0).getRecipient();
        Calendar cal = Calendar.getInstance();
        switch(points)
        {
            case(1):
                //TODO: notification to discord/private message from "system" user
                break;
            case(2):
                // Move or flag player for jail
                if(player.isOnline())
                {
                    moveToJail(player);
                }
                else
                {
                    flagPlayer(player);
                }
                break;
            case(3):
                // 24 hour ban
                cal.add(Calendar.DAY_OF_MONTH, 1);
                Bukkit.getBanList(BanList.Type.NAME).addBan(
                        player.getName(),
                        pointsList.get(pointsList.size()-1).getReason(),
                        cal.getTime(),
                        null);
                break;
            case(4):
                // 72 hour ban
                cal.add(Calendar.DAY_OF_MONTH, 3);
                Bukkit.getBanList(BanList.Type.NAME).addBan(
                        player.getName(),
                        pointsList.get(pointsList.size()-1).getReason(),
                        cal.getTime(),
                        null);
                break;
            case(5):
                // Permanent ban
                Bukkit.getBanList(BanList.Type.NAME).addBan(
                        player.getName(),
                        pointsList.get(pointsList.size()-1).getReason(),
                        null,
                        null);
                break;
            default:
                // Permanent ban
                Bukkit.getBanList(BanList.Type.NAME).addBan(
                        player.getName(),
                        "You have exceeded 5 charter points. You have been permanently banned. " +
                        "Please submit an appeal on everneth.com if you want to review your ban and get it lifted.",
                        null,
                        null);
                break;
        }
    }

    public DbRow getCharterPoint()
    {
        return null;
    }

    private void moveToJail(Player player)
    {
        World world = EMI.getPlugin().getServer().getWorld(EMI.getPlugin().getConfig().getString("world_folder"));
        Location location = new Location(
                world,
                EMI.getPlugin().getConfig().getDouble("jail-x"),
                EMI.getPlugin().getConfig().getDouble("jail-y"),
                EMI.getPlugin().getConfig().getDouble("jail-z"));
        player.teleport(location);
        player.setGameMode(GameMode.ADVENTURE);
    }

    private void flagPlayer(Player player)
    {
        DB.executeUpdateAsync("UPDATE players SET flagged = 1 WHERE player_uuid = ?", player.getUniqueId());
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
