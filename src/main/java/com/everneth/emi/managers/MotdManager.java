package com.everneth.emi.managers;

import co.aikar.idb.DB;
import com.everneth.emi.models.Motd;
import org.bukkit.Bukkit;

import java.sql.SQLException;
import java.util.HashMap;

public class MotdManager
{
    private static MotdManager motdManager;
    private MotdManager() {}
    private HashMap<Long, Motd> motds = new HashMap<>();
    public static MotdManager getMotdManager()
    {
        if(motdManager == null)
        {
            motdManager = new MotdManager();
        }
        return motdManager;
    }

    public void addMotd(Motd motd)
    {
        try
        {
            Long motdID = DB.executeInsert("INSERT INTO motds (player_id, tag, message) VALUES (?, ?, ?)",
                    motd.getPlayer().getId(),
                    motd.getTag(),
                    motd.getMessage());
            motd.setId(motdID);
            motds.put(motdID, motd);
        }
        catch(SQLException e)
        {
            Bukkit.getLogger().info("ERROR: MotdManager/addMotd: " + e.toString());
        }
    }

    public HashMap<Long, Motd> getMotds()
    {
        return motds;
    }
}
