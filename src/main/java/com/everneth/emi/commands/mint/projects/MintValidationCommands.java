package com.everneth.emi.commands.mint.projects;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Private;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import com.everneth.emi.Utils;
import com.everneth.emi.managers.DevopProjectManager;
import com.everneth.emi.models.EMIPlayer;
import com.everneth.emi.models.devop.DevopLogMaterial;
import com.everneth.emi.models.devop.DevopLogTask;
import com.everneth.emi.models.devop.DevopProject;
import com.everneth.emi.utils.PlayerUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

public class MintValidationCommands extends BaseCommand
{
    private final String devopProjectTag = "&7[&dMint&5Projects] ";

    /**
     * This command is used by project moderators to validate player logs.
     *
     * @param player       Automatic input from the player who executed the command
     * @param devopProject Input for specified project
     */
    @Subcommand("validate")
    @Syntax("<Project>")
    @CommandPermission("emi.devop.validate")
    public void onValidate(Player player, String devopProject)
    {
        DevopProjectManager manager = DevopProjectManager.getDevopProjectManager();
        DevopProject project = manager.getProject(devopProject);

        // Basic test to determine if the command has been issued correctly
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

        // If the player already submitted a validation request and hasn't responded, send them their queued validation with clickable message
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

        // If there's no logs, simply return and tell the player
        if(project.getMaterialLogValidation().isEmpty() && project.getTaskLogValidation().isEmpty())
        {
            player.sendMessage(Utils.color(devopProjectTag + "&cNo logs to validate!"));
            return;
        }

        // If there's material logs to validate, send the first log with a clickable message
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

        // If there's task logs to validate, send the first log with a clickable message
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

    /**
     * This command is used by the plugin as a clickable message to validate the log.
     *
     * @param player       Automatic input from the player who executed the command
     * @param devopProject Input for specified project
     */
    @Subcommand("validateyes")
    @Syntax("<Project>")
    @CommandPermission("emi.devop.validate")
    @Private
    public void onValidateYes(Player player, String devopProject)
    {
        DevopProjectManager manager = DevopProjectManager.getDevopProjectManager();
        DevopProject project = manager.getProject(devopProject);
        EMIPlayer validator = PlayerUtils.getEMIPlayer(player.getName());

        // Validate the material or task log
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

    /**
     * This command is used by the plugin as a clickable message to reject the log.
     *
     * @param player       Automatic input from the player who executed the command
     * @param devopProject Input for specified project
     */
    @Subcommand("validateno")
    @Syntax("<Project>")
    @CommandPermission("emi.devop.validate")
    @Private
    public void onValidateNo(Player player, String devopProject)
    {
        DevopProjectManager manager = DevopProjectManager.getDevopProjectManager();
        DevopProject project = manager.getProject(devopProject);
        EMIPlayer validator = PlayerUtils.getEMIPlayer(player.getName());

        // Reject the material or task log
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

    /**
     * This method generates the validation message for the player to click yes or no.
     *
     * @param devopProject Input for specified project
     *
     * @return Returns a TextComponent ready to send to a player
     */
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
