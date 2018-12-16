package com.everneth.emi.tournament;

import com.everneth.emi.models.EventCreation;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class TournamentData
{
    private static TournamentData ourInstance = new TournamentData();
    private HashMap<Player, EventCreation> newEvents = new HashMap<>();

    public static TournamentData getInstance()
    {
        return ourInstance;
    }

    private TournamentData()
    {
    }

    public HashMap<Player, EventCreation> getNewEvents()
    {
        return newEvents;
    }
}
