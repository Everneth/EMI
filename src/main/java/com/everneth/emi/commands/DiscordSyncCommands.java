package com.everneth.emi.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import co.aikar.idb.DB;
import co.aikar.idb.DbRow;
import com.everneth.emi.managers.DiscordSyncManager;
import com.everneth.emi.EMI;
import com.jagrosh.jdautilities.command.annotation.JDACommand;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Class: DiscordsyncCommand
 * Author: Faceman (@TptMike)
 * Purpose: Initiate a request to sync minecraft and discord accounts together in the
 * EMI database.
 */

@CommandAlias("discord")
@Description("Discord account sync manager")
public class DiscordsyncCommand extends BaseCommand {
    Plugin plugin = EMI.getPlugin();

    @Subcommand("sync")
    @Description("Sync your discord account to your minecraft account ")
    @Syntax("<Name#0000>")
    public void onDiscordsync(Player player, String discordDetails) {

        // Get a list of guild members, and individual strings for the passed in member
        if (!syncExists(player)) {
            List<Member> memberList = EMI.getJda().getGuildById(plugin.getConfig().getLong("guild-id")).getMembers();
            List<User> userList = new ArrayList<>();
            int poundIndex = discordDetails.indexOf('#');
            String name = discordDetails.substring(0, poundIndex);
            String discriminator = discordDetails.substring(poundIndex + 1);
            DiscordSyncManager dsm = DiscordSyncManager.getDSM();
            // Search the guild member list for all users with the same name
            for (Member member : memberList) {
                // If a match is found, add it to the userlist for checking the discriminator
                if (member.getUser().getName().equals(name))
                    userList.add(member.getUser());
            }

            // If no match was found, notify the user
            if (userList.isEmpty())
                player.sendMessage("User not found! Please check your details and try again. If this is your second attempt, please contact Comms.");
            else {
                // We've found at least 1 match
                if (userList.size() == 1) {
                    // check the discriminator for a match
                    if (userList.get(0).getDiscriminator().equals(discriminator)) {
                        // Match found, start sync
                        player.sendMessage("Please check your Discord DMs to verify your account.");
                        dsm.addSyncRequest(player, userList.get(0));
                        userList.get(0).openPrivateChannel().queue((channel) ->
                                {
                                    channel.sendMessage(player.getName() + " is attempting to link their minecraft account with our Discord guild. if this is you, please use !!confirmsync to complete the account synchronization. If this is not done by you, please use !!denysync forward this message to staff immediately. Thank you!").queue();
                                }
                        );
                    }
                    // discriminator did not match, notify the user
                    else {
                        player.sendMessage("User not found! Please check your details and try again. If this is your second attempt, please contact Comms.");
                    }
                }
                // We've found multiple users with the same name
                else {
                    boolean userFound = false;
                    // Loop through the list to check discriminators
                    for (User user : userList) {
                        if (user.getName().equals(name) && user.getDiscriminator().equals(discriminator)) {
                            // found our user, start sync
                            userFound = true;
                            dsm.addSyncRequest(player, user);
                            user.openPrivateChannel().queue((channel) ->
                                    {
                                        channel.sendMessage(player.getName() + " is attempting to link their minecraft account with our Discord guild. if this is you, please use !!confirmsync to complete the account synchronization. If this is not done by you, please forward this message to staff immediately. Thank you!").queue();
                                    }
                            );
                        }
                    }
                    // If no user is found, notify the command sender
                    if (!userFound) {
                        player.sendMessage("User not found! Please check your discord details and try again. If this is your second attempt, please contact Comms.");
                    }
                }
            }
        } else {
            player.sendMessage("You have already synced this account. If this is in error, please contact staff.");
        }
    }

    @Subcommand("unsync")
    @Description("If you have lost access to your discord account, you may unsync and re-sync with a different account.")
    public void onDiscordUnsync(Player player) {
        if (syncExists(player)) {
            CompletableFuture<DbRow> playerObjectFuture = DB.getFirstRowAsync("SELECT discord_id FROM players " +
                    "WHERE player_uuid = ?", player.getUniqueId().toString());
            long pendingRoleId = EMI.getPlugin().getConfig().getLong("pending-role-id");

            DbRow playerRow;
            Long discordId = 0L;
            try {
                playerRow = playerObjectFuture.get();
                discordId = playerRow.getLong("discord_id");
            } catch (Exception e) {
                System.out.print(e.getMessage());
            }
            Member member = EMI.getJda().getGuildById(plugin.getConfig().getLong("guild-id")).getMemberById(discordId);
            member.getUser().openPrivateChannel().queue(privateChannel ->
                    privateChannel.sendMessage("Your discord account has been unsynced with your minecraft account. If you did not request an unsync " +
                            "please contact staff immediately."));
            member.getRoles().remove(pendingRoleId);
            DB.executeUpdateAsync("UPDATE players SET discord_id = 0 WHERE player_uuid = ?", player.getUniqueId().toString());
            player.sendMessage("Your discord account has been successfully unsynced. Please use /discord sync to set up with a new account.");
        }
        else {
            player.sendMessage("You do not have a discord account synced with your minecraft account.");
        }
    }

    private boolean syncExists(Player player) {
        DbRow playerRow;
        Long discordId = 0L;
        CompletableFuture<DbRow> playerObjectFuture = DB.getFirstRowAsync("SELECT discord_id FROM players\n" +
                "WHERE player_uuid = ?", player.getUniqueId().toString());
        // get the results from the future
        try {
            playerRow = playerObjectFuture.get();
            discordId = playerRow.getLong("discord_id");
        } catch (Exception e) {
            System.out.print(e.getMessage());
        }
        if (discordId == null || discordId == 0)
            return false;
        else {
            return true;
        }
    }
}
