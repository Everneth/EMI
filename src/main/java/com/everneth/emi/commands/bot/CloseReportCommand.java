package com.everneth.emi.commands.bot;

import com.everneth.emi.EMI;
import com.everneth.emi.utils.FileUtils;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.core.entities.Message;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CloseReportCommand extends Command {
    public CloseReportCommand()
    {
        this.name = "close-report";
    }
    @Override
    protected void execute(CommandEvent event)
    {

    }
    private File transcribeToPost(List<Message> messageList)
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
    private File transcribeToFile(List<Message> messageList)
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
    private static<T> List<T> reverseList(List<T> list)
    {
        List<T> reverse = new ArrayList<>(list);
        Collections.reverse(reverse);
        return reverse;
    }
}
