package com.everneth.emi.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import co.aikar.idb.DB;
import co.aikar.idb.DbRow;
import com.everneth.emi.EMI;
import com.everneth.emi.Utils;
import com.everneth.emi.models.ConfigMessage;
import com.everneth.emi.models.EMIPlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.*;

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
        if (!EMIPlayer.syncExists(player.getUniqueId())) {
            player.sendMessage(Utils.color("&7You must have a synced discord account to add an alt."));
            return;
        }
        UUID uuid = EMIPlayer.getPlayerUUID(requestedName);
        if (uuid == null) {
            player.sendMessage("Could not find a minecraft user by that name.");
            return;
        }

        EMIPlayer dbRow = EMIPlayer.getEmiPlayer(player.getName());
        String dbUsername = dbRow.getName();
        String altUsername = dbRow.getAltName();

        Date now = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // the player does not have an alt already whitelisted, we want to check if the requested account is already whitelisted
        if (altUsername == null) {
            EMIPlayer requestedAlt = EMIPlayer.getEmiPlayer(requestedName);

            // if the last query returned null, the account has not been whitelisted already
            if (requestedAlt.isEmpty()) {
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
        EMIPlayer playerData = EMIPlayer.getEmiPlayer(player.getUniqueId());
        String playerUsername = playerData.getName();
        String altUsername = playerData.getAltName();

        // Use our calendar to calculate if 3 days have passed and if user is staff, don't allow if both are not true
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -3);
        if (!player.hasPermission("emi.par.alt.remove"))
        {
            if (playerData.getDateAltAdded() != null && cal.before(playerData.getDateAltAdded())) {
                player.sendMessage(Utils.color("&cYou must wait at least &f3 days &cafter adding an alternate account before removing it."));
                return;
            }
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
        EMIPlayer player = EMIPlayer.getEmiPlayer(username);
        if (player.isEmpty()) {
            sender.sendMessage(Utils.color("&c") + ConfigMessage.PLAYER_NOT_FOUND.get());
            return;
        }
        String playerUsername = player.getName();
        String altUsername = player.getAltName();

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
