package com.everneth.EMI.commands.mint;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.idb.DB;
import com.everneth.EMI.EMI;
import org.apache.commons.lang.UnhandledException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.sql.SQLException;

@CommandPermission("emi.mint.motd")
public class MotdCommand extends BaseCommand {
    // TODO: Annotations & Calls
    private int playerId;
    private static EMI plugin;

    @Subcommand("set")
    @CommandPermission("emi.mint.motd.set")
    public void onSet(Player player, String motd, @Optional boolean isPublic)
    {
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
            player.sendMessage(ChatColor.GRAY + "[" + ChatColor.RED + "✘" + ChatColor.GRAY + "] Error 1 - SQL Error - Contact Comms. :(");
        }

        // By default isPublic is false
        if(playerId != 0)
        {
            if(isPublic)
            {
                DB.executeUpdateAsync("UPDATE motds SET message = ?, player_id = ?, public = ? WHERE ministry_id = 3", motd, playerId, 1);
                player.sendMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + "✓" + ChatColor.GRAY + "] INT MOTD updated successfully!");
            }
            else
            {
                DB.executeUpdateAsync("UPDATE motds SET message = ?, player_id = ?, WHERE ministry_id = 3", motd, playerId);
                player.sendMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + "✓" + ChatColor.GRAY + "] INT MOTD updated successfully!");
            }

        }
    }
}
