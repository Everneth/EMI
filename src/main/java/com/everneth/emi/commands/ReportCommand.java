package com.everneth.emi.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Dependency;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class ReportCommand extends BaseCommand {
    private JDA jda;

    @Dependency
    private Plugin plugin;

    public ReportCommand(JDA jda)
    {
        this.jda = jda;
    }

    @Default
    public void onReport(CommandSender sender, String message)
    {
        Player player = (Player)sender;

        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(player.getName());
        eb.setDescription(player.getUniqueId().toString());
        eb.setColor(0xffff00);
        eb.addField("X", Double.toString(player.getLocation().getX()), true);
        eb.addField("Y", Double.toString(player.getLocation().getY()), true);
        eb.addField("Z", Double.toString(player.getLocation().getZ()), true);
        eb.addField("Dimension", player.getWorld().toString(), true);
        eb.addField("Time", Long.toString(player.getPlayerTime()), true);
        eb.addField("Description", message, false);
        eb.setFooter("Help requested!", null);

        this.jda.getTextChannelById(499654660248961027L).sendMessage(eb.build()).queue();
    }
}
