package com.everneth.emi.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.everneth.emi.EMI;
import com.everneth.emi.Utils;
import net.dv8tion.jda.api.EmbedBuilder;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;

/**
 *     Class: SupportCommand
 *     Author: Faceman (@TptMike)
 *     Purpose: Generate an embed and post it in the designated Discord channel
 *     using the JDA bot
 *
 */

@CommandAlias("support")
public class SupportCommand extends BaseCommand {

    @Dependency
    private Plugin plugin;

    @Default
    @CommandAlias("support")
    @Description("Send a message requesting support in the discord's #help channel.")
    @Syntax("[message]")
    public void onSupport(CommandSender sender, String message)
    {
        // Get the player and supply all potentially useful
        // information to the embed builder
        Player player = (Player)sender;

        LocalDateTime now = LocalDateTime.now();
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
        eb.addField("Description", message, false);
        eb.setFooter("Help requested!", null);

        // Make the bot post the embed to the channel and notify the player
        EMI.getJda().getTextChannelById(EMI.getPlugin().getConfig().getLong("help-channel-id")).sendMessage(eb.build()).queue();
        player.sendMessage(Utils.color("<&6The Wench&f> Your report submitted to &6#help&f! A staff member " +
                "will get back to you shortly. <3"));
    }
}