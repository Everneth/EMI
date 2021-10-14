package com.everneth.emi.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.everneth.emi.Utils;
import com.everneth.emi.managers.MotdManager;
import com.everneth.emi.models.EMIPlayer;
import com.everneth.emi.models.Motd;
import com.everneth.emi.utils.PlayerUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 *      Class: MotdCommand
 *      Author: Sterling (@sterlingheaton)
 *      Purpose: The command structure for motd
 */

@CommandAlias("motd")
public class MotdCommand extends BaseCommand
{
    @Dependency
    private Plugin plugin;

    @Default
    @CommandPermission("emi.motd.motd")
    public void onDefault(Player player)
    {
        MotdManager motdManager = MotdManager.getMotdManager();

        for(Motd motd : motdManager.getMotds().values())
        {
            player.sendMessage(Utils.color(motd.displayMotd()));
        }
    }

    @Subcommand("set")
    @Syntax("<tag> [message]")
    @CommandPermission("emi.motd.set")
    public void onMotdSet(Player player, String tag, String[] message)
    {
        MotdManager motdManager = MotdManager.getMotdManager();
        Motd motd = new Motd(Utils.sanitizedColor(tag).toLowerCase(), tag, Utils.buildMessage(message, 0, false));

        if(tag.length() >= 30)
        {
            player.sendMessage(Utils.color("&cTag length can't be more than 30!"));
            return;
        }

        if(motdManager.getMotds().containsKey(motd.getSanitizedTag()))
        {
            motdManager.updateMotd(motd);
            player.sendMessage(Utils.color("&aMotd has been updated!"));
            return;
        }

        motdManager.addMotd(motd);
        player.sendMessage(Utils.color("&aMotd has been set!"));
    }

    @Subcommand("delete")
    @Syntax("<tag>")
    @CommandPermission("emi.motd.delete")
    public void onMotdDelete(Player player, String tag)
    {
        MotdManager motdManager = MotdManager.getMotdManager();
        Motd motd = motdManager.getMotds().get(tag.toLowerCase());

        if(motd == null)
        {
            player.sendMessage(Utils.color("&cThat tag isn't accosiated with any motd!"));
            return;
        }

        motdManager.deleteMotd(motd);
        player.sendMessage(Utils.color("&aMotd deleted!"));
    }
}
