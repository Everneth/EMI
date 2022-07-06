package com.everneth.emi.commands.bot;

import com.everneth.emi.managers.DiscordSyncManager;
import com.everneth.emi.EMI;
import com.everneth.emi.models.EMIPlayer;
import com.everneth.emi.models.enums.DiscordRole;
import com.jagrosh.jdautilities.command.SlashCommand;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import java.util.UUID;

/**
 *     Class: ConfirmSyncCommand
 *     Author: Faceman (@TptMike)
 *     Purpose: The JDA bot !!denysync command that removes the active sync request from the service
 */

public class DenySyncCommand extends SlashCommand {
    private DiscordSyncManager dsm = DiscordSyncManager.getDSM();

    public DenySyncCommand()
    {
        this.name = "denysync";
        this.help = "Deny a synchronization request to your discord account.";

        this.guildOnly = false;
    }
    @Override
    public void execute(SlashCommandEvent event)
    {
        UUID key = dsm.findSyncRequestUUID(event.getUser());
        EMIPlayer emiPlayer = EMIPlayer.getEmiPlayer(event.getUser().getIdLong());
        if(key == null && !emiPlayer.isSynced())
        {
            event.reply("No request found. It either expired or the server was restarted.").setEphemeral(true).queue();
        }
        else if (key == null && emiPlayer.isSynced())
        {
            event.reply("Your account is already synced and running this command did nothing.").setEphemeral(true).queue();
        }
        else if (key != null)
        {
            dsm.removeSyncRequest(key.toString());
            event.reply("Sync request denied successfully.").queue();
        }
    }
}
