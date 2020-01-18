package com.everneth.emi.commands.playerassistance;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.idb.DB;
import co.aikar.idb.DbRow;
import com.everneth.emi.managers.DiscordSyncManager;
import com.everneth.emi.EMI;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Class: DiscordsyncCommand
 * Author: Faceman (@TptMike)
 * Purpose: Initiate a request to sync minecraft and discord accounts together in the
 * EMI database.
 */

@CommandAlias("discordsync")
public class DiscordsyncCommand extends BaseCommand {
    Plugin plugin = EMI.getPlugin();

    @CommandAlias("discordsync")
    public void onDiscordsync(CommandSender sender, String discordDetails) {

        // Get the player, a list of guild members, and individual strings for the passed in member
        Player player = (Player) sender;
        if (!syncExists(player)) {
            List<Member> memberList = EMI.getJda().getGuildById(plugin.getConfig().getLong("guild-id")).getMembers();
            List<User> userList = new ArrayList<User>();
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
                sender.sendMessage("User not found! Please check your details and try again. If this is your second attempt, please contact Comms.");
            else {
                // We've found at least 1 match
                if (userList.size() == 1) {
                    // check the discriminator for a match
                    if (userList.get(0).getDiscriminator().equals(discriminator)) {
                        // Match found, start sync
                        sender.sendMessage("Please check your Discord DMs to verify your account.");
                        dsm.addSyncRequest(player, userList.get(0));
                        userList.get(0).openPrivateChannel().queue((channel) ->
                                {
                                    channel.sendMessage(sender.getName() + " is attempting to link their minecraft account with our Discord guild. if this is you, please use !!confirmsync to complete the account synchronization. If this is not done by you, please forward this message to staff immediately. Thank you!").queue();
                                }
                        );
                    }
                    // discriminator did not match, notify the user
                    else {
                        sender.sendMessage("User not found! Please check your details and try again. If this is your second attempt, please contact Comms.");
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
                                        channel.sendMessage(sender.getName() + " is attempting to link their minecraft account with our Discord guild. if this is you, please use !!confirmsync to complete the account synchronization. If this is not done by you, please forward this message to staff immediately. Thank you!").queue();
                                    }
                            );
                        }
                    }
                    // If no user is found, notify the command sender
                    if (!userFound) {
                        sender.sendMessage("User not found! Please check your discord details and try again. If this is your second attempt, please contact Comms.");
                    }
                }
            }
        } else {
            sender.sendMessage("You have already synced this account. If this is in error, please contact staff.");
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
