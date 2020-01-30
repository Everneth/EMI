package com.everneth.emi.commands.comm;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import co.aikar.idb.DB;
import com.everneth.emi.Utils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.sql.SQLException;

/**
 *     Class: CommCommand
 *     Author: Redstonehax (@SterlingHeaton)
 *     Purpose: The command structure of /comm and all subcommands
 *     Notes: In future, see about making a CommBaseCommand parent class and move subcommands into their own classes
 */

@CommandAlias("comm")
public class CommCommand extends BaseCommand
{
    @Dependency
    private Plugin plugin;
}
