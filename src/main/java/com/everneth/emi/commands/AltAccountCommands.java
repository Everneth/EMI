package com.everneth.emi.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import co.aikar.idb.DB;
import com.everneth.emi.EMI;
import com.everneth.emi.Utils;
import com.everneth.emi.models.EMIPlayer;
import com.everneth.emi.models.enums.ConfigMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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
        EMIPlayer emiPlayer = EMIPlayer.getEmiPlayer(player.getUniqueId());
        EMIPlayer requestedAlt = EMIPlayer.getEmiPlayer(requestedName);
        if (!requestedAlt.isEmpty()) {
            player.sendMessage("That account is already whitelisted.");
            return;
        }
        else if (!emiPlayer.isSynced()) {
            player.sendMessage(Utils.color("&7You must have a synced discord account to add an alt."));
            return;
        }
        else if (emiPlayer.getAltUuid() != null) {
            player.sendMessage("You already have an alternate account synced.");
            return;
        }
        else {
            emiPlayer.setAltName(requestedName);
        }

        // We've confirmed that the requested name is not whitelisted already, we should confirm it actually exists
        if (emiPlayer.getAltUuid() == null) {
            player.sendMessage(Utils.color("&cCould not find a Minecraft user by that name."));
            return;
        }

        Date now = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // the player does not have an alt already whitelisted, we want to check if the requested account is already whitelisted
        EMI.getPlugin().getServer().dispatchCommand(Bukkit.getConsoleSender(), "whitelist add " + requestedName);
        DB.executeUpdateAsync("UPDATE players SET alt_name = ?, alt_uuid = ?, date_alt_added = ? WHERE player_uuid = ?",
                requestedName,
                emiPlayer.getAltUuid().toString(),
                format.format(now),
                player.getUniqueId().toString());
        player.sendMessage(Utils.color("&6" + requestedName + " &fhas been whitelisted as your alt."));
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
