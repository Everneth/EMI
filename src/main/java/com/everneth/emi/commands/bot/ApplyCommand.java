package com.everneth.emi.commands.bot;

import com.everneth.emi.EMI;
import com.everneth.emi.models.WhitelistApp;
import com.everneth.emi.services.WhitelistAppService;
import com.jagrosh.jdautilities.command.SlashCommand;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

public class ApplyCommand extends SlashCommand {
    public ApplyCommand()
    {
        this.name = "apply";
        this.help = "Fill out an application to request whitelisting.";

        this.guildOnly = false;
    }
    @Override
    public void execute(SlashCommandEvent event)
    {
        long applicationRoleId = EMI.getPlugin().getConfig().getLong("applicant-role-id");
        Role applicantRole = event.getGuild().getRoleById(applicationRoleId);

        if(WhitelistAppService.getService().findByDiscordId(event.getUser().getIdLong()) == null) {
            WhitelistAppService.getService().addApp(event.getUser().getIdLong(), new WhitelistApp());
        }
    }
}
