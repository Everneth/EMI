package com.everneth.emi.commands.mint;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.everneth.emi.Utils;
import com.everneth.emi.managers.ReportManager;
import com.everneth.emi.models.Report;
import com.everneth.emi.utils.PlayerUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.sql.SQLException;

/**
 *     Class: MintCommand
 *     Author: Faceman (@TptMike)
 *     Purpose: The command structure of /mint and all subcommands
 *     Notes: In future, see about making a MintBaseCommand parent class and move subcommands into their own classes
 */

@CommandAlias("mint")
public class MintCommand extends BaseCommand
{
    @Dependency
    private Plugin plugin;

    @CommandPermission("emi.mint")
    @Subcommand("help")
    public void onHelpCommand(CommandSender sender, String message)
    {
        ReportManager rm = ReportManager.getReportManager();
        // Get the player and supply all potentially useful
        // information to the embed builder
        Player player = (Player)sender;

        if(rm.hasActiveReport(player.getUniqueId()))
        {
            player.sendMessage(Utils.color("<&6The Wench&f> You already have an active issue in our system. Please use " +
                    "your active report/request for new or existing issues in progress. <3"));
        }
        else {
            Report.buildPrivateChannel(player, message, "_mint");
            // Make the bot post the embed to the channel and notify the player
            if(PlayerUtils.syncExists(player.getUniqueId()))
                player.sendMessage(Utils.color("<&6The Wench&f> I have created a direct channel with the &dMinistry of the Interior&f. &a&lProceed to Discord to continue the chat. &d<3"));
            else
                player.sendMessage(Utils.color("<&6The Wench&f> I have created a direct channel with the &dMinistry of the Interior&f. Please use &6/rr <message>&f to message MINT directly! A member " +
                        "will get back to you shortly. If you miss any replies, you will be notified the next time you join the server. &c&lYour Discord is NOT linked, please use that command to reply and get missed messages. &d<3"));
        }

    }


}