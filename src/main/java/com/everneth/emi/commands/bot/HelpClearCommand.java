package com.everneth.emi.commands.bot;

import com.everneth.emi.EMI;
import com.everneth.emi.utils.FileUtils;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageHistory;
import net.dv8tion.jda.core.entities.Role;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *     Class: HelpCommand
 *     Author: Faceman (@TptMike)
 *     Purpose: The JDA bot !!help-clear command that removes all but the specified message by id
 */

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
                List<Message> messageList = event.getTextChannel().getIterableHistory().complete();
                if(messageList.size() == 2) {
                    // Hold up! Theres only the root message and the command! Delete the command, instruct the user
                    event.getChannel().deleteMessageById(event.getChannel().getLatestMessageIdLong()).queue();
                    event.replyInDm("There is nothing to clear in #help, hun! Careful with this command!");
                }
                else {
                    // Take our messages and build a string, we'll dump that string into a message file
                    // and embed the file into a message
                    File embedFile = transcribe(messageList);
                    Message message = new MessageBuilder().append("Transcript from #help").build();
                    event.getGuild().getTextChannelById(178247194862682112L).sendFile(embedFile, message).queue();

                    // We've got a history, lets clear out
                    for (Message msg : messageList) {
                        // Is message the root?
                        if (msg.getIdLong() != EMI.getPlugin().getConfig().getLong("root-report-msg")) {
                            // No, delete
                            event.getChannel().deleteMessageById(msg.getIdLong()).queue();
                        }
                    }
                    event.replyInDm("Channel cleared, dear. <3 ");
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
    private File transcribe(List<Message> messageList)
    {
        StringBuilder sb = new StringBuilder();

        List<Message> reverse = reverseList(messageList);

        for (Message msg : reverse)
        {
            if(msg.getIdLong() == EMI.getPlugin().getConfig().getLong("root-report-msg"))
            {
                continue;
            }
            String logMsg = msg.getMember().getEffectiveName() + ": " + msg.getContentRaw() + "\n";
            sb.append(logMsg);
        }
        try {
            File fileToEmbed = FileUtils.writeFileFromString("transcript.txt", sb.toString());
            return fileToEmbed;
        }
        catch (IOException e)
        {
            EMI.getPlugin().getLogger().warning(e.getMessage());
        }
        return null;
    }
    public static<T> List<T> reverseList(List<T> list)
    {
        List<T> reverse = new ArrayList<>(list);
        Collections.reverse(reverse);
        return reverse;
    }
}
