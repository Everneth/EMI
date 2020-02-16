package com.everneth.emi.commands.comp;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.plugin.Plugin;

import java.sql.SQLException;

/**
 *     Class: MintCommand
 *     Author: Redstonehax (@SterlingHeaton)
 *     Purpose: The command structure of /comp and all subcommands
 *     Notes: In future, see about making a CompBaseCommand parent class and move subcommands into their own classes
 */

@CommandAlias("comp")
public class CompCommand extends BaseCommand
{
    @Dependency
    private Plugin plugin;
}
