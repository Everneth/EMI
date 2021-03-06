package com.everneth.emi.models;

import co.aikar.idb.DbRow;
import com.everneth.emi.EMI;
import com.everneth.emi.managers.ReportManager;
import com.everneth.emi.utils.PlayerUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.managers.GuildManager;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;

public class Report {
    private long discordUserId;
    private long channelId;
    private long messageId;

    public Report(long channelId)
    {
        this.discordUserId = 0;
        this.channelId = channelId;
        this.messageId = 0;
    }
    public Report(long channelId, long messageId)
    {
        this.discordUserId = 0;
        this.channelId = channelId;
        this.messageId = messageId;
    }
    public Report(long channelId, long messageId, long discordUserId)
    {
        this.discordUserId = discordUserId;
        this.channelId = channelId;
        this.messageId = messageId;
    }

    public long getDiscordUserId() {
        return discordUserId;
    }

    public void setDiscordUserId(long discordUserId) {
        this.discordUserId = discordUserId;
    }

    public long getChannelId() {
        return channelId;
    }

    public void setChannelId(long channelId) {
        this.channelId = channelId;
    }

    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    public static void buildPrivateChannel(Player player, String message, String reportType)
    {
        LocalDateTime now = LocalDateTime.now();
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        Member discordMember;
        GuildManager guildManager = EMI.getJda().getGuildById(EMI.getPlugin().getConfig().getLong("guild-id")).getManager();
        Role staffRole = guildManager.getGuild().getRoleById(EMI.getPlugin().getConfig().getLong("staff-role-id"));
        Role mintRole = guildManager.getGuild().getRoleById(EMI.getPlugin().getConfig().getLong("mint-role-id"));
        Role botRole = guildManager.getGuild().getRolesByName(EMI.getJda().getSelfUser().getName(), true).get(0);
        ReportManager rm = ReportManager.getReportManager();

        DbRow playerRow = PlayerUtils.getPlayerRow(player.getUniqueId());

        switch(reportType) {
            case "_staff":
                EmbedBuilder ebs = new EmbedBuilder();
                ebs.setTitle(player.getName());
                ebs.setDescription(player.getUniqueId().toString());
                ebs.setColor(0x0082ff);
                ebs.setThumbnail("https://minotar.net/helm/" + player.getUniqueId() + "/100.png");
                ebs.addField("X", Double.toString(player.getLocation().getX()), true);
                ebs.addField("Y", Double.toString(player.getLocation().getY()), true);
                ebs.addField("Z", Double.toString(player.getLocation().getZ()), true);
                ebs.addField("Dimension", player.getWorld().getEnvironment().toString(), true);
                ebs.addField("Time Requested (EST)", format.format(now), true);
                ebs.addField("Description", message, false);
                ebs.setFooter("Staff help requested!", null);

                if(hasSynced(playerRow)) {
                    discordMember = guildManager.getGuild().getMemberById(playerRow.getLong("discord_id"));
                    ChannelAction<TextChannel> channelAction = guildManager.getGuild().createTextChannel(player.getName().toLowerCase() + reportType);
                    channelAction.addPermissionOverride(guildManager.getGuild().getPublicRole(), 0, Permission.VIEW_CHANNEL.getRawValue())
                            .addPermissionOverride(staffRole, Permission.ALL_CHANNEL_PERMISSIONS, 0)
                            .addPermissionOverride(botRole, Permission.ALL_TEXT_PERMISSIONS, 0)
                            .addPermissionOverride(discordMember, Permission.MESSAGE_READ.getRawValue(), 0)
                            .addPermissionOverride(discordMember, Permission.MESSAGE_HISTORY.getRawValue(), 0)
                            .addPermissionOverride(discordMember, Permission.MESSAGE_WRITE.getRawValue(), 0).queue(
                            channel -> {
                                Report reportToAdd = new Report(channel.getIdLong());
                                reportToAdd.setDiscordUserId(discordMember.getUser().getIdLong());
                                rm.addReport(player.getUniqueId(), reportToAdd);
                                rm.addReportRecord(reportToAdd, playerRow.getInt("player_id"));
                                channel.getGuild().getTextChannelById(channel.getIdLong()).sendMessage(ebs.build()).queue();
                            });
                }
                else
                {
                    ChannelAction<TextChannel> channelAction = guildManager.getGuild().createTextChannel(player.getName().toLowerCase() + reportType);
                    channelAction.addPermissionOverride(guildManager.getGuild().getPublicRole(), 0, Permission.VIEW_CHANNEL.getRawValue())
                            .addPermissionOverride(staffRole, Permission.ALL_TEXT_PERMISSIONS, 0)
                            .addPermissionOverride(botRole, Permission.ALL_TEXT_PERMISSIONS, 0).queue(
                            channel -> {
                                Report reportToAdd = new Report(channel.getIdLong());
                                rm.addReport(player.getUniqueId(), reportToAdd);
                                rm.addReportRecord(reportToAdd, playerRow.getInt("player_id"));
                                channel.getGuild().getTextChannelById(channel.getIdLong()).sendMessage(ebs.build()).queue();
                            });
                }
                break;
            case "_mint":
                EmbedBuilder ebm = new EmbedBuilder();
                ebm.setTitle(player.getName());
                ebm.setDescription(player.getUniqueId().toString());
                ebm.setColor(0xa262c4);
                ebm.setThumbnail("https://minotar.net/helm/" + player.getUniqueId() + "/100.png");
                ebm.addField("X", Double.toString(player.getLocation().getX()), true);
                ebm.addField("Y", Double.toString(player.getLocation().getY()), true);
                ebm.addField("Z", Double.toString(player.getLocation().getZ()), true);
                ebm.addField("Dimension", player.getWorld().getEnvironment().toString(), true);
                ebm.addField("Time Requested (EST)", format.format(now), true);
                ebm.addField("Description", message, false);
                ebm.setFooter("MINT help requested!", null);

                if(hasSynced(playerRow)) {
                    discordMember = guildManager.getGuild().getMemberById(playerRow.getLong("discord_id"));
                    ChannelAction<TextChannel> channelAction = guildManager.getGuild().createTextChannel(player.getName().toLowerCase() + reportType);
                    channelAction.addPermissionOverride(guildManager.getGuild().getPublicRole(), 0, Permission.VIEW_CHANNEL.getRawValue())
                            .addPermissionOverride(staffRole, Permission.ALL_CHANNEL_PERMISSIONS, 0)
                            .addPermissionOverride(mintRole, Permission.ALL_CHANNEL_PERMISSIONS, 0)
                            .addPermissionOverride(botRole, Permission.ALL_TEXT_PERMISSIONS, 0)
                            .addPermissionOverride(discordMember, Permission.MESSAGE_READ.getRawValue(), 0)
                            .addPermissionOverride(discordMember, Permission.MESSAGE_HISTORY.getRawValue(), 0)
                            .addPermissionOverride(discordMember, Permission.MESSAGE_WRITE.getRawValue(), 0).queue(
                            channel -> {
                                Report reportToAdd = new Report(channel.getIdLong());
                                reportToAdd.setDiscordUserId(discordMember.getUser().getIdLong());
                                rm.addReport(player.getUniqueId(), reportToAdd);
                                rm.addReportRecord(reportToAdd, playerRow.getInt("player_id"));
                                channel.getGuild().getTextChannelById(channel.getIdLong()).sendMessage(ebm.build()).queue();
                            });
                }
                else
                {
                    ChannelAction<TextChannel> channelAction = guildManager.getGuild().createTextChannel(player.getName().toLowerCase() + reportType);
                    channelAction.addPermissionOverride(guildManager.getGuild().getPublicRole(), 0, Permission.VIEW_CHANNEL.getRawValue())
                            .addPermissionOverride(staffRole, Permission.ALL_CHANNEL_PERMISSIONS, 0)
                            .addPermissionOverride(mintRole, Permission.ALL_CHANNEL_PERMISSIONS, 0)
                            .addPermissionOverride(botRole, Permission.ALL_TEXT_PERMISSIONS, 0).queue(
                            channel -> {
                                Report reportToAdd = new Report(channel.getIdLong());
                                rm.addReport(player.getUniqueId(), reportToAdd);
                                rm.addReportRecord(reportToAdd, playerRow.getInt("player_id"));
                                channel.getGuild().getTextChannelById(channel.getIdLong()).sendMessage(ebm.build()).queue();
                            });
                }
                break;
        }
    }

    public static boolean hasSynced(DbRow row)
    {
        return row.getLong("discord_id") != null && row.getLong("discord_id") != 0;
    }
}
