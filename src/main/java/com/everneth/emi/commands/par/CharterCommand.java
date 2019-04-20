package com.everneth.emi.commands.par;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
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
    @CommandPermission("emi.par.charter.issue")
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
    @CommandPermission("emi.par.charter.ban")
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
    @CommandPermission("emi.par.charter.history")
    @Subcommand("history")
    @CommandAlias("h")
    public void onHistoryCommand(CommandSender sender, String name, @Default("false") boolean includeExpired)
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
    @CommandPermission("emi.par.charter.edit")
    @Subcommand("edit")
    @CommandAlias("e")
    public void onEditCommand(CommandSender sender, int pointId, int newPointAmt, @Optional String newReason)
    {
        CharterPoint charterPoint = CharterPoint.getCharterPoint(pointId);
        if(charterPoint == null)
        {
            sender.sendMessage("ERROR: No record found. Did you enter the right ID number?");
        }
        else
        {
            int oldAmt = charterPoint.getAmount();
            charterPoint.setAmount(newPointAmt);
            if(newReason == null || newReason.equals(""))
            {
                charterPoint.setReason(newReason);
            }
            if(charterPoint.updateCharterPoint(charterPoint, pointId))
            {
                sender.sendMessage("Success: [ " + oldAmt + " -> " + charterPoint.getAmount() + " ]" );
            }
            else
            {
                sender.sendMessage("ERROR: Please report the following to Comms..." );
                sender.sendMessage("RECORD [" + pointId + "] UPDATE TABLE FAIL. CC-onEdit()");
            }
        }
    }
    @CommandPermission("emi.par.charter.remove")
    @Subcommand("remove")
    @CommandAlias("r")
    public void onRemoveCommand(CommandSender sender, int pointId)
    {

    }
    @CommandPermission("emi.par.charter.pardon")
    @Subcommand("pardon")
    @CommandAlias("p")
    public void onPardonCommand(CommandSender sender, String name, @Default("true") boolean removeFlag)
    {
        Player player = (Player) sender;
        long pardonPoint = CharterPoint.pardonPlayer(name, player, removeFlag);
        if(pardonPoint == 0)
        {
            sender.sendMessage("Could not pardon " + name + ". Either there is no player by this name, it has changed, or its misspelled.");
        }
        else
        {
            sender.sendMessage(name + " has been pardoned and points set to 1.");
        }
    }
}
