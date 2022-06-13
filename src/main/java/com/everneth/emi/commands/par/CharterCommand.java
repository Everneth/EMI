package com.everneth.emi.commands.par;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import co.aikar.commands.annotation.Optional;
import co.aikar.idb.DbRow;
import com.everneth.emi.EMI;
import com.everneth.emi.Utils;
import com.everneth.emi.models.CharterPoint;
import com.everneth.emi.models.EMIPlayer;
import com.everneth.emi.utils.PlayerUtils;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@CommandAlias("charter")
public class CharterCommand extends BaseCommand {
    @CommandPermission("emi.par.charter.pg")
    @Syntax("<page #>")
    @Subcommand("pg")
    public void onPageCommand(CommandSender sender, int page)
    {
        Player commandSender = (Player)sender;
        Gson gson = new Gson();
        SortedMap<Integer, String> map = new TreeMap<Integer, String>(Collections.reverseOrder());
        int itemsPerPage = EMI.getPlugin().getConfig().getInt("items-per-page");

        try(JsonReader reader = new JsonReader(new FileReader(EMI.getPlugin().getDataFolder() + File.separator + "cache" + File.separator + commandSender.getUniqueId().toString() + ".json")))
        {
            map = gson.fromJson(reader, SortedMap.class);
        }
        catch (IOException e)
        {
            System.out.println(e.getMessage());
        }

        int numPages = (((map.size() % itemsPerPage) == 0) ? map.size() / itemsPerPage : (map.size() / itemsPerPage) + 1);

        if(page > numPages)
        {
            sender.sendMessage(Utils.color("&c(╯°□°）╯︵ ┻━┻ &fThe list contains " + numPages + " pages... &c&o&nHOW DO I GIVE YOU " + page + "?!" ));
        }
        else if(page <= 0)
        {
            sender.sendMessage(Utils.color("&c(╯°□°）╯︵ ┻━┻ &fClearly by putting 0 or less you're trying to break things for no reason. &c&o&nDamnit &mPande&r&c&o&n Bobby..."));
        }
        else {
            paginate(sender, map, page, itemsPerPage);
            sender.sendMessage(Utils.color("&eUse /cpage [page #] to move to the next page"));
        }
    }
    @CommandPermission("emi.par.charter.issue")
    @Subcommand("issue")
    @CommandAlias("cissue")
    @Syntax("<player> <amount> <reason>")
    public void onIssueCommand(CommandSender sender, String player, int amount, String reason)
    {
        Player issuer = (Player) sender;
        DbRow recipientRecord = PlayerUtils.getPlayerRow(player);
        long pointRecordId = 0;

        if(recipientRecord == null)
        {
            issuer.sendMessage(Utils.color("&9[Charter] &cERROR: Player not found! &3Is the name spelled correctly?"));
        }
        else
        {
            EMIPlayer recipient = new EMIPlayer(
                    recipientRecord.getString("player_uuid"),
                    recipientRecord.getString("player_name"),
                    recipientRecord.getString("alt_name"),
                    recipientRecord.getInt("player_id")
            );
            EMIPlayer issuerPlayer = new EMIPlayer(
                    issuer.getUniqueId().toString(),
                    issuer.getName(),
                    null
            );
            CharterPoint point = new CharterPoint(issuerPlayer, recipient, reason, amount);
            pointRecordId = point.issuePoint();
            point.enforceCharter(sender);
        }
    }
    @CommandPermission("emi.par.charter.ban")
    @Subcommand("ban")
    @CommandAlias("cban")
    @Syntax("<player> [reason]")
    public void onBanCommand(CommandSender sender, String player, @Optional String reason)
    {
        Player issuer = (Player) sender;
        DbRow recipientRecord = PlayerUtils.getPlayerRow(player);
        if(reason == null)
        {
            reason = "You have exceeded 5 charter points and are permanently banned. Please contact staff on discord if you wish to appeal.";
        }
        if(recipientRecord == null)
        {
            issuer.sendMessage(Utils.color("&9[Charter] &cERROR: Player not found! &3Is the name spelled correctly?"));
        }
        else
        {
            EMIPlayer recipient = new EMIPlayer(
                    recipientRecord.getString("player_uuid"),
                    recipientRecord.getString("player_name"),
                    recipientRecord.getString("alt_name"),
                    recipientRecord.getInt("player_id")
            );
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "ban " + player + Utils.color(" &c" + reason));
            sender.sendMessage(Utils.color("Banned &c" + player));
            String altName = recipientRecord.getString("alt_name");
            if (altName != null) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "ban " + player + Utils.color(" &c" + reason));
                sender.sendMessage(Utils.color("Banned &c" + altName));
            }
            sender.sendMessage(Utils.color("&9[Charter] &3" + recipient.getName() + " and any whitelisted alts have been permanently banned." +
                    "Please login to the test server and manually ban any of these accounts."));
        }
    }
    @CommandPermission("emi.par.charter.history")
    @Subcommand("history")
    @CommandAlias("chistory")
    @Syntax("<name> [<include expired>]")
    public void onHistoryCommand(CommandSender sender, String name, @Default("false") boolean includeExpired)
    {
        //before doing anything does this player exist?
        DbRow player = PlayerUtils.getPlayerRow(name);
        if(player == null || player.isEmpty())
        {
            sender.sendMessage(Utils.color("&9[Charter]&3 This player does not exist in our system."));
        }
        else
        {
        List<DbRow> points;
        points = PlayerUtils.getAllPoints(name);

        if(points.isEmpty())
        {
            sender.sendMessage(Utils.color("&9[Charter] &3No charter point history found. Excellent citizenship!"));
        }
        else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            LocalDateTime now = LocalDateTime.now();

            SortedMap<Integer, String> map = new TreeMap<Integer, String>(Collections.reverseOrder());
            int i = 1;
            int numActive = 0;
            int numExpired = 0;
            for (DbRow charterPoint : points) {
                boolean isExpired = now.isAfter(charterPoint.get("date_expired"));
                boolean isExpunged = charterPoint.get("expunged");
                if (includeExpired) {
                    if (isExpunged || isExpired) {
                        String msg = " &3#" + charterPoint.getInt("charter_point_id") + "&7 - (&b" +
                                formatter.format(charterPoint.get("date_issued")) + "&7&m) &c&m" + charterPoint.getInt("amount") +
                                " point(s)&7&m issued to &c&m" + charterPoint.getString("issued_to") + " by &l&d&m" + charterPoint.getString("issued_by") + ".&3&m \nReason: &7&m" +
                                charterPoint.getString("reason") + "-- &o[Expires: &b&o&m" +
                                formatter.format(charterPoint.get("date_expired")) + "]";
                        map.put(i, msg);
                        if (isExpired) {
                            ++numExpired;
                        }
                    } else {
                        String msg = "&3#" + charterPoint.getInt("charter_point_id") + "&7 - (&b" +
                                formatter.format(charterPoint.get("date_issued")) + "&7) &c" + charterPoint.getInt("amount") +
                                "&o point(s)&7 issued to &c" + charterPoint.getString("issued_to") + " &7&oby &l&d" + charterPoint.getString("issued_by") + "&r.&3&o \nReason: &7&o" +
                                charterPoint.getString("reason") + " -- &o(Expires: &b&o" +
                                formatter.format(charterPoint.get("date_expired")) + "&b&o)";
                        map.put(i, msg);
                        numActive++;
                    }
                    i++;
                } else {
                    if (!isExpunged && !isExpired) {
                        String msg = "&3#" + charterPoint.getInt("charter_point_id") + "&7 - (&b" +
                                formatter.format(charterPoint.get("date_issued")) + "&7) &c" + charterPoint.getInt("amount") +
                                "&o point(s)&7 issued to &c" + charterPoint.getString("issued_to") + " &7&oby &l&d" + charterPoint.getString("issued_by") + "&r.&3&o \nReason: &7&o" +
                                charterPoint.getString("reason") + " -- &o(Expires: &b&o" +
                                formatter.format(charterPoint.get("date_expired")) + "&b&o)";
                        map.put(i, msg);
                        numActive++;
                    }
                    i++;
                }
            }

            Gson gson = new Gson();

            Player commandSender = (Player) sender;
            try (FileWriter file = new FileWriter(EMI.getPlugin().getDataFolder() + File.separator + "cache" + File.separator + commandSender.getUniqueId().toString() + ".json")) {
                file.write(gson.toJson(map));
                file.flush();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
            paginate(sender, map, 1, EMI.getPlugin().getConfig().getInt("items-per-page"));
            sender.sendMessage(Utils.color("&e==== STATS: " + numActive + " active | " + (points.size() - numActive) + " historical ===="));
            sender.sendMessage(Utils.color("&eUse /cpage [page #] to move to the next page"));
            }
        }
    }
    @CommandPermission("emi.par.charter.recent")
    @Subcommand("recent")
    @CommandAlias("crecent")
    public void onRecentCommand(CommandSender sender)
    {

    }

    @CommandPermission("emi.par.charter.edit")
    @Subcommand("edit")
    @CommandAlias("cedit")
    @Syntax("<id> <new amount> [new reason]")
    public void onEditCommand(CommandSender sender, int pointId, int newPointAmt, @Optional String newReason)
    {
        CharterPoint charterPoint = CharterPoint.getCharterPoint(pointId);
        if(charterPoint == null)
        {
            sender.sendMessage(Utils.color("&cERROR: &eNo record found. Did you enter the right ID number?"));
        }
        else
        {
            int oldAmt = charterPoint.getAmount();
            String oldReason = charterPoint.getReason();
            charterPoint.setAmount(newPointAmt);
            if(newReason != null)
            {
                charterPoint.setReason(newReason);
            }
            else
            {
                newReason = oldReason;
            }
            if(charterPoint.updateCharterPoint(charterPoint, pointId))
            {
                if(oldReason.equals(newReason)) {
                    sender.sendMessage(Utils.color("&9[Charter] &3Success: [Points: " + oldAmt + " -> " + charterPoint.getAmount() + " / Reason unchanged. (" + oldReason + ")]"));
                }
                else
                {
                    sender.sendMessage(Utils.color("&9[Charter] &3Success: [Points: " + oldAmt + " -> " + charterPoint.getAmount() + " / New reason: " + newReason + "]"));
                }
                charterPoint.enforceCharter(sender);
            }
            else
            {
                sender.sendMessage(Utils.color("&cERROR: &e Please report the following to HC..." ));
                sender.sendMessage("RECORD [" + pointId + "] UPDATE TABLE FAIL. CC-onEdit()");
            }
        }
    }
    @CommandPermission("emi.par.charter.remove")
    @Subcommand("remove")
    @CommandAlias("cremove")
    @Syntax("<id>")
    public void onRemoveCommand(CommandSender sender, int pointId)
    {
        CharterPoint charterPoint = CharterPoint.getCharterPoint(pointId);
        if(charterPoint == null)
        {
            sender.sendMessage(Utils.color("&cERROR: &eNo record found. Did you enter the right ID number?"));
        }
        else
        {
            if(charterPoint.removeCharterPoint(pointId))
            {
                sender.sendMessage(Utils.color("&9[Charter] &3The point(s) issued to " + charterPoint.getRecipient().getName() + " have been removed (expunged) from the players history."));
            }
            else
            {
                sender.sendMessage(Utils.color("&9[Charter] &3Could not remove point(s) issued to " + charterPoint.getRecipient().getName() + ". DB error on update. Please notify HC."));
            }
        }
    }
    @CommandPermission("emi.par.charter.pardon")
    @Subcommand("pardon")
    @CommandAlias("cpardon")
    @Syntax("<name? [<remove flag>]")
    public void onPardonCommand(CommandSender sender, String name, @Default("true") boolean removeFlag)
    {
        Player player = (Player) sender;
        long pardonPointSuccess = CharterPoint.pardonPlayer(name, player, removeFlag);
        if(pardonPointSuccess == 0)
        {
            player.sendMessage(Utils.color("&9[Charter] &3Could not pardon " + name + ". Either there is no player by this name, it has changed, or its misspelled."));
        }
        else
        {
            DbRow playerRow = PlayerUtils.getPlayerRow(name);
            String playerName = playerRow.getString("player_name");
            String altName = playerRow.getString("alt_name");
            Bukkit.getBanList(BanList.Type.NAME).pardon(playerName);
            if (altName != null) {
                Bukkit.getBanList(BanList.Type.NAME).pardon(altName);
            }
            player.sendMessage(Utils.color("&9[Charter] &3" + name + " and any listed alts have been pardoned and had their points set to 1."));
        }
    }

    private void paginate(CommandSender sender, SortedMap<Integer, String> map,
                          int page, int pageLength) {
        sender.sendMessage(ChatColor.YELLOW + "List: Page (" + String.valueOf(page) + " of " + (((map.size() % pageLength) == 0) ? map.size() / pageLength : (map.size() / pageLength) + 1) + ")");
        int i = 0, k = 0;
        page--;
        for (final Map.Entry<Integer, String> e : map.entrySet()) {
            k++;
            if ((((page * pageLength) + i + 1) == k) && (k != ((page * pageLength) + pageLength + 1))) {
                i++;
                sender.sendMessage(Utils.color(e.getValue()));
            }
        }
    }
}
