package com.everneth.emi.commands.devop;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import co.aikar.idb.DbRow;
import com.everneth.emi.managers.DevopProjectManager;
import com.everneth.emi.Utils;
import com.everneth.emi.models.EMIPlayer;
import com.everneth.emi.models.devop.*;
import com.everneth.emi.utils.PlayerUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;

/**
 *      Class: DevopCommand
 *      Author: Sterling (@sterlingheaton)
 *      Purpose: Command structure to aid in managing public projects
 */

@CommandAlias("devop")
public class DevopCommand extends BaseCommand
{
    @Dependency
    private Plugin plugin;
    private String devopProjectTag = "&7[&dDev&5op&7] ";

    //TODO add tab-complete
    @Subcommand("log material")
    @Syntax("<Project> <Material> <Amount> <TimeWorked> <Description>")
    @CommandPermission("emi.devop.log")
    public void onLogMaterial(Player player, String devopProject, String materialString, int amount, String time, String[] description)
    {
        DevopProjectManager manager = DevopProjectManager.getDevopProjectManager();
        DevopProject project = manager.getProject(devopProject);

        if(project == null)
        {
            player.sendMessage(Utils.color(devopProjectTag + "&cProject doesn't exist!"));
            return;
        }

        if(project.getComplete() == 1)
        {
            player.sendMessage(Utils.color(devopProjectTag + "&cLog can't be sent because the project is complete!"));
            return;
        }

        DevopMaterial material = project.getMaterial(materialString);

        if(material == null)
        {
            player.sendMessage(Utils.color(devopProjectTag + "&cMaterial isn't associated with this project!"));
            return;
        }

        if(material.getComplete() == 1)
        {
            player.sendMessage(Utils.color(devopProjectTag + "&cMaterial has already been completed!"));
            return;
        }

        int timeWorked = encodeTime(time);

        if(timeWorked == -1)
        {
            player.sendMessage(Utils.color(devopProjectTag + "&cInvalid time format, must be HOURS:MINUTES (00:00)!"));
            return;
        }

        EMIPlayer logger = PlayerUtils.getEMIPlayer(player.getName());
        DevopLogMaterial log = new DevopLogMaterial(project.getId(), material.getId(), logger, null, 0, amount, timeWorked, Utils.getCurrentDate(), Utils.buildMessage(description, 0, false));

        project.addMaterialLog(log);
        player.sendMessage(Utils.color(devopProjectTag + "&aMaterial log submitted for validation!"));
    }

    //TODO add tab-complete
    @Subcommand("log task")
    @Syntax("<Project> <TimeWorked> <Description>")
    @CommandPermission("emi.devop.log")
    public void onLogTask(Player player, String devopProject, String time, String[] description)
    {
        DevopProjectManager manager = DevopProjectManager.getDevopProjectManager();
        DevopProject project = manager.getProject(devopProject);

        if(project == null)
        {
            player.sendMessage(Utils.color(devopProjectTag + "&cProject doesn't exist!"));
            return;
        }

        if(project.getComplete() == 1)
        {
            player.sendMessage(Utils.color(devopProjectTag + "&cLog can't be sent because the project is complete!"));
            return;
        }

        int timeWorked = encodeTime(time);

        if(timeWorked == -1)
        {
            player.sendMessage(Utils.color(devopProjectTag + "&cInvalid time format, must be HOURS:MINUTES (00:00)!"));
            return;
        }

        EMIPlayer logger = PlayerUtils.getEMIPlayer(player.getName());
        DevopLogTask log = new DevopLogTask(project.getId(), logger, null, 0, timeWorked, Utils.getCurrentDate(), Utils.buildMessage(description, 0, false));

        project.addTaskLog(log);
        player.sendMessage(Utils.color(devopProjectTag + "&aTask log submitted for validation!"));
    }

    //TODO add tab-complete
    @Subcommand("material complete")
    @Syntax("<Project> <Material>")
    @CommandPermission("emi.devop.material.complete")
    public void onMaterialComplete(Player player, String devopProject, String materialString)
    {
        DevopProjectManager manager = DevopProjectManager.getDevopProjectManager();
        DevopProject project = manager.getProject(devopProject);

        if(project == null)
        {
            player.sendMessage(Utils.color(devopProjectTag + "&cProject doesn't exist!"));
            return;
        }

        if(project.getComplete() == 1)
        {
            player.sendMessage(Utils.color(devopProjectTag + "&cMaterial can't be completed because the project is complete!"));
            return;
        }

        DevopMaterial material = project.getMaterial(materialString);

        if(material == null)
        {
            player.sendMessage(Utils.color(devopProjectTag + "&cMaterial isn't associated with any in this project!"));
            return;
        }

        if(material.getComplete() == 1)
        {
            player.sendMessage(Utils.color(devopProjectTag + "&cMaterial has already been completed!"));
            return;
        }
        project.completeMaterial(material.getId());
        player.sendMessage(Utils.color(devopProjectTag + "&aMaterial Completed!"));
    }

    //TODO add tab-complete
    @Subcommand("material add")
    @Syntax("<Project> <Material> <Amount>")
    @CommandPermission("emi.devop.material.add")
    public void onMaterialCreate(Player player, String devopProject, String materialString, int amount)
    {
        DevopProjectManager manager = DevopProjectManager.getDevopProjectManager();
        DevopProject project = manager.getProject(devopProject);

        if(project == null)
        {
            player.sendMessage(Utils.color(devopProjectTag + "&cProject doesn't exist!"));
            return;
        }

        if(project.getComplete() == 1)
        {
            player.sendMessage(Utils.color(devopProjectTag + "&cMaterial can't be added because the project is complete!"));
            return;
        }

        DevopMaterial material = new DevopMaterial(project.getId(), materialString, amount, 0, 0, 0);

        project.addMaterial(material);
        player.sendMessage(Utils.color(devopProjectTag + "&aMaterial added!"));
    }

    //TODO add tab-complete
    @Subcommand("material delete")
    @Syntax("<Project> <Material>")
    @CommandPermission("emi.devop.material.delete")
    public void onMaterialDelete(Player player, String devopProject, String materialString)
    {
        DevopProjectManager manager = DevopProjectManager.getDevopProjectManager();
        DevopProject project = manager.getProject(devopProject);

        if(project == null)
        {
            player.sendMessage(Utils.color(devopProjectTag + "&cProject doesn't exist!"));
            return;
        }

        if(project.getComplete() == 1)
        {
            player.sendMessage(Utils.color(devopProjectTag + "&cMaterial can't be deleted because the project is complete!"));
            return;
        }

        DevopMaterial material = project.getMaterial(materialString);

        if(material == null)
        {
            player.sendMessage(Utils.color(devopProjectTag + "&cMaterial isn't associated with any in this project!"));
            return;
        }

        project.deleteMaterial(material);
        player.sendMessage(Utils.color(devopProjectTag + "&aMaterial deleted!"));
    }

    //TODO add tab-complete
    @Subcommand("material focus")
    @Syntax("<Project> <Material>")
    @CommandPermission("emi.devop.material.focus")
    public void onMaterialFocus(Player player, String devopProject, String materialString)
    {
        DevopProjectManager manager = DevopProjectManager.getDevopProjectManager();
        DevopProject project = manager.getProject(devopProject);

        if(project == null)
        {
            player.sendMessage(Utils.color(devopProjectTag + "&cProject doesn't exist!"));
            return;
        }

        if(project.getComplete() == 1)
        {
            player.sendMessage(Utils.color(devopProjectTag + "&cMaterial can't be focused because the project is complete!"));
            return;
        }

        DevopMaterial material = project.getMaterial(materialString);

        if(material == null)
        {
            player.sendMessage(Utils.color(devopProjectTag + "&cMaterial isn't associated with any in this project!"));
            return;
        }

        if(material.getComplete() == 1)
        {
            player.sendMessage(Utils.color(devopProjectTag + "&cMaterial can't be focused because the material is complete!"));
            return;
        }

        if(material.getFocused() == 1)
        {
            project.unFocusMaterial(material);
            player.sendMessage(Utils.color(devopProjectTag + "&cMaterial unfocused!"));
            return;
        }

        DevopMaterial formerMaterial = null;

        for(DevopMaterial devopMaterial : project.getMaterials().values())
        {
            if(devopMaterial.getFocused() == 1)
            {
                formerMaterial = devopMaterial;
                break;
            }
        }

        project.switchMaterialFocus(material, formerMaterial);
        player.sendMessage(Utils.color(devopProjectTag + "&aMaterial focused!"));
    }

    //TODO add tab-complete
    @Subcommand("material list")
    @Syntax("<Project>")
    @CommandPermission("emi.devop.material.list")
    public void onMaterialList(Player player, String devopProject)
    {
        DevopProjectManager manager = DevopProjectManager.getDevopProjectManager();
        DevopProject project = manager.getProject(devopProject);

        if(project == null)
        {
            player.sendMessage(Utils.color(devopProjectTag + "&cProject doesn't exist!"));
            return;
        }

        if(project.getMaterials().isEmpty())
        {
            player.sendMessage(Utils.color(devopProjectTag + "&cNo materials to list!"));
            return;
        }

        DevopMaterial focusedMaterial = null;
        ArrayList<DevopMaterial> currentMaterials = new ArrayList<>();
        ArrayList<DevopMaterial> completeMaterials = new ArrayList<>();

        for(DevopMaterial material : project.getMaterials().values())
        {
            if(material.getFocused() == 1)
            {
                focusedMaterial = material;
            }
            else if(material.getComplete() == 1)
            {
                completeMaterials.add(material);
            }
            else
            {
                currentMaterials.add(material);
            }
        }

        player.sendMessage(Utils.color(devopProjectTag + "&aMaterials for &6" + project.getName()));

        player.sendMessage(Utils.color(devopProjectTag + "&aMaterials:"));

        if(focusedMaterial != null)
        {
            player.sendMessage(Utils.color("&aFocused: &6" + focusedMaterial.getMaterial() + "&8[&a" + focusedMaterial.getCollected() + "&8/&2" + focusedMaterial.getTotal() + "&8]"));
        }

        if(!currentMaterials.isEmpty())
        {
            player.sendMessage(Utils.color("&aCurrent: &6" + buildMaterialList(currentMaterials)));
        }

        if(!completeMaterials.isEmpty())
        {
            player.sendMessage(Utils.color("&aComplete: &6" + buildMaterialList(completeMaterials)));
        }
    }

    //TODO Add tab-complete to devopProject, add complete check
    @Subcommand("project complete")
    @Syntax("<Project>")
    @CommandPermission("emi.devop.project.complete")
    public void onProjectComplete(Player player, String devopProject)
    {
        DevopProjectManager manager = DevopProjectManager.getDevopProjectManager();
        DevopProject project = manager.getProject(devopProject);

        if(project == null)
        {
            player.sendMessage(Utils.color(devopProjectTag + "&cProject doesn't exist!"));
            return;
        }

        if(project.getComplete() == 1)
        {
            player.sendMessage(Utils.color(devopProjectTag + "&cProject has already been completed!"));
            return;
        }

        if(!project.getMaterialLogValidation().isEmpty() && !project.getTaskLogValidation().isEmpty())
        {
            player.sendMessage(Utils.color(devopProjectTag + "&cProject can't be completed because not all of the logs have been validated!"));
            return;
        }
        project.completeProject();
        player.sendMessage(Utils.color(devopProjectTag + "&aProject completed!"));
    }

    //TODO add tab-complete
    @Subcommand("project create")
    @Syntax("<Project> <Lead> <Description>")
    @CommandPermission("emi.devop.project.create")
    public void onProjectCreate(Player player, String projectName, String lead, String[] description)
    {
        DevopProjectManager manager = DevopProjectManager.getDevopProjectManager();
        DevopProject project = manager.getProject(projectName);

        if(project != null)
        {
            player.sendMessage(Utils.color(devopProjectTag + "&cProject already exists!"));
            return;
        }

        DbRow dbPlayerLead = PlayerUtils.getPlayerRow(lead);

        if(dbPlayerLead == null || dbPlayerLead.isEmpty())
        {
            player.sendMessage(Utils.color(devopProjectTag + "&cUnrecognized player, did you spell the name correctly?"));
            return;
        }

        EMIPlayer playerLead = new EMIPlayer(dbPlayerLead.getString("player_uuid"), dbPlayerLead.getString("player_name"), dbPlayerLead.getInt("player_id"));

        project = new DevopProject(playerLead, projectName, Utils.getCurrentDate(), null, 0, 0, Utils.buildMessage(description, 0, false));

        manager.addProject(project);
        player.sendMessage(Utils.color(devopProjectTag + "&aSuccessfully created the project!"));
        player.performCommand("devop project join " + project.getName());
    }

    //TODO add tab-complete
    @Subcommand("project focus")
    @Syntax("<Project>")
    @CommandPermission("emi.devop.project.focus")
    public void onProjectFocus(Player player, String projectName)
    {
        DevopProjectManager manager = DevopProjectManager.getDevopProjectManager();
        DevopProject project = manager.getProject(projectName);

        if(project == null)
        {
            player.sendMessage(Utils.color(devopProjectTag + "&cProject doesn't exist!"));
            return;
        }

        if(project.getComplete() == 1)
        {
            player.sendMessage(Utils.color(devopProjectTag + "&cProject can't be focused because it's complete!"));
            return;
        }

        if(project.getFocused() == 1)
        {
            manager.unFocus(project);
            player.sendMessage(Utils.color(devopProjectTag + "&aProject unfocused!"));
            return;
        }

        DevopProject formerProject = null;

        for(DevopProject devopProject : manager.getProjects().values())
        {
            if(devopProject.getFocused() == 1)
            {
                formerProject = devopProject;
                break;
            }
        }

        manager.switchFocus(project, formerProject);
        player.sendMessage(Utils.color(devopProjectTag + "&aProject focused!"));
    }

    // TODO add tab-complete
    @Subcommand("project info")
    @Syntax("<Project>")
    @CommandPermission("emi.devop.project.info")
    public void onProjectInfo(Player player, String projectName)
    {
        DevopProjectManager manager = DevopProjectManager.getDevopProjectManager();
        DevopProject project = manager.getProject(projectName);

        if(project == null)
        {
            player.sendMessage(Utils.color(devopProjectTag + "&cProject doesn't exist!"));
            return;
        }

        String endDate = project.getEndDate();
        String focusedTaskString = "";
        String focusedMaterialString = "";
        DevopTask focusedTask = project.getFocusedTask();
        DevopMaterial focusedMaterial = project.getFocusedMaterial();

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

        player.sendMessage(Utils.color(devopProjectTag + "&aInformation for &6" + project.getName() + " &aby &6" + project.getLeader().getName() + "\n" +
                "&aDates: &6" + decodeDate(project.getStartDate()) + " &ato &6" + endDate + "\n" +
                "&aDescription: &6" + project.getDescription() + "\n" +
                "&aFocused task: &6" + focusedTaskString + "\n" +
                "&aFocused material: &6" + focusedMaterialString + "\n" +
                "&aWorkers: &6" + Utils.buildMessage(workers.toArray(new String[0]), 0, true)));
    }

    //TODO add tab-complete
    @Subcommand("project join")
    @Syntax("<Project>")
    @CommandPermission("emi.devop.project.join")
    public void onProjectJoin(Player player, String devopProject)
    {
        DevopProjectManager manager = DevopProjectManager.getDevopProjectManager();
        DevopProject project = manager.getProject(devopProject);

        if(project == null)
        {
            player.sendMessage(Utils.color(devopProjectTag + "&cProject doesn't exist!"));
            return;
        }

        if(project.getComplete() == 1)
        {
            player.sendMessage(Utils.color(devopProjectTag + "&cCan't join project because it's complete!"));
            return;
        }

        for(EMIPlayer emiPlayer : project.getWorkers())
        {
            if(emiPlayer.getUniqueId().equalsIgnoreCase(player.getUniqueId().toString()))
            {
                player.sendMessage(Utils.color(devopProjectTag + "&cYou're already part of this project!"));
                return;
            }
        }

        EMIPlayer emiPlayer = PlayerUtils.getEMIPlayer(player.getName());

        project.addWorker(emiPlayer);
        player.sendMessage(Utils.color(devopProjectTag + "&aSuccessfully joined the project!"));
    }

    @Subcommand("project list")
    @CommandPermission("emi.devop.project.list")
    public void onProjectList(Player player)
    {
        DevopProjectManager manager = DevopProjectManager.getDevopProjectManager();

        if(manager.getProjects().isEmpty())
        {
            player.sendMessage(Utils.color(devopProjectTag + "&cNo projects to list!"));
            return;
        }

        String focusedProject = null;
        ArrayList<String> currentProjects = new ArrayList<>();
        ArrayList<String> completeProjects = new ArrayList<>();

        for(DevopProject project : manager.getProjects().values())
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

        player.sendMessage(Utils.color(devopProjectTag + "&6Projects:"));

        if(focusedProject != null)
        {
            player.sendMessage(Utils.color("&aFocused: &6" + focusedProject));
        }

        if(!currentProjects.isEmpty())
        {
            player.sendMessage(Utils.color("&aCurrent: &6" + Utils.buildMessage(currentProjects.toArray(new String[0]), 0, true)));
        }

        if(!completeProjects.isEmpty())
        {
            player.sendMessage(Utils.color("&aComplete Projects: &6" + Utils.buildMessage(completeProjects.toArray(new String[0]), 0, true)));
        }
    }

    @Subcommand("project work")
    @Syntax("<Project>")
    @CommandPermission("emi.devop.project.work")
    public void onWork(Player player, String devopProject)
    {
        DevopProjectManager manager = DevopProjectManager.getDevopProjectManager();
        DevopProject project = manager.getProject(devopProject);

        if(project == null)
        {
            player.sendMessage(Utils.color(devopProjectTag + "&cProject doesn't exist!"));
            return;
        }

        if(project.getComplete() == 1)
        {
            player.sendMessage(Utils.color(devopProjectTag + "&cNo work for this project because it's complete!"));
            return;
        }

        player.sendMessage(Utils.color(devopProjectTag + "&aWork needed:"));

        int totalLoops = 0;
        boolean isThereWork = false;

        for(DevopMaterial material : project.getMaterials().values())
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

        for(DevopTask task : project.getTasks().values())
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
            player.sendMessage(Utils.color("&cThere's no work available for this project!"));
        }
    }

    @Subcommand("task complete")
    @Syntax("<Project> <taskID>")
    @CommandPermission("emi.devop.task.complete")
    public void onTaskComplete(Player player, String devopProject, long taskID)
    {
        DevopProjectManager manager = DevopProjectManager.getDevopProjectManager();
        DevopProject project = manager.getProject(devopProject);

        if(project == null)
        {
            player.sendMessage(Utils.color(devopProjectTag + "&cProject doesn't exist!"));
            return;
        }

        if(project.getComplete() == 1)
        {
            player.sendMessage(Utils.color(devopProjectTag + "&cTask can't be completed because the project is complete!"));
            return;
        }

        if(project.getTasks().get(taskID) == null)
        {
            player.sendMessage(Utils.color(devopProjectTag + "&cTaskID isnt associated with any tasks!"));
            return;
        }

        if(project.getTasks().get(taskID).getComplete() == 1)
        {
            player.sendMessage(Utils.color(devopProjectTag + "&cTask has already been completed!"));
            return;
        }
        project.completeTask(taskID);
        player.sendMessage(Utils.color(devopProjectTag + "&aTask completed!"));
    }

    @Subcommand("task add")
    @Syntax("<Project> <Task>")
    @CommandPermission("emi.devop.task.add")
    public void onTaskCreate(Player player, String devopProject, String[] taskParts)
    {
        DevopProjectManager manager = DevopProjectManager.getDevopProjectManager();
        DevopProject project = manager.getProject(devopProject);

        if(project == null)
        {
            player.sendMessage(Utils.color(devopProjectTag + "&cProject doesn't exist!"));
            return;
        }

        if(project.getComplete() == 1)
        {
            player.sendMessage(Utils.color(devopProjectTag + "&cTask cant be added because the project is complete!"));
            return;
        }

        String taskString = Utils.buildMessage(taskParts, 0, false);
        DevopTask task = new DevopTask(project.getId(), taskString, 0, 0);

        project.addTask(task);
        player.sendMessage(Utils.color(devopProjectTag + "&aTask added!"));
    }

    @Subcommand("task delete")
    @Syntax("<Project> <TaskID>")
    @CommandPermission("emi.devop.task.delete")
    public void onTaskDelete(Player player, String devopProject, long taskID)
    {
        DevopProjectManager manager = DevopProjectManager.getDevopProjectManager();
        DevopProject project = manager.getProject(devopProject);

        if (project == null)
        {
            player.sendMessage(Utils.color(devopProjectTag + "&cProject doesn't exist!"));
            return;
        }

        if(project.getComplete() == 1)
        {
            player.sendMessage(Utils.color(devopProjectTag + "&cTask can't be deleted because the project is complete!"));
            return;
        }

        DevopTask task = project.getTasks().get(taskID);

        if(task == null)
        {
            player.sendMessage(Utils.color(devopProjectTag + "&cTaskID isnt associated with any tasks!"));
            return;
        }

        project.deleteTask(task);
        player.sendMessage(Utils.color(devopProjectTag + "&aTask deleted!"));
    }

    @Subcommand("task focus")
    @Syntax("<Project> <taskID>")
    @CommandPermission("emi.devop.task.focus")
    public void onTaskFocus(Player player, String devopProject, long taskID)
    {
        DevopProjectManager manager = DevopProjectManager.getDevopProjectManager();
        DevopProject project = manager.getProject(devopProject);

        if(project == null)
        {
            player.sendMessage(Utils.color(devopProjectTag + "&cProject doesn't exist!"));
            return;
        }

        if(project.getComplete() == 1)
        {
            player.sendMessage(Utils.color(devopProjectTag + "&cTask can't be focused because the project is complete!"));
            return;
        }

        DevopTask task = project.getTasks().get(taskID);

        if(task == null)
        {
            player.sendMessage(Utils.color(devopProjectTag + "&cTaskID isnt associated with any tasks."));
            return;
        }

        if(task.getComplete() == 1)
        {
            player.sendMessage(Utils.color(devopProjectTag + "&cTask can't be focused because the task is complete!"));
            return;
        }

        if(task.getFocused() == 1)
        {
            project.unFocusTask(task);
            player.sendMessage(Utils.color(devopProjectTag + "&cTask unfocused!"));
            return;
        }

        DevopTask formerTask = null;

        for(DevopTask devopTask : project.getTasks().values())
        {
            if(devopTask.getFocused() == 1)
            {
                formerTask = devopTask;
                break;
            }
        }

        project.switchTaskFocus(task, formerTask);
        player.sendMessage(Utils.color(devopProjectTag + "&aTask focused!"));
    }

    @Subcommand("task list")
    @Syntax("<Project>")
    @CommandPermission("emi.devop.task.list")
    public void onTaskList(Player player, String devopProject)
    {
        DevopProjectManager manager = DevopProjectManager.getDevopProjectManager();
        DevopProject project = manager.getProject(devopProject);

        if(project == null)
        {
            player.sendMessage(Utils.color(devopProjectTag + "&cProject doesn't exist!"));
            return;
        }

        if(project.getTasks().isEmpty())
        {
            player.sendMessage(Utils.color(devopProjectTag + "&cNo tasks to list!"));
            return;
        }

        DevopTask focusedTask = null;
        ArrayList<DevopTask> currentTasks = new ArrayList<>();
        ArrayList<DevopTask> completeTasks = new ArrayList<>();

        for(DevopTask task : project.getTasks().values())
        {
            if(task.getFocused() == 1)
            {
                focusedTask = task;
            }
            else if(task.getComplete() == 1)
            {
                completeTasks.add(task);
            }
            else
            {
                currentTasks.add(task);
            }
        }

        player.sendMessage(Utils.color(devopProjectTag + "&aTasks for &6" + project.getName()));

        if(focusedTask != null)
        {
            if(player.hasPermission("emi.devop.view.taskid"))
            {
                player.sendMessage(Utils.color("&aFocused: &7[&9" + focusedTask.getId() + "&7] &6" + focusedTask.getTask()));
            }
            else
            {
                player.sendMessage(Utils.color("&aFocused: &7[&9*&7] &6" + focusedTask.getTask()));
            }
        }

        if(!currentTasks.isEmpty())
        {
            if(player.hasPermission("emi.devop.view.taskid"))
            {
                player.sendMessage(Utils.color("&aCurrent: " + buildTaskList(currentTasks, true)));
            }
            else
            {
                player.sendMessage(Utils.color("&aCurrent: " + buildTaskList(currentTasks, false)));
            }
        }

        if(!completeTasks.isEmpty())
        {
            if(player.hasPermission("emi.devop.view.taskid"))
            {
                player.sendMessage(Utils.color("&aComplete: " + buildTaskList(completeTasks, true)));
            }
            else
            {
                player.sendMessage(Utils.color("&aComplete: " + buildTaskList(completeTasks, false)));
            }
        }
    }

    @Subcommand("validate")
    @Syntax("<Project>")
    @CommandPermission("emi.devop.validate")
    public void onValidate(Player player, String devopProject)
    {
        DevopProjectManager manager = DevopProjectManager.getDevopProjectManager();
        DevopProject project = manager.getProject(devopProject);

        if(project == null)
        {
            player.sendMessage(Utils.color(devopProjectTag + "&cProject doesn't exist!"));
            return;
        }

        if(project.getComplete() == 1)
        {
            player.sendMessage(Utils.color(devopProjectTag + "&cNothing to validate because project is complete!"));
            return;
        }

        if(project.getQueuedValidateMaterial().containsKey(player.getUniqueId()) || project.getQueuedValidateTask().containsKey(player.getUniqueId()))
        {
            if(project.getQueuedValidateMaterial().containsKey(player.getUniqueId()))
            {
                DevopLogMaterial devopLogMaterial = project.getQueuedValidateMaterial().get(player.getUniqueId());
                player.spigot().sendMessage(buildValidationMessage(devopProject));
                player.sendMessage(Utils.color(devopProjectTag + "&6" + devopLogMaterial.getLogger().getName() +
                        " &agathered &6" + devopLogMaterial.getMaterialCollected() + " " + project.getMaterials().get(devopLogMaterial.getMaterialID()).getMaterial() +
                        " &ain the time of &6" + decodeTime(devopLogMaterial.getTimeWorked()) +
                        " &aon the date of: &6" + decodeDate(devopLogMaterial.getLogDate()) + "."));
                return;
            }

            if(project.getQueuedValidateTask().containsKey(player.getUniqueId()))
            {
                DevopLogTask devopLogTask = project.getQueuedValidateTask().get(player.getUniqueId());
                player.spigot().sendMessage(buildValidationMessage(devopProject));
                player.sendMessage(Utils.color(devopProjectTag + "&6" + devopLogTask.getLogger().getName() +
                        " &aworked on: &6" + devopLogTask.getDescription() +
                        " &ain the time of &6" + decodeTime(devopLogTask.getTimeWorked()) +
                        " &aon the date of: &6" + decodeDate(devopLogTask.getLogDate()) + "."));
                return;
            }
        }

        if(project.getMaterialLogValidation().isEmpty() && project.getTaskLogValidation().isEmpty())
        {
            player.sendMessage(Utils.color(devopProjectTag + "&cNo logs to validate!"));
            return;
        }

        if(!project.getMaterialLogValidation().isEmpty())
        {
            DevopLogMaterial materialLog = project.getMaterialLogValidation().values().iterator().next();
            project.getQueuedValidateMaterial().put(player.getUniqueId(), materialLog);
            project.getMaterialLogValidation().remove(materialLog.getId());
            player.spigot().sendMessage(buildValidationMessage(devopProject));
            player.sendMessage(Utils.color(devopProjectTag + "&6" + materialLog.getLogger().getName() +
                    " &agathered &6" + materialLog.getMaterialCollected() + " " + project.getMaterials().get(materialLog.getMaterialID()).getMaterial() +
                    " &ain the time of &6" + decodeTime(materialLog.getTimeWorked()) +
                    " &aon the date of: &6" + decodeDate(materialLog.getLogDate()) + "."));
            return;
        }

        if(!project.getTaskLogValidation().isEmpty())
        {
            DevopLogTask taskLog = project.getTaskLogValidation().values().iterator().next();
            project.getQueuedValidateTask().put(player.getUniqueId(), taskLog);
            project.getTaskLogValidation().remove(taskLog.getId());
            player.spigot().sendMessage(buildValidationMessage(devopProject));
            player.sendMessage(Utils.color(devopProjectTag + "&6" + taskLog.getLogger().getName() +
                    " &aworked on: &6" + taskLog.getDescription() +
                    " &ain the time of &6" + decodeTime(taskLog.getTimeWorked()) +
                    " &aon the date of: &6" + decodeDate(taskLog.getLogDate()) + "."));
        }
    }

    @Subcommand("validateyes")
    @Syntax("<Project>")
    @CommandPermission("emi.devop.validate")
    @Private
    public void onValidateYes(Player player, String devopProject)
    {
        DevopProjectManager manager = DevopProjectManager.getDevopProjectManager();
        DevopProject project = manager.getProject(devopProject);
        EMIPlayer validator = PlayerUtils.getEMIPlayer(player.getName());

        if(project.getQueuedValidateMaterial().containsKey(player.getUniqueId()))
        {
            project.validateMaterial(project.getQueuedValidateMaterial().get(player.getUniqueId()), true, validator);
        }
        else if(project.getQueuedValidateTask().containsKey(player.getUniqueId()))
        {
            project.validateTask(project.getQueuedValidateTask().get(player.getUniqueId()), true, validator);
        }
        else
        {
            player.sendMessage(Utils.color(devopProjectTag + "&cNo logs have been queued for you!"));
            return;
        }

        player.sendMessage(Utils.color(devopProjectTag + "&aLog has been approved!"));
    }

    @Subcommand("validateno")
    @Syntax("<Project>")
    @CommandPermission("emi.devop.validate")
    @Private
    public void onValidateNo(Player player, String devopProject)
    {
        DevopProjectManager manager = DevopProjectManager.getDevopProjectManager();
        DevopProject project = manager.getProject(devopProject);
        EMIPlayer validator = PlayerUtils.getEMIPlayer(player.getName());

        if(project.getQueuedValidateMaterial().containsKey(player.getUniqueId()))
        {
            project.validateMaterial(project.getQueuedValidateMaterial().get(player.getUniqueId()), false, validator);
        }
        else if(project.getQueuedValidateTask().containsKey(player.getUniqueId()))
        {
            project.validateTask(project.getQueuedValidateTask().get(player.getUniqueId()), false, validator);
        }
        else
        {
            player.sendMessage(Utils.color(devopProjectTag + "&cNo logs have been queued for you!"));
            return;
        }

        player.sendMessage(Utils.color(devopProjectTag + "&aLog has been rejected!"));
    }

    private TextComponent buildValidationMessage(String devopProject)
    {
        TextComponent messageYes = new TextComponent(Utils.color("&aYes"));
        messageYes.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/devop validateyes " + devopProject));
        messageYes.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to validate this log").color(ChatColor.DARK_GREEN).create()));

        TextComponent messageNo = new TextComponent(Utils.color("&cNo"));
        messageNo.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/devop validateno " + devopProject));
        messageNo.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to deny the log").color(ChatColor.DARK_RED).create()));

        TextComponent messagePart1 = new TextComponent(Utils.color(devopProjectTag + "&6Do you want to validate this log? &7["));
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
            Bukkit.getLogger().info("ERROR: devopCommand/processTimeString: " + e.toString());
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

    private String buildTaskList(ArrayList<DevopTask> tasks, boolean hasPermission)
    {
        StringBuilder builder = new StringBuilder();

        for(DevopTask task : tasks)
        {
            if(hasPermission)
            {
                builder.append("&7[&9").append(task.getId()).append("&7] &6").append(task.getTask().trim()).append(" ");
            }
            else
            {
                builder.append("&7[&9*&7] &6").append(task.getTask().trim()).append(" ");
            }
        }
        return builder.toString();
    }

    private String buildMaterialList(ArrayList<DevopMaterial> materials)
    {
        StringBuilder builder = new StringBuilder();

        for(DevopMaterial material : materials)
        {
            if(material.getComplete() == 1)
            {
                builder.append("&6").append(material.getMaterial()).append("&8[&2").append(material.getTotal()).append("&8] ");
            }
            else
            {
                builder.append("&6").append(material.getMaterial()).append("&8[&a").append(material.getCollected()).append("&8/&2").append(material.getTotal()).append("&8] ");
            }
        }
        return builder.toString();
    }
}
