package com.everneth.emi.commands.mint.projects;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import co.aikar.idb.DbRow;
import com.everneth.emi.Utils;
import com.everneth.emi.managers.MintProjectManager;
import com.everneth.emi.models.EMIPlayer;
import com.everneth.emi.models.mint.MintLogMaterial;
import com.everneth.emi.models.mint.MintLogTask;
import com.everneth.emi.models.mint.MintMaterial;
import com.everneth.emi.models.mint.MintProject;
import com.everneth.emi.utils.PlayerUtils;
import org.bukkit.entity.Player;

/**
 * This class defines and adds functionality to any mint command that handels view.
 *
 * @author Sterling (@sterlingheaton)
 */

@CommandAlias("mint")
public class MintViewCommands extends BaseCommand
{
    private final String mintProjectTag = "&7[&dMint&5Projects] ";


    /**
     * This command allows project moderaters to check what tasks have been done by a specified player.
     *
     * @param player      Automatic input for the command sender
     * @param mintProject Input for the project name
     * @param worker      Input for the worker
     */
    @Subcommand("view task")
    @Syntax("<Project> <Worker>")
    @CommandPermission("emi.mint.view")
    public void onViewTasks(Player player, String mintProject, String worker)
    {
        MintProjectManager manager = MintProjectManager.getMintProjectManager();
        MintProject project = manager.getProject(mintProject);

        // Basic tests to determine if the command has been issued correctly
        if(project == null)
        {
            player.sendMessage(Utils.color(mintProjectTag + "&cProject doesn't exist!"));
            return;
        }

        DbRow dbWorker = PlayerUtils.getPlayerRow(worker);

        if(dbWorker == null || dbWorker.isEmpty())
        {
            player.sendMessage(Utils.color(mintProjectTag + "&cUnrecognized player, did you spell the name correctly?"));
            return;
        }

        // Searches for and displays tasks that meet the right project and worker
        EMIPlayer playerWorker = new EMIPlayer(dbWorker.getString("player_uuid"), dbWorker.getString("player_name"), dbWorker.getInt("player_id"));

        player.sendMessage(Utils.color(mintProjectTag + "&aTask logs submitted by &6" + playerWorker.getName() + " &afor project &6" + project.getName() + "&a:"));

        for(MintLogTask task : project.getTaskLog().values())
        {
            if(!task.getLogger().equals(playerWorker))
            {
                continue;
            }

            if(task.getValidated() == 1)
            {
                player.sendMessage(Utils.color("&7[&aValidated&7] &6" + task.getDescription().trim() +
                        " &awas done at &6" + decodeDate(task.getLogDate()) +
                        " &aand took &6" + decodeTime(task.getTimeWorked())));
            }
            else
            {
                player.sendMessage(Utils.color("&7[&cNot Validated&7] &6" + task.getDescription().trim() +
                        " &awas done at &6" + decodeDate(task.getLogDate()) +
                        " &aand took &6" + decodeTime(task.getTimeWorked())));
            }
        }
    }

    /**
     * This command allows project moderaters to check what materials have been harvested by a specified player.
     *
     * @param player      Automatic input for the command sender
     * @param mintProject Input for the project name
     * @param worker      Input for the worker
     */
    @Subcommand("view material")
    @Syntax("<Project> <Worker>")
    @CommandPermission("emi.mint.view")
    public void onMaterialView(Player player, String mintProject, String worker)
    {
        MintProjectManager manager = MintProjectManager.getMintProjectManager();
        MintProject project = manager.getProject(mintProject);

        // Basic tests to determine if the command has been issued correctly
        if(project == null)
        {
            player.sendMessage(Utils.color(mintProjectTag + "&cProject doesn't exist!"));
            return;
        }

        DbRow dbWorker = PlayerUtils.getPlayerRow(worker);

        if(dbWorker == null || dbWorker.isEmpty())
        {
            player.sendMessage(Utils.color(mintProjectTag + "&cUnrecognized player, did you spell the name correctly?"));
            return;
        }

        // Searches for and displays materials that meet the right project and worker
        EMIPlayer playerWorker = new EMIPlayer(dbWorker.getString("player_uuid"), dbWorker.getString("player_name"), dbWorker.getInt("player_id"));

        player.sendMessage(Utils.color(mintProjectTag + "&Material logs submitted by &6" + playerWorker.getName() + " &afor project &6" + project.getName() + "&a:"));

        for(MintLogMaterial material : project.getMaterialLog().values())
        {
            if(!material.getLogger().equals(playerWorker))
            {
                continue;
            }

            if(material.getValidated() == 1)
            {
                player.sendMessage(Utils.color("&7[&aValidated&7] &6" +
                        "&6Material&8(&6" + material.getMaterialCollected() + "&8) " +
                        " &awas gathered at &6" + decodeDate(material.getLogDate()) +
                        "&a and took" + decodeTime(material.getTimeWorked())));
            }
            else
            {
                player.sendMessage(Utils.color("&7[&cNot Validated&7] &6" +
                        "&6Material&8(&6" + material.getMaterialCollected() + "&8) " +
                        " &awas gathered at &6" + decodeDate(material.getLogDate()) +
                        "&a and took" + decodeTime(material.getTimeWorked())));
            }
        }
    }

    /**
     * This command allows project moderaters to check what players gathered a specified material.
     *
     * @param player         Automatic input for command sender
     * @param mintProject    Input for the project name
     * @param materialString Input for the material name
     */
    @Subcommand("view material")
    @Syntax("<Project> <Material>")
    @CommandPermission("emi.mint.view")
    public void onMaterialViewNoWorker(Player player, String mintProject, String materialString)
    {
        MintProjectManager manager = MintProjectManager.getMintProjectManager();
        MintProject project = manager.getProject(mintProject);

        // Basic tests to determine if the command has been issued correctly
        if(project == null)
        {
            player.sendMessage(Utils.color(mintProjectTag + "&cProject doesn't exist!"));
            return;
        }

        MintMaterial material = project.getMaterial(materialString);

        if(material == null)
        {
            player.sendMessage(Utils.color(mintProjectTag + "&cMaterial isn't associated with this project!"));
            return;
        }

        // Searches for and displays players that meet the right project and material
        player.sendMessage(Utils.color(mintProjectTag + "&aPlayers who gathered &6" + material.getMaterial() + " &afor project &6" + project.getName()));

        for(MintLogMaterial logMaterial : project.getMaterialLog().values())
        {
            if(logMaterial.getMaterialID() != material.getId())
            {
                continue;
            }

            if(logMaterial.getValidated() == 1)
            {
                player.sendMessage(Utils.color("&7[&aValidated&7] &6" + logMaterial.getLogger().getName() +
                        " &agathered &6" + logMaterial.getMaterialCollected() +
                        " &aon &6" + decodeDate(logMaterial.getLogDate()) +
                        " &aand took &6" + decodeTime(logMaterial.getTimeWorked())));
            }
            else
            {
                player.sendMessage(Utils.color("&7[&cNot Validated&7] &6" + logMaterial.getLogger().getName() +
                        " &agathered &6" + logMaterial.getMaterialCollected() +
                        " &aon &6" + decodeDate(logMaterial.getLogDate()) +
                        " &aand took &6" + decodeTime(logMaterial.getTimeWorked())));
            }
        }
    }

    /**
     * This method takes in a time in minutes and returns a readable time.
     *
     * @param time Input for time measured in minutes
     *
     * @return Returns a string showing hours and minutes
     */
    private String decodeTime(int time)
    {
        String hours = String.valueOf(time / 60);
        String minutes = String.valueOf(time % 60);

        return (hours + " hours " + minutes + " minutes");
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
