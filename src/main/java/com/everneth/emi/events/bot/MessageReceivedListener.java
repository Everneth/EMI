package com.everneth.emi.events.bot;

import co.aikar.idb.DB;
import co.aikar.idb.DbRow;
import com.everneth.emi.EMI;
import com.everneth.emi.ReportManager;
import com.everneth.emi.Utils;
import com.everneth.emi.models.Report;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.UUID;

public class MessageReceivedListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        if(event.isFromType(ChannelType.TEXT) && event.getChannel().getName().contains("_staff"))
        {
            ReportManager rm = ReportManager.getReportManager();
            String channelName = event.getChannel().getName();
            String playerName = channelName.substring(0, channelName.indexOf('_'));

            UUID player_uuid = rm.findReportByChannelId(event.getChannel().getIdLong());

            Player player = EMI.getPlugin().getServer().getPlayer(player_uuid);

            if(player.isOnline())
            {
                player.sendMessage(Utils.color("<&7 +"+ event.getMember().getNickname() +"&f> " + event.getMessage().getContentRaw()));
            }
            else if (!player.isOnline() && !rm.hasDiscord(player.getUniqueId()))
            {
                DbRow report = rm.getReportRecord(player_uuid);
                try {
                    DB.executeInsert("INSERT INTO report_messages (report_id, author, message, read, date_read) " +
                                    "VALUES (?, ?, ?, ?, ?)",
                            report.getInt("report_id"),
                            event.getMember().getNickname(),
                            event.getMessage().getContentRaw(),
                            0,
                            null);
                }
                catch(SQLException e)
                {
                    System.out.println(e.getMessage());
                }
            }
        }
    }
}