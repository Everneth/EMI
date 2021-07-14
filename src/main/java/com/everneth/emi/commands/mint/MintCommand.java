package com.everneth.emi.commands.mint;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.sql.SQLException;

/**
 *     Class: MintCommand
 *     Author: Faceman (@TptMike)
 *     Purpose: The command structure of /mint and all subcommands
 *     Notes: In future, see about making a MintBaseCommand parent class and move subcommands into their own classes
 */

@CommandAlias("mint")
public class MintCommand extends BaseCommand
{
    @Dependency
    private Plugin plugin;

    @CommandPermission("emi.mint")
    @Subcommand("help")
    public void onHelpCommand(CommandSender sender)
    {

    }


}