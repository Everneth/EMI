package com.everneth.emi.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import co.aikar.idb.DB;
import co.aikar.idb.DbRow;
import com.everneth.emi.EMI;
import com.everneth.emi.Utils;
import com.everneth.emi.utils.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.*;
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
        if (!PlayerUtils.syncExists(player.getUniqueId())) {
            player.sendMessage(Utils.color("&7You must have a synced discord account to add an alt."));
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

        Date now = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // the player does not have an alt already whitelisted, we want to check if the requested account is already whitelisted
        if (altUsername == null) {
            DbRow requestedAltRow = PlayerUtils.getPlayerRow(requestedName);

            // if the last query returned null, the account has not been whitelisted already
            if (requestedAltRow == null) {
                EMI.getPlugin().getServer().dispatchCommand(Bukkit.getConsoleSender(), "whitelist add " + requestedName);
                DB.executeUpdateAsync("UPDATE players SET alt_name = ?, alt_uuid = ?, date_alt_added = ? WHERE player_uuid = ?",
                        requestedName,
                        uuid.toString(),
                        format.format(now),
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

        // Use our calendar to calculate if 3 days have passed and if user is staff, don't allow if both are not true
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -3);
        if (playerRow.get("date_alt_added") != null
                && !cal.after(playerRow.get("date_alt_added"))
                && !player.hasPermission("emi.par.alt.remove"))
        {
            player.sendMessage(Utils.color("&cYou must wait at least &f3 days &cafter adding an alternate account before removing it."));
            return;
        }

        if (altUsername == null) {
            player.sendMessage("You do not have an alternate account whitelisted.");
        }
        else {
            EMI.getPlugin().getServer().dispatchCommand(Bukkit.getConsoleSender(), "whitelist remove " + altUsername);
            DB.executeUpdateAsync("UPDATE players SET alt_name = NULL, alt_uuid = NULL, date_alt_added = NULL WHERE ? IN (player_uuid,alt_uuid)",
                    player.getUniqueId().toString());
            player.sendMessage(Utils.color("Your alt, &6" + altUsername + "&f, has been removed from the whitelist."));
        }
    }

    @Subcommand("remove")
    @Description("Force remove an alternate account from a player's account.")
    @Syntax("<user>")
    @CommandPermission("emi.par.alt.remove")
    public void onRemoveAlt(CommandSender sender, String username)
    {
        DbRow playerRow = PlayerUtils.getPlayerRow(username);
        if (playerRow == null) {
            sender.sendMessage(Utils.color("&cThere is nobody with that username."));
            return;
        }
        String playerUsername = playerRow.getString("player_name");
        String altUsername = playerRow.getString("alt_name");

        if (altUsername == null) {
            sender.sendMessage("There is no alternate account associated with that name.");
        }
        else {
            EMI.getPlugin().getServer().dispatchCommand(Bukkit.getConsoleSender(), "whitelist remove " + altUsername);
            DB.executeUpdateAsync("UPDATE players SET alt_name = NULL, alt_uuid = NULL, date_alt_added = NULL WHERE ? IN (player_name,alt_name)",
                    username);
            sender.sendMessage(Utils.color("&c" + playerUsername+ "'s alt, &f" + altUsername + "&c, has been removed from the whitelist."));
        }
    }
}
