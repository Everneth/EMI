package com.everneth.emi.commands.mint;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import co.aikar.idb.DB;
import co.aikar.idb.DbRow;
import com.everneth.emi.MintProjectManager;
import com.everneth.emi.Utils;
import com.everneth.emi.models.*;
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
    public void onLogMaterial(Player player, String mintProject, String time, String material, int amount, String[] description)
    {

    }

    //TODO fix messages
    //TODO add more time checks
    @Subcommand("log work")
    @Syntax("<ProjectName> <TimeWorked> <Description>")
    @CommandPermission("emi.mint.log")
    public void onLogWork(Player player, String mintProject, String time, String[] description)
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
        MintWorkLog log = new MintWorkLog(loggedBy, null, 0, time, Utils.getCurrentDate(), Utils.buildMessage(description, 0));

        project.addLogWork(log);

        player.sendMessage(Utils.color("&aSuccessfully logged your work done!"));
    }

    @Subcommand("material complete")
    @Syntax("<ProjectName> <MaterialID>")
    @CommandPermission("emi.material.complete")
    public void onMaterialComplete(Player player, String mintProject, long materialID)
    {
        MintProjectManager manager = MintProjectManager.getMintProjectManager();
        MintProject project = manager.getProject(mintProject);

        if(project == null)
        {
            player.sendMessage(Utils.color("&cProject doesnt exist!"));
            return;
        }

        MintMaterialRequirement material = project.getMaterialRequirements().get(materialID);

        if(material == null)
        {
            player.sendMessage(Utils.color("&cMaterialID isnt associated with any material in this project."));
            return;
        }

        if(material.getComplete() == 1)
        {
            player.sendMessage(Utils.color("&cMaterial is already set to focused!"));
            return;
        }
        project.completeMaterial(materialID);
        player.sendMessage(Utils.color("&aSuccessfully marked the material as complete!"));
    }

    @Subcommand("material add")
    @Syntax("<ProjectName> <MaterialName> <Amount>")
    @CommandPermission("emi.material.add")
    public void onMaterialCreate(Player player, String mintProject, String materialString, int amount)
    {
        MintProjectManager manager = MintProjectManager.getMintProjectManager();
        MintProject project = manager.getProject(mintProject);

        if (project == null)
        {
            player.sendMessage(Utils.color("&cProject doesnt exist!"));
            return;
        }

        MintMaterialRequirement material = new MintMaterialRequirement(materialString, amount, 0, 0);

        project.addMaterial(material);
        player.sendMessage(Utils.color("&aSuccessfully added material to project!"));
    }

    @Subcommand("material delete")
    @Syntax("<ProjectName> <MaterialID>")
    @CommandPermission("emi.material.delete")
    public void onMaterialDelete(Player player, String mintProject, long materialID)
    {
        MintProjectManager manager = MintProjectManager.getMintProjectManager();
        MintProject project = manager.getProject(mintProject);

        if (project == null)
        {
            player.sendMessage(Utils.color("&cProject doesnt exist!"));
            return;
        }

        MintMaterialRequirement material = project.getMaterialRequirements().get(materialID);

        if(material == null)
        {
            player.sendMessage(Utils.color("&cMaterial doesnt exist"));
            return;
        }

        project.deleteMaterial(material);
        player.sendMessage(Utils.color("&aSuccessfully deleted material!"));
    }

    @Subcommand("material focus")
    @Syntax("<ProjectName> <MaterialID>")
    @CommandPermission("emi.material.focus")
    public void onMaterialFocus(Player player, String mintProject, long materialID)
    {
        MintProjectManager manager = MintProjectManager.getMintProjectManager();
        MintProject project = manager.getProject(mintProject);

        if (project == null)
        {
            player.sendMessage(Utils.color("&cProject doesnt exist!"));
            return;
        }

        MintMaterialRequirement material = project.getMaterialRequirements().get(materialID);

        if(material == null)
        {
            player.sendMessage(Utils.color("&cMaterial doesnt exist"));
            return;
        }

        if(material.getFocused() == 1)
        {
            player.sendMessage(Utils.color("&cMaterial is already set to focused!"));
            return;
        }

        if(material.getComplete() == 1)
        {
            player.sendMessage(Utils.color("&cMaterial cant be set to focus because it's complete"));
            return;
        }

        MintMaterialRequirement formerMaterial = null;

        for(MintMaterialRequirement mintMaterialRequirement : project.getMaterialRequirements().values())
        {
            if(mintMaterialRequirement.getFocused() == 1)
            {
                formerMaterial = mintMaterialRequirement;
                break;
            }
        }

        project.switchMaterialFocus(material, formerMaterial);
        player.sendMessage(Utils.color("&aSuccessfully set material to focused!"));
    }

    @Subcommand("material list")
    @Syntax("<ProjectName>")
    @CommandPermission("emi.mint.material.list")
    public void onMaterialList(Player player, String mintProject)
    {
        MintProjectManager manager = MintProjectManager.getMintProjectManager();
        MintProject project = manager.getProject(mintProject);

        if(project == null)
        {
            player.sendMessage(Utils.color("&cProject doesnt exist!"));
            return;
        }

        if(project.getMaterialRequirements().isEmpty())
        {
            player.sendMessage("&cProject doesnt have any tasks to list!");
            return;
        }

        player.sendMessage(Utils.color("&aMaterials for project: &6" + project.getName()));
        for(MintMaterialRequirement material : project.getMaterialRequirements().values())
        {
            player.sendMessage(Utils.color("&8[&9" + material.getMaterialID() + "&8] &a" + material.getMaterial() + " &e" + material.getAmount()));
        }
    }

    //TODO fix messages and add validation check
    @Subcommand("project complete")
    @Syntax("<ProjectName>")
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
        project.completeProject();
        player.sendMessage(Utils.color("&aProject has been marked for completion!"));
    }

    //TODO fix messages
    @Subcommand("project create")
    @Syntax("<ProjectName> <PlayerLead> <Description>")
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
    @Syntax("<ProjectName>")
    @CommandPermission("emi.mint.project.focus")
    public void onProjectFocus(Player player, String projectName)
    {
        MintProjectManager manager = MintProjectManager.getMintProjectManager();
        MintProject project = manager.getProject(projectName);

        if(project == null)
        {
            player.sendMessage(Utils.color("&cProject doesnt exist!"));
            return;
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
    @Syntax("<ProjectName>")
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

        String endDate = project.getEndDate();

        if(endDate == null)
        {
            endDate = "Current";
        }

        player.sendMessage(Utils.color("&aInformation for project: &6" + project.getName() + "&7 " + project.getFocused() + " " + project.getComplete() + "\n" +
                "&aProject lead: &6" + project.getLead().getName() + "\n" +
                "&aProject description: &6" + project.getDescription() + "\n" +
                "&aFocused Task: &6" + project.getFocusedTask().getTask() + "\n" +
                "&aFocused Material: &6" + project.getFocusedMaterial().getMaterial() + "&7-&e" + project.getFocusedMaterial().getAmount() + "\n" +
                "&aDates: &6" + project.getStartDate() + " &7- &6" + endDate + "\n" +
                "&aWorkers: &6" + project.getWorkers().toString()));
    }

    //TODO Fix messages
    @Subcommand("project join")
    @Syntax("<ProjectName>")
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
    //TODO add null check to info message
    @Subcommand("project list")
    @CommandPermission("emi.mint.project.list")
    public void onProjectList(Player player)
    {
        MintProjectManager manager = MintProjectManager.getMintProjectManager();

        if(manager.getProjects().isEmpty())
        {
            player.sendMessage(Utils.color("&cNo projects have been added!"));
            return;
        }

        ArrayList<String> currentProjects = new ArrayList<>();
        String focusedProject = null;
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
        player.sendMessage(Utils.color("&aFocused Project: &6 " + focusedProject + "\n" +
                "&aOnGoing Projects: &6" + currentProjects.toString() + "\n" +
                "&aComplete Projects: &6" + completeProjects.toString()));
    }

    @Subcommand("project work")
    @CommandPermission("emi.mint.project.work")
    public void onWork(Player player, String mintProject)
    {
        //TODO Compete when finished with task and materials commands
    }

    @Subcommand("task complete")
    @Syntax("<ProjectName> <taskID>")
    @CommandPermission("emi.task.complete")
    public void onTaskComplete(Player player, String mintProject, long taskID)
    {
        MintProjectManager manager = MintProjectManager.getMintProjectManager();
        MintProject project = manager.getProject(mintProject);

        if(project == null)
        {
            player.sendMessage(Utils.color("&cProject doesnt exist!"));
            return;
        }

        if(project.getTaskRequirements().get(taskID) == null)
        {
            player.sendMessage(Utils.color("&cTask in project &6" + project.getName() + " &cdoesnt exist!"));
            return;
        }

        if(project.getTaskRequirements().get(taskID).getComplete() == 1)
        {
            player.sendMessage(Utils.color("&cTask in project &6" + project.getName() + " &cis already complete!"));
            return;
        }
        project.completeTask(taskID);
        player.sendMessage(Utils.color("&aSuccessfully marked the task as complete!"));
    }

    @Subcommand("task add")
    @Syntax("<ProjectName> <Task>")
    @CommandPermission("emi.task.add")
    public void onTaskCreate(Player player, String mintProject, String[] taskParts)
    {
        MintProjectManager manager = MintProjectManager.getMintProjectManager();
        MintProject project = manager.getProject(mintProject);

        if(project == null)
        {
            player.sendMessage(Utils.color("&cProject doesnt exist!"));
            return;
        }

        String taskString = Utils.buildMessage(taskParts, 0);

        MintTaskRequirement task = new MintTaskRequirement(taskString, 0, 0);

        project.addTask(task);
        player.sendMessage(Utils.color("&aSuccessfully added task to project &6" + project.getName()));
    }

    @Subcommand("task delete")
    @CommandPermission("emi.task.delete")
    public void onTaskDelete(Player player, String mintProject, long taskID)
    {
        MintProjectManager manager = MintProjectManager.getMintProjectManager();
        MintProject project = manager.getProject(mintProject);

        if (project == null)
        {
            player.sendMessage(Utils.color("&cProject doesnt exist!"));
            return;
        }

        MintTaskRequirement task = project.getTaskRequirements().get(taskID);

        if (task == null)
        {
            player.sendMessage(Utils.color("&cTaskID isnt associated with any other tasks"));
            return;
        }

        project.deleteTask(task);
        player.sendMessage(Utils.color("&aSuccessfully deleted task!"));
    }

    @Subcommand("task focus")
    @Syntax("<ProjectName> <taskID>")
    @CommandPermission("emi.task.focus")
    public void onTaskFocus(Player player, String mintProject, long taskID)
    {
        MintProjectManager manager = MintProjectManager.getMintProjectManager();
        MintProject project = manager.getProject(mintProject);

        if(project == null)
        {
            player.sendMessage(Utils.color("&cProject doesnt exist!"));
            return;
        }

        MintTaskRequirement task = project.getTaskRequirements().get(taskID);

        if(task == null)
        {
            player.sendMessage(Utils.color("&cTask doesnt exist in project &6" + project.getName()));
            return;
        }

        if(task.getFocused() == 1)
        {
            player.sendMessage(Utils.color("&cTask is already set to focused!"));
            return;
        }

        if(task.getComplete() == 1)
        {
            player.sendMessage(Utils.color("&cCant set complete tasks to be focused!"));
            return;
        }

        MintTaskRequirement formerTask = null;

        for(MintTaskRequirement mintTaskRequirement : project.getTaskRequirements().values())
        {
            if(mintTaskRequirement.getFocused() == 1)
            {
                formerTask = mintTaskRequirement;
                break;
            }
        }

        project.switchTaskFocus(task, formerTask);
        player.sendMessage(Utils.color("&aSuccessfully set task as focued!"));
    }

    @Subcommand("task list")
    @Syntax("<ProjectName>")
    @CommandPermission("emi.task.list")
    public void onTaskList(Player player, String mintProject)
    {
        MintProjectManager manager = MintProjectManager.getMintProjectManager();
        MintProject project = manager.getProject(mintProject);

        if(project == null)
        {
            player.sendMessage(Utils.color("&cProject doesnt exist!"));
            return;
        }

        if(project.getTaskRequirements().isEmpty())
        {
            player.sendMessage(Utils.color("&cProject doesnt have any tasks to list!"));
            return;
        }

        player.sendMessage(Utils.color("&aTasks for project: &6" + project.getName()));
        for(MintTaskRequirement task : project.getTaskRequirements().values())
        {
            player.sendMessage(Utils.color("&8[&9" + task.getTaskID() + "&8] &a" + task.getTask()));
        }
    }

    @Subcommand("validate")
    @CommandPermission("emi.validate")
    public void onValidate(Player player)
    {

    }
}