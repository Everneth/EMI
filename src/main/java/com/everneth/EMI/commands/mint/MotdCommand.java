package com.everneth.EMI.commands.mint;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Subcommand;
import org.bukkit.entity.Player;

@CommandPermission("emi.mint.motd")
public class MotdCommand extends BaseCommand {
    // TODO: Annotations & Calls

    @Subcommand("set")
    @CommandPermission("emi.mint.motd.set")
    public static void onSet(Player player, String motd)
    {
        // TODO: Database table to query
    }
}
