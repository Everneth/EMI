package com.everneth.emi.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import co.aikar.idb.DB;
import com.everneth.emi.EMI;
import com.everneth.emi.Utils;
import com.everneth.emi.managers.DiscordSyncManager;
import com.everneth.emi.models.EMIPlayer;
import com.everneth.emi.models.enums.ConfigMessage;
import com.everneth.emi.models.enums.DiscordRole;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

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
    @Syntax("<discord username>")
    public void onDiscordsync(Player player, String discordUsername) {
        // If the account is already synced, notify the user and return
        EMIPlayer emiPlayer = EMIPlayer.getEmiPlayer(player.getUniqueId());
        if (emiPlayer.isSynced()) {
            player.sendMessage(Utils.color("&cYou have already synced this account. If this is in error, please contact staff."));
            return;
        }

        // JDA does not support searching members by username directly so this is the best we got
        List<User> users = EMI.getJda().getUsersByName(discordUsername, true);
        if (users.size() == 0) {
            player.sendMessage(Utils.color("&c") + ConfigMessage.USER_NOT_FOUND.get());
            return;
        }
        Member member = EMI.getGuild().getMemberById(users.get(0).getIdLong());
        DiscordSyncManager dsm = DiscordSyncManager.getDSM();
        // We've found the member in the guild and want to attempt to message them, open a sync request if message sends
        emiPlayer.setDiscordId(member.getIdLong());
        AtomicBoolean messageSent = emiPlayer.sendDiscordMessage(ConfigMessage.ACCOUNT_SYNCED.getWithArgs(player.getName()));
        // Add sync request pre-emptively so if message sends the sync request can still be confirmed before the runnable executes
        dsm.addSyncRequest(player, member.getUser());
        EMI.getPlugin().getServer().getScheduler().runTaskLaterAsynchronously(EMI.getPlugin(), new Runnable() {
            @Override
            public void run() {
                if (messageSent.get()) {
                    player.sendMessage(Utils.color("&a") + "Message sent. Please check your discord DMs to confirm your synchronization!");
                } else {
                    // ~4 seconds have passed and message has not sent. Assume it failed
                    player.sendMessage(Utils.color("&c") + ConfigMessage.DISCORD_MESSAGE_FAILED.get());
                    dsm.removeSyncRequest(player);
                }
            }
        }, 20L * 4); // time in seconds converted to ticks

    }

    @Subcommand("unsync")
    @Description("If you have lost access to your discord account, you may unsync and re-sync with a different account.")
    public void onDiscordUnsync(Player player) {
        EMIPlayer emiPlayer = EMIPlayer.getEmiPlayer(player.getUniqueId());
        long discordId = emiPlayer.getDiscordId();
        if (!emiPlayer.isSynced()) {
            player.sendMessage("You do not have a discord account synced with your minecraft account.");
            return;
        }

        // Update database before attempting to message the user on Discord, to prevent any issues if the account has left the guild
        DB.executeUpdateAsync("UPDATE players SET discord_id = NULL WHERE ? IN (player_uuid,alt_uuid)", player.getUniqueId().toString());

        // We are not relying on the success of the message send, so we can ignore the atomic return
        emiPlayer.sendDiscordMessage(ConfigMessage.ACCOUNT_UNSYNCED.get());
        Guild guild = EMI.getGuild();
        guild.removeRoleFromMember(emiPlayer.getGuildMember(), DiscordRole.SYNCED.get()).queue();

        player.sendMessage(Utils.color("Your discord account has been successfully unsynced. " +
                "Please use &a/discord sync &fto set up with a new account."));
    }
}
