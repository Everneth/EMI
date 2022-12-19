package com.everneth.emi.models;

import co.aikar.idb.DB;
import co.aikar.idb.DbRow;
import com.everneth.emi.EMI;
import com.everneth.emi.Utils;
import com.everneth.emi.models.enums.ConfigMessage;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CharterPoint {
    private EMIPlayer issuer;
    private EMIPlayer recipient;
    private String reason;
    private int amount;
    private int pointId;
    private LocalDateTime issueDate;
    private LocalDateTime expirationDate;
    boolean isExpunged;

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

    public boolean isExpired() {
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(this.expirationDate);
    }

    public boolean isExpunged() {
        return isExpunged;
    }

    public long issuePoint()
    {
        EMIPlayer issuer = EMIPlayer.getEmiPlayer(this.getIssuer().getUuid());
        Date now = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Calendar cal = Calendar.getInstance();

        cal.add(Calendar.DAY_OF_MONTH, 60);

        try {
            return DB.executeInsert("INSERT INTO charter_points " +
                            "(issued_to, reason, amount, issued_by, date_issued, date_expired) " +
                    "VALUES (?,?,?,?,?,?)",
                    this.getRecipient().getId(),
                    this.getReason(),
                    this.getAmount(),
                    issuer.getId(),
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
        EMIPlayer recipient = EMIPlayer.getEmiPlayer(this.recipient.getName());
        List<CharterPoint> pointsList = recipient.getAllPoints();
        int points = 0;

        // Add up the total points for every point not expired or expunged
        for(CharterPoint point : pointsList)
            points += (!isExpired() && !isExpunged) ? point.amount : 0;

        Calendar cal = Calendar.getInstance();
        switch(points)
        {
            case(1):
                // We don't want players with 1 point to get flagged
                break;
            case(2):
                // 12 hour ban
                cal.add(Calendar.HOUR_OF_DAY, 12);
                break;
            case(3):
                // 24 hour ban
                cal.add(Calendar.DAY_OF_MONTH, 1);
                break;
            case(4):
                // 72 hour ban
                cal.add(Calendar.DAY_OF_MONTH, 3);
                break;
            default:
                // 5+ points, a permanent ban, flag the user in case of potential future pardons
                flagPlayer(recipient);
                break;
        }

        String response = ConfigMessage.POINTS_ACCRUED.getWithArgs(recipient.getName(), points);
        sender.sendMessage(Utils.color("&9[Charter] &3" + response));

        if (points >= 2) {
            // Both accounts need to be banned with expiry set to the same time
            String names[] = { recipient.getName(), recipient.getAltName() };
            for (String name : names) {
                if (name == null) continue;

                Bukkit.getBanList(BanList.Type.NAME).addBan(
                        name,
                        Utils.color("%c" + reason),
                        points >= 5 ? null : cal.getTime(),
                        null);

                sender.sendMessage(Utils.color("&9[Charter] &fBanned &c" + name));
            }

            if (points >= 5) {
                sender.sendMessage(Utils.color("&9[Charter] &3Please manually ban any of their accounts on the test server."));
            }
        }

        String message = ConfigMessage.POINTS_GAINED_WARNING.getWithArgs(
                recipient.getGuildMember().getEffectiveName(), amount, issuer.getName(), reason, points, cal.getTimeInMillis() / 1000);
        recipient.sendDiscordMessage(message);
    }

    public static CharterPoint getCharterPoint(int id)
    {
        DbRow record = new DbRow();
        try {
            record = DB.getFirstRowAsync("SELECT * FROM charter_points WHERE charter_point_id = ?", id).get();
        }
        catch (Exception e)
        {
            EMI.getPlugin().getLogger().warning(e.getMessage());
        }
        if(record == null)
        {
            return null;
        }
        else
        {
            return dbRowToCharterPoint(record);
        }
    }

    public static List<CharterPoint> getRecentPoints() {
        List<CharterPoint> points = new ArrayList<>();
        try {
            CompletableFuture<List<DbRow>> futurePoints = DB.getResultsAsync("SELECT * FROM charter_points ORDER BY charter_point_id DESC LIMIT 50");
            List<DbRow> rows = futurePoints.get();
            for (DbRow row : rows) {
                points.add(dbRowToCharterPoint(row));
            }
        }
        catch (Exception e) {
            EMI.getPlugin().getLogger().warning(e.getMessage());
        }

        return points;
    }

    public static long pardonPlayer(String name, Player sender, boolean removeFlag)
    {
        int retVal = 0;
        EMIPlayer recipient = EMIPlayer.getEmiPlayer(name);
        LocalDateTime now = LocalDateTime.now();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        if(recipient.isEmpty())
        {
            return retVal;
        }
        else {
            try {
                DB.executeUpdateAsync("UPDATE charter_points SET date_expired = ? WHERE issued_to = ? AND date_expired > NOW()",
                        format.format(now),
                        recipient.getId()).get();
                retVal = 1;
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        if(removeFlag)
        {
            DB.executeUpdateAsync("UPDATE players SET flagged = 0 WHERE player_id = ?", recipient.getId());
        }

        // build point to issue after pardon is complete
            EMIPlayer senderPlayer = new EMIPlayer(
                    sender.getUniqueId(),
                    sender.getName()
            );


            CharterPoint charterPoint = new CharterPoint(senderPlayer, recipient, "You have been issued 1 point as part of the pardon process.", 1);
            long pointRecord = charterPoint.issuePoint();
            charterPoint.enforceCharter(sender);
            return retVal;
    }

    private void flagPlayer(EMIPlayer player)
    {
        DB.executeUpdateAsync("UPDATE players SET flagged = 1 WHERE player_uuid = ?", player.getUuid());
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

        return retVal != 0;
    }

    public boolean removeCharterPoint()
    {
        int retVal = 0;
        try {
            expirationDate = LocalDateTime.now();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            retVal = DB.executeUpdateAsync("UPDATE charter_points SET expunged = 1, date_expired = ? WHERE charter_point_id = ?",
                    format.format(expirationDate),
                    pointId
            ).get();
        } catch (Exception e)
        {
            System.out.println(e.getMessage());
            return false;
        }
        return retVal != 0;
    }

    // Method to quickly convert a row from the charter point table to a CharterPoint object
    public static CharterPoint dbRowToCharterPoint(DbRow row) {
        EMIPlayer issuer = EMIPlayer.getEmiPlayer(row.getInt("issued_by"));
        EMIPlayer recipient = EMIPlayer.getEmiPlayer(row.getInt("issued_to"));

        CharterPoint point = new CharterPoint(
                issuer,
                recipient,
                row.getString("reason"),
                row.getInt("amount"));

        point.pointId = row.getInt("charter_point_id");

        // Need to parse the date strings in from the table of format yyyy-MM-dd HH:mm:ss
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        point.issueDate = LocalDateTime.parse(row.getString("date_issued"));
        point.expirationDate = LocalDateTime.parse(row.getString("date_expired"), formatter);

        point.isExpunged = row.get("expunged");
        return point;
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        // Differentiate expired from non-expired by changing the color coding from blue to yellow
        String dateColor = (isExpired() || isExpunged) ? "&e" : "&b";
        String expiry = (isExpired() || isExpunged) ? "Expired" : "Expires";
        String msg = " &3#" + pointId + "&7 - ({0}" + formatter.format(issueDate) + "&7) &c" + amount +
                " point(s)&7 issued to &c" + recipient.getName() + " &7&oby &l&d" + issuer.getName() + "&7.\n&3Reason: &7&o" +
                reason + "&7-- ({0}{1}: &o" + formatter.format(expirationDate) + "&7)";
        return MessageFormat.format(msg, dateColor, expiry);
    }
}
