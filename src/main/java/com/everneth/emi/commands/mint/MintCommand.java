package com.everneth.emi.commands.mint;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import co.aikar.idb.DB;
import co.aikar.idb.DbRow;
import com.everneth.emi.MintProjectManager;
import com.everneth.emi.Utils;
import com.everneth.emi.models.EMIPlayer;
import com.everneth.emi.models.MintProject;
import com.everneth.emi.utils.PlayerUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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

    //TODO fix messages and add validation check
    @Subcommand("project complete")
    @Syntax("<Project Name>")
    @CommandPermission("emi.mint.project.complete")
    public void onProjectComplete(Player player, String mintProject)
    {
        MintProjectManager manager = MintProjectManager.getMintProjectManager();
        MintProject project = manager.getProject(mintProject);

        if(project == null)
        {
            player.sendMessage(Utils.color("&cProject doesnt exist!"));
            return;
        }

        if(project.getComplete() != 0)
        {
            player.sendMessage(Utils.color("&cProject has already been marked for completion"));
            return;
        }
        project.complete();
        player.sendMessage(Utils.color("&aProject has been marked for completion!"));
    }

    //TODO fix messages
    @Subcommand("project create")
    @Syntax("<Project Name> <Player Lead> <Description>")
    @CommandPermission("emi.mint.project.create")
    public void onProjectAdd(Player player, String projectName, String projectLead, String[] description)
    {
        MintProjectManager manager = MintProjectManager.getMintProjectManager();
        MintProject project = manager.getProject(projectName);

        if(project != null)
        {
            player.sendMessage(Utils.color("&cProject already exist!"));
            return;
        }

        DbRow dbPlayerLead = PlayerUtils.getPlayerRow(projectLead);

        if(dbPlayerLead.isEmpty())
        {
            player.sendMessage("&cUnrecognized player, did you spell it correctly?");
            return;
        }

        EMIPlayer playerLead = new EMIPlayer(dbPlayerLead.getString("player_uuid"), dbPlayerLead.getString("player_name"), dbPlayerLead.getInt("player_id"));

        project = new MintProject(playerLead, projectName, Utils.getCurrentDate(), null, 0, 0, Utils.buildMessage(description, 0));

        manager.addProject(project);
        player.sendMessage(Utils.color("&cSuccessfully added the project &6" + project.getName()));
    }

    //TODO fix messages and check for completed projects
    @Subcommand("project focus")
    @CommandPermission("emi.mint.project.focus")
    public void onProjectFocus(Player player, String projectName)
    {
        MintProjectManager manager = MintProjectManager.getMintProjectManager();
        MintProject project = manager.getProject(projectName);

        if(project == null)
        {
            player.sendMessage(Utils.color("&cProject doesnt exist!"));
        }

        if(project.getFocused() == 1)
        {
            player.sendMessage(Utils.color("&cProject is already focused!"));
            return;
        }

        if(project.getComplete() == 1)
        {
            player.sendMessage(Utils.color("&cProject is already complete and therefore cant be set as focused!"));
            return;
        }

        MintProject formerProject = null;
        
        for(MintProject mintProject : manager.getProjects().values())
        {
            if(mintProject.getFocused() == 1)
            {
                formerProject = mintProject;
                break;
            }
        }

        manager.switchFocus(project, formerProject);
        player.sendMessage(Utils.color("&aProject has been marked as focused!"));
    }

    // TODO fix messages and add tasks/materials
    @Subcommand("project info")
    @CommandPermission("emi.mint.info")
    public void onProjectInfo(Player player, String projectName)
    {
        MintProjectManager manager = MintProjectManager.getMintProjectManager();
        MintProject project = manager.getProject(projectName);

        if(project == null)
        {
            player.sendMessage(Utils.color("&cCouldn't find the project &6" + projectName));
            return;
        }

        player.sendMessage(Utils.color("&aInformation for project: &6" + project.getName() + "&7 " + project.getFocused() + " " + project.getComplete() + "\n" +
                "&aProject lead: &6" + project.getLead().getName() + "\n" +
                "&aProject description: &6" + project.getDescription() + "\n" +
                "&aDates: &6" + project.getStartDate() + " &7- &6" + project.getEndDate() + "\n" +
                "&aWorkers: &6" + project.getWorkers().toString()));
    }

    //TODO Fix messages
    @Subcommand("project join")
    @CommandPermission("emi.mint.project.join")
    public void onProjectJoin(Player player, String mintProject)
    {
        MintProjectManager manager = MintProjectManager.getMintProjectManager();
        MintProject project = manager.getProject(mintProject);

        if(project == null)
        {
            player.sendMessage(Utils.color("&cProject &6" + mintProject + " &cdoesn't exist!"));
            return;
        }

        for(EMIPlayer emiPlayer : project.getWorkers())
        {
            if(emiPlayer.getUniqueId().equalsIgnoreCase(player.getUniqueId().toString()))
            {
                player.sendMessage(Utils.color("&cYou have already joined this project."));
                return;
            }
        }

        DbRow playerRow = PlayerUtils.getPlayerRow(player.getName());
        EMIPlayer emiPlayer = new EMIPlayer(playerRow.getString("player_uuid"), playerRow.getString("player_name"), playerRow.getInt("player_id"));

        project.addWorker(emiPlayer);
        player.sendMessage(Utils.color("&aSuccessfully joined the project: &6" + project.getName()));
    }

    //TODO fix messages
    @Subcommand("project list")
    @CommandPermission("emi.mint.project.list")
    public void onProjectList(Player player)
    {
        MintProjectManager manager = MintProjectManager.getMintProjectManager();

        for(MintProject project : manager.getProjects().values())
        {
            player.sendMessage(Utils.color("&a" + project.getName()));
        }
    }

    @Subcommand("project work")
    @CommandPermission("emi.mint.project.work")
    public void onWork(Player player, String project)
    {
        //TODO Compete when finished with task and materials commands
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