package com.everneth.emi.commands.mint;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import co.aikar.idb.DB;
import com.everneth.emi.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.sql.SQLException;

/**
 *     Class: MintCommand
 *     Author: Faceman (@TptMike)
 *     Purpose: The command structure of /mint and all subcommands
 *     Notes: In future, see about making a MintBaseCommand parent class and move subcommands into their own classes
 */

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
            sender.sendMessage(Utils.color(Utils.chatTag + " &cError! commands-mint-1. Report to Comms!"));
        }

        if(this.message != null)
        {
            sender.sendMessage(Utils.color("&7[&dMINT&7] " + this.message));
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
            this.playerId = DB.getFirstColumn(
                    "SELECT player_id FROM players WHERE player_uuid = ?",
                    player.getUniqueId().toString()
            );
        }
        // ERROR 1
        catch (SQLException e)
        {
            this.plugin.getLogger().severe("SQL Exception: SELECT player_id\n onSet() method\n" + e.getMessage());
            sender.sendMessage(Utils.color(Utils.chatTag + " &cError! commands-mint-2. Report to Comms!"));
        }

        if(playerId != 0)
        {
            DB.executeUpdateAsync(
                    "UPDATE motds SET message = ?, player_id = ? WHERE ministry_id = 3",
                    motd,
                    playerId
            );
            sender.sendMessage(Utils.color(Utils.chatTag + " &aMint MOTD has been updated!"));
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
            sender.sendMessage(Utils.color(Utils.chatTag + " &cError! commands-mint-3. Report to Comms!"));
        }

        if(playerId != 0)
        {
            DB.executeUpdateAsync("UPDATE motds SET message = ?, player_id = ? WHERE ministry_id = 3", null, playerId);
            sender.sendMessage(Utils.color(Utils.chatTag + " &aMint MOTD has been cleared!"));
        }
    }

    @Subcommand("log material")
    @CommandPermission("emi.mint.log")
    public void onLogMaterial(Player player, String project, String time, String material, int amount, String[] description)
    {

    }

    @Subcommand("log work")
    @CommandPermission("emi.mint.log")
    public void onLogWork(Player player, String project, String time, String[] description)
    {

    }

    @Subcommand("material complete")
    @CommandPermission("emi.material.complete")
    public void onMaterialComplete(Player player, String project, int materialID)
    {

    }

    @Subcommand("material create")
    @CommandPermission("emi.material.create")
    public void onMaterialCreate(Player player, String project, String material, int amount)
    {

    }

    @Subcommand("material delete")
    @CommandPermission("emi.material.delete")
    public void onMaterialDelete(Player player, String project, String materialID)
    {

    }

    @Subcommand("material focus")
    @CommandPermission("emi.material.focus")
    public void onMaterialFocus(Player player, String project, String materialID)
    {

    }

    @Subcommand("project complete")
    @CommandPermission("emi.mint.project.complete")
    public void onProjectComplete(Player player, String project)
    {

    }

    @Subcommand("project create")
    @CommandPermission("emi.mint.project.create")
    public void onProjectAdd(Player player, String projectName, String projectLead, String[] description)
    {

    }

    @Subcommand("project info")
    @CommandPermission("emi.mint.info")
    public void onProjectInfo(Player player, String project)
    {

    }

    @Subcommand("project join")
    @CommandPermission("emi.mint.project.join")
    public void onProjectJoin(Player player, String project)
    {

    }

    @Subcommand("project list")
    @CommandPermission("emi.mint.project.list")
    public void onProjectList(Player player, String list)
    {

    }

    @Subcommand("project work")
    @CommandPermission("emi.mint.project.work")
    public void onWork(Player player, String project)
    {

    }

    @Subcommand("task complete")
    @CommandPermission("emi.task.complete")
    public void onTaskComplete(Player player, String project, int taskID)
    {

    }

    @Subcommand("task create")
    @CommandPermission("emi.task.create")
    public void onTaskCreate(Player player, String project, String[] task)
    {

    }

    @Subcommand("task delete")
    @CommandPermission("emi.task.delete")
    public void onTaskDelete(Player player, String project, String taskID)
    {

    }

    @Subcommand("task focus")
    @CommandPermission("emi.task.focus")
    public void onTaskFocus(Player player, String project, String taskID)
    {

    }

    @Subcommand("validate")
    @CommandPermission("emi.validate")
    public void onValidate(Player player, String project)
    {

    }
}