package com.everneth.emi.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Subcommand;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

@CommandAlias("round")
public class RoundCommand extends BaseCommand
{
    @Dependency
    private Plugin plugin;

    @Subcommand("start")
    @CommandPermission("emi.round.start")
    public void onRoundStart(CommandSender sender)
    {

    }

    @Subcommand("winner")
    @CommandPermission("emi.round.winner")
    public void onRoundWinner(CommandSender sender, String winningEntity)
    {

    }

    @Subcommand("end")
    @CommandPermission("emi.round.end")
    public void onRoundEnd(CommandSender sender)
    {

    }
}
