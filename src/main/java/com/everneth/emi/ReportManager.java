package com.everneth.emi;

import co.aikar.idb.DB;
import co.aikar.idb.DbRow;
import com.everneth.emi.models.Report;
import com.sun.xml.internal.ws.util.CompletedFuture;
import net.dv8tion.jda.core.entities.User;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class ReportManager {
    private static ReportManager rm;
    private HashMap<UUID, Report> reportMap = new HashMap<UUID, Report>();
    private ReportManager() {}
    public static ReportManager getReportManager()
    {
        if(rm == null)
        {
            rm = new ReportManager();
        }
        return rm;
    }
    public void addReport(UUID uuid, Report report)
    {
        this.reportMap.put(uuid, report);
    }
    public void removeReport(UUID uuid)
    {
        this.reportMap.remove(uuid);
    }
    public Report findReportById(UUID uuid)
    {
        return this.reportMap.get(uuid);
    }
    public UUID findReportByChannelId(Long channelId)
    {
        return getKeyFromChannelId(this.reportMap, channelId);
    }

    private UUID getKeyFromChannelId(Map hm, long channelId)
    {
        for (Object o : hm.keySet())
        {
            if(this.reportMap.get((UUID) o).getChannelId() == channelId)
            {
                return (UUID) o;
            }
        }
        return null;
    }

    public boolean hasDiscord(UUID uuid)
    {
        Long discordId = rm.findReportById(uuid).getDiscordUserId();
        return !discordId.equals(0L);
    }

    public int messagesMissed(UUID uuid)
    {
        Report report = rm.findReportById(uuid);
        DbRow playerRecord = getPlayerRow(uuid);
        EMI.getJda().getTextChannelById(report.getChannelId()).sendMessage("***" + playerRecord.getString("player_name") + "** has joined the game.*").queue();
        try {
            CompletableFuture<List<DbRow>> result = DB.getResultsAsync("SELECT * FROM report_messages WHERE initiator_id = ? AND read = 0", playerRecord.getInt("player_id"));
            return result.get().size();

        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            return 0;
        }
    }
    public List<DbRow> getMissedMessages(UUID uuid)
    {
        Report report = rm.findReportById(uuid);
        DbRow playerRecord = getPlayerRow(uuid);
        try
        {
            CompletableFuture<List<DbRow>> results = DB.getResultsAsync("SELECT author, message FROM report_messages WHERE initiator_id = ? AND read = 0",
                    playerRecord.getInt("player_id"));
            markReportMessagesRead(playerRecord);
            return results.get();
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            return null;
        }
    }

    private void markReportMessagesRead(DbRow player)
    {
        DB.executeUpdateAsync("UPDATE report_messages SET read = 1 WHERE initiator_id = ? AND read = 0",
                player.getInt("player_id"));
    }

    public DbRow getReportRecord(UUID uuid)
    {
        DbRow playerRow = getPlayerRow(uuid);
        try {
            CompletableFuture<DbRow> result =  DB.getFirstRowAsync("SELECT * FROM reports WHERE initiator_id = ?", playerRow.getInt("player_id"));
            return result.get();
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public void addReportRecord(Report report, int playerId)
    {
        Date now = new Date();
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

        try {
            DB.executeInsert("INSERT INTO reports (initiator_id, channel_id, active, date_opened, date_closed, embed_message_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?)", playerId, report.getChannelId(), 1, format.format(now), null, report.getMessageId());
        }
        catch(SQLException e)
        {
            System.out.println(e.getMessage());
        }
    }

    public boolean hasActiveReport(UUID uuid)
    {
        Report report = findReportById(uuid);
        if(report != null) {
            return true;
        }
        else
        {
            return false;
        }
    }

    public void closeReport(UUID uuid)
    {
        Date now = new Date();
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        DbRow playerRow = getPlayerRow(uuid);
        DB.executeUpdateAsync(
                "UPDATE reports SET active = 0, date_closed = ? WHERE initiator_id = ?",
                format.format(now),
                playerRow.getInt("player_id")
        );
        markReportMessagesRead(playerRow);
        rm.removeReport(uuid);
    }

    public void loadManager()
    {
        List<DbRow> results = new ArrayList<DbRow>();
        try {
            results = DB.getResultsAsync(
                    "SELECT channel_id, player_uuid, discord_id FROM reports INNER JOIN players " +
                            "ON reports.initiator_id = player.player_id WHERE active = ?",
                    1).get();
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        if(!results.isEmpty()) {
            for (DbRow result : results) {
                this.reportMap.put(UUID.fromString(result.getString("player_uuid")),
                        new Report(
                                result.getLong("channel_id"),
                                result.getLong("embed_message_id"),
                                result.getLong("discord_id")
                        )
                );
            }
        }
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
}
