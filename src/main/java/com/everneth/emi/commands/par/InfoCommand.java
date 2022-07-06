package com.everneth.emi.commands.par;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import com.everneth.emi.EMI;
import com.everneth.emi.Utils;
import com.everneth.emi.models.EMIPlayer;
import org.bukkit.command.CommandSender;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Class: InfoCommand
 * Author: Riki
 * Purpose: Request some useful information about a user
 */

public class InfoCommand extends BaseCommand {

    @CommandAlias("info")
    @Description("Request some useful information about a user")
    @CommandPermission("emi.par.info")
    public void onInfo(CommandSender sender, String username) {
        EMIPlayer player = EMIPlayer.getEmiPlayer(username);

        if (player.isEmpty()) {
            sender.sendMessage(Utils.color("&cCould not find a user with the name " + username + "."));
            return;
        }

        String report = Utils.color("&c-= &fReport for " + username + " &c=-\n");
        report += Utils.color("&c==================================\n");
        report += Utils.color("&cMain Account&f: " + player.getName() + "\n");

        // if there is no associated alternate account, just append N/A
        if (player.getAltName() != null) {
            report += Utils.color("&aAlternate Account&f: " + player.getAltName() + "\n");
            LocalDateTime dateAdded = player.getDateAltAdded();
            report += Utils.color("&aAlt Whitelisted&f: " + dateAdded.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n\n");
        }
        else {
            report += Utils.color("&aAlternate Account&f: N/A\n");
            report += Utils.color("&aAlt Whitelisted&f: N/A\n\n");
        }

        // if there is no associated discord account we just need to append N/A for username and Id
        long discordId = player.getDiscordId();
        if (discordId != 0) {
            String discordUsername = EMI.getJda()
                    .getGuildById(EMI.getPlugin().getConfig().getLong("guild-id"))
                    .getMemberById(discordId)
                    .getUser().getName();
            report += Utils.color("&9Discord Username&f: " + discordUsername + "\n");
            report += Utils.color("&9Discord Id&f: " + discordId + "\n");
        }
        else {
            report += Utils.color("&9Discord Username&f: N/A\n");
            report += Utils.color("&9Discord Id&f: N/A");
        }
        report += Utils.color("&c==================================");
        sender.sendMessage(report);
    }
}
