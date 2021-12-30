package com.everneth.emi.commands.bot;

import com.everneth.emi.EMI;
import com.everneth.emi.utils.FileUtils;
import com.jagrosh.jdautilities.command.SlashCommand;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

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

public class HelpClearCommand extends SlashCommand {

    public HelpClearCommand()
    {
        this.name = "help-clear";
        this.help = "Clears the help channel, but must be used inside of that channel.";

        this.defaultEnabled = false;
        this.enabledRoles = new String[]{EMI.getPlugin().getConfig().getString("staff-role-id")};
    }
    @Override
    protected void execute(SlashCommandEvent event)
    {
        // You have permissions, but this is the wrong channel you goon
        if (event.getChannel().getIdLong() != EMI.getPlugin().getConfig().getLong("help-channel-id")) {
            event.reply("Sorry dear, you *can* use this command but **not** in this channel. :heart: ").setEphemeral(true).queue();
            return;
        }

        // Got the role! Lets build a list of messages to clear.
        List<Message> messageList = event.getTextChannel().getIterableHistory().complete();
        if (messageList.size() == 1) {
            // Hold up! Theres only the root message and the command! Delete the command, instruct the user
            event.getChannel().deleteMessageById(event.getChannel().getLatestMessageIdLong()).queue();
            event.reply("There is nothing to clear in #help, hun! Careful with this command!").setEphemeral(true).queue();
            return;
        }

        // Take our messages and build a string, we'll dump that string into a message file
        // and embed the file into a message
        File embedFile = transcribe(messageList);
        Message message = new MessageBuilder().append("Transcript from #help").build();
        Long staffChannelId = EMI.getPlugin().getConfig().getLong("staff-channel-id");
        event.getGuild().getTextChannelById(staffChannelId).sendMessage(message).addFile(embedFile).queue();

        // We've got a history, lets clear out
        for (Message msg : messageList) {
            // Is message the root?
            if (msg.getIdLong() == EMI.getPlugin().getConfig().getLong("root-report-msg"))
                continue;

            try {
                // No, delete
                event.getChannel().deleteMessageById(msg.getIdLong()).queue();

                // We sleep the thread for one second to avoid hitting the rate limit
                Thread.sleep(1000);
            } catch (Exception e) {
                EMI.getPlugin().getLogger().severe(e.getMessage());
            }
        }

        event.reply("Channel cleared, dear. <3").setEphemeral(true).queue();
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
