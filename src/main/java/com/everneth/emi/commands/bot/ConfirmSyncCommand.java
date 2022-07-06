package com.everneth.emi.commands.bot;

import co.aikar.idb.DbRow;
import com.everneth.emi.managers.DiscordSyncManager;
import com.everneth.emi.EMI;

import com.everneth.emi.models.EMIPlayer;
import com.everneth.emi.models.enums.DiscordRole;
import com.jagrosh.jdautilities.command.SlashCommand;
import net.dv8tion.jda.api.entities.User;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 *     Class: ConfirmSyncCommand
 *     Author: Faceman (@TptMike)
 *     Purpose: The JDA bot !!comfirmsync command that adds the users discord ID to EMIs player table
 */

public class ConfirmSyncCommand extends SlashCommand {
    private CompletableFuture<DbRow> playerObjectFuture;

    public ConfirmSyncCommand()
    {
        this.name = "confirmsync";
        this.help = "Confirm an account synchronization from a minecraft account";

        this.guildOnly = false;
    }

    @Override
    protected void execute(SlashCommandEvent event)
    {
        DiscordSyncManager dsm = DiscordSyncManager.getDSM();
        User toFind = dsm.findSyncRequest(event.getUser());
        EMIPlayer emiPlayer = EMIPlayer.getEmiPlayer(event.getUser().getIdLong());

        if (toFind == null) {
            event.reply("No sync request exists for your account or it has already been synced.").setEphemeral(true).queue();
            return;
        }
        else if (emiPlayer.isSynced()) {
            event.reply("You have already synced this account. If this is in error, please contact staff.").setEphemeral(true).queue();
            return;
        }

        int playerId = dsm.syncAccount(toFind);
        if (playerId == 0) {
            event.reply("Could not sync account, no player record found.").setEphemeral(true).queue();
            return;
        }

        UUID key = dsm.findSyncRequestUUID(event.getUser());
        dsm.removeSyncRequest(key.toString());
        EMI.getGuild().addRoleToMember(emiPlayer.getGuildMember(), DiscordRole.SYNCED.get()).queue();

        event.reply("Your account has been synced and your roles updated!").queue();
    }
}
