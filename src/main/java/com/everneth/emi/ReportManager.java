package com.everneth.emi;

import co.aikar.idb.DB;
import co.aikar.idb.DbRow;
import com.everneth.emi.models.Report;
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
        DbRow playerRow = getPlayerRow(uuid);
        DB.executeUpdateAsync(
                "UPDATE reports SET active = 0 WHERE initiator_id = ?",
                playerRow.getInt("player_id")
        );
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
