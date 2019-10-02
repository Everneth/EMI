package com.everneth.emi.commands.mint;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import co.aikar.idb.DB;
import co.aikar.idb.DbRow;
import com.everneth.emi.MintProjectManager;
import com.everneth.emi.Utils;
import com.everneth.emi.models.*;
import com.everneth.emi.models.mint.*;
import com.everneth.emi.utils.PlayerUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

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
    private String mintProjectTag = "&7[&dMint&5Pro&7] ";

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

    //TODO add tab-complete
    @Subcommand("log material")
    @Syntax("<Project> <Material> <Amount> <TimeWorked> <Description>")
    @CommandPermission("emi.mint.log")
    public void onLogMaterial(Player player, String mintProject, String materialString, int amount, String time, String[] description)
    {
        MintProjectManager manager = MintProjectManager.getMintProjectManager();
        MintProject project = manager.getProject(mintProject);

        if(project == null)
        {
            player.sendMessage(Utils.color(mintProjectTag + "&cProject doesn't exist!"));
            return;
        }

        if(project.getComplete() == 1)
        {
            player.sendMessage(Utils.color(mintProjectTag + "&cLog can't be sent because the project is complete!"));
            return;
        }

        MintMaterial material = project.getMaterial(materialString);

        if(material == null)
        {
            player.sendMessage(Utils.color(mintProjectTag + "&cMaterial isn't associated with this project."));
            return;
        }

        if(material.getComplete() == 1)
        {
            player.sendMessage(Utils.color(mintProjectTag + "&cMaterial has already been completed!"));
            return;
        }

        int timeWorked = encodeTime(time);

        if(timeWorked == -1)
        {
            player.sendMessage(Utils.color(mintProjectTag + "&cInvalid time format, must be HOURS:MINUTES (00:00)."));
            return;
        }

        EMIPlayer logger = PlayerUtils.getEMIPlayer(player.getName());
        MintLogMaterial log = new MintLogMaterial(project.getId(), material.getId(), logger, null, 0, amount, timeWorked, Utils.getCurrentDate(), Utils.buildMessage(description, 0, false));

        project.addMaterialLog(log);
        player.sendMessage(Utils.color(mintProjectTag + "&aMaterial log submitted for validation!"));
    }

    //TODO add tab-complete
    @Subcommand("log task")
    @Syntax("<Project> <TimeWorked> <Description>")
    @CommandPermission("emi.mint.task")
    public void onLogTask(Player player, String mintProject, String time, String[] description)
    {
        MintProjectManager manager = MintProjectManager.getMintProjectManager();
        MintProject project = manager.getProject(mintProject);

        if(project == null)
        {
            player.sendMessage(Utils.color(mintProjectTag + "&cProject doesn't exist!"));
            return;
        }

        if(project.getComplete() == 1)
        {
            player.sendMessage(Utils.color(mintProjectTag + "&cLog can't be sent because the project is complete!"));
            return;
        }

        int timeWorked = encodeTime(time);

        if(timeWorked == -1)
        {
            player.sendMessage(Utils.color(mintProjectTag + "&cInvalid time format, must be HOURS:MINUTES (00:00)."));
            return;
        }

        EMIPlayer logger = PlayerUtils.getEMIPlayer(player.getName());
        MintLogTask log = new MintLogTask(project.getId(), logger, null, 0, timeWorked, Utils.getCurrentDate(), Utils.buildMessage(description, 0, false));

        project.addTaskLog(log);
        player.sendMessage(Utils.color(mintProjectTag + "&aTask log submitted for validation!"));
    }

    //TODO add tab-complete
    @Subcommand("material complete")
    @Syntax("<Project> <Material>")
    @CommandPermission("emi.material.complete")
    public void onMaterialComplete(Player player, String mintProject, String materialString)
    {
        MintProjectManager manager = MintProjectManager.getMintProjectManager();
        MintProject project = manager.getProject(mintProject);

        if(project == null)
        {
            player.sendMessage(Utils.color(mintProjectTag + "&cProject doesn't exist!"));
            return;
        }

        if(project.getComplete() == 1)
        {
            player.sendMessage(Utils.color(mintProjectTag + "&cMaterial can't be completed because the project is complete!"));
            return;
        }

        MintMaterial material = project.getMaterial(materialString);

        if(material == null)
        {
            player.sendMessage(Utils.color(mintProjectTag + "&cMaterial isn't associated with any in this project."));
            return;
        }

        if(material.getComplete() == 1)
        {
            player.sendMessage(Utils.color(mintProjectTag + "&cMaterial has already been completed!"));
            return;
        }
        project.completeMaterial(material.getId());
        player.sendMessage(Utils.color(mintProjectTag + "&aMaterial Completed!"));
    }

    //TODO add tab-complete
    @Subcommand("material add")
    @Syntax("<Project> <Material> <Amount>")
    @CommandPermission("emi.material.add")
    public void onMaterialCreate(Player player, String mintProject, String materialString, int amount)
    {
        MintProjectManager manager = MintProjectManager.getMintProjectManager();
        MintProject project = manager.getProject(mintProject);

        if(project == null)
        {
            player.sendMessage(Utils.color(mintProjectTag + "&cProject doesn't exist!"));
            return;
        }

        if(project.getComplete() == 1)
        {
            player.sendMessage(Utils.color(mintProjectTag + "&cMaterial can't be completed because the project is complete!"));
            return;
        }

        MintMaterial material = new MintMaterial(project.getId(), materialString, amount, 0, 0, 0);

        project.addMaterial(material);
        player.sendMessage(Utils.color(mintProjectTag + "&aMaterial added!"));
    }

    //TODO add tab-complete
    @Subcommand("material delete")
    @Syntax("<Project> <Material>")
    @CommandPermission("emi.material.delete")
    public void onMaterialDelete(Player player, String mintProject, String materialString)
    {
        MintProjectManager manager = MintProjectManager.getMintProjectManager();
        MintProject project = manager.getProject(mintProject);

        if(project == null)
        {
            player.sendMessage(Utils.color(mintProjectTag + "&cProject doesn't exist!"));
            return;
        }

        if(project.getComplete() == 1)
        {
            player.sendMessage(Utils.color(mintProjectTag + "&cMaterial can't be completed because the project is complete!"));
            return;
        }

        MintMaterial material = project.getMaterial(materialString);

        if(material == null)
        {
            player.sendMessage(Utils.color(mintProjectTag + "&cMaterial isn't associated with any in this project."));
            return;
        }

        project.deleteMaterial(material);
        player.sendMessage(Utils.color(mintProjectTag + "&aMaterial deleted!"));
    }

    //TODO add tab-complete
    @Subcommand("material focus")
    @Syntax("<Project> <Material>")
    @CommandPermission("emi.material.focus")
    public void onMaterialFocus(Player player, String mintProject, String materialString)
    {
        MintProjectManager manager = MintProjectManager.getMintProjectManager();
        MintProject project = manager.getProject(mintProject);

        if(project == null)
        {
            player.sendMessage(Utils.color(mintProjectTag + "&cProject doesn't exist!"));
            return;
        }

        if(project.getComplete() == 1)
        {
            player.sendMessage(Utils.color(mintProjectTag + "&cMaterial can't be focused because the project is complete!"));
            return;
        }

        MintMaterial material = project.getMaterial(materialString);

        if(material == null)
        {
            player.sendMessage(Utils.color(mintProjectTag + "&cMaterial isn't associated with any in this project."));
            return;
        }

        if(material.getComplete() == 1)
        {
            player.sendMessage(Utils.color(mintProjectTag + "&cMaterial can't be focused because the material is complete!"));
            return;
        }

        if(material.getFocused() == 1)
        {
            project.unFocusMaterial(material);
            player.sendMessage(Utils.color(mintProjectTag + "&cMaterial has been unfocused!"));
            return;
        }

        MintMaterial formerMaterial = null;

        for(MintMaterial mintMaterial : project.getMaterials().values())
        {
            if(mintMaterial.getFocused() == 1)
            {
                formerMaterial = mintMaterial;
                break;
            }
        }

        project.switchMaterialFocus(material, formerMaterial);
        player.sendMessage(Utils.color(mintProjectTag + "&aMaterial focused!"));
    }

    //TODO add tab-complete
    @Subcommand("material list")
    @Syntax("<Project>")
    @CommandPermission("emi.mint.material.list")
    public void onMaterialList(Player player, String mintProject)
    {
        MintProjectManager manager = MintProjectManager.getMintProjectManager();
        MintProject project = manager.getProject(mintProject);

        if(project == null)
        {
            player.sendMessage(Utils.color(mintProjectTag + "&cProject doesn't exist!"));
            return;
        }

        if(project.getComplete() == 1)
        {
            player.sendMessage(Utils.color(mintProjectTag + "&cMaterial can't be completed because the project is complete!"));
            return;
        }

        if(project.getMaterials().isEmpty())
        {
            player.sendMessage(Utils.color(mintProjectTag + "&cNo materials to list."));
            return;
        }

        player.sendMessage(Utils.color(mintProjectTag + "&aMaterials:"));
        for(MintMaterial material : project.getMaterials().values())
        {
            player.sendMessage(Utils.color("&7[&9*&7] &a" + material.getMaterial() + " &8[&a" + material.getCollected() + "&8/&2" + material.getTotal() + "&8]"));
        }
    }

    //TODO Add tab-complete to mintProject, add complete check
    @Subcommand("project complete")
    @Syntax("<Project>")
    @CommandPermission("emi.mint.project.complete")
    public void onProjectComplete(Player player, String mintProject)
    {
        MintProjectManager manager = MintProjectManager.getMintProjectManager();
        MintProject project = manager.getProject(mintProject);

        if(project == null)
        {
            player.sendMessage(Utils.color(mintProjectTag + "&cProject doesn't exist!"));
            return;
        }

        if(project.getComplete() == 1)
        {
            player.sendMessage(Utils.color(mintProjectTag + "&cProject has already been completed!"));
            return;
        }

        if(!project.getMaterialLogValidation().isEmpty() && !project.getTaskLogValidation().isEmpty())
        {
            player.sendMessage(Utils.color(mintProjectTag + "&cProject can't be completed because not all of the logs have been validated!"));
            return;
        }
        project.completeProject();
        player.sendMessage(Utils.color(mintProjectTag + "&aProject completed!"));
    }

    //TODO add tab-complete
    @Subcommand("project create")
    @Syntax("<Project> <Lead> <Description>")
    @CommandPermission("emi.mint.project.create")
    public void onProjectCreate(Player player, String projectName, String lead, String[] description)
    {
        MintProjectManager manager = MintProjectManager.getMintProjectManager();
        MintProject project = manager.getProject(projectName);

        if(project != null)
        {
            player.sendMessage(Utils.color(mintProjectTag + "&cProject already exist!"));
            return;
        }

        DbRow dbPlayerLead = PlayerUtils.getPlayerRow(lead);

        if(dbPlayerLead == null || dbPlayerLead.isEmpty())
        {
            player.sendMessage(Utils.color(mintProjectTag + "&cUnrecognized player, did you spell the name correctly?"));
            return;
        }

        EMIPlayer playerLead = new EMIPlayer(dbPlayerLead.getString("player_uuid"), dbPlayerLead.getString("player_name"), dbPlayerLead.getInt("player_id"));

        project = new MintProject(playerLead, projectName, Utils.getCurrentDate(), null, 0, 0, Utils.buildMessage(description, 0, false));

        manager.addProject(project);
        player.sendMessage(Utils.color(mintProjectTag + "&aSuccessfully created the project!"));
        this.onProjectJoin(player, projectName);
    }

    //TODO add tab-complete
    @Subcommand("project focus")
    @Syntax("<Project>")
    @CommandPermission("emi.mint.project.focus")
    public void onProjectFocus(Player player, String projectName)
    {
        MintProjectManager manager = MintProjectManager.getMintProjectManager();
        MintProject project = manager.getProject(projectName);

        if(project == null)
        {
            player.sendMessage(Utils.color(mintProjectTag + "&cProject doesn't exist!"));
            return;
        }

        if(project.getComplete() == 1)
        {
            player.sendMessage(Utils.color(mintProjectTag + "&cProject can't be focused because it's complete!"));
            return;
        }

        if(project.getFocused() == 1)
        {
            manager.unFocus(project);
            player.sendMessage(Utils.color(mintProjectTag + "&aProject has been unfocused!"));
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
        player.sendMessage(Utils.color(mintProjectTag + "&aProject has been focused!"));
    }

    // TODO add tab-complete
    @Subcommand("project info")
    @Syntax("<Project>")
    @CommandPermission("emi.mint.info")
    public void onProjectInfo(Player player, String projectName)
    {
        MintProjectManager manager = MintProjectManager.getMintProjectManager();
        MintProject project = manager.getProject(projectName);

        if(project == null)
        {
            player.sendMessage(Utils.color(mintProjectTag + "&cProject doesn't exist!"));
            return;
        }

        String endDate = project.getEndDate();
        String focusedTaskString = "Nothing focused!";
        String focusedMaterialString = "Nothing focused!";
        MintTask focusedTask = project.getFocusedTask();
        MintMaterial focusedMaterial = project.getFocusedMaterial();

        if(endDate == null)
        {
            endDate = "now";
        }

        endDate = decodeDate(endDate);

        if(focusedTask != null)
        {
            focusedTaskString = focusedTask.getTask();
        }

        if(focusedMaterial != null)
        {
            focusedMaterialString = (focusedMaterial.getMaterial() + " &8[&a" + focusedMaterial.getCollected() + "&8/&2" + (focusedMaterial.getTotal()-focusedMaterial.getCollected()) + "&8]");
        }

        ArrayList<String> workers = new ArrayList<>();

        for(EMIPlayer emiPlayer : project.getWorkers())
        {
            workers.add(emiPlayer.getName());
        }

        player.sendMessage(Utils.color(mintProjectTag + "&aInformation for project: &6" + project.getName() + " &aby &6" + project.getLeader().getName() + "\n" +
                "&aDates: &6" + decodeDate(project.getStartDate()) + " &ato &6" + endDate + "\n" +
                "&aDescription: &6" + project.getDescription() + "\n" +
                "&aFocused task: &6" + focusedTaskString + "\n" +
                "&aFocused material: &6" + focusedMaterialString + "\n" +
                "&aWorkers: &6" + Utils.buildMessage(workers.toArray(new String[0]), 0, true)));
    }

    //TODO add tab-complete
    @Subcommand("project join")
    @Syntax("<Project>")
    @CommandPermission("emi.mint.project.join")
    public void onProjectJoin(Player player, String mintProject)
    {
        MintProjectManager manager = MintProjectManager.getMintProjectManager();
        MintProject project = manager.getProject(mintProject);

        if(project == null)
        {
            player.sendMessage(Utils.color(mintProjectTag + "&cProject doesn't exist!"));
            return;
        }

        if(project.getComplete() == 1)
        {
            player.sendMessage(Utils.color(mintProjectTag + "&cCan't join project because it's complete!"));
            return;
        }

        for(EMIPlayer emiPlayer : project.getWorkers())
        {
            if(emiPlayer.getUniqueId().equalsIgnoreCase(player.getUniqueId().toString()))
            {
                player.sendMessage(Utils.color(mintProjectTag + "&cYou're already part of this project."));
                return;
            }
        }

        EMIPlayer emiPlayer = PlayerUtils.getEMIPlayer(player.getName());

        project.addWorker(emiPlayer);
        player.sendMessage(Utils.color(mintProjectTag + "&aSuccessfully joined the project!"));
    }

    @Subcommand("project list")
    @CommandPermission("emi.mint.project.list")
    public void onProjectList(Player player)
    {
        MintProjectManager manager = MintProjectManager.getMintProjectManager();

        if(manager.getProjects().isEmpty())
        {
            player.sendMessage(Utils.color(mintProjectTag + "&cNo projects to list!"));
            return;
        }

        String focusedProject = null;
        ArrayList<String> currentProjects = new ArrayList<>();
        ArrayList<String> completeProjects = new ArrayList<>();

        for(MintProject project : manager.getProjects().values())
        {
            if(project.getFocused() == 1)
            {
                focusedProject = project.getName();
            }
            else if(project.getComplete() == 1)
            {
                completeProjects.add(project.getName());
            }
            else
            {
                currentProjects.add(project.getName());
            }
        }

        if(focusedProject != null)
        {
            player.sendMessage(Utils.color(mintProjectTag + "&aFocused Project: &6" + focusedProject));
        }
        else
        {
            player.sendMessage(Utils.color(mintProjectTag + "&aFocused Project: &cNothing focused."));
        }

        if(currentProjects.isEmpty())
        {
            player.sendMessage(Utils.color(mintProjectTag + "&aCurrent Projects: &cNo current projects."));
        }
        else
        {
            player.sendMessage(Utils.color(mintProjectTag + "&aCurrent Projects: " + Utils.buildMessage(currentProjects.toArray(new String[0]), 0, true)));
        }

        if(completeProjects.isEmpty())
        {
            player.sendMessage(Utils.color(mintProjectTag + "&aComplete Projects: &cNo complete projects."));
        }
        else
        {
            player.sendMessage(Utils.color(mintProjectTag + "&aComplete Projects: " + Utils.buildMessage(completeProjects.toArray(new String[0]), 0, true)));
        }
    }

    @Subcommand("project work")
    @Syntax("<Project>")
    @CommandPermission("emi.mint.project.work")
    public void onWork(Player player, String mintProject)
    {
        MintProjectManager manager = MintProjectManager.getMintProjectManager();
        MintProject project = manager.getProject(mintProject);

        if(project == null)
        {
            player.sendMessage(Utils.color(mintProjectTag + "&cProject doesn't exist!"));
            return;
        }

        if(project.getComplete() == 1)
        {
            player.sendMessage(Utils.color(mintProjectTag + "&cNo work for this project because it's complete!"));
            return;
        }

        player.sendMessage(Utils.color(mintProjectTag + "&aWork needed:"));

        int totalLoops = 0;
        boolean isThereWork = false;

        for(MintMaterial material : project.getMaterials().values())
        {
            if(totalLoops == 3)
            {
                break;
            }

            if(material.getComplete() == 1)
            {
                continue;
            }
            player.sendMessage(Utils.color("&7[&9Material&7] &a" + material.getMaterial() + " &8[&a" + material.getCollected() + "&8/&2" + material.getTotal() + "&8]"));
            totalLoops++;
            isThereWork = true;
        }

        totalLoops = 0;

        for(MintTask task : project.getTasks().values())
        {
            if(totalLoops == 3)
            {
                break;
            }

            if(task.getComplete() == 1)
            {
                continue;
            }
            player.sendMessage(Utils.color("&7[Task&7] &a" + task.getTask()));
            totalLoops++;
            isThereWork = true;
        }

        if(!isThereWork)
        {
            player.sendMessage(Utils.color("&cThere is no work available for this project."));
        }
    }

    @Subcommand("task complete")
    @Syntax("<Project> <taskID>")
    @CommandPermission("emi.task.complete")
    public void onTaskComplete(Player player, String mintProject, long taskID)
    {
        MintProjectManager manager = MintProjectManager.getMintProjectManager();
        MintProject project = manager.getProject(mintProject);

        if(project == null)
        {
            player.sendMessage(Utils.color(mintProjectTag + "&cProject doesn't exist!"));
            return;
        }

        if(project.getComplete() == 1)
        {
            player.sendMessage(Utils.color(mintProjectTag + "&cTask can't be completed because the project is complete!"));
            return;
        }

        if(project.getTasks().get(taskID) == null)
        {
            player.sendMessage(Utils.color(mintProjectTag + "&cTaskID isnt associated with any tasks."));
            return;
        }

        if(project.getTasks().get(taskID).getComplete() == 1)
        {
            player.sendMessage(Utils.color(mintProjectTag + "&cTask has already been completed!"));
            return;
        }
        project.completeTask(taskID);
        player.sendMessage(Utils.color(mintProjectTag + "&aTask completed!"));
    }

    @Subcommand("task add")
    @Syntax("<Project> <Task>")
    @CommandPermission("emi.task.add")
    public void onTaskCreate(Player player, String mintProject, String[] taskParts)
    {
        MintProjectManager manager = MintProjectManager.getMintProjectManager();
        MintProject project = manager.getProject(mintProject);

        if(project == null)
        {
            player.sendMessage(Utils.color(mintProjectTag + "&cProject doesn't exist!"));
            return;
        }

        if(project.getComplete() == 1)
        {
            player.sendMessage(Utils.color(mintProjectTag + "&cTask cant be added becasue the project is complete!"));
            return;
        }

        String taskString = Utils.buildMessage(taskParts, 0, false);
        MintTask task = new MintTask(project.getId(), taskString, 0, 0);

        project.addTask(task);
        player.sendMessage(Utils.color(mintProjectTag + "&aTask added!"));
    }

    @Subcommand("task delete")
    @Syntax("<Project> <TaskID>")
    @CommandPermission("emi.task.delete")
    public void onTaskDelete(Player player, String mintProject, long taskID)
    {
        MintProjectManager manager = MintProjectManager.getMintProjectManager();
        MintProject project = manager.getProject(mintProject);

        if (project == null)
        {
            player.sendMessage(Utils.color(mintProjectTag + "&cProject doesn't exist!"));
            return;
        }

        if(project.getComplete() == 1)
        {
            player.sendMessage(Utils.color(mintProjectTag + "&cTask can't be deleted because the project is complete!"));
            return;
        }

        MintTask task = project.getTasks().get(taskID);

        if(task == null)
        {
            player.sendMessage(Utils.color(mintProjectTag + "&cTaskID isnt associated with any tasks."));
            return;
        }

        project.deleteTask(task);
        player.sendMessage(Utils.color(mintProjectTag + "&aTask deleted!"));
    }

    @Subcommand("task focus")
    @Syntax("<Project> <taskID>")
    @CommandPermission("emi.task.focus")
    public void onTaskFocus(Player player, String mintProject, long taskID)
    {
        MintProjectManager manager = MintProjectManager.getMintProjectManager();
        MintProject project = manager.getProject(mintProject);

        if(project == null)
        {
            player.sendMessage(Utils.color(mintProjectTag + "&cProject doesn't exist!"));
            return;
        }

        if(project.getComplete() == 1)
        {
            player.sendMessage(Utils.color(mintProjectTag + "&cTask can't be focused because the project is complete!"));
            return;
        }

        MintTask task = project.getTasks().get(taskID);

        if(task == null)
        {
            player.sendMessage(Utils.color(mintProjectTag + "&cTaskID isnt associated with any tasks."));
            return;
        }

        if(task.getComplete() == 1)
        {
            player.sendMessage(Utils.color(mintProjectTag + "&cTask can't be focused because the task is complete!"));
            return;
        }

        if(task.getFocused() == 1)
        {
            project.unFocusTask(task);
            player.sendMessage(Utils.color(mintProjectTag + "&cTask has been unfocused!"));
            return;
        }

        MintTask formerTask = null;

        for(MintTask mintTask : project.getTasks().values())
        {
            if(mintTask.getFocused() == 1)
            {
                formerTask = mintTask;
                break;
            }
        }

        project.switchTaskFocus(task, formerTask);
        player.sendMessage(Utils.color(mintProjectTag + "&aTask focused!"));
    }

    @Subcommand("task list")
    @Syntax("<Project>")
    @CommandPermission("emi.task.list")
    public void onTaskList(Player player, String mintProject)
    {
        MintProjectManager manager = MintProjectManager.getMintProjectManager();
        MintProject project = manager.getProject(mintProject);

        if(project == null)
        {
            player.sendMessage(Utils.color(mintProjectTag + "&cProject doesn't exist!"));
            return;
        }

        if(project.getTasks().isEmpty())
        {
            player.sendMessage(Utils.color(mintProjectTag + "&cNo tasks to list."));
            return;
        }

        player.sendMessage(Utils.color(mintProjectTag + "&aTasks:"));
        for(MintTask task : project.getTasks().values())
        {
            if(player.hasPermission("emi.mint.view.taskID"))
            {
                player.sendMessage(Utils.color("&7[&9" + task.getId() + "&7] &a" + task.getTask()));
            }
            else
            {
                player.sendMessage(Utils.color("&7[&9*&7] &a" + task.getTask()));
            }
        }
    }

    @Subcommand("validate")
    @Syntax("<Project>")
    @CommandPermission("emi.validate")
    public void onValidate(Player player, String mintProject)
    {
        MintProjectManager manager = MintProjectManager.getMintProjectManager();
        MintProject project = manager.getProject(mintProject);

        if(project == null)
        {
            player.sendMessage(Utils.color(mintProjectTag + "&cProject doesn't exist!"));
            return;
        }

        if(project.getComplete() == 1)
        {
            player.sendMessage(Utils.color(mintProjectTag + "&cNothing to validate because project is complete!"));
            return;
        }

        if(project.getValidateMaterial().containsKey(player.getUniqueId()) || project.getValidateTask().containsKey(player.getUniqueId()))
        {
            player.sendMessage(Utils.color(mintProjectTag + "&cYou already have a validation log to answer."));
            return;
        }

        if(project.getMaterialLogValidation().isEmpty() && project.getTaskLogValidation().isEmpty())
        {
            player.sendMessage(Utils.color(mintProjectTag + "&cNo logs to validate."));
            return;
        }

        if(!project.getMaterialLogValidation().isEmpty())
        {
            MintLogMaterial materialLog = project.getMaterialLogValidation().values().iterator().next();
            project.getValidateMaterial().put(player.getUniqueId(), materialLog);
            project.getMaterialLogValidation().remove(materialLog.getId());
            player.spigot().sendMessage(buildValidationMessage(mintProject));
            player.sendMessage(Utils.color(mintProjectTag + "&6" + materialLog.getLogger().getName() +
                    " &agathered &6" + materialLog.getMaterialCollected() + " " + project.getMaterials().get(materialLog.getMaterialID()).getMaterial() +
                    " &ain the time of &6" + decodeTime(materialLog.getTimeWorked()) +
                    " &aon the date of: &6" + decodeDate(materialLog.getLogDate()) + "."));
            return;
        }

        if(!project.getTaskLogValidation().isEmpty())
        {
            MintLogTask taskLog = project.getTaskLogValidation().values().iterator().next();
            project.getValidateTask().put(player.getUniqueId(), taskLog);
            project.getTaskLogValidation().remove(taskLog.getId());
            player.spigot().sendMessage(buildValidationMessage(mintProject));
            player.sendMessage(Utils.color(mintProjectTag + "&6" + taskLog.getLogger().getName() +
                    " &aworked on: &6" + taskLog.getDescription() +
                    " &ain the time of &6" + decodeTime(taskLog.getTimeWorked()) +
                    " &aon the date of: &6" + decodeDate(taskLog.getLogDate()) + "."));
        }
    }

    @Subcommand("validateyes")
    @Syntax("<Project>")
    @CommandPermission("emi.validate")
    @Private
    public void onValidateYes(Player player, String mintProject)
    {
        MintProjectManager manager = MintProjectManager.getMintProjectManager();
        MintProject project = manager.getProject(mintProject);
        EMIPlayer validator = PlayerUtils.getEMIPlayer(player.getName());

        if(project.getValidateMaterial().containsKey(player.getUniqueId()))
        {
            project.validateMaterial(project.getValidateMaterial().get(player.getUniqueId()), true, validator);
        }
        else if(project.getValidateTask().containsKey(player.getUniqueId()))
        {
            project.validateTask(project.getValidateTask().get(player.getUniqueId()), true, validator);
        }
        else
        {
            player.sendMessage(Utils.color(mintProjectTag + "&cNo logs have been queued for you."));
            return;
        }

        player.sendMessage(Utils.color(mintProjectTag + "&aLog has been validated"));
    }

    @Subcommand("validateno")
    @Syntax("<Project>")
    @CommandPermission("emi.validate")
    @Private
    public void onValidateNo(Player player, String mintProject)
    {
        MintProjectManager manager = MintProjectManager.getMintProjectManager();
        MintProject project = manager.getProject(mintProject);
        EMIPlayer validator = PlayerUtils.getEMIPlayer(player.getName());

        if(project.getValidateMaterial().containsKey(player.getUniqueId()))
        {
            project.validateMaterial(project.getValidateMaterial().get(player.getUniqueId()), false, validator);
        }
        else if(project.getValidateTask().containsKey(player.getUniqueId()))
        {
            project.validateTask(project.getValidateTask().get(player.getUniqueId()), false, validator);
        }
        else
        {
            player.sendMessage(Utils.color(mintProjectTag + "&cNo logs have been queued for you."));
            return;
        }

        player.sendMessage(Utils.color(mintProjectTag + "&aLog has been rejected"));
    }

    private TextComponent buildValidationMessage(String mintProject)
    {
        TextComponent messageYes = new TextComponent(Utils.color("&aYes"));
        messageYes.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mint validateyes " + mintProject));
        messageYes.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to validate this log").color(ChatColor.DARK_GREEN).create()));

        TextComponent messageNo = new TextComponent(Utils.color("&cNo"));
        messageNo.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mint validateno " + mintProject));
        messageNo.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to deny the log").color(ChatColor.DARK_RED).create()));

        TextComponent messagePart1 = new TextComponent(Utils.color(mintProjectTag + "&6Do you want to validate this log? &7["));
        TextComponent messagePart2 = new TextComponent(Utils.color("&7] &6or &7["));
        TextComponent messagePart3 = new TextComponent(Utils.color("&7]"));

        messagePart1.addExtra(messageYes);
        messagePart1.addExtra(messagePart2);
        messagePart1.addExtra(messageNo);
        messagePart1.addExtra(messagePart3);

        return messagePart1;
    }

    private int encodeTime(String time)
    {
        String[] timeSplit = time.split(":");

        if(timeSplit.length != 2)
        {
            return -1;
        }

        int hours;
        int minutes;

        try
        {
             hours = Integer.parseInt(timeSplit[0]);
             minutes = Integer.parseInt(timeSplit[1]);
        }
        catch(NumberFormatException e)
        {
            Bukkit.getLogger().info("ERROR: MintCommand/processTimeString: " + e.toString());
            return -1;
        }

        if(hours > 99 || minutes > 59)
        {
            return -1;
        }

        return ((hours*60) + minutes);
    }

    private String decodeTime(int time)
    {
        String hours = String.valueOf(time / 60);
        String minutes = String.valueOf(time % 60);

        return (hours + " hours " + minutes + " minutes");
    }

    private String decodeDate(String date)
    {
        String[] dateSplit = date.split(" ");

        return dateSplit[0];
    }
}