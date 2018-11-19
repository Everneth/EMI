package com.everneth.emi.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Dependency;
import com.everneth.emi.EMI;
import net.dv8tion.jda.core.EmbedBuilder;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.text.SimpleDateFormat;
import java.util.Date;

@CommandAlias("report")
public class ReportCommand extends BaseCommand {
    //private JDA jda;

    @Dependency
    private Plugin plugin;

    @Default
    @CommandAlias("report")
    @CatchUnknown
    public void onReport(CommandSender sender, String message)
    {
        Player player = (Player)sender;

        Date now = new Date();
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(player.getName());
        eb.setDescription(player.getUniqueId().toString());
        eb.setColor(0xffff00);
        eb.setThumbnail("https://minotar.net/helm/" + player.getUniqueId() + "/100.png");
        eb.addField("X", Double.toString(player.getLocation().getX()), true);
        eb.addField("Y", Double.toString(player.getLocation().getY()), true);
        eb.addField("Z", Double.toString(player.getLocation().getZ()), true);
        eb.addField("Dimension", player.getWorld().getEnvironment().toString(), true);
        eb.addField("Time Reported (EST)", format.format(now), true);
        eb.addField("Server", player.getServer().getServerName(), true);
        eb.addField("Description", message, false);
        eb.setFooter("Help requested!", null);

        EMI.getJda().getTextChannelById(EMI.getPlugin().getConfig().getString("report-channel")).sendMessage(eb.build()).queue();
        player.sendMessage(ChatColor.GREEN + "Report submitted to " + ChatColor.RED +
                "#help" + ChatColor.GREEN + "!");
    }
}
