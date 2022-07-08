package com.everneth.emi.events;

import com.everneth.emi.managers.MintProjectManager;
import com.everneth.emi.models.mint.MintLogMaterial;
import com.everneth.emi.models.mint.MintLogTask;
import com.everneth.emi.models.mint.MintProject;
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

        MintProjectManager manager = MintProjectManager.getMintProjectManager();
        for(MintProject project : manager.getProjects().values())
        {
            if(project.getQueuedValidateMaterial().containsKey(player.getUniqueId()))
            {
                MintLogMaterial devopLogMaterial = project.getQueuedValidateMaterial().get(player.getUniqueId());
                project.getMaterialLogValidation().put(devopLogMaterial.getId(), devopLogMaterial);
                project.getQueuedValidateMaterial().remove(player.getUniqueId());
            }

            if(project.getQueuedValidateTask().containsKey(player.getUniqueId()))
            {
                MintLogTask mintLogTask = project.getQueuedValidateTask().get(player.getUniqueId());
                project.getTaskLogValidation().put(mintLogTask.getId(), mintLogTask);
                project.getQueuedValidateTask().remove(player.getUniqueId());
            }
        }
    }
}
