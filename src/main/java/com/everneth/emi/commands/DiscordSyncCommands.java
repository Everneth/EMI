package com.everneth.emi.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import co.aikar.idb.DB;
import co.aikar.idb.DbRow;
import com.everneth.emi.Utils;
import com.everneth.emi.managers.DiscordSyncManager;
import com.everneth.emi.EMI;
import com.everneth.emi.models.EMIPlayer;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.text.MessageFormat;
import java.util.List;

/**
 * Class: DiscordSyncCommands
 * Author: Faceman (@TptMike) and Riki
 * Purpose: Initiate and revoke a request to sync minecraft and discord accounts together in the
 * EMI database.
 */

@CommandAlias("discord")
@Description("Discord account sync manager")
public class DiscordSyncCommands extends BaseCommand {
    Plugin plugin = EMI.getPlugin();
    private FileConfiguration config = plugin.getConfig();

    @Subcommand("sync")
    @Description("Sync your discord account to your minecraft account.")
    @Syntax("<Name#0000>")
    public void onDiscordsync(Player player, String discordDetails) {
        // If the account is already synced, notify the user and return
        if (EMIPlayer.syncExists(player.getUniqueId())) {
            player.sendMessage(Utils.color("&cYou have already synced this account. If this is in error, please contact staff."));
            return;
        }

        // Get a list of guild members, and individual strings for the passed in member
        List<Member> memberList = EMI.getJda().getGuildById(config.getLong("guild-id")).getMembers();
        String name, discriminator;
        // If the input does not contain the discriminator don't even attempt to find the user
        if (discordDetails.contains("#")) {
            int poundIndex = discordDetails.indexOf('#');
            // If there are not four digits following the '#', return and notify the user
            if (poundIndex + 4 != discordDetails.length() - 1) {
                player.sendMessage(Utils.color("&cInvalid discriminator. Please make sure your discriminator has 4 digits."));
                return;
            }
            name = discordDetails.substring(0, poundIndex);
            discriminator = discordDetails.substring(poundIndex + 1);
        }
        else {
            player.sendMessage(Utils.color("&cInvalid name. Please include name and discriminator (&fName#0000&c)."));
            return;
        }

        DiscordSyncManager dsm = DiscordSyncManager.getDSM();
        // Search the guild member list for all users with the same name
        for (Member member : memberList) {
            // If we find a user matching the name and discriminator, proceed with sync request
            User user = member.getUser();
            if (user.getName().equalsIgnoreCase(name) && user.getDiscriminator().equals(discriminator)) {
                user.openPrivateChannel()
                        .flatMap(privateChannel -> privateChannel.sendMessage(MessageFormat.format(config.getString("account-sync-alert"), player.getName())))
                        .queue(message -> {
                            dsm.addSyncRequest(player, user);
                            player.sendMessage(Utils.color("&aMessage sent. Please check your discord DMs to confirm your synchronization!"));
                        }, new ErrorHandler()
                                .handle(ErrorResponse.CANNOT_SEND_TO_USER, (error) -> {
                                    player.sendMessage(Utils.color("&c" + config.getString("message-send-error")));
                                }));
                return;
            }
        }
        player.sendMessage(Utils.color("&c" + config.getString("user-not-found-error")));
    }

    @Subcommand("unsync")
    @Description("If you have lost access to your discord account, you may unsync and re-sync with a different account.")
    public void onDiscordUnsync(Player player) {
        EMIPlayer playerRow = EMIPlayer.getEmiPlayer(player.getUniqueId());
        long discordId = playerRow.getDiscordId();
        if (discordId == 0) {
            player.sendMessage("You do not have a discord account synced with your minecraft account.");
            return;
        }
        Guild guild = EMI.getJda().getGuildById(config.getLong("guild-id"));
        Member member = guild.getMemberById(discordId);

        member.getUser().openPrivateChannel()
                .flatMap(privateChannel -> privateChannel.sendMessage(config.getString("account-unsync-alert")))
                .queue(null, new ErrorHandler()
                        .handle(ErrorResponse.CANNOT_SEND_TO_USER, (error) ->
                            player.sendMessage(Utils.color("&c" + config.getString("message-send-error")))));

        Role syncRole = guild.getRoleById(config.getLong("synced-role-id"));
        guild.removeRoleFromMember(member, syncRole).queue();

        DB.executeUpdateAsync("UPDATE players SET discord_id = NULL WHERE ? IN (player_uuid,alt_uuid)", player.getUniqueId().toString());
        player.sendMessage(Utils.color("Your discord account has been successfully unsynced. Please use &a/discord sync &fto set up with a new account."));
    }
}
