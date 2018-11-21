package com.everneth.emi.commands.bot;

import com.everneth.emi.EMI;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Role;
import java.util.List;

public class HelpClearCommand extends Command {

    public HelpClearCommand()
    {
        this.name = "help-clear";
    }
    @Override
    protected void execute(CommandEvent event)
    {
        // We need to make sure the command sender has the correct authorized roles.
        // Assume no one has roles
        boolean hasRequiredRoles = false;

        // Get the roles from the member
        List<Role> roleList = event.getMember().getRoles();

        // Lets check them
        for(Role role : roleList)
        {
            if(role.getName().equals("Staff") || role.getName().equals("Ministry Member (Helper)"))
            {
                // Found a required role, no need to find the other, break from the loop
                hasRequiredRoles = true;
                break;
            }
        }
        // We've looped through. Do we have the role?
        if(hasRequiredRoles)
        {
            if(event.getChannel().getIdLong() == EMI.getPlugin().getConfig().getLong("report-channel"))
            {
                // Got the role! Lets build a list of messages to clear.
                List<Message> messageList = event.getChannel().getHistory().getRetrievedHistory();
                if(messageList.size() == 2) {
                    // Hold up! Theres only the root message and the command! Delete the command, instruct the user
                    event.getChannel().deleteMessageById(event.getChannel().getLatestMessageIdLong()).queue();
                    event.replyInDm("There is nothing to clear in #help, hun! Careful with this command!");
                }
                else {
                    // We've got a history, lets clear out
                    for (Message msg : messageList) {
                        // Is message the root?
                        if (msg.getIdLong() != EMI.getPlugin().getConfig().getLong("root-report-msg")) {
                            // No, delete
                            event.getChannel().deleteMessageById(msg.getIdLong()).queue();
                        }
                    }
                }
            }
            else
            {
                // You have permissions, but this is the wrong channel you goon
                event.reply("Sorry dear, you *can* use this command but **not** in this channel. :heart: ");
            }
        }
        else
        {
            // You can;t even use this at all, we're not checking any further
            // TODO: Mute member if attempts are made to use command to spam replies
            event.reply("Sorry dear, you do not have the required role to use this command. :heart: ");
        }
    }
}