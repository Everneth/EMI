package com.everneth.emi.commands.par;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import co.aikar.commands.annotation.Optional;
import co.aikar.idb.DbRow;
import com.everneth.emi.Utils;
import com.everneth.emi.models.CharterPoint;
import com.everneth.emi.utils.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

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
        List<DbRow> points;
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
            SortedMap<Integer, String> map = new TreeMap<Integer, String>(Collections.reverseOrder());
            int i = 1;
            for(DbRow charterPoint : points)
            {
                if(Boolean.valueOf(charterPoint.getInt("expunged").toString())) {
                    String msg = "&m&3#" + charterPoint.getInt("charter_point_id") + "&7 - (&b" +
                            charterPoint.getString("date_issued") + "&7)" + charterPoint.getInt("amount") +
                            "&o point(s) issued by &l&d" + charterPoint.getString("issued_by") + ".&r&m&3&o Reason: &7" +
                            charterPoint.getString("reason") + " [Expires " +
                            charterPoint.getString("date_expired") + "]";
                    map.put(i, msg);
                }
                else
                {
                    String msg = "&3#" + charterPoint.getInt("charter_point_id") + "&7 - (&b" +
                            charterPoint.getString("date_issued") + "&7)" + charterPoint.getInt("amount") +
                            "&o point(s) issued by &l&d" + charterPoint.getString("issued_by") + ".&r&3&o Reason: &7" +
                            charterPoint.getString("reason") + " [Expires " +
                            charterPoint.getString("date_expired") + "]";
                    map.put(i, msg);
                }
                i++;
            }
            paginate(sender, map, 1, 5);
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
        CharterPoint charterPoint = CharterPoint.getCharterPoint(pointId);
        if(charterPoint == null)
        {
            sender.sendMessage("ERROR: No record found. Did you enter the right ID number?");
        }
        else
        {
            if(charterPoint.removeCharterPoint(pointId))
            {
                sender.sendMessage("The point(s) issued to " + charterPoint.getRecipient().getName() + " have been removed (expunged) from the players history.");
            }
            else
            {
                sender.sendMessage("Could not remove point(s) issued to " + charterPoint.getRecipient().getName() + ". DB error on update. Please notify Comms.");
            }
        }
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

    public void paginate(CommandSender sender, SortedMap<Integer, String> map,
                          int page, int pageLength) {
        sender.sendMessage(ChatColor.YELLOW + "List: Page (" + String.valueOf(page) + " of " + (((map.size() % pageLength) == 0) ? map.size() / pageLength : (map.size() / pageLength) + 1));
        int i = 0, k = 0;
        page--;
        for (final Map.Entry<Integer, String> e : map.entrySet()) {
            k++;
            if ((((page * pageLength) + i + 1) == k) && (k != ((page * pageLength) + pageLength + 1))) {
                i++;
                sender.sendMessage(Utils.color(" - " + e.getValue()));
            }
        }
    }
}
