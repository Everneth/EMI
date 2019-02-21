package com.everneth.emi.events.bot;

import com.everneth.emi.EMI;
import com.everneth.emi.ReportManager;
import com.everneth.emi.Utils;
import com.everneth.emi.models.Report;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

public class MessageReceievedListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        if(event.isFromType(ChannelType.TEXT))
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
            else
            {
                //TODO: Add message to report_messages table
            }
        }
    }
}
