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
            WhitelistAppService.getService().addApp(event.getAuthor().getIdLong(), new WhitelistApp());
        }
    }
}
