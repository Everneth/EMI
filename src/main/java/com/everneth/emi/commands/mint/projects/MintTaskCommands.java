package com.everneth.emi.commands.mint.projects;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import com.everneth.emi.Utils;
import com.everneth.emi.managers.MintProjectManager;
import com.everneth.emi.models.EMIPlayer;
import com.everneth.emi.models.mint.MintLogTask;
import com.everneth.emi.models.mint.MintProject;
import com.everneth.emi.models.mint.MintTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;

/**
 * This class defines and adds functionality to any mint command that handles tasks.
 *
 * @author Sterling (@sterlingheaton)
 */

@CommandAlias("mint")
public class MintTaskCommands extends BaseCommand
{
    private final String mintProjectTag = "&7[&dMint&5Projects&7] ";

    /**
     * This command is used by players to log any task they've completed.
     *
     * @param player      Automatic input from the player who executed the command
     * @param mintProject Input for specified project
     * @param time        Input for length of time worked
     * @param description Input for a short description of what the player did (if applicable)
     */
    //TODO add tab-complete
    @Subcommand("log task")
    @Syntax("<Project> <TimeWorked> <Description>")
    @CommandPermission("emi.mint.log")
    public void onLogTask(Player player, String mintProject, String time, String[] description)
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
            player.sendMessage(Utils.color(mintProjectTag + "&cLog can't be sent because the project is complete!"));
            return;
        }

        int timeWorked = encodeTime(time);

        if(timeWorked == -1)
        {
            player.sendMessage(Utils.color(mintProjectTag + "&cInvalid time format, must be HOURS:MINUTES (00:00)!"));
            return;
        }

        // Valid information is then into the project
        EMIPlayer logger = EMIPlayer.getEMIPlayer(player.getName());
        MintLogTask log = new MintLogTask(project.getId(), logger, null, 0, timeWorked, Utils.getCurrentDate(), Utils.buildMessage(description, 0, false));

        project.addTaskLog(log);
        player.sendMessage(Utils.color(mintProjectTag + "&aTask log submitted for validation!"));
    }

    /**
     * This command is used by project moderators to set a task as complete.
     *
     * @param player      Automatic input from the player who executed the command
     * @param mintProject Input for specified project
     * @param taskID      Input for a specified Task
     */
    @Subcommand("task complete")
    @Syntax("<Project> <taskID>")
    @CommandPermission("emi.mint.task.complete")
    public void onTaskComplete(Player player, String mintProject, long taskID)
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
            player.sendMessage(Utils.color(mintProjectTag + "&cTask can't be completed because the project is complete!"));
            return;
        }

        if(project.getTasks().get(taskID) == null)
        {
            player.sendMessage(Utils.color(mintProjectTag + "&cTaskID isn't associated with any tasks!"));
            return;
        }

        if(project.getTasks().get(taskID).getComplete() == 1)
        {
            player.sendMessage(Utils.color(mintProjectTag + "&cTask has already been completed!"));
            return;
        }

        // Valid information is then put into the project
        project.completeTask(taskID);
        player.sendMessage(Utils.color(mintProjectTag + "&aTask completed!"));
    }

    /**
     * This command is used by project moderators to add a task to a project.
     *
     * @param player      Automatic input from the player who executed the command
     * @param mintProject Input for specified project
     * @param taskParts   Input for a task
     */
    @Subcommand("task add")
    @Syntax("<Project> <Task>")
    @CommandPermission("emi.mint.task.add")
    public void onTaskCreate(Player player, String mintProject, String[] taskParts)
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
            player.sendMessage(Utils.color(mintProjectTag + "&cTask cant be added because the project is complete!"));
            return;
        }

        // Valid information is then put into the project
        String taskString = Utils.buildMessage(taskParts, 0, false);
        MintTask task = new MintTask(project.getId(), taskString, 0, 0);

        project.addTask(task);
        player.sendMessage(Utils.color(mintProjectTag + "&aTask added!"));
    }

    /**
     * This command is used by project moderators to delete a task from a project.
     *
     * @param player      Automatic input from the player who executed the command
     * @param mintProject Input for specified project
     * @param taskID      Input for a specified Task
     */
    @Subcommand("task delete")
    @Syntax("<Project> <TaskID>")
    @CommandPermission("emi.mint.task.delete")
    public void onTaskDelete(Player player, String mintProject, long taskID)
    {
        MintProjectManager manager = MintProjectManager.getMintProjectManager();
        MintProject project = manager.getProject(mintProject);

        // Basic test to determine if the command has been issued correctly
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
            player.sendMessage(Utils.color(mintProjectTag + "&cTaskID isn't associated with any tasks!"));
            return;
        }

        // Valid information is then put into the project
        project.deleteTask(task);
        player.sendMessage(Utils.color(mintProjectTag + "&aTask deleted!"));
    }

    /**
     * This command is used by project moderators to mark a task as focused.
     *
     * @param player      Automatic input from the player who executed the command
     * @param mintProject Input for specified project
     * @param taskID      Input for a specified Task
     */
    @Subcommand("task focus")
    @Syntax("<Project> <taskID>")
    @CommandPermission("emi.mint.task.focus")
    public void onTaskFocus(Player player, String mintProject, long taskID)
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
            player.sendMessage(Utils.color(mintProjectTag + "&cTask can't be focused because the project is complete!"));
            return;
        }

        MintTask task = project.getTasks().get(taskID);

        if(task == null)
        {
            player.sendMessage(Utils.color(mintProjectTag + "&cTaskID isn't associated with any tasks."));
            return;
        }

        if(task.getComplete() == 1)
        {
            player.sendMessage(Utils.color(mintProjectTag + "&cTask can't be focused because the task is complete!"));
            return;
        }

        // If the specified task is focused, mark it as unfocused
        if(task.getFocused() == 1)
        {
            project.unFocusTask(task);
            player.sendMessage(Utils.color(mintProjectTag + "&cTask unfocused!"));
            return;
        }

        MintTask formerTask = null;

        // Searches and marks current project as unfocused
        for(MintTask mintTask : project.getTasks().values())
        {
            if(mintTask.getFocused() == 1)
            {
                formerTask = mintTask;
                break;
            }
        }

        // Valid information is then put into the project
        project.switchTaskFocus(task, formerTask);
        player.sendMessage(Utils.color(mintProjectTag + "&aTask focused!"));
    }

    /**
     * This command is used by players to view tasks for a project.
     *
     * @param player      Automatic input from the player who executed the command
     * @param mintProject Input for specified project
     */
    @Subcommand("task list")
    @Syntax("<Project>")
    @CommandPermission("emi.mint.task.list")
    public void onTaskList(Player player, String mintProject)
    {
        MintProjectManager manager = MintProjectManager.getMintProjectManager();
        MintProject project = manager.getProject(mintProject);

        // Basic test to determine if the command has been issued correctly
        if(project == null)
        {
            player.sendMessage(Utils.color(mintProjectTag + "&cProject doesn't exist!"));
            return;
        }

        if(project.getTasks().isEmpty())
        {
            player.sendMessage(Utils.color(mintProjectTag + "&cNo tasks to list!"));
            return;
        }

        // Create empty objects and put tasks in them based on their status
        MintTask focusedTask = null;
        ArrayList<MintTask> currentTasks = new ArrayList<>();
        ArrayList<MintTask> completeTasks = new ArrayList<>();

        for(MintTask task : project.getTasks().values())
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

        player.sendMessage(Utils.color(mintProjectTag + "&aTasks for &6" + project.getName()));

        // Send player the tasks sorted by Focused Task, Current Tasks, and Completed Tasks
        // Also sends players the taskID, based on their permissions
        if(focusedTask != null)
        {
            if(player.hasPermission("emi.mint.view.taskid"))
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
            if(player.hasPermission("emi.mint.view.taskid"))
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
            if(player.hasPermission("emi.mint.view.taskid"))
            {
                player.sendMessage(Utils.color("&aComplete: " + buildTaskList(completeTasks, true)));
            }
            else
            {
                player.sendMessage(Utils.color("&aComplete: " + buildTaskList(completeTasks, false)));
            }
        }
    }

    /**
     * This method takes an ArrayList of Tasks and appends them all together depending on if the player has a permission or not.
     *
     * @param tasks         Input for Tasks
     * @param hasPermission Input for if the player has a permission
     *
     * @return Returns a string of all Tasks in the ArrayList of Tasks
     */
    private String buildTaskList(ArrayList<MintTask> tasks, boolean hasPermission)
    {
        StringBuilder builder = new StringBuilder();

        for(MintTask task : tasks)
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

    /**
     * This method takes an input and tries to return a time measured in minutes.
     *
     * @param time Input for a time format HH:MM
     *
     * @return Time measured in minutes or -1 if the format was entered in wrong
     */
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
            Bukkit.getLogger().info("ERROR: mintCommand/processTimeString: " + e);
            return -1;
        }

        if(hours > 99 || minutes > 59)
        {
            return -1;
        }

        return ((hours*60) + minutes);
    }
}
