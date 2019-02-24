package com.everneth.emi.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Dependency;
import co.aikar.idb.DB;
import co.aikar.idb.DbRow;
import com.everneth.emi.EMI;
import com.everneth.emi.ReportManager;
import com.everneth.emi.Utils;
import com.everneth.emi.models.Report;
import net.dv8tion.jda.client.entities.Application;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.managers.GuildManager;
import net.dv8tion.jda.core.requests.restaction.ChannelAction;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
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
    private JDA bot = EMI.getJda();

    @Dependency
    private Plugin plugin = EMI.getPlugin();

    @Default
    @CommandAlias("report")
    @CatchUnknown
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

            buildPrivateChannel(player, message);
            // Make the bot post the embed to the channel and notify the player
            player.sendMessage(Utils.color("<&6The Wench&f> Your report submitted to &6#help&f! A staff member " +
                    "will get back to you shortly. <3"));
        }
    }

    private void buildPrivateChannel(Player player, String message)
    {
        Date now = new Date();
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        Member discordMember;
        GuildManager guildManager = EMI.getJda().getGuildById(EMI.getPlugin().getConfig().getLong("guild-id")).getManager();
        Role staffRole = guildManager.getGuild().getRoleById(EMI.getPlugin().getConfig().getLong("staff-role-id"));
        Role botRole = guildManager.getGuild().getRolesByName(EMI.getJda().getSelfUser().getName(), true).get(0);
        ReportManager rm = ReportManager.getReportManager();

        DbRow playerRow = getPlayerRow(player.getUniqueId());

        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(player.getName());
        eb.setDescription(player.getUniqueId().toString());
        eb.setColor(0xffff00);
        eb.setThumbnail("https://minotar.net/helm/" + player.getUniqueId() + "/100.png");
        eb.addField("X", Double.toString(player.getLocation().getX()), true);
        eb.addField("Y", Double.toString(player.getLocation().getY()), true);
        eb.addField("Z", Double.toString(player.getLocation().getZ()), true);
        eb.addField("Dimension", player.getWorld().getEnvironment().toString(), true);
        eb.addField("Time Reported (EST)", format.format(now), true);
        eb.addField("Server", player.getServer().getServerName(), true);
        eb.addField("Description", message, false);
        eb.setFooter("Help requested!", null);

        if(hasSynced(playerRow)) {
            discordMember = guildManager.getGuild().getMemberById(playerRow.getLong("discord_id"));
            ChannelAction channelAction = guildManager.getGuild().getController().createTextChannel(player.getName().toLowerCase() + "_staff");
            channelAction.addPermissionOverride(guildManager.getGuild().getPublicRole(), 0, Permission.VIEW_CHANNEL.getRawValue())
                    .addPermissionOverride(staffRole, Permission.ALL_TEXT_PERMISSIONS, 0)
                    .addPermissionOverride(botRole, Permission.ALL_TEXT_PERMISSIONS, 0)
                    .addPermissionOverride(discordMember, Permission.MESSAGE_WRITE.getRawValue(), 0).queue(
                    (channel) -> {
                        Report reportToAdd = new Report(channel.getIdLong());
                        reportToAdd.setDiscordUserId(discordMember.getUser().getIdLong());
                        rm.addReport(player.getUniqueId(), reportToAdd);
                        rm.addReportRecord(reportToAdd, playerRow.getInt("player_id"));
                        channel.getGuild().getTextChannelById(channel.getIdLong()).sendMessage(eb.build()).queue();
                    });
        }
        else
        {
            ChannelAction channelAction = guildManager.getGuild().getController().createTextChannel(player.getName().toLowerCase() + "_staff");
            channelAction.addPermissionOverride(guildManager.getGuild().getPublicRole(), 0, Permission.VIEW_CHANNEL.getRawValue())
                    .addPermissionOverride(staffRole, Permission.ALL_TEXT_PERMISSIONS, 0)
                    .addPermissionOverride(botRole, Permission.ALL_TEXT_PERMISSIONS, 0).queue(
                    (channel) -> {
                        Report reportToAdd = new Report(channel.getIdLong());
                        rm.addReport(player.getUniqueId(), reportToAdd);
                        rm.addReportRecord(reportToAdd, playerRow.getInt("player_id"));
                        channel.getGuild().getTextChannelById(channel.getIdLong()).sendMessage(eb.build()).queue();
                    });
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
    private boolean hasSynced(DbRow row)
    {
        if(row.getLong("discord_id") == null || row.getLong("discord_id") == 0)
        {
            return false;
        }
        else
        {
            return true;
        }
    }
}
