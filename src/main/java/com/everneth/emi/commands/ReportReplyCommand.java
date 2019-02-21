package com.everneth.emi.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import com.everneth.emi.EMI;
import com.everneth.emi.ReportManager;
import com.everneth.emi.Utils;
import com.everneth.emi.models.Report;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.TextChannel;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("report-reply|rr|rreply")
public class ReportReplyCommand extends BaseCommand {
    private JDA bot = EMI.getJda();
    private ReportManager rm = ReportManager.getReportManager();
    @Default
    @CommandAlias("report-reply|rr|rreply")
    @CatchUnknown
    public void onReportReply(CommandSender sender, String message) {
        Player player = (Player) sender;
        if(rm.hasActiveReport(player.getUniqueId()))
        {
            Report report = rm.findReportById(player.getUniqueId());
            TextChannel chan = bot.getTextChannelById(report.getChannelId());
            chan.sendMessage(player.getName() + ": " + message).queue();
        }
        else
        {
            player.sendMessage(Utils.color("&cYou do not have an active report!"));
        }
    }
}
