package com.everneth.emi.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Dependency;
import com.everneth.emi.EMI;
import com.everneth.emi.Utils;
import com.everneth.emi.managers.ReportManager;
import com.everneth.emi.models.EMIPlayer;
import com.everneth.emi.models.Report;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 *     Class: ReportCommand
 *     Author: Faceman (@TptMike)
 *     Purpose: Generate an embed and post it in the designated Discord channel
 *     using the JDA bot
 *
 */

@CommandAlias("report")
public class ReportCommand extends BaseCommand {

    @Dependency
    private Plugin plugin = EMI.getPlugin();

    @Default
    public void onReport(CommandSender sender, String message)
    {
        ReportManager rm = ReportManager.getReportManager();
        // Get the player and supply all potentially useful
        // information to the embed builder
        Player player = (Player)sender;
        EMIPlayer emiPlayer = EMIPlayer.getEmiPlayer(player.getUniqueId());
        if(rm.hasActiveReport(player.getUniqueId()))
        {
            player.sendMessage(Utils.color("<&6The Wench&f> You already have an active report in our system. Please use " +
                    "your active report for new or existing issues in progress. <3"));
        }
        else {
            Report.buildPrivateChannel(player, message, "_staff");
            // Make the bot post the embed to the channel and notify the player
            if(emiPlayer.isSynced())
                player.sendMessage(Utils.color("<&6The Wench&f> I have created a direct channel with &9staff&f. &a&lProceed to Discord to continue the chat. &d<3"));
            else
                player.sendMessage(Utils.color("<&6The Wench&f> I have created a direct channel with &9staff&f. Please use &6/rr <message>&f to message staff directly! A staff member " +
                        "will get back to you shortly. If you miss any replies, you will be notified the next time you join the server. &c&lYour Discord is NOT linked, please use that command to reply and get missed messages. &d<3"));
        }
    }
}
