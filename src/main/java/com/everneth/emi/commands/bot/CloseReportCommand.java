package com.everneth.emi.commands.bot;

import com.everneth.emi.EMI;
import com.everneth.emi.managers.ReportManager;
import com.everneth.emi.models.PostResponse;
import com.everneth.emi.utils.FileUtils;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class CloseReportCommand extends Command {
    public CloseReportCommand() {
        this.name = "close-report";
    }

    @Override
    protected void execute(CommandEvent event) {

        long mintChannelId = EMI.getPlugin().getConfig().getLong("mint-channel-id");
        long staffChannelId = EMI.getPlugin().getConfig().getLong("staff-channel-id");

        // We need to make sure the command sender has the correct authorized roles.
        // Assume no one has roles
        boolean hasRequiredRoles = false;
        // Get the roles from the member
        List<Role> roleList = event.getMember().getRoles();
        String playerName = event.getTextChannel().getName().substring(0, event.getTextChannel().getName().indexOf('_'));
        ReportManager rm = ReportManager.getReportManager();

        if(event.getChannel().getName().contains("_staff"))
        {
            UUID uuid = rm.findReportByChannelId(event.getChannel().getIdLong());
            // Lets check them
            for (Role role : roleList) {
                if (role.getIdLong() == staffChannelId) {
                    // Found a required role, no need to continue, break from the loop
                    hasRequiredRoles = true;
                    break;
                }
            }
            // We've looped through. Do we have the role?
            if (hasRequiredRoles) {
                // Got the role! Lets build a list of messages to clear.
                List<Message> messageList = event.getTextChannel().getIterableHistory().complete();
                // Take our messages and build a string, we'll dump that string into a message file
                // and embed the file into a message
                File embedFile = transcribeToFile(messageList);
                String msg = "Log from " + playerName + "'s report has been attached.";
                Message message = new MessageBuilder().append(msg).build();
                event.getGuild().getTextChannelById(
                        EMI.getPlugin().getConfig().getLong("staff-channel-id")
                ).sendMessage(message).addFile(embedFile).queue();

                event.getTextChannel().delete().queue();
            } else {
                // You can;t even use this at all, we're not checking any further
                // TODO: Mute member if attempts are made to use command to spam replies
                event.reply("Sorry dear, you must be a member of staff to use this command. :heart: ");
            }
        }
        else if(event.getChannel().getName().contains("_mint"))
        {
            UUID uuid = rm.findReportByChannelId(event.getChannel().getIdLong());
            // Lets check them
            for (Role role : roleList) {
                if (role.getIdLong() == mintChannelId) {
                    // Found a required role, no need to continue, break from the loop
                    hasRequiredRoles = true;
                    break;
                }
            }
            // We've looped through. Do we have the role?
            if (hasRequiredRoles) {
                // Got the role! Lets build a list of messages to clear.
                List<Message> messageList = event.getTextChannel().getIterableHistory().complete();
                // Take our messages and build a string, we'll dump that string into a message file
                // and embed the file into a message
                File embedFile = transcribeToFile(messageList);
                String msg = "Log from " + playerName + "'s request has been attached.";
                Message message = new MessageBuilder().append(msg).build();
                event.getGuild().getTextChannelById(
                        EMI.getPlugin().getConfig().getLong("mint-channel-id")
                ).sendMessage(message).addFile(embedFile).queue();

                event.getTextChannel().delete().queue();
            } else {
                // You can;t even use this at all, we're not checking any further
                // TODO: Mute member if attempts are made to use command to spam replies
                event.reply("Sorry dear, you must be a member of MINT or Staff to use this command. :heart: ");
            }
        }

    }

    private File transcribeToFile(List<Message> messageList) {
        StringBuilder sb = new StringBuilder();

        List<Message> reverse = reverseList(messageList);

        for (Message msg : reverse) {
            String logMsg = msg.getMember().getEffectiveName() + ": " + msg.getContentRaw() + "\n";
            sb.append(logMsg);
        }
        try {
            File fileToEmbed = FileUtils.writeFileFromString("transcript.txt", sb.toString());
            return fileToEmbed;
        } catch (IOException e) {
            EMI.getPlugin().getLogger().warning(e.getMessage());
        }
        return null;
    }

    private static <T> List<T> reverseList(List<T> list) {
        List<T> reverse = new ArrayList<>(list);
        Collections.reverse(reverse);
        return reverse;
    }
}
