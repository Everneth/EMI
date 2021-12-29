package com.everneth.emi.commands.bot;

import com.everneth.emi.managers.DiscordSyncManager;
import com.everneth.emi.EMI;
import com.jagrosh.jdautilities.command.SlashCommand;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

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

        if(key == null && !hasSyncRole(event))
        {
            event.reply("No request found. It either expired or the server was restarted.").queue();
        }
        else if (key == null && hasSyncRole(event))
        {
            event.reply("Your account is already synced and running this command did nothing.").queue();
        }
        else if (key != null)
        {
            dsm.removeSyncRequest(key.toString());
            event.reply("Sync request denied successfully.").queue();
        }
    }

    private boolean hasSyncRole(SlashCommandEvent event)
    {
        long guildId = EMI.getPlugin().getConfig().getLong("guild-id");
        long syncRoleId = EMI.getPlugin().getConfig().getLong("sync-role-id");
        Role syncRole = EMI.getJda().getGuildById(guildId).getRoleById(syncRoleId);
        return event.getMember().getRoles().contains(syncRole);
    }
}
