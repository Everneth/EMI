package com.everneth.emi.commands.bot;

import com.everneth.emi.EMI;
import com.everneth.emi.models.WhitelistApp;
import com.everneth.emi.services.WhitelistAppService;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.Role;

public class ApplyCommand extends Command {
    public ApplyCommand()
    {
        this.name = "apply";
        this.guildOnly = false;
    }
    @Override
    public void execute(CommandEvent event)
    {
        long applicationRoleId = EMI.getPlugin().getConfig().getLong("applicant-role-id");
        Role applicantRole = event.getGuild().getRoleById(applicationRoleId);

        if(event.getMember().getRoles().contains(applicantRole) && WhitelistAppService.getService().findByDiscordId(event.getAuthor().getIdLong()) == null) {
            event.replyInDm("Hi there! You have started the application process for whitelisting on the Everneth " +
                    "Minecraft Server. I will be asking you a few questions so that we can get to know you and " +
                    "start your on-boarding to our community. **PLEASE NOTE:** Responses here on out will be for your " +
                    "application. Any errors may result you having to redo your application. **DO NOT CHANGE YOUR MINECRAFT " +
                    "NAME WHILE YOUR APPLICATION IS IN PROGRESS!** Thank you and ***good luck!***");
            event.replyInDm("What is your Minecraft IGN?");
            WhitelistAppService.getService().addApp(event.getAuthor().getIdLong(), new WhitelistApp());
        }
    }
}
