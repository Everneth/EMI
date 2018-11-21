package com.everneth.emi.commands.mint;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import co.aikar.idb.DB;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.sql.SQLException;
import java.util.UUID;

@CommandAlias("mint")
public class MintCommand extends BaseCommand {

    @Dependency
    private Plugin plugin;

    // TODO: Annotations & Calls
    private String message;
    private int playerId;

    @Subcommand("motd")
    @CommandPermission("emi.mint.motd")
    public void onMotd(CommandSender sender)
    {
        try
        {
            this.message = DB.getFirstColumn("SELECT message FROM motds WHERE ministry_id = 3");
        }
        catch (SQLException e)
        {
            sender.sendMessage(ChatColor.GRAY + "[" + ChatColor.RED + "✘" + ChatColor.GRAY + "] Error 2 - SQL Error - Contact Comms. :(");
        }

        if(this.message != null)
        {
            sender.sendMessage(ChatColor.GRAY + "[" + ChatColor.LIGHT_PURPLE + "MINT" + ChatColor.GRAY + "] " + this.message);
        }
    }


    @Subcommand("motd set")
    @CommandPermission("emi.mint.motd.set")
    public void onSet(CommandSender sender, String motd)
    {
        Player player = (Player)sender;
        // Attempt to get the playerId from players table
        // we're after the int ID not the UUID for speed reasons
        // Ints compare faster than strings!
        try
        {
            this.playerId = DB.getFirstColumn("SELECT player_id FROM players WHERE player_uuid = ?", player.getUniqueId().toString());
        }
        // ERROR 1
        catch (SQLException e)
        {
            this.plugin.getLogger().severe("SQL Exception: SELECT player_id\n onSet() method\n" + e.getMessage());
            sender.sendMessage(ChatColor.GRAY + "[" + ChatColor.RED + "✘" + ChatColor.GRAY + "] Error 1 - SQL Error - Contact Comms. :(");
        }

        if(playerId != 0)
        {
            DB.executeUpdateAsync("UPDATE motds SET message = ?, player_id = ? WHERE ministry_id = 3", motd, playerId);
            sender.sendMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + "✓" + ChatColor.GRAY + "] MINT MOTD updated successfully!");
        }
    }
    @Subcommand("motd clear")
    @CommandPermission("emi.mint.motd.set")
    public void onClear(CommandSender sender)
    {
        Player player = (Player)sender;
        // Attempt to get the playerId from players table
        // we're after the int ID not the UUID for speed reasons
        // Ints compare faster than strings!
        try
        {
            this.playerId = DB.getFirstColumn("SELECT player_id FROM players WHERE player_uuid = ?", player.getUniqueId().toString());
        }
        // ERROR 1
        catch (SQLException e)
        {
            this.plugin.getLogger().severe("SQL Exception: SELECT player_id\n onSet() method\n" + e.getMessage());
            sender.sendMessage(ChatColor.GRAY + "[" + ChatColor.RED + "✘" + ChatColor.GRAY + "] Error 3 - SQL Error - Contact Comms. :(");
        }

        if(playerId != 0)
        {
            DB.executeUpdateAsync("UPDATE motds SET message = ?, player_id = ? WHERE ministry_id = 3", null, playerId);
            sender.sendMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + "✓" + ChatColor.GRAY + "] MINT MOTD updated successfully!");
        }
    }
}