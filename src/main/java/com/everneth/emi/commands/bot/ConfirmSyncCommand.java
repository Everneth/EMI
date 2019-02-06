package com.everneth.emi.commands.bot;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

/**
 *     Class: ConfirmSyncCommand
 *     Author: Faceman (@TptMike)
 *     Purpose: The JDA bot !!comfirmsync command that adds the users discord ID to EMIs player table
 */

public class ConfirmSyncCommand extends Command {
    public ConfirmSyncCommand()
    {
        this.name = "confirmsync";
    }
    @Override
    protected void execute(CommandEvent event)
    {
        // TODO: use syncRequestManager to find any active requests when the confirm command is issued
        // TODO: Make sure this command is given in DM with the bot
    }
}
