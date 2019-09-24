package com.everneth.emi.commands.mint;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import co.aikar.idb.DB;
import co.aikar.idb.DbRow;
import com.everneth.emi.MintProjectManager;
import com.everneth.emi.Utils;
import com.everneth.emi.models.*;
import com.everneth.emi.models.mint.MintMaterial;
import com.everneth.emi.models.mint.MintProject;
import com.everneth.emi.models.mint.MintTask;
import com.everneth.emi.models.mint.MintLogTask;
import com.everneth.emi.utils.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.sql.SQLException;
import java.util.ArrayList;

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

    @Subcommand("log material")
    @Syntax("<Project> <TimeWorked> <Material> <Amount> <Description>")
    @CommandPermission("emi.mint.log")
    public void onLogMaterial(Player player, String mintProject, String time, String material, int amount, String[] description)
    {

    }

    //TODO fix messages
    //TODO add more time checks
    @Subcommand("log task")
    @Syntax("<Project> <TimeWorked> <Description>")
    @CommandPermission("emi.mint.task")
    public void onLogTask(Player player, String mintProject, String time, String[] description)
    {
        MintProjectManager manager = MintProjectManager.getMintProjectManager();
        MintProject project = manager.getProject(mintProject);

        if(project == null)
        {
            player.sendMessage(Utils.color("&cProject doesnt exist!"));
            return;
        }

        String[] splitTime = time.split(":");

        if(splitTime.length != 3)
        {
            player.sendMessage(Utils.color("&cIncorrect syntax for time, must have three times set up: 10:30:35 (HH:MM:SS)"));
            return;
        }

        int[] numberTimes = new int[3];

        try
        {
            numberTimes[0] = Integer.parseInt(splitTime[0]);
            numberTimes[1] = Integer.parseInt(splitTime[1]);
            numberTimes[2] = Integer.parseInt(splitTime[2]);
        }
        catch(NumberFormatException e)
        {
            Bukkit.getLogger().info("ERROR: MintCommand/onLogWork/isNumberCheck: " + e.toString());
            return;
        }

        DbRow dbLoggedBy = PlayerUtils.getPlayerRow(player.getName());
        EMIPlayer loggedBy = new EMIPlayer(dbLoggedBy.getString("player_uuid"), dbLoggedBy.getString("player_name"), dbLoggedBy.getInt("player_id"));
        MintLogTask log = new MintLogTask(project.getId(), loggedBy, null, 0, 0, Utils.getCurrentDate(), Utils.buildMessage(description, 0, false));

        project.addLogWork(log);

        player.sendMessage(Utils.color("&aSuccessfully logged your work done!"));
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
            player.sendMessage(Utils.color(mintProjectTag + "&Material can't be completed because the project is complete!"));
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
            player.sendMessage(Utils.color(mintProjectTag + "&Material can't be completed because the project is complete!"));
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
            player.sendMessage(Utils.color(mintProjectTag + "&Material can't be completed because the project is complete!"));
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
            player.sendMessage(Utils.color(mintProjectTag + "&Material can't be completed because the project is complete!"));
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
            player.sendMessage(Utils.color(mintProjectTag + "&Material can't be completed because the project is complete!"));
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
            player.sendMessage(Utils.color("&7[&9*&7] &a" + material.getMaterial() + "&7(&aNeed &6" + (material.getTotal()-material.getCollected()) + " &amore&7)"));
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

    //TODO add tab-complete for lead, add complete check
    @Subcommand("project create")
    @Syntax("<Project> <PlayerLead> <Description>")
    @CommandPermission("emi.mint.project.create")
    public void onProjectCreate(Player player, String name, String lead, String[] description)
    {
        MintProjectManager manager = MintProjectManager.getMintProjectManager();
        MintProject project = manager.getProject(name);

        if(project != null)
        {
            player.sendMessage(Utils.color(mintProjectTag + "&cProject doesn't exist!"));
            return;
        }

        DbRow dbPlayerLead = PlayerUtils.getPlayerRow(lead);

        if(dbPlayerLead.isEmpty())
        {
            player.sendMessage(Utils.color(mintProjectTag + "&cUnrecognized player, did you spell the name correctly?"));
            return;
        }

        EMIPlayer playerLead = new EMIPlayer(dbPlayerLead.getString("player_uuid"), dbPlayerLead.getString("player_name"), dbPlayerLead.getInt("player_id"));

        project = new MintProject(playerLead, name, Utils.getCurrentDate(), null, 0, 0, Utils.buildMessage(description, 0, false));

        manager.addProject(project);
        player.sendMessage(Utils.color(mintProjectTag + "&aSuccessfully created the project!"));
    }

    //TODO add tab-complete for projects, add complete check
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

    // TODO add tab-complete for projectName and add check for project completion
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

        if(endDate == null)
        {
            endDate = "now";
        }

        ArrayList<String> workers = new ArrayList<>();

        for(EMIPlayer emiPlayer : project.getWorkers())
        {
            workers.add(emiPlayer.getName());
        }

        player.sendMessage(Utils.color(mintProjectTag + "&aInformation for project: &6" + project.getName() + " &aby &6" + project.getLead().getName() + "\n" +
                "&aDates: &6" + project.getStartDate() + " &7- &6" + endDate + "\n" +
                "&aDescription: &6" + project.getDescription() + "\n" +
                "&aFocused task: &6" + project.getFocusedTask().getTask() + "\n" +
                "&aFocused material: &6" + project.getFocusedMaterial().getMaterial() + "&7(&9need &6" + (project.getFocusedMaterial().getTotal() - project.getFocusedMaterial().getCollected()) + "&7) \n" +
                "&aWorkers: &6" + Utils.buildMessage(workers.toArray(new String[0]), 0, true)));
    }

    //TODO add tab-complete for mintProject, add complete check
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
    @CommandPermission("emi.mint.project.work")
    public void onWork(Player player, String mintProject)
    {
        //TODO Compete when finished with task and materials commands
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
            player.sendMessage(Utils.color(mintProjectTag + "&TaskID isnt associated with any tasks."));
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

        if (task == null)
        {
            player.sendMessage(Utils.color(mintProjectTag + "&TaskID isnt associated with any tasks."));
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
            player.sendMessage(Utils.color(mintProjectTag + "&TaskID isnt associated with any tasks."));
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
    @CommandPermission("emi.validate")
    public void onValidate(Player player)
    {

    }
}