package com.everneth.emi.models;

import co.aikar.idb.DB;
import co.aikar.idb.DbRow;
import com.everneth.emi.EMI;
import com.everneth.emi.Utils;
import com.everneth.emi.models.enums.ConfigMessage;
import com.everneth.emi.models.enums.ServerApiUrl;
import com.google.gson.Gson;
import net.dv8tion.jda.api.entities.Member;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.net.http.HttpClient;
import java.sql.Date;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
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
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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
        return LocalDateTime.now().isAfter(this.expirationDate);
    }

    public boolean isExpunged() {
        return isExpunged;
    }

    public long issue()
    {
        EMIPlayer issuer = EMIPlayer.getEmiPlayer(this.getIssuer().getUuid());
        issueDate = LocalDateTime.now();
        expirationDate = issueDate.plusDays(60);

        try {
            return DB.executeInsert("INSERT INTO charter_points " +
                            "(issued_to, reason, amount, issued_by, date_issued, date_expired) " +
                    "VALUES (?,?,?,?,?,?)",
                    this.getRecipient().getId(),
                    this.getReason(),
                    this.getAmount(),
                    issuer.getId(),
                    issueDate.format(DATE_FORMAT),
                    expirationDate.format(DATE_FORMAT));
        }
        catch (SQLException e)
        {
            EMI.getPlugin().getLogger().info(e.getMessage());
            return 0;
        }
    }

    public void enforceCharter(CommandSender sender, boolean pointsRemoved)
    {
        EMIPlayer recipient = EMIPlayer.getEmiPlayer(this.recipient.getName());
        List<CharterPoint> pointsList = recipient.getAllPoints();
        int points = 0;

        // Add up the total points for every point not expired or expunged
        for(CharterPoint point : pointsList)
            points += (point.isExpired() || point.isExpunged) ? 0 : point.amount;

        Calendar cal = Calendar.getInstance();
        ZoneId zone = ZoneId.systemDefault();
        cal.setTime(Date.from(issueDate.atZone(zone).toInstant()));
        switch (points) {
            case (2) ->
                    // 12 hour ban
                    cal.add(Calendar.HOUR_OF_DAY, 12);
            case (3) ->
                    // 24 hour ban
                    cal.add(Calendar.DAY_OF_MONTH, 1);
            case (4) ->
                    // 72 hour ban
                    cal.add(Calendar.DAY_OF_MONTH, 3);
        }

        String response = ConfigMessage.POINTS_ACCRUED.getWithArgs(recipient.getName(), points);
        sender.sendMessage(Utils.color("&9[Charter] &3" + response));

        if (points >= 2 || pointsRemoved) {
            // Both accounts need to be banned with expiry set to the same time
            String names[] = { recipient.getName(), recipient.getAltName() };
            for (String name : names) {
                if (name == null) continue;

                Bukkit.getBanList(BanList.Type.NAME).addBan(
                        name,
                        Utils.color("&c" + reason),
                        points >= 5 ? null : cal.getTime(),
                        null);

                Player player = Bukkit.getPlayer(name);
                if (player != null)
                    player.kickPlayer("You've been banned.");

                sender.sendMessage(Utils.color("&9[Charter] &fBanned &c" + name + " &faccordingly."));
            }

            if (points >= 5) {
                try {
                    CloseableHttpClient httpclient = HttpClients.createDefault();
                    String url = ServerApiUrl.TEST_SERVER.get() + "/cmd/ban/" + recipient.getName() + "?token=" +
                            EMI.getConfigString("api-token");
                    HttpPost postRequest = new HttpPost(url);
                    postRequest.setHeader("Accept", "application/json");
                    postRequest.setHeader("Content-type", "application/json");
                    ResponseHandler<String> responseHandler = restResponse ->
                    {
                        int status = restResponse.getStatusLine().getStatusCode();
                        if (status >= 200 && status < 300) {
                            HttpEntity entity = restResponse.getEntity();
                            return entity != null ? EntityUtils.toString(entity) : null;
                        } else {
                            throw new ClientProtocolException("Unexpected response status: " + status);
                        }
                    };
                    String responseBody = httpclient.execute(postRequest, responseHandler);
                    Gson gson = new Gson();
                    CommandResponse commandResponse = gson.fromJson(responseBody, CommandResponse.class);
                    sender.sendMessage(Utils.color("&9[Charter] &3 " + commandResponse.getMessage()));

                }
                catch (Exception e)
                {
                    EMI.getPlugin().getLogger().severe(e.getMessage());
                    sender.sendMessage(Utils.color("&9[Charter] &3Please manually ban any of their accounts on the test server."));
                }
            }
        }

        Member guildMember = recipient.getGuildMember();
        if (guildMember == null) {
            sender.sendMessage(Utils.color("&cCould not find guild member associated with " + this.recipient.getName() +
                    ".\n&fPlease message them on Discord for me to inform them about their points. Use &b/info &f if you do not know " +
                    "their Discord username."));
        }
        else if (pointsRemoved) {
            String message = ConfigMessage.POINTS_REMOVED_WARNING.getWithArgs(
                    recipient.getGuildMember().getEffectiveName(), amount, points);
            recipient.sendDiscordMessage(message);
        }
        else {
            String message = ConfigMessage.POINTS_GAINED_WARNING.getWithArgs(
                    recipient.getGuildMember().getEffectiveName(), amount, issuer.getName(), reason, points, cal.getTimeInMillis() / 1000);
            recipient.sendDiscordMessage(message);
        }
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

    public static long pardonPlayer(String name, Player sender)
    {
        int retVal = 0;
        EMIPlayer recipient = EMIPlayer.getEmiPlayer(name);
        LocalDateTime now = LocalDateTime.now();

        if (recipient.isEmpty()) {
            return retVal;
        } else {
            try {
                DB.executeUpdateAsync("UPDATE charter_points SET expunged = 1 WHERE issued_to = ? AND date_expired > NOW()",
                        recipient.getId()
                ).get();
                retVal = 1;
            } catch (Exception e) {
                EMI.getPlugin().getLogger().info(e.getMessage());
            }
        }

        // build point to issue after pardon is complete
        EMIPlayer senderPlayer = new EMIPlayer(
                sender.getUniqueId(),
                sender.getName()
        );


        CharterPoint charterPoint = new CharterPoint(senderPlayer, recipient, "You have been issued 1 point as part of the pardon process.", 1);
        long pointRecord = charterPoint.issue();
        charterPoint.enforceCharter(sender, false);
        return retVal;
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
            EMI.getPlugin().getLogger().info(e.getMessage());
            return false;
        }

        return retVal != 0;
    }

    public boolean removeCharterPoint()
    {
        int retVal = 0;
        try {
            expirationDate = LocalDateTime.now();
            retVal = DB.executeUpdateAsync("UPDATE charter_points SET expunged = 1 WHERE charter_point_id = ?", pointId).get();
        } catch (Exception e) {
            EMI.getPlugin().getLogger().info(e.getMessage());
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

        // The DateTime's from the DB should get automatically read in and cast as LocalDateTime
        point.issueDate = row.get("date_issued");
        point.expirationDate = row.get("date_expired");

        point.isExpunged = row.get("expunged");
        return point;
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        // Differentiate expired from non-expired by changing the color coding from blue to yellow
        String dateColor = (isExpired() || isExpunged) ? "&e" : "&b";
        String expiry = (isExpired()) ? "Expired" : "Expires";
        String expunged = isExpunged ? "&f(&cExpunged&f)" : "";
        String msg = Utils.color("&b" + "-".repeat(45) + '\n');
        msg += " &b#" + pointId + "&7 - ({0}" + formatter.format(issueDate) + "&7) &c" + amount +
                " point(s)&7 issued to &c" + recipient.getName() + " &7&oby &l&d" + issuer.getName() +
                "&7. ({0}{1}: &o" + expirationDate.format(formatter) + "&7) " + expunged +
                "&7\n &3Reason: &7&o" + reason;
        // single quotes must be escaped
        msg = msg.replace("'", "''");
        return MessageFormat.format(msg, dateColor, expiry);
    }
}
