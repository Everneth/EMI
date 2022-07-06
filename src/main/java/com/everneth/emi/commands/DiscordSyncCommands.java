package com.everneth.emi.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import co.aikar.idb.DB;
import com.everneth.emi.Utils;
import com.everneth.emi.managers.DiscordSyncManager;
import com.everneth.emi.EMI;
import com.everneth.emi.models.ConfigMessage;
import com.everneth.emi.models.EMIPlayer;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

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
        EMIPlayer emiPlayer = EMIPlayer.getEmiPlayer(player.getUniqueId());
        if (emiPlayer.isSynced()) {
            player.sendMessage(Utils.color("&cYou have already synced this account. If this is in error, please contact staff."));
            return;
        }

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
        Member member = EMI.getJda().getGuildById(config.getLong("guild-id")).getMemberByTag(name, discriminator);
        if (member == null) {
            player.sendMessage(Utils.color("&c") + ConfigMessage.USER_NOT_FOUND);
        }
        else {
            // We've found the member in the guild and want to attempt to message them, open a sync request if message sends
            emiPlayer.setDiscordId(member.getIdLong());
            boolean messageSent = emiPlayer.sendDiscordMessage(ConfigMessage.ACCOUNT_SYNCED.getWithArgs(player.getName()), player);
            if (messageSent) {
                dsm.addSyncRequest(player, member.getUser());
            }
        }
    }

    @Subcommand("unsync")
    @Description("If you have lost access to your discord account, you may unsync and re-sync with a different account.")
    public void onDiscordUnsync(Player player) {
        EMIPlayer member = EMIPlayer.getEmiPlayer(player.getUniqueId());
        long discordId = member.getDiscordId();
        if (discordId == 0) {
            player.sendMessage("You do not have a discord account synced with your minecraft account.");
            return;
        }

        boolean messageSent = member.sendDiscordMessage(ConfigMessage.ACCOUNT_UNSYNCED.get(), player);
        if (messageSent) {
            Guild guild = EMI.getJda().getGuildById(config.getLong("guild-id"));
            Role syncRole = guild.getRoleById(config.getLong("synced-role-id"));
            guild.removeRoleFromMember(member.getGuildMember(), syncRole).queue();

            DB.executeUpdateAsync("UPDATE players SET discord_id = NULL WHERE ? IN (player_uuid,alt_uuid)", player.getUniqueId().toString());
            player.sendMessage(Utils.color("Your discord account has been successfully unsynced. " +
                    "Please use &a/discord sync &fto set up with a new account."));
        }
    }
}
