package com.everneth.emi.managers;

import co.aikar.idb.DB;
import co.aikar.idb.DbRow;
import com.everneth.emi.EMI;
import com.everneth.emi.models.EMIPlayer;
import com.everneth.emi.models.Report;

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
    public DbRow getReportRecord(UUID uuid)
    {
        EMIPlayer playerRow = EMIPlayer.getEmiPlayer(uuid);
        try {
            CompletableFuture<DbRow> result =  DB.getFirstRowAsync("SELECT * FROM reports WHERE initiator_id = ?", playerRow.getId());
            return result.get();
        }
        catch (Exception e)
        {
            EMI.getPlugin().getLogger().info(e.getMessage());
            return null;
        }
    }

    public void addReportRecord(Report report, int playerId)
    {
        Date now = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try {
            DB.executeInsert("INSERT INTO reports (initiator_id, channel_id, active, date_opened) " +
                    "VALUES (?, ?, ?, ?)",
                    playerId,
                    report.getChannelId(),
                    1,
                    format.format(now));
        }
        catch(SQLException e)
        {
            EMI.getPlugin().getLogger().info(e.getMessage());
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
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        EMIPlayer playerRow = EMIPlayer.getEmiPlayer(uuid);
        DbRow reportRecord = getReportRecord(uuid);
        DB.executeUpdateAsync(
                "UPDATE reports SET active = 0, date_closed = ? WHERE initiator_id = ?",
                format.format(now),
                playerRow.getId()
        );
        rm.removeReport(uuid);
    }

    public void loadManager()
    {
        CompletableFuture<List<DbRow>> futureResults = new CompletableFuture<>();
        List<DbRow> results = new ArrayList<DbRow>();
        try {
            futureResults = DB.getResultsAsync(
                    "SELECT channel_id, player_uuid, discord_id FROM reports INNER JOIN players " +
                            "ON reports.initiator_id = players.player_id WHERE active = ?",
                    1);
            results = futureResults.get();
        }
        catch (Exception e)
        {
            EMI.getPlugin().getLogger().info(e.getMessage());
        }
        if(!results.isEmpty()) {
            for (DbRow result : results) {

                if(result != null) {
                    if (result.getLong("discord_id") != null || result.getLong("discord_id") != 0) {
                        this.reportMap.put(UUID.fromString(result.getString("player_uuid")),
                                new Report(result.getLong("channel_id"),
                                        result.getLong("discord_id"))
                        );
                    } else {
                        this.reportMap.put(UUID.fromString(result.getString("player_uuid")),
                                new Report(result.getLong("channel_id"))
                        );
                    }
                }
                else
                {
                    EMI.getPlugin().getLogger().severe("Error loading report manager service. Null results returned.");
                }
            }
        }
    }
}
