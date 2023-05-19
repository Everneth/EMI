package com.everneth.emi.commands.bot;

import com.everneth.emi.EMI;
import com.everneth.emi.managers.ReportManager;
import com.everneth.emi.models.EMIPlayer;
import com.everneth.emi.models.enums.DiscordRole;
import com.everneth.emi.utils.FileUtils;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class CloseReportCommand extends SlashCommand {

    public CloseReportCommand() {
        this.name = "close-report";
        this.help = "Close an open report from a user";
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        // We need to make sure the command sender has the correct authorized roles.
        // Assume no one has roles
        boolean hasRequiredRoles = false;
        // Get the roles from the member
        List<Role> roleList = event.getMember().getRoles();
        ReportManager rm = ReportManager.getReportManager();

        if(event.getChannel().getName().contains("_staff"))
        {
            UUID uuid = rm.findReportByChannelId(event.getChannel().getIdLong());
            EMIPlayer player = EMIPlayer.getEmiPlayer(uuid);
            // Lets check if the user is a staff member
            if (roleList.contains(DiscordRole.STAFF.get())) {
                // Got the role! Lets build a list of messages to clear.
                List<Message> messageList = event.getTextChannel().getIterableHistory().complete();
                // Take our messages and build a string, we'll dump that string into a message file
                // and embed the file into a message
                File embedFile = transcribeToFile(messageList);
                String msg = "Log from " + player.getName() + "'s report has been attached.";

                event.getGuild().getTextChannelById(
                        EMI.getPlugin().getConfig().getLong("staff-channel-id")
                ).sendMessage(msg).addFiles(FileUpload.fromData(embedFile)).queue();

                event.getTextChannel().delete().queue();
                rm.closeReport(uuid);
            } else {
                // You can;t even use this at all, we're not checking any further
                // TODO: Mute member if attempts are made to use command to spam replies
                event.reply("Sorry dear, you must be a member of staff to use this command. :heart: ").setEphemeral(true).queue();
            }
        }
        else if(event.getChannel().getName().contains("_mint"))
        {
            UUID uuid = rm.findReportByChannelId(event.getChannel().getIdLong());
            EMIPlayer player = EMIPlayer.getEmiPlayer(uuid);
            // Lets check if the user has either of the required roles
            hasRequiredRoles = roleList.contains(DiscordRole.STAFF.get()) || roleList.contains(DiscordRole.MINT.get());
            if (hasRequiredRoles) {
                // Got the role! Lets build a list of messages to clear.
                List<Message> messageList = event.getTextChannel().getIterableHistory().complete();
                // Take our messages and build a string, we'll dump that string into a message file
                // and embed the file into a message
                File embedFile = transcribeToFile(messageList);
                String msg = "Log from " + player.getName() + "'s request has been attached.";

                event.getGuild().getTextChannelById(
                        EMI.getPlugin().getConfig().getLong("mint-channel-id")
                ).sendMessage(msg).addFiles(FileUpload.fromData(embedFile)).queue();

                event.getTextChannel().delete().queue();
                rm.closeReport(uuid);
            } else {
                // You can;t even use this at all, we're not checking any further
                // TODO: Mute member if attempts are made to use command to spam replies
                event.reply("Sorry dear, you must be a member of MINT or Staff to use this command. :heart: ").setEphemeral(true).queue();
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
