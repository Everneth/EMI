package com.everneth.emi.commands.bot;

import com.everneth.emi.models.EMIPlayer;
import com.everneth.emi.models.enums.ConfigMessage;
import com.everneth.emi.models.enums.DiscordRole;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;

public class ApplyCommand extends SlashCommand {
    public ApplyCommand()
    {
        this.name = "apply";
        this.help = "Fill out an application to request whitelisting.";
    }
    @Override
    public void execute(SlashCommandEvent event)
    {
        EMIPlayer applicant = new EMIPlayer();
        applicant.setDiscordId(event.getUser().getIdLong());

        if(WhitelistAppService.getService().findByDiscordId(event.getUser().getIdLong()) != null) {
            event.reply("You already have an active application!").setEphemeral(true).queue();
            return;
        }
        else if (event.getMember().getRoles().contains(DiscordRole.CITIZEN.get())) {
            event.reply("You're already a member!").setEphemeral(true).queue();
            return;
        }
        boolean messageSent = applicant.sendDiscordMessage("What is your Minecraft IGN? " +
                "**NOTE:** If you enter an invalid IGN, you will be asked again.");

        if (messageSent) {
            event.reply("I have messaged you the first question!").queue();
            WhitelistAppService.getService().addApp(event.getUser().getIdLong(), new WhitelistApp());
        }
        else {
            event.reply(ConfigMessage.DISCORD_MESSAGE_FAILED.get()).setEphemeral(true).queue();
        }
    }
}
