package com.everneth.emi.commands.bot;

import com.everneth.emi.DiscordSyncManager;
import com.everneth.emi.EMI;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.core.entities.Role;

import java.util.UUID;

/**
 *     Class: ConfirmSyncCommand
 *     Author: Faceman (@TptMike)
 *     Purpose: The JDA bot !!denysync command that removes the active sync request from the service
 */

public class DenySyncCommand extends Command {
    private DiscordSyncManager dsm = DiscordSyncManager.getDSM();

    public DenySyncCommand()
    {
        this.name = "denysync";
        this.guildOnly = false;
    }
    @Override
    public void execute(CommandEvent event)
    {
        UUID key = dsm.findSyncRequestUUID(event.getAuthor());

        if(key == null && !hasSyncRole(event))
        {
            event.replyInDm("No request found. It either expired or the server was restarted.");
        }
        else if (key == null && hasSyncRole(event))
        {
            event.replyInDm("Your account is already synced and running this command did nothing.");
        }
        else if (key != null)
        {
            dsm.removeSyncRequest(key.toString());
            event.replyInDm("Sync request denied successfully.");
        }
    }
    private boolean hasSyncRole(CommandEvent event)
    {
        Role syncRole = event.getGuild().getRoleById(EMI.getPlugin().getConfig().getLong("sync-role-id"));
        return event.getMember().getRoles().contains(syncRole);
    }
}
