package com.everneth.emi.commands.bot;

import co.aikar.idb.DB;
import co.aikar.idb.DbRow;
import com.everneth.emi.managers.DiscordSyncManager;
import com.everneth.emi.EMI;

import com.jagrosh.jdautilities.command.SlashCommand;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.util.concurrent.CompletableFuture;

/**
 *     Class: ConfirmSyncCommand
 *     Author: Faceman (@TptMike)
 *     Purpose: The JDA bot !!comfirmsync command that adds the users discord ID to EMIs player table
 */

public class ConfirmSyncCommand extends SlashCommand {
    private CompletableFuture<DbRow> playerObjectFuture;
    private CompletableFuture<Integer> futurePlayerId;
    public ConfirmSyncCommand()
    {
        this.name = "confirmsync";
        this.help = "Confirm an account synchronization from a minecraft account";
    }
    @Override
    protected void execute(SlashCommandEvent event)
    {
        DiscordSyncManager dsm = DiscordSyncManager.getDSM();
        User toFind = dsm.findSyncRequest(event.getUser());
        long guildId = EMI.getPlugin().getConfig().getLong("guild-id");
        long syncRoleId = EMI.getPlugin().getConfig().getLong("synced-role-id");
        long pendingRoleId = EMI.getPlugin().getConfig().getLong("pending-role-id");
        long memberRoleId = EMI.getPlugin().getConfig().getLong("member-role-id");

        if(event.getChannelType() == ChannelType.PRIVATE)
        {
            if(toFind == null)
            {
                event.reply("No sync request exists for your account or it hs already been synced.").queue();
            }
            else {
                if (syncExists(toFind)) {
                    event.reply("You have already synced this account. If this is in error, please contact staff.").queue();
                }
                else {
                    int playerId = syncAccount(toFind);
                    if (playerId == 0) {
                        event.reply("Could not sync account, no player record found.").queue();
                    } else {
                        dsm.removeSyncRequest(this.getPlayerRow(playerId).getString("player_uuid"));
                        EMI.getJda().getGuildById(guildId).addRoleToMember(
                                EMI.getJda().getGuildById(guildId).getMemberById(event.getUser().getIdLong()),
                                EMI.getJda().getGuildById(guildId).getRoleById(syncRoleId)
                        ).queue();
                        Role memberRole = EMI.getJda().getGuildById(guildId).getRoleById(memberRoleId);
                        if (EMI.getPlugin().getConfig().getBoolean("use-pending-role")) {
                            Role pendingRole = EMI.getJda().getGuildById(guildId).getRoleById(pendingRoleId);
                            Member member = event.getMember();
                            if (member.getRoles().contains(pendingRole)) {
                                member.getRoles().remove(pendingRole);
                                member.getRoles().add(memberRole);
                                event.reply("Your account has been synced and your roles updated!").queue();
                            } else {
                                event.reply("Your account has been synced!").queue();
                            }
                        } else {
                            event.reply("Your account has been synced!").queue();
                        }
                    }
                }
            }
        }
    }
    private boolean syncExists(User user)
    {
        DbRow playerRow;
        Long discordId = 0L;
        DiscordSyncManager dsm = DiscordSyncManager.getDSM();
        playerObjectFuture = DB.getFirstRowAsync("SELECT discord_id FROM players\n" +
                "WHERE player_uuid = ?", dsm.findSyncRequestUUID(user).toString());
        // get the results from the future
        try {
            playerRow = playerObjectFuture.get();
            discordId = playerRow.getLong("discord_id");
        }
        catch (Exception e)
        {
            System.out.print(e.getMessage());
        }
        if(discordId == null || discordId == 0)
            return false;
        else
        {
            return true;
        }
    }
    private int syncAccount(User user)
    {
        DiscordSyncManager dsm = DiscordSyncManager.getDSM();
        int playerId = 0;
        futurePlayerId = DB.executeUpdateAsync("UPDATE players SET discord_id = ? " +
                "WHERE player_uuid = ?", user.getIdLong(), dsm.findSyncRequestUUID(user).toString());
        // get the results from the future
        try {
             playerId = futurePlayerId.get();
        }
        catch (Exception e)
        {
            System.out.print(e.getMessage());
        }
        return playerId;
    }
    private DbRow getPlayerRow(int id)
    {
        CompletableFuture<DbRow> futurePlayer;
        DbRow player = new DbRow();
        futurePlayer = DB.getFirstRowAsync("SELECT * FROM players WHERE player_id = ?", id);
        try {
            player = futurePlayer.get();
        }
        catch (Exception e)
        {
            EMI.getPlugin().getLogger().info(e.getMessage());
        }
        return player;
    }
}
