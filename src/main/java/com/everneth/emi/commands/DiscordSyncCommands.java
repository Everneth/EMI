package com.everneth.emi.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import co.aikar.idb.DB;
import co.aikar.idb.DbRow;
import com.everneth.emi.Utils;
import com.everneth.emi.managers.DiscordSyncManager;
import com.everneth.emi.EMI;
import com.everneth.emi.utils.PlayerUtils;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

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

    @Subcommand("sync")
    @Description("Sync your discord account to your minecraft account.")
    @Syntax("<Name#0000>")
    public void onDiscordsync(Player player, String discordDetails) {
        // If the account is already synced, notify the user and return
        if (PlayerUtils.syncExists(player.getUniqueId())) {
            player.sendMessage(Utils.color("&cYou have already synced this account. If this is in error, please contact staff."));
            return;
        }

        // Get a list of guild members, and individual strings for the passed in member
        List<Member> memberList = EMI.getJda().getGuildById(plugin.getConfig().getLong("guild-id")).getMembers();
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
                dsm.addSyncRequest(player, user);
                try {
                    user.openPrivateChannel().queue(privateChannel ->
                            privateChannel.sendMessage(player.getName() + " is attempting to link their minecraft account with our Discord guild. " +
                                    "If this is you, please use `/confirmsync` to complete the account synchronization. " +
                                    "If this is not done by you, please use `/denysync` and forward this message to staff immediately. Thank you!").queue());
                }
                catch (Exception e)
                {
                    EMI.getPlugin().getLogger().warning(e.getMessage());
                    dsm.removeSyncRequest(player);
                    player.sendMessage(Utils.color("&cFailed to create a sync request because I cannot message you on Discord. " +
                            "Please enable messages from Everneth Discord members and try again."));
                }
                player.sendMessage(Utils.color("&aMessage sent. Please check your discord DMs to confirm your synchronization!"));
                return;
            }
        }
        player.sendMessage(Utils.color("&cUser not found! Please check your details and try again. If this is your third attempt, please contact Staff."));
    }

    @Subcommand("unsync")
    @Description("If you have lost access to your discord account, you may unsync and re-sync with a different account.")
    public void onDiscordUnsync(Player player) {
        DbRow playerRow = PlayerUtils.getPlayerRow(player.getUniqueId());
        Long discordId = playerRow.getLong("discord_id");
        if (discordId == null || discordId == 0) {
            player.sendMessage("You do not have a discord account synced with your minecraft account.");
            return;
        }
        Guild guild = EMI.getJda().getGuildById(plugin.getConfig().getLong("guild-id"));
        Member member = guild.getMemberById(discordId);
        member.getUser().openPrivateChannel().queue(privateChannel ->
                privateChannel.sendMessage("Your discord account has been unsynced with your minecraft account. If you did not request an unsync " +
                            "please contact staff immediately.").queue());

        Role syncRole = guild.getRoleById(EMI.getPlugin().getConfig().getLong("synced-role-id"));
        guild.removeRoleFromMember(member, syncRole).queue();

        DB.executeUpdateAsync("UPDATE players SET discord_id = 0 WHERE ? IN (player_uuid,alt_uuid)", player.getUniqueId().toString());
        player.sendMessage(Utils.color("Your discord account has been successfully unsynced. Please use &a/discord sync &fto set up with a new account."));
    }
}
