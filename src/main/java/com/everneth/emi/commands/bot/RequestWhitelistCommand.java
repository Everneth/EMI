package com.everneth.emi.commands.bot;


import com.everneth.emi.models.EMIPlayer;
import com.everneth.emi.services.WhitelistService;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.ArrayList;

public class RequestWhitelistCommand extends SlashCommand {
    public RequestWhitelistCommand() {
        this.name = "whitelist";
        this.help = "Request temporary whitelisting so you may synchronize your discord and minecraft accounts";

        this.options = new ArrayList<>();
        this.options.add(new OptionData(OptionType.STRING, "name", "Your minecraft username").setRequired(true));
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        EMIPlayer player = EMIPlayer.getEmiPlayer(event.getMember().getIdLong());
        if (!player.isEmpty()) {
            event.reply("You are already synced, you do not need to apply for temporary whitelisting.").setEphemeral(true).queue();
            return;
        }
        String username = event.getOption("name").getAsString();
        if (username.isEmpty()) {
            event.reply("You did not provide a name for me to whitelist").setEphemeral(true).queue();
            return;
        }
        boolean isWhitelisted = false;
        if (isWhitelisted || WhitelistService.getService().isWhitelisted(username)) {
            event.reply("That user is already on the whitelist. If this is an error please contact Staff.").setEphemeral(true).queue();
            return;
        }

        WhitelistService.getService().addToWhitelistTemporarily(event.getUser().getIdLong(), username);
        event.reply("Added you to the whitelist for 5 minutes. Please login to `play.everneth.com` and use `/discord sync <discord username>`").queue();
    }
}
