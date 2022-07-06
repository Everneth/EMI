package com.everneth.emi.models;

import com.everneth.emi.EMI;
import com.everneth.emi.managers.ReportManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.managers.GuildManager;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        Member discordMember;
        GuildManager guildManager = EMI.getJda().getGuildById(EMI.getPlugin().getConfig().getLong("guild-id")).getManager();
        Role staffRole = guildManager.getGuild().getRoleById(EMI.getPlugin().getConfig().getLong("staff-role-id"));
        Role mintRole = guildManager.getGuild().getRoleById(EMI.getPlugin().getConfig().getLong("mint-role-id"));
        Role botRole = guildManager.getGuild().getRolesByName(EMI.getJda().getSelfUser().getName(), true).get(0);
        ReportManager rm = ReportManager.getReportManager();

        EMIPlayer emiPlayer = EMIPlayer.getEmiPlayer(player.getUniqueId());

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
                ebs.addField("Time Requested (EST)", formatter.format(now), true);
                ebs.addField("Description", message, false);
                ebs.setFooter("Staff help requested!", null);

                if(emiPlayer.isSynced()) {
                    long userPermissions = Permission.ALL_TEXT_PERMISSIONS
                            + Permission.VIEW_CHANNEL.getRawValue()
                            - Permission.MESSAGE_MANAGE.getRawValue();
                    discordMember = guildManager.getGuild().getMemberById(emiPlayer.getDiscordId());
                    ChannelAction<TextChannel> channelAction = guildManager.getGuild().createTextChannel(player.getName().toLowerCase() + reportType);
                    channelAction.addPermissionOverride(guildManager.getGuild().getPublicRole(), 0, Permission.VIEW_CHANNEL.getRawValue())
                            .addPermissionOverride(staffRole, Permission.ALL_CHANNEL_PERMISSIONS, 0)
                            .addPermissionOverride(botRole, Permission.ALL_TEXT_PERMISSIONS, 0)
                            .addPermissionOverride(discordMember, userPermissions, 0).queue(
                            channel -> {
                                Report reportToAdd = new Report(channel.getIdLong());
                                reportToAdd.setDiscordUserId(discordMember.getUser().getIdLong());
                                rm.addReport(player.getUniqueId(), reportToAdd);
                                rm.addReportRecord(reportToAdd, emiPlayer.getId());
                                channel.getGuild().getTextChannelById(channel.getIdLong()).sendMessageEmbeds(ebs.build()).queue();
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
                                rm.addReportRecord(reportToAdd, emiPlayer.getId());
                                channel.getGuild().getTextChannelById(channel.getIdLong()).sendMessageEmbeds(ebs.build()).queue();
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
                ebm.addField("Time Requested (EST)", formatter.format(now), true);
                ebm.addField("Description", message, false);
                ebm.setFooter("MINT help requested!", null);

                if(emiPlayer.isSynced()) {
                    long userPermissions = Permission.ALL_TEXT_PERMISSIONS
                            + Permission.VIEW_CHANNEL.getRawValue()
                            - Permission.MESSAGE_MANAGE.getRawValue();
                    discordMember = guildManager.getGuild().getMemberById(emiPlayer.getDiscordId());
                    ChannelAction<TextChannel> channelAction = guildManager.getGuild().createTextChannel(player.getName().toLowerCase() + reportType);
                    channelAction.addPermissionOverride(guildManager.getGuild().getPublicRole(), 0, Permission.VIEW_CHANNEL.getRawValue())
                            .addPermissionOverride(staffRole, Permission.ALL_CHANNEL_PERMISSIONS, 0)
                            .addPermissionOverride(mintRole, Permission.ALL_CHANNEL_PERMISSIONS, 0)
                            .addPermissionOverride(botRole, Permission.ALL_TEXT_PERMISSIONS, 0)
                            .addPermissionOverride(discordMember, userPermissions, 0).queue(
                            channel -> {
                                Report reportToAdd = new Report(channel.getIdLong());
                                reportToAdd.setDiscordUserId(discordMember.getUser().getIdLong());
                                rm.addReport(player.getUniqueId(), reportToAdd);
                                rm.addReportRecord(reportToAdd, emiPlayer.getId());
                                channel.getGuild().getTextChannelById(channel.getIdLong()).sendMessageEmbeds(ebm.build()).queue();
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
                                rm.addReportRecord(reportToAdd, emiPlayer.getId());
                                channel.getGuild().getTextChannelById(channel.getIdLong()).sendMessageEmbeds(ebm.build()).queue();
                            });
                }
                break;
        }
    }
}
