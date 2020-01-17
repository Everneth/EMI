package com.everneth.emi.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.idb.DbRow;
import com.everneth.emi.managers.ReportManager;
import com.everneth.emi.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

@CommandAlias("getreplies|gr|get-replies")
public class GetRepliesCommand extends BaseCommand {
    private ReportManager rm = ReportManager.getReportManager();

    @CommandAlias("getreplies|gr|get-replies")
    public void onReportReply(CommandSender sender) {
        Player player = (Player) sender;
        if(rm.hasActiveReport(player.getUniqueId())) {
            List<DbRow> messages = rm.getMissedMessages(player.getUniqueId());

            for(DbRow msg : messages)
            {
                player.sendMessage(Utils.color("&d&o[REPORT] <" + msg.getString("author") + ">&5 " +
                        msg.getString("message")));
            }
            player.sendMessage(Utils.color("&7Please use /report-reply or /rr to reply to staff."));
        }
        else
        {
            player.sendMessage(Utils.color("&cYou do not have an active report!"));
        }
    }
}
