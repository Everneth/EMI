package com.everneth.emi.events;

import com.everneth.emi.EMI;
import com.everneth.emi.Utils;
import com.everneth.emi.models.EventCreation;
import com.everneth.emi.tournament.TournamentData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Set;
import java.util.UUID;

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
        Utils.bugTest(message);
        int step = TournamentData.getInstance().getNewEvents().get(player).getStep();

        switch(step)
        {
            case 0:
                TournamentData.getInstance().getNewEvents().get(player).setName(message);
                player.sendMessage(Utils.color(TournamentData.getInstance().getNewEvents().get(player).getTYPEMESSAGE()));
                break;
            case 1:
                TournamentData.getInstance().getNewEvents().get(player).setType(Integer.parseInt(message));
                player.sendMessage(Utils.color(TournamentData.getInstance().getNewEvents().get(player).getDESCRIPTIONMESSAGE()));
                break;
            case 2:
                TournamentData.getInstance().getNewEvents().get(player).setDescription(message);
                player.sendMessage(Utils.color(TournamentData.getInstance().getNewEvents().get(player).getLINKMESSAGE()));
                break;
            case 3:
                TournamentData.getInstance().getNewEvents().get(player).setLink(message);
                player.sendMessage(Utils.color(TournamentData.getInstance().getNewEvents().get(player).getDATEMESSAGE()));
                break;
            case 4:
                TournamentData.getInstance().getNewEvents().get(player).setDate(message);
                player.sendMessage(Utils.color(TournamentData.getInstance().getNewEvents().get(player).getLOCATIONMESSAGE()));
                break;
            case 5:
                int[] location = parseLocation(message);
                TournamentData.getInstance().getNewEvents().get(player).setX(location[0]);
                TournamentData.getInstance().getNewEvents().get(player).setY(location[1]);
                TournamentData.getInstance().getNewEvents().get(player).setZ(location[2]);
                break;
            case 6:
                EventCreation potentialEvent = TournamentData.getInstance().getNewEvents().get(player);
                player.sendMessage(Utils.color(Utils.chatTag + " &f "
                                                + potentialEvent.getName() + " "
                                                + potentialEvent.getType() + " "
                                                + potentialEvent.getDescription() + " "
                                                + potentialEvent.getLink() + " "
                                                + potentialEvent.getDate() + " "
                                                + potentialEvent.getX() + " "
                                                + potentialEvent.getY() + " "
                                                + potentialEvent.getZ()));
        }

        step++;
        TournamentData.getInstance().getNewEvents().get(player).setStep(step);
    }

    private int[] parseLocation(String message)
    {
        int[] location = new int[3];
        String[] splitMessage = message.split(" ");

        for(int i = 0; i < location.length; i++)
        {
            location[i] = Integer.parseInt(splitMessage[i]);
        }

        return location;
    }
}
