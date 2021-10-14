package com.everneth.emi.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import co.aikar.idb.DB;
import co.aikar.idb.DbRow;
import com.everneth.emi.EMI;
import com.everneth.emi.Utils;
import com.everneth.emi.utils.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Class: AltAccountCommands
 * Author: Riki
 * Purpose: Initiate and revoke a request to (un)whitelist an alternate account (limit one per user)
 */

@CommandAlias("alt|altaccount|alternateaccount")
@Description("Alternate account registration manager")
public class AltAccountCommands extends BaseCommand {

    @Subcommand("add")
    @Description("Register an alternate account for personal use and add it to the whitelist.")
    @Syntax("<name>")
    public void onAddAlt(Player player, String requestedName) {
        if (player.getName().equalsIgnoreCase(requestedName)) {
            player.sendMessage("You cannot add yourself as an alt account");
            return;
        }
        UUID uuid = PlayerUtils.getPlayerUUID(requestedName);
        if (uuid == null) {
            player.sendMessage("Could not find a minecraft user by that name.");
            return;
        }

        DbRow dbRow = PlayerUtils.getPlayerRow(player.getName());
        String dbUsername = dbRow.getString("player_name");
        String altUsername = dbRow.getString("alt_name");

        // the player does not have an alt already whitelisted, we want to check if the requested account is already whitelisted
        if (altUsername == null) {
            DbRow requestedAltRow = PlayerUtils.getPlayerRow(requestedName);

            // if the last query returned null, the account has not been whitelisted already
            if (requestedAltRow == null) {
                EMI.getPlugin().getServer().dispatchCommand(Bukkit.getConsoleSender(), "whitelist add " + requestedName);
                DB.executeUpdateAsync("UPDATE players SET alt_name = ?, alt_uuid = ? WHERE player_uuid = ?",
                        requestedName,
                        uuid.toString(),
                        player.getUniqueId().toString());
                player.sendMessage(Utils.color("&6" + requestedName + " &fhas been whitelisted as your alt."));
            }
            else {
                player.sendMessage(Utils.color("That account has been whitelisted by someone else.\n&cContact staff if you believe this is an error."));
            }
        }
        else if (altUsername.equals(player.getName())) {
            player.sendMessage("You cannot use an alternate account to whitelist alternate accounts.");
        }
        else {
            player.sendMessage(Utils.color("&cYour alternate account, &6" + altUsername + "&c, is already whitelisted."));
        }
    }

    @Subcommand("remove")
    @Description("Remove registered alternate account from the whitelist")
    public void onRemoveAlt(Player player) {
        DbRow playerRow = PlayerUtils.getPlayerRow(player.getUniqueId());
        String playerUsername = playerRow.getString("player_name");
        String altUsername = playerRow.getString("alt_name");

        if (player.getName().equals(playerUsername)) {
            if (altUsername == null) {
                player.sendMessage("You do not have an alternate account whitelisted.");
            }
            else {
                EMI.getPlugin().getServer().dispatchCommand(Bukkit.getConsoleSender(), "whitelist remove " + altUsername);
                DB.executeUpdateAsync("UPDATE players SET alt_name = NULL, alt_uuid = NULL WHERE player_uuid = ?",
                        player.getUniqueId().toString());
                player.sendMessage(Utils.color("Your alt, &6" + altUsername + "&f, has been removed from the whitelist."));
            }

        }
        else {
            player.sendMessage("You must use your main account to remove your alt from the whitelist.");
        }
    }
}
