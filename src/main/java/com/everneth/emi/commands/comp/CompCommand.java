package com.everneth.emi.commands.comp;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import co.aikar.idb.DB;
import com.everneth.emi.Utils;
import com.everneth.emi.models.EventCreation;
import com.everneth.emi.tournament.TournamentData;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
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
            sender.sendMessage(Utils.color(Utils.chatTag + " &cError! commands-comp-1. Report to Comms!"));
        }

        if(this.message != null)
        {
            sender.sendMessage(Utils.color("&7[&cCOMP&7] " + this.message));
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
            this.playerId = DB.getFirstColumn(
                    "SELECT player_id FROM players WHERE player_uuid = ?",
                    player.getUniqueId().toString()
            );
        }
        // ERROR 1
        catch (SQLException e)
        {
            this.plugin.getLogger().severe("SQL Exception: SELECT player_id\n onSet() method\n" + e.getMessage());
            sender.sendMessage(Utils.color(Utils.chatTag + " &cError! commands-comp-2. Report to Comms!"));
        }

        if(playerId != 0)
        {
            DB.executeUpdateAsync(
                    "UPDATE motds SET message = ?, player_id = ? WHERE ministry_id = 1",
                    motd,
                    playerId
            );
            sender.sendMessage(Utils.color(Utils.chatTag + " &aComp MOTD has been updated!"));
        }
    }
    @Subcommand("motd clear")
    @CommandPermission("emi.comp.motd.set")
    public void onClear(CommandSender sender)
    {
        Player player = (Player)sender;
        // Attempt to get the playerId from players table
        // we're after the int ID not the UUID for speed reasons
        // Ints compare faster than strings!
        try
        {
            this.playerId = DB.getFirstColumn(
                    "SELECT player_id FROM players WHERE player_uuid = ?",
                    player.getUniqueId().toString()
            );
        }
        // ERROR 1
        catch (SQLException e)
        {
            this.plugin.getLogger().severe("SQL Exception: SELECT player_id\n onSet() method\n" + e.getMessage());
            sender.sendMessage(Utils.color(Utils.chatTag + " &cError! commands-comp-3. Report to Comms!"));
        }

        if(playerId != 0)
        {
            DB.executeUpdateAsync("UPDATE motds SET message = ?, player_id = ? WHERE ministry_id = 1",
                    null,
                    playerId
            );
            sender.sendMessage(Utils.color(Utils.chatTag + " &aComp MOTD has been cleared!"));
        }
    }

//    @Subcommand("event create")
//    @CommandPermission("emi.comp.event.create")
//    public void onEventCreate(CommandSender sender, String name, int type, @Optional String date, @Optional int x, @Optional int y, @Optional int z, @Optional String link, @Optional String description)
//    {
//        Player player = (Player) sender;
//
//        try
//        {
//            this.playerId = DB.getFirstColumn("SELECT player_id FROM players WHERE player_uuid = ?", player.getUniqueId().toString());
//        }
//        catch(SQLException e)
//        {
//            this.plugin.getLogger().severe("SQL Exception: SELECT player_id\n onSet() method\n" + e.getMessage());
//            sender.sendMessage(Utils.color(Utils.chatTag + " &cError: commands.comp-4. Report to Comms!"));
//            return;
//        }
//
//        try
//        {
//            DB.executeInsert("INSERT INTO events (event_name, event_type, created_by, event_date, x, y, z, forum_link, description) "
//                    + "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)", name, type, playerId, date, x, y, z, link, description);
//        }
//        catch(SQLException e)
//        {
//            this.plugin.getLogger().severe("SQL Exception: INSERT INTO events\n onEventCreate() method\n" + e.getMessage());
//            sender.sendMessage(Utils.color(Utils.chatTag + " &cError: commaands.comp-5. Report to Comms!"));
//            return;
//        }
//    }

    @Subcommand("event create")
    @CommandPermission("emi.comp.event.create")
    public void onEventCreate(CommandSender sender)
    {
        Player player = (Player) sender;

        try
        {
            this.playerId = DB.getFirstColumn("SELECT player_id FROM players WHERE player_uuid = ?", player.getUniqueId().toString());
        }
        catch(SQLException e)
        {
            this.plugin.getLogger().severe("SQL Exception: SELECT player_id\n onSet() method\n" + e.getMessage());
            sender.sendMessage(Utils.color(Utils.chatTag + " &cError: commands.comp-4. Report to Comms!"));
            return;
        }

        TournamentData.getInstance().getNewEvents().put(player, new EventCreation(playerId));

        player.sendMessage(Utils.color(TournamentData.getInstance().getNewEvents().get(player).getNAMEMESSAGE()));
    }

    @Subcommand("event edit")
    @CommandPermission("emi.comp.event.edit")
    public void onEvnetEdit(CommandSender sender, String name, String edit)
    {

    }

    @Subcommand("event list")
    @CommandPermission("emi.comp.event.list")
    public void onEventList(CommandSender sender)
    {

    }

    @Subcommand("event join")
    @CommandPermission("emi.comp.event.join")
    public void onEventJoin(CommandSender sender, String name)
    {

    }

    @Subcommand("event start")
    @CommandPermission("emi.comp.event.start")
    public void onEventStart(CommandSender sender, String eventName)
    {

    }

    @Subcommand("event end")
    @CommandPermission("emi.comp.event.end")
    public void onEventEnd(CommandSender sender, String eventName)
    {

    }
}
