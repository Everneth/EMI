package com.everneth.emi.commands.mint.projects;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import com.everneth.emi.Utils;
import com.everneth.emi.managers.DevopProjectManager;
import com.everneth.emi.models.EMIPlayer;
import com.everneth.emi.models.devop.DevopLogMaterial;
import com.everneth.emi.models.devop.DevopMaterial;
import com.everneth.emi.models.devop.DevopProject;
import com.everneth.emi.utils.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;

/**
 * This class defines and adds functionality to any mint command that handels materials.
 *
 * @author Sterling (@sterlingheaton)
 */

@CommandAlias("mint")
public class MintMaterialCommands extends BaseCommand
{
    private final String devopProjectTag = "&7[&dMint&5Projects] ";

    /**
     * This command is used by players to log any material gathering they completed.
     *
     * @param player         Automatic input from the player who executed the command
     * @param devopProject   Input for specified project
     * @param materialString Input for specified material
     * @param amount         Input for the amount gathered
     * @param time           Input for length of time worked
     * @param description    Input for a short description of what the player did (if applicable)
     */
    //TODO add tab-complete
    @Subcommand("log material")
    @Syntax("<Project> <Material> <Amount> <TimeWorked> <Description>")
    @CommandPermission("emi.devop.log")
    public void onLogMaterial(Player player, String devopProject, String materialString, int amount, String time, String[] description)
    {
        DevopProjectManager manager = DevopProjectManager.getDevopProjectManager();
        DevopProject project = manager.getProject(devopProject);

        // Basic tests to determine if the command has been issued correctly
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

        // Valid information is then put into the project
        EMIPlayer logger = PlayerUtils.getEMIPlayer(player.getName());
        DevopLogMaterial log = new DevopLogMaterial(project.getId(), material.getId(), logger, null, 0, amount, timeWorked, Utils.getCurrentDate(), Utils.buildMessage(description, 0, false));

        project.addMaterialLog(log);
        player.sendMessage(Utils.color(devopProjectTag + "&aMaterial log submitted for validation!"));
    }

    /**
     * This command is used by project moderators to mark a material as complete.
     *
     * @param player         Automatic input from the player who executed the command
     * @param devopProject   Input for specified project
     * @param materialString Input for specified material
     */
    //TODO add tab-complete
    @Subcommand("material complete")
    @Syntax("<Project> <Material>")
    @CommandPermission("emi.devop.material.complete")
    public void onMaterialComplete(Player player, String devopProject, String materialString)
    {
        DevopProjectManager manager = DevopProjectManager.getDevopProjectManager();
        DevopProject project = manager.getProject(devopProject);

        // Basic tests to determine if the command has been issued correctly
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

        // Valid information is then put into the project
        project.completeMaterial(material.getId());
        player.sendMessage(Utils.color(devopProjectTag + "&aMaterial Completed!"));
    }


    /**
     * This command is used by project moderators to add materials to a project.
     *
     * @param player         Automatic input from the player who executed the command
     * @param devopProject   Input for specified project
     * @param materialString Input for specified material
     * @param amount         Input for how much material the project needs
     */
    //TODO add tab-complete
    @Subcommand("material add")
    @Syntax("<Project> <Material> <Amount>")
    @CommandPermission("emi.devop.material.add")
    public void onMaterialCreate(Player player, String devopProject, String materialString, int amount)
    {
        DevopProjectManager manager = DevopProjectManager.getDevopProjectManager();
        DevopProject project = manager.getProject(devopProject);

        // Basic tests to determine if the command has been issued correctly
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

        // Valid information is then into the project
        DevopMaterial material = new DevopMaterial(project.getId(), materialString, amount, 0, 0, 0);

        project.addMaterial(material);
        player.sendMessage(Utils.color(devopProjectTag + "&aMaterial added!"));
    }

    /**
     * This command is used by project moderators to delete materials from a project.
     *
     * @param player         Automatic input from the player who executed the command
     * @param devopProject   Input for specified project
     * @param materialString Input for specified material
     */
    //TODO add tab-complete
    @Subcommand("material delete")
    @Syntax("<Project> <Material>")
    @CommandPermission("emi.devop.material.delete")
    public void onMaterialDelete(Player player, String devopProject, String materialString)
    {
        DevopProjectManager manager = DevopProjectManager.getDevopProjectManager();
        DevopProject project = manager.getProject(devopProject);

        // Basic tests to determine if the command has been issued correctly
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

        // Valid information is then put into the project
        project.deleteMaterial(material);
        player.sendMessage(Utils.color(devopProjectTag + "&aMaterial deleted!"));
    }

    /**
     * This command is used by project moderators to focus materials from a project.
     *
     * @param player         Automatic input from the player who executed the command
     * @param devopProject   Input for specified project
     * @param materialString Input for specified material
     */
    //TODO add tab-complete
    @Subcommand("material focus")
    @Syntax("<Project> <Material>")
    @CommandPermission("emi.devop.material.focus")
    public void onMaterialFocus(Player player, String devopProject, String materialString)
    {
        DevopProjectManager manager = DevopProjectManager.getDevopProjectManager();
        DevopProject project = manager.getProject(devopProject);

        // Basic tests to determine if the command has been issued correctly
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

        // If the specified material is focused, mark it as unfocused
        if(material.getFocused() == 1)
        {
            project.unFocusMaterial(material);
            player.sendMessage(Utils.color(devopProjectTag + "&cMaterial unfocused!"));
            return;
        }

        // Searches and marks current material as unfocused
        DevopMaterial formerMaterial = null;

        for(DevopMaterial devopMaterial : project.getMaterials().values())
        {
            if(devopMaterial.getFocused() == 1)
            {
                formerMaterial = devopMaterial;
                break;
            }
        }

        // Valid information is then put into the project
        project.switchMaterialFocus(material, formerMaterial);
        player.sendMessage(Utils.color(devopProjectTag + "&aMaterial focused!"));
    }

    /**
     * This command is used by players to view all materials from a project.
     *
     * @param player       Automatic input from the player who executed the command
     * @param devopProject Input for specified project
     */
    //TODO add tab-complete
    @Subcommand("material list")
    @Syntax("<Project>")
    @CommandPermission("emi.devop.material.list")
    public void onMaterialList(Player player, String devopProject)
    {
        DevopProjectManager manager = DevopProjectManager.getDevopProjectManager();
        DevopProject project = manager.getProject(devopProject);

        // Basic tests to determine if the command has been issued correctly
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

        // Create empty objects and put materials in them based on their status
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

        // Send player materials sorted by Focused Materials, Current Materials, and Completed Materials
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

    /**
     * This method takes an ArrayList of Materials and appends them all together.
     *
     * @param materials Input for Materials
     *
     * @return Returns a string of all Materials in the ArrayList of Materials
     */
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
            Bukkit.getLogger().info("ERROR: devopCommand/processTimeString: " + e.toString());
            return -1;
        }

        if(hours > 99 || minutes > 59)
        {
            return -1;
        }

        return ((hours*60) + minutes);
    }
}
