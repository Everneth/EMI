package com.everneth.emi.events;

import co.aikar.idb.DB;
import com.everneth.emi.EMI;
import com.everneth.emi.Utils;
import com.everneth.emi.models.EventCreation;
import com.everneth.emi.tournament.TournamentData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.sql.SQLException;
import java.util.Set;

public class ChatEvents implements Listener
{
    private EMI plugin;

    public ChatEvents(EMI plugin)
    {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event)
    {
       Player player = event.getPlayer();

        if(TournamentData.getInstance().getNewEvents().containsKey(player))
        {
            chatProcessing(player, event.getMessage());

            event.setCancelled(true);
        }
        else
        {
            Set creators = TournamentData.getInstance().getNewEvents().keySet();
            event.getRecipients().removeAll(creators);
        }
    }
    // Name -> Type -> Description -> Forum Link -> DATE -> Location
    public void chatProcessing(Player player, String message)
    {
        EventCreation potentialEvent = TournamentData.getInstance().getNewEvents().get(player);
        int step = potentialEvent.getStep();

        switch(step)
        {
            default:
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
        }

        if(potentialEvent.isReplaceVar())
        {
            potentialEvent.setReplaceVar(false);
        }
//        Utils.bugTest(message);
//        int step = TournamentData.getInstance().getNewEvents().get(player).getStep();
//
//        switch(step)
//        {
//            case 0:
//                TournamentData.getInstance().getNewEvents().get(player).setName(message);
//                player.sendMessage(Utils.color(TournamentData.getInstance().getNewEvents().get(player).getTYPEMESSAGE()));
//                break;
//            case 1:
//                TournamentData.getInstance().getNewEvents().get(player).setType(Integer.parseInt(message));
//                player.sendMessage(Utils.color(TournamentData.getInstance().getNewEvents().get(player).getDESCRIPTIONMESSAGE()));
//                break;
//            case 2:
//                TournamentData.getInstance().getNewEvents().get(player).setDescription(message);
//                player.sendMessage(Utils.color(TournamentData.getInstance().getNewEvents().get(player).getLINKMESSAGE()));
//                break;
//            case 3:
//                TournamentData.getInstance().getNewEvents().get(player).setLink(message);
//                player.sendMessage(Utils.color(TournamentData.getInstance().getNewEvents().get(player).getDATEMESSAGE()));
//                break;
//            case 4:
//                TournamentData.getInstance().getNewEvents().get(player).setDate(message);
//                player.sendMessage(Utils.color(TournamentData.getInstance().getNewEvents().get(player).getLOCATIONMESSAGE()));
//                break;
//            case 5:
//                int[] location = parseLocation(message);
//                TournamentData.getInstance().getNewEvents().get(player).setX(location[0]);
//                TournamentData.getInstance().getNewEvents().get(player).setY(location[1]);
//                TournamentData.getInstance().getNewEvents().get(player).setZ(location[2]);
//
//                EventCreation potentialEvent = TournamentData.getInstance().getNewEvents().get(player);
//                player.sendMessage(Utils.color(Utils.chatTag + " &f "
//                        + potentialEvent.getName() + " "
//                        + potentialEvent.getType() + " "
//                        + potentialEvent.getDescription() + " "
//                        + potentialEvent.getLink() + " "
//                        + potentialEvent.getDate() + " "
//                        + potentialEvent.getX() + " "
//                        + potentialEvent.getY() + " "
//                        + potentialEvent.getZ()));
//
//                try
//                {
//                    DB.executeInsert("INSERT INTO events (event_name, event_date, created_by, description, x, y, z, forum_link)" +
//                            "VALUES(?, ?, ?, ?, ?, ?, ?, ?)", potentialEvent.getName(), potentialEvent.getDate(), potentialEvent.getPlayerId(), potentialEvent.getDescription(), potentialEvent.getX(), potentialEvent.getY(), potentialEvent.getZ(), potentialEvent.getLink());
//                    TournamentData.getInstance().getNewEvents().remove(player);
//                }
//                catch(SQLException e)
//                {
//                    this.plugin.getLogger().severe("SQL Exception: INSERT INTO events. \n" + e.getMessage());
//                    player.sendMessage(Utils.color(Utils.chatTag + " &cError! events-CE-1. Report to Comms!"));
//                }
//
//                break;
//        }
//
//        step++;
//        TournamentData.getInstance().getNewEvents().get(player).setStep(step);
    }
}
