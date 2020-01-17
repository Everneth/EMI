package com.everneth.emi.commands;

import co.aikar.commands.annotation.*;
import com.everneth.emi.Utils;
import com.everneth.emi.managers.MotdManager;
import com.everneth.emi.models.EMIPlayer;
import com.everneth.emi.models.Motd;
import com.everneth.emi.utils.PlayerUtils;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 *      Class: MotdCommand
 *      Author: Sterling (@sterlingheaton)
 *      Purpose: The command structure for motd
 */

@CommandAlias("motd")
public class MotdCommand
{
    @Dependency
    private Plugin plugin;

    @Default
    @CommandPermission("emi.motd.motd")
    public void onDefault(Player player)
    {

    }

    @Subcommand("set")
    @CommandPermission("emi.motd.set")
    public void onMotdSet(Player player, String tag, String[] message)
    {
        MotdManager motdManager = MotdManager.getMotdManager();
        EMIPlayer emiPlayer = PlayerUtils.getEMIPlayer(player.getName());
        Motd motd = new Motd(emiPlayer, tag, Utils.buildMessage(message, 0, false));

        motdManager.addMotd(motd);
    }

    @Subcommand("delete")
    @CommandPermission("emi.motd.delete")
    public void onMotdDelete(Player player, String id)
    {

    }
}
