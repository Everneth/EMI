package com.everneth.emi.events;

import com.everneth.emi.EMI;
import com.everneth.emi.managers.DevopProjectManager;
import com.everneth.emi.managers.ReportManager;
import com.everneth.emi.models.Report;
import com.everneth.emi.models.devop.DevopLogMaterial;
import com.everneth.emi.models.devop.DevopLogTask;
import com.everneth.emi.models.devop.DevopProject;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

public class LeaveEvent implements Listener {
    private Plugin plugin;
    public LeaveEvent(Plugin plugin)
    {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        Player player = event.getPlayer();
        ReportManager rm = ReportManager.getReportManager();

        if(rm.hasActiveReport(player.getUniqueId()))
        {
            Report report = rm.findReportById(player.getUniqueId());
            EMI.getJda().getTextChannelById(report.getChannelId()).sendMessage("***" + player.getName() + "** has left the game.*").queue();
        }

        DevopProjectManager manager = DevopProjectManager.getDevopProjectManager();
        for(DevopProject project : manager.getProjects().values())
        {
            if(project.getQueuedValidateMaterial().containsKey(player.getUniqueId()))
            {
                DevopLogMaterial devopLogMaterial = project.getQueuedValidateMaterial().get(player.getUniqueId());
                project.getMaterialLogValidation().put(devopLogMaterial.getId(), devopLogMaterial);
                project.getQueuedValidateMaterial().remove(player.getUniqueId());
            }

            if(project.getQueuedValidateTask().containsKey(player.getUniqueId()))
            {
                DevopLogTask devopLogTask = project.getQueuedValidateTask().get(player.getUniqueId());
                project.getTaskLogValidation().put(devopLogTask.getId(), devopLogTask);
                project.getQueuedValidateTask().remove(player.getUniqueId());
            }
        }
    }
}
