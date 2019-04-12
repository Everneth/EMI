package com.everneth.emi.commands.par;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.idb.DbRow;
import com.everneth.emi.Utils;
import com.everneth.emi.models.CharterPoint;
import com.everneth.emi.utils.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@CommandAlias("charter")
public class CharterCommand extends BaseCommand {
    @Subcommand("issue")
    @CommandAlias("i")
    public void onIssueCommand(CommandSender sender, String player, int amount, String reason)
    {
        Player issuer = (Player) sender;
        DbRow recipientRecord = PlayerUtils.getPlayerRow(player);
        long pointRecordId = 0;

        if(recipientRecord == null)
        {
            issuer.sendMessage(Utils.color("ERROR: Player not found! Is the name spelled correctly?"));
        }
        else
        {
            Player recipient = Bukkit.getServer().getOfflinePlayer(UUID.fromString(recipientRecord.getString("player_uuid"))).getPlayer();
            CharterPoint point = new CharterPoint(issuer, recipient, reason, amount);
            pointRecordId = point.issuePoint();
            point.enforceCharter();
        }
    }
    @Subcommand("ban")
    @CommandAlias("b")
    public void onBanCommand(CommandSender sender, String player, @Optional String reason)
    {
        Player issuer = (Player) sender;
        DbRow recipientRecord = PlayerUtils.getPlayerRow(player);
        if(reason == null)
        {
            reason = "You have exceeded 5 charter points and are permanently banned. Please appeal on the forums.";
        }
        if(recipientRecord == null)
        {
            issuer.sendMessage(Utils.color("ERROR: Player not found! Is the name spelled correctly?"));
        }
        else
        {
            Player recipient = Bukkit.getServer().getOfflinePlayer(UUID.fromString(recipientRecord.getString("player_uuid"))).getPlayer();
            CharterPoint point = new CharterPoint(issuer, recipient, reason, 5);
            point.enforceCharter();
        }
    }
    @CommandPermission("emi.par.history")
    @Subcommand("history")
    @CommandAlias("h")
    public void onHistoryCommand(CommandSender sender, String name, @Optional boolean includeExpired)
    {
        List<CharterPoint> points;
        if(includeExpired) {
            points = PlayerUtils.getAllPoints(name, true);
        }
        else
        {
            points = PlayerUtils.getAllPoints(name, false);
        }
        if(points.isEmpty())
        {
            sender.sendMessage("No charter point history found. Excellent citizenship!");
        }
        else
        {
            // TODO: Pagination of histories
        }
    }
}
