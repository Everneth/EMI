package com.everneth.emi.commands.bot;


import co.aikar.idb.DbRow;
import com.everneth.emi.EMI;
import com.everneth.emi.services.WhitelistService;
import com.everneth.emi.utils.PlayerUtils;
import com.jagrosh.jdautilities.command.SlashCommand;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.ArrayList;
import java.util.List;

public class RequestWhitelistCommand extends SlashCommand {
    public RequestWhitelistCommand() {
        this.name = "whitelist";
        this.help = "Request temporary whitelisting so you may synchronize your discord and minecraft accounts";

        this.options = new ArrayList<>();
        this.options.add(new OptionData(OptionType.STRING, "name", "Your minecraft username").setRequired(true));

        this.defaultEnabled = false;
        this.enabledRoles = new String[]{EMI.getPlugin().getConfig().getString("member-role-id")};
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        DbRow playerRow = PlayerUtils.getPlayerRow(event.getMember().getIdLong());
        if (playerRow != null) {
            event.reply("You are already synced, you do not need to apply for temporary whitelisting.").setEphemeral(true).queue();
            return;
        }
        String username = event.getOption("name").getAsString();
        if (username == null || username.isEmpty()) {
            event.reply("You did not provide a name for me to whitelist").setEphemeral(true).queue();
            return;
        }
        if (PlayerUtils.getPlayerRow(username) != null || WhitelistService.getService().isWhitelisted(username)) {
            event.reply("That user is already on the whitelist. If this is an error please contact Staff.").setEphemeral(true).queue();
            return;
        }

        WhitelistService.getService().addToWhitelistTemporarily(username);
        event.reply("Added you to the whitelist for 5 minutes. Please login to `play.everneth.com` and use `/discord sync <Name#0000>`").queue();
    }
}
