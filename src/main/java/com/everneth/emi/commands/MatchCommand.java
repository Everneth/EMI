package com.everneth.emi.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Subcommand;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

@CommandAlias("match")
public class MatchCommand extends BaseCommand
{
    @Dependency
    private Plugin plugin;

    @Subcommand("setup")
    @CommandPermission("emi.match.setup")
    public void onMatchSetup(CommandSender sender)
    {

    }

    @Subcommand("end")
    @CommandPermission("emi.match.end")
    public void onMatchEnd(CommandSender sender)
    {

    }
}
