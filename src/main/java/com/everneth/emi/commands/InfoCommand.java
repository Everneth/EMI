package com.everneth.emi.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.idb.DbRow;
import com.everneth.emi.EMI;
import com.everneth.emi.Utils;
import com.everneth.emi.utils.PlayerUtils;
import org.bukkit.command.CommandSender;

import java.sql.Timestamp;
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
        DbRow playerRow = PlayerUtils.getPlayerRow(username);

        if (playerRow == null) {
            sender.sendMessage(Utils.color("&cCould not find a user with the name " + username + "."));
            return;
        }

        String report = Utils.color("&c-= &fReport for " + username + " &c=-\n");
        report += Utils.color("&c==================================\n");
        report += Utils.color("&cMain Account&f: " + playerRow.getString("player_name") + "\n");

        // if there is no associated alternate account, just append N/A
        String altName = playerRow.getString("alt_name");
        if (altName != null) {
            report += Utils.color("&aAlternate Account&f: " + altName + "\n");
            LocalDateTime dateAdded = ((Timestamp) playerRow.get("date_alt_added")).toLocalDateTime();
            report += Utils.color("&aAlt Whitelisted&f: " + dateAdded.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n\n");
        }
        else {
            report += Utils.color("&aAlternate Account&f: N/A\n");
            report += Utils.color("&aAlt Whitelisted&f: N/A\n\n");
        }

        // if there is no associated discord account we just need to append N/A for username and Id
        Long discordId = playerRow.getLong("discord_id");
        if (discordId != null) {
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
