package com.everneth.emi.models;

import co.aikar.idb.DB;
import co.aikar.idb.DbRow;
import com.everneth.emi.EMI;
import com.everneth.emi.Utils;
import com.everneth.emi.utils.PlayerUtils;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class CharterPoint {
    private EMIPlayer issuer;
    private EMIPlayer recipient;
    private String reason;
    private int amount;
    private int pointId;

    public CharterPoint(EMIPlayer issuer, EMIPlayer recipient, String reason, int amount)
    {
        this.issuer = issuer;
        this.recipient = recipient;
        this.reason = reason;
        this.amount = amount;
    }

    public EMIPlayer getIssuer() {
        return issuer;
    }

    public void setIssuer(EMIPlayer issuer) {
        this.issuer = issuer;
    }

    public EMIPlayer getRecipient() {
        return recipient;
    }

    public void setRecipient(EMIPlayer recipient) {
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

    public int getPointId() {
        return pointId;
    }
    public void setPointId(int pointId)
    {
        this.pointId = pointId;
    }

    public long issuePoint()
    {
        DbRow issuer = getPlayerRow(UUID.fromString(this.getIssuer().getUniqueId()));
        Date now = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Calendar cal = Calendar.getInstance();

        cal.add(Calendar.DAY_OF_MONTH, 30);

        try {
            return DB.executeInsert("INSERT INTO charter_points " +
                            "(issued_to, reason, amount, issued_by, date_issued, date_expired) " +
                    "VALUES (?,?,?,?,?,?)",
                    this.getRecipient().getId(),
                    this.getReason(),
                    this.getAmount(),
                    issuer.getInt("player_id"),
                    format.format(now),
                    format.format(cal.getTime()));
        }
        catch (SQLException e)
        {
            System.out.println(e.getMessage());
            return 0;
        }
    }

    public void enforceCharter(CommandSender sender)
    {
        List<DbRow> pointsList = PlayerUtils.getAllPoints(this.recipient.getName());
        int points = 0;
        Date now = new Date();

        for(DbRow point : pointsList)
        {
            boolean isExpired = now.after(point.get("date_expired"));
            boolean isExpunged = point.get("expunged");
            if(!isExpired && !isExpunged) {
                points += point.getInt("amount");
            }
        }

        EMIPlayer player = new EMIPlayer(pointsList.get(0).getString("recipient_uuid"), pointsList.get(0).getString("issued_to"));
        Calendar cal = Calendar.getInstance();
        switch(points)
        {
            case(1):
                //TODO: notification to discord/private message from "system" user
                break;
            case(2):
                cal.add(Calendar.HOUR_OF_DAY, 12);
                Bukkit.getBanList(BanList.Type.NAME).addBan(
                        player.getName(),
                        Utils.color("&c" + pointsList.get(pointsList.size()-1).getString("reason") + "&c"),
                        cal.getTime(),
                        null);
                sender.sendMessage(Utils.color("&9[Charter] &3" + this.getRecipient().getName() + " accumulated 2 points " +
                        "and has been banned for 12 hours."));
                break;
            case(3):
                // 24 hour ban
                cal.add(Calendar.DAY_OF_MONTH, 1);
                Bukkit.getBanList(BanList.Type.NAME).addBan(
                        player.getName(),
                        Utils.color("&c" + pointsList.get(pointsList.size()-1).getString("reason") + "&c"),
                        cal.getTime(),
                        null);
                sender.sendMessage(Utils.color("&9[Charter] &3" + this.getRecipient().getName() + " accumulated 3 points " +
                        "and has been banned for 24 hours."));
                break;
            case(4):
                // 72 hour ban
                cal.add(Calendar.DAY_OF_MONTH, 3);
                Bukkit.getBanList(BanList.Type.NAME).addBan(
                        player.getName(),
                        Utils.color("&c" + pointsList.get(pointsList.size()-1).getString("reason") + "&c"),
                        cal.getTime(),
                        null);
                sender.sendMessage(Utils.color("&9[Charter] &3" + this.getRecipient().getName() + " accumulated 4 points " +
                        "and has been banned for 72 hours."));
                break;
            case(5):
                // Permanent ban
                Bukkit.getBanList(BanList.Type.NAME).addBan(
                        player.getName(),
                        Utils.color("&c" + pointsList.get(pointsList.size()-1).getString("reason") + "&c"),
                        null,
                        null);
                sender.sendMessage(Utils.color("&9[Charter] &3" + this.getRecipient().getName() + " accumulated 5 points! " +
                        "If this was not in error, please proceed with /charter ban <player> <reason>."));
                flagPlayer(player);
                break;
            default:
                // Permanent ban
                sender.sendMessage(Utils.color("&9[Charter] &3" + this.getRecipient().getName() + " accumulated more than 5 points! " +
                        "If this was not in error, please proceed with /charter ban <player> <reason>."));
                flagPlayer(player);
                break;
        }
    }

    public static CharterPoint getCharterPoint(int id)
    {
        return PlayerUtils.getOnePoint(id);
    }

    public static long pardonPlayer(String name, Player sender, boolean removeFlag)
    {
        int retVal = 0;
        DbRow playerRecord = PlayerUtils.getPlayerRow(name);
        Date now = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        if(playerRecord.isEmpty())
        {
            return retVal;
        }
        else {
            try {
                DB.executeUpdateAsync("UPDATE charter_points SET date_expired = ? WHERE issued_to = ? AND date_expired > NOW()",
                        format.format(now),
                        playerRecord.getInt("player_id")).get();
                retVal = 1;
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        if(removeFlag)
        {
            DB.executeUpdateAsync("UPDATE players SET flagged = 0 WHERE player_id = ?", playerRecord.getInt("player_id"));
        }

        // build point to issue after pardon is complete
            EMIPlayer recipient = new EMIPlayer(
                    playerRecord.getString("player_uuid"),
                    playerRecord.getString("player_name"),
                    playerRecord.getInt("player_id")
            );
            EMIPlayer senderPlayer = new EMIPlayer(
                    sender.getUniqueId().toString(),
                    sender.getName()
            );


            CharterPoint charterPoint = new CharterPoint(senderPlayer, recipient, "You have been issued 1 point as part of the pardon process.", 1);
            long pointRecord = charterPoint.issuePoint();
            charterPoint.enforceCharter(sender);
            return retVal;
    }

    private void flagPlayer(EMIPlayer player)
    {
        DB.executeUpdateAsync("UPDATE players SET flagged = 1 WHERE player_uuid = ?", player.getUniqueId());
    }

    public boolean updateCharterPoint(CharterPoint charterPoint, int id)
    {
        int retVal = 0;
        try {
            retVal = DB.executeUpdateAsync("UPDATE charter_points SET reason = ?, amount = ? WHERE charter_point_id = ?",
                    charterPoint.getReason(),
                    charterPoint.getAmount(),
                    id
            ).get();
        } catch (Exception e)
        {
            System.out.println(e.getMessage());
            return false;
        }
        if(retVal != 0)
            return true;
        else
            return false;
    }

    public boolean removeCharterPoint(int id)
    {
        int retVal = 0;
        try {
            retVal = DB.executeUpdateAsync("UPDATE charter_points SET expunged = 1 WHERE charter_point_id = ?",
                    id
            ).get();
        } catch (Exception e)
        {
            System.out.println(e.getMessage());
            return false;
        }
        if(retVal != 0)
            return true;
        else
            return false;
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
