package com.everneth.emi.commands.bot;

import com.everneth.emi.EMI;
import com.everneth.emi.models.WhitelistApp;
import com.everneth.emi.services.WhitelistAppService;
import com.jagrosh.jdautilities.command.SlashCommand;
import net.dv8tion.jda.api.entities.Role;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;

public class ApplyCommand extends SlashCommand {
    public ApplyCommand()
    {
        this.name = "apply";
        this.help = "Fill out an application to request whitelisting.";

        this.disabledRoles = new String[]{EMI.getPlugin().getConfig().getString("member-role-id")};
    }
    @Override
    public void execute(SlashCommandEvent event)
    {
        long applicationRoleId = EMI.getPlugin().getConfig().getLong("applicant-role-id");
        Role applicantRole = event.getGuild().getRoleById(applicationRoleId);

        if(WhitelistAppService.getService().findByDiscordId(event.getUser().getIdLong()) == null) {
            event.getUser().openPrivateChannel().queue(privateChannel ->
                    privateChannel.sendMessage("What is your Minecraft IGN? **NOTE:** If you enter an invalid IGN, you will be asked again.")
                            .queue(message -> {
                                event.reply("I have messaged you the first question!").queue();
                                WhitelistAppService.getService().addApp(event.getUser().getIdLong(), new WhitelistApp());
                            }, new ErrorHandler()
                                    .handle(ErrorResponse.CANNOT_SEND_TO_USER, error ->
                                        event.reply("Please enable direct messages from Everneth's server members. (Right Click the Everneth Discord Icon > Privacy Settings)")
                                                .setEphemeral(true).queue()))
            );
        }
    }
}
