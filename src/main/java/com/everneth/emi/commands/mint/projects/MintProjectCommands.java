package com.everneth.emi.commands.mint.projects;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import co.aikar.idb.DbRow;
import com.everneth.emi.Utils;
import com.everneth.emi.managers.MintProjectManager;
import com.everneth.emi.models.EMIPlayer;
import com.everneth.emi.models.mint.MintMaterial;
import com.everneth.emi.models.mint.MintProject;
import com.everneth.emi.models.mint.MintTask;
import com.everneth.emi.utils.PlayerUtils;
import org.bukkit.entity.Player;

import java.util.ArrayList;

/**
 * This class defines and adds functionality to any mint command that handels projects.
 *
 * @author Sterling (@sterlingheaton)
 */

@CommandAlias("mint")
public class MintProjectCommands extends BaseCommand
{
    private final String mintProjectTag = "&7[&dMint&5Projects] ";

    /**
     * This command is used by project moderators to complete a project.
     *
     * @param player       Automatic input from the player who executed the command
     * @param mintProject Input for specified project
     */
    //TODO Add tab-complete to mintProject, add complete check
    @Subcommand("project complete")
    @Syntax("<Project>")
    @CommandPermission("emi.mint.project.complete")
    public void onProjectComplete(Player player, String mintProject)
    {
        MintProjectManager manager = MintProjectManager.getMintProjectManager();
        MintProject project = manager.getProject(mintProject);

        // Basic tests to determine if the command has been issued correctly
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

        // Valid information is then put into the project
        project.completeProject();
        player.sendMessage(Utils.color(mintProjectTag + "&aProject completed!"));
    }

    /**
     * This command is used by project moderators to create a project.
     *
     * @param player      Automatic input from the player who executed the command
     * @param projectName Input for project name
     * @param lead        Input for project leader
     * @param description Input for project description
     */
    //TODO add tab-complete
    @Subcommand("project create")
    @Syntax("<Project> <Lead> <Description>")
    @CommandPermission("emi.mint.project.create")
    public void onProjectCreate(Player player, String projectName, String lead, String[] description)
    {
        MintProjectManager manager = MintProjectManager.getMintProjectManager();
        MintProject project = manager.getProject(projectName);

        // Basic tests to determine if the command has been issued correctly
        if(project != null)
        {
            player.sendMessage(Utils.color(mintProjectTag + "&cProject already exists!"));
            return;
        }

        DbRow dbPlayerLead = PlayerUtils.getPlayerRow(lead);

        if(dbPlayerLead == null || dbPlayerLead.isEmpty())
        {
            player.sendMessage(Utils.color(mintProjectTag + "&cUnrecognized player, did you spell the name correctly?"));
            return;
        }

        // Valid information is then put into the project
        EMIPlayer playerLead = new EMIPlayer(dbPlayerLead.getString("player_uuid"), dbPlayerLead.getString("player_name"), dbPlayerLead.getInt("player_id"));
        project = new MintProject(playerLead, projectName, Utils.getCurrentDate(), null, 0, 0, Utils.buildMessage(description, 0, false));
        manager.addProject(project);
        player.sendMessage(Utils.color(mintProjectTag + "&aSuccessfully created the project!"));
        player.performCommand("mint project join " + project.getName());
    }

    /**
     * This command is used by project moderators to focus a project.
     *
     * @param player      Automatic input from the player who executed the command
     * @param projectName Input for specified project
     */
    //TODO add tab-complete
    @Subcommand("project focus")
    @Syntax("<Project>")
    @CommandPermission("emi.mint.project.focus")
    public void onProjectFocus(Player player, String projectName)
    {
        MintProjectManager manager = MintProjectManager.getMintProjectManager();
        MintProject project = manager.getProject(projectName);

        // Basic tests to determine if the command has been issued correctly
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

        // If the specified project is focused, mark it as unfocused
        if(project.getFocused() == 1)
        {
            manager.unFocus(project);
            player.sendMessage(Utils.color(mintProjectTag + "&aProject unfocused!"));
            return;
        }

        MintProject formerProject = null;

        // Searches and marks current project as unfocused
        for(MintProject mintProject : manager.getProjects().values())
        {
            if(mintProject.getFocused() == 1)
            {
                formerProject = mintProject;
                break;
            }
        }

        // Valid information is then put into the project
        manager.switchFocus(project, formerProject);
        player.sendMessage(Utils.color(mintProjectTag + "&aProject focused!"));
    }

    /**
     * This command is used by players to view basic project information.
     *
     * @param player      Automatic input from the player who executed the command
     * @param projectName Input for specified project
     */
    // TODO add tab-complete
    @Subcommand("project info")
    @Syntax("<Project>")
    @CommandPermission("emi.mint.project.info")
    public void onProjectInfo(Player player, String projectName)
    {
        MintProjectManager manager = MintProjectManager.getMintProjectManager();
        MintProject project = manager.getProject(projectName);

        // Basic tests to determine if the command has been issued correctly
        if(project == null)
        {
            player.sendMessage(Utils.color(mintProjectTag + "&cProject doesn't exist!"));
            return;
        }

        // Gather basic information on the project
        String endDate = project.getEndDate();
        String focusedTaskString = "";
        String focusedMaterialString = "";
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

        // Send player the basic information about the project
        player.sendMessage(Utils.color(mintProjectTag + "&aInformation for &6" + project.getName() + " &aby &6" + project.getLeader().getName() + "\n" +
                "&aDates: &6" + decodeDate(project.getStartDate()) + " &ato &6" + endDate + "\n" +
                "&aDescription: &6" + project.getDescription() + "\n" +
                "&aFocused task: &6" + focusedTaskString + "\n" +
                "&aFocused material: &6" + focusedMaterialString + "\n" +
                "&aWorkers: &6" + Utils.buildMessage(workers.toArray(new String[0]), 0, true)));
    }

    /**
     * This command is used by players to join a project.
     *
     * @param player       Automatic input from the player who executed the command
     * @param mintProject Input for specified project
     */
    //TODO add tab-complete
    @Subcommand("project join")
    @Syntax("<Project>")
    @CommandPermission("emi.mint.project.join")
    public void onProjectJoin(Player player, String mintProject)
    {
        MintProjectManager manager = MintProjectManager.getMintProjectManager();
        MintProject project = manager.getProject(mintProject);

        // Basic tests to determine if the command has been issued correctly
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
                player.sendMessage(Utils.color(mintProjectTag + "&cYou're already part of this project!"));
                return;
            }
        }

        // Valid information is then put into the project
        EMIPlayer emiPlayer = PlayerUtils.getEMIPlayer(player.getName());

        project.addWorker(emiPlayer);
        player.sendMessage(Utils.color(mintProjectTag + "&aSuccessfully joined the project!"));
    }

    /**
     * This command is used by players to list all projects.
     *
     * @param player Automatic input from the player who executed the command
     */
    @Subcommand("project list")
    @CommandPermission("emi.mint.project.list")
    public void onProjectList(Player player)
    {
        MintProjectManager manager = MintProjectManager.getMintProjectManager();

        // Basic test to determine if the command has been issued correctly
        if(manager.getProjects().isEmpty())
        {
            player.sendMessage(Utils.color(mintProjectTag + "&cNo projects to list!"));
            return;
        }


        // Create empty objects and put projects in them based on their status
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

        // Send player the projects sorted by Focused Project, Current Projects, and Completed Projects
        player.sendMessage(Utils.color(mintProjectTag + "&6Projects:"));

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

    /**
     * This command is used by players to view all tasks and materials for a project.
     *
     * @param player       Automatic input from the player who executed the command
     * @param mintProject Input for specified project
     */
    @Subcommand("project work")
    @Syntax("<Project>")
    @CommandPermission("emi.mint.project.work")
    public void onWork(Player player, String mintProject)
    {
        MintProjectManager manager = MintProjectManager.getMintProjectManager();
        MintProject project = manager.getProject(mintProject);

        // Basic test to determine if the command has been issued correctly
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

        // Send player work for the project: Material then Tasks
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
            player.sendMessage(Utils.color("&cThere's no work available for this project!"));
        }
    }

    /**
     * This method takes input for a date, splits it, and returns the first value.
     *
     * @param date Input for a formatted date
     *
     * @return Returns the first value in the array
     */
    private String decodeDate(String date)
    {
        String[] dateSplit = date.split(" ");

        return dateSplit[0];
    }
}
