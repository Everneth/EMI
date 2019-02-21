package com.everneth.emi.events;

import com.everneth.emi.EMI;
import com.everneth.emi.ReportManager;
import com.everneth.emi.models.Report;
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
            EMI.getJda().getTextChannelById(report.getChannelId()).sendMessage("**" + player.getName() + "* has left the game.*").queue();
        }
    }
}
