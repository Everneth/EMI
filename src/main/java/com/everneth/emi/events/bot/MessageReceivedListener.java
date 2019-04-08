package com.everneth.emi.events.bot;

import co.aikar.idb.DB;
import co.aikar.idb.DbRow;
import com.everneth.emi.EMI;
import com.everneth.emi.ReportManager;
import com.everneth.emi.Utils;
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
        if(event.isFromType(ChannelType.TEXT) && event.getChannel().getName().contains("_staff") && !event.getAuthor().isBot())
        {
            ReportManager rm = ReportManager.getReportManager();
            String channelName = event.getChannel().getName();
            String playerName = channelName.substring(0, channelName.indexOf('_'));

            UUID player_uuid = rm.findReportByChannelId(event.getChannel().getIdLong());

            if(player_uuid != null) {
                OfflinePlayer offlinePlayer = EMI.getPlugin().getServer().getOfflinePlayer(player_uuid);


                if (offlinePlayer.isOnline()) {
                    Player player = offlinePlayer.getPlayer();
                    String name;

                    if(event.getMember().getNickname() == null)
                    {
                        name = event.getMember().getEffectiveName();
                    }
                    else
                    {
                        name = event.getMember().getNickname();
                    }
                    player.sendMessage(Utils.color("&o&d[REPORT]&f<&8" + name + "&f>&7 " + event.getMessage().getContentRaw()));
                } else if (!offlinePlayer.isOnline() && !rm.hasDiscord(player_uuid)) {
                    DbRow report = rm.getReportRecord(player_uuid);
                    String author;
                    if(event.getMember().getNickname() == null)
                    {
                        author = event.getMember().getEffectiveName();
                    }
                    else
                    {
                        author = event.getMember().getNickname();
                    }
                    try {
                        DB.executeInsert("INSERT INTO report_messages (report_id, author, message, msg_read, date_read) " +
                                        "VALUES (?, ?, ?, ?, ?)",
                                report.getInt("report_id"),
                                author,
                                event.getMessage().getContentRaw(),
                                0,
                                null);
                    } catch (SQLException e) {
                        System.out.println(e.getMessage());
                    }
                }
            }
        }
    }
}
