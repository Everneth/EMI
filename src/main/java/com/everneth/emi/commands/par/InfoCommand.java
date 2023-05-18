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
        String altName = player.getAltName();
        LocalDateTime dateAdded = player.getDateAltAdded();
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        report += Utils.color("&aAlternate Account&f: " + (altName != null ? altName : "N/A") + "\n");
        report += Utils.color("&aAlt Whitelisted&f: " + (altName != null ? dateAdded.format(dateFormat) : "N/A") + "\n\n");

        // if there is no associated discord account we just need to append N/A for username and Id
        long discordId = player.getDiscordId();
        String discordUsername = discordId != 0 ?
                EMI.getJda().getGuildById(EMI.getPlugin().getConfig().getLong("guild-id"))
                        .getMemberById(discordId)
                        .getUser().getName() :
                "N/A";
        report += Utils.color("&9Discord Username&f: " + discordUsername + "\n");
        report += Utils.color("&9Discord Id&f: " + (discordId != 0 ? discordId : "N/A") + "\n");

        report += Utils.color("&c==================================");
        sender.sendMessage(report);
    }
}
