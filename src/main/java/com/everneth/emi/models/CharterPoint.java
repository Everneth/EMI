package com.everneth.emi.models;

import co.aikar.idb.DB;
import co.aikar.idb.DbRow;
import com.everneth.emi.EMI;
import com.everneth.emi.Utils;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

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
        EMIPlayer issuer = EMIPlayer.getEmiPlayer(UUID.fromString(this.getIssuer().getUniqueId()));
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
        List<DbRow> pointsList = EMIPlayer.getAllPoints(this.recipient.getName());
        int points = 0;
        LocalDateTime now = LocalDateTime.now();

        for(DbRow point : pointsList)
        {
            boolean isExpired = now.isAfter(point.get("date_expired"));
            boolean isExpunged = point.get("expunged");
            if(!isExpired && !isExpunged) {
                points += point.getInt("amount");
            }
        }

        EMIPlayer player = EMIPlayer.getEmiPlayer(this.recipient.getName());
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
                flagPlayer(player);
                break;
        }
        
        sender.sendMessage(Utils.color("&9[Charter] &3" + this.getRecipient().getName() + " accumulated " + points + " point(s) " +
                "and has been messaged/banned accordingly."));

        if (points >= 2) {
            // Both accounts need to be banned with expiry set to the same time
            String names[] = { player.getName(), player.getAltName() };
            for (String name : names) {
                if (name == null) continue;

                Bukkit.getBanList(BanList.Type.NAME).addBan(
                        name,
                        Utils.color("%c" + this.reason),
                        points >= 5 ? null : cal.getTime(),
                        null);
            }
        }

        // This will eventually be replaced with a much nicer method in the EMIPlayer model
        User user = EMI.getJda().getUserById(this.recipient.getId());
        if (user != null) {
            String message = buildBanMessage(points, user.getName(), cal);
            user.openPrivateChannel()
                    .flatMap(privateChannel -> privateChannel.sendMessage(message))
                    .queue(null,
                            new ErrorHandler()
                                    .handle(ErrorResponse.CANNOT_SEND_TO_USER, (error) -> {
                                        sender.sendMessage(Utils.color("&cCould not message user on Discord. Please notify them of the circumstances ASAP."));
                                    }));
        }
    }

    public static CharterPoint getCharterPoint(int id)
    {
        return EMIPlayer.getOnePoint(id);
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

    private String buildBanMessage(int totalPoints, String discordName, Calendar expiry) {
        String message = "Hey " + discordName + ". I'm here to notify you that you have been issued " +
                this.amount + " point(s) by " + this.issuer.getName() + " with the following reason:\n> *" + this.reason + "*\n";

        if (totalPoints >= 2) {
            message += "You have accumulated " + totalPoints + " points in the last 60 days, and have been banned accordingly. " +
                    "As per the charter, this ban ";
            message += totalPoints >= 5 ? "will not expire." : "will expire at the following time: <t:" + expiry.getTimeInMillis() / 1000 + ">";
        }
        else {
            message += "As this is your only point in the last 60 days, you may consider this a warning.";
        }

        message += "\n\n**If you believe this to be an error or wish to appeal, feel free to contact an admin.**";
        return message;
    }
}
