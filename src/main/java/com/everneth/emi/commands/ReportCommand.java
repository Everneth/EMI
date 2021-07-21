package com.everneth.emi.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Dependency;
import co.aikar.idb.DB;
import co.aikar.idb.DbRow;
import com.everneth.emi.EMI;
import com.everneth.emi.managers.ReportManager;
import com.everneth.emi.Utils;
import com.everneth.emi.models.Report;
import com.everneth.emi.utils.PlayerUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.managers.GuildManager;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import javax.xml.soap.Text;
import java.nio.channels.Channel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

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

        if(rm.hasActiveReport(player.getUniqueId()))
        {
            player.sendMessage(Utils.color("<&6The Wench&f> You already have an active report in our system. Please use " +
                    "your active report for new or existing issues in progress. <3"));
        }
        else {
            Report.buildPrivateChannel(player, message, "_staff");
            // Make the bot post the embed to the channel and notify the player
            if(Report.hasSynced(PlayerUtils.getPlayerRow(player.getUniqueId())))
                player.sendMessage(Utils.color("<&6The Wench&f> I have created a direct channel with &9staff&f. &a&lProceed to Discord to continue the chat. &d<3"));
            else
                player.sendMessage(Utils.color("<&6The Wench&f> I have created a direct channel with &9staff&f. Please use &6/rr <message>&f to message staff directly! A staff member " +
                        "will get back to you shortly. If you miss any replies, you will be notified the next time you join the server. &c&lYour Discord is NOT linked, please use that command to reply and get missed messages. &d<3"));
        }
    }
}
