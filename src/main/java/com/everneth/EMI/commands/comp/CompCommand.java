package com.everneth.EMI.commands.comp;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import co.aikar.idb.DB;
import com.everneth.EMI.EMI;
import org.apache.commons.lang.UnhandledException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.sql.SQLException;

@CommandAlias("comp")
public class CompCommand extends BaseCommand
{

    @Dependency
    private Plugin plugin;

    // TODO: Annotations & Calls
    private String message;
    private int playerId;

    @Subcommand("motd")
    @CommandPermission("emi.comp.motd")
    public void onMotd(CommandSender sender)
    {
        try
        {
            this.message = DB.getFirstColumn("SELECT message FROM motds WHERE ministry_id = 1");
        }
        catch (SQLException e)
        {
            sender.sendMessage(ChatColor.GRAY + "[" + ChatColor.RED + "✘" + ChatColor.GRAY + "] Error 2 - SQL Error - Contact Comms. :(");
        }

        if(this.message != null)
        {
            sender.sendMessage(ChatColor.GRAY + "[" + ChatColor.LIGHT_PURPLE + "COMP" + ChatColor.GRAY + "] " + this.message);
        }
    }


    @Subcommand("motd set")
    @CommandPermission("emi.comp.motd.set")
    public void onSet(CommandSender sender, String motd)
    {
        Player player = (Player)sender;
        // Attempt to get the playerId from players table
        // we're after the int ID not the UUID for speed reasons
        // Ints compare faster than strings!
        try
        {
            playerId = DB.getFirstColumn("SELECT player_id FROM players WHERE player_uuid = ?", player.getUniqueId());
        }
        // ERROR 1
        catch (SQLException e)
        {
            plugin.getLogger().severe("SQL Exception: SELECT player_id\n onSet() method\n" + e.getMessage());
            sender.sendMessage(ChatColor.GRAY + "[" + ChatColor.RED + "✘" + ChatColor.GRAY + "] Error 1 - SQL Error - Contact Comms. :(");
        }

        if(playerId != 0)
        {
            DB.executeUpdateAsync("UPDATE motds SET message = ?, player_id = ? WHERE ministry_id = 1", motd, playerId);
            sender.sendMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + "✓" + ChatColor.GRAY + "] COMP MOTD updated successfully!");
        }
    }
}
