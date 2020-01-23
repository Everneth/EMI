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
    private HashMap<String, Motd> motds = new HashMap<>();
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
            DB.executeInsert("INSERT INTO motds (sanitized_tag, tag, message) VALUES (?, ?, ?)",
                    motd.getSanitizedTag(),
                    motd.getTag(),
                    motd.getMessage());
            motds.put(motd.getSanitizedTag(), motd);
        }
        catch(SQLException e)
        {
            Bukkit.getLogger().info("ERROR: MotdManager/addMotd: " + e.toString());
        }
    }

    public void updateMotd(Motd motd)
    {
        try
        {
            DB.executeUpdate("UPDATE motds SET tag = ?, message = ? WHERE sanitized_tag = ?",
                    motd.getTag(),
                    motd.getMessage(),
                    motd.getSanitizedTag());
            motds.get(motd.getSanitizedTag()).setTag(motd.getTag());
            motds.get(motd.getSanitizedTag()).setMessage(motd.getMessage());
        }
        catch(SQLException e)
        {
            Bukkit.getLogger().info("ERROR: MotdManager/updateMotd: " + e.toString());
        }
    }

    public void deleteMotd(Motd motd)
    {
        try
        {
            DB.executeUpdate("DELETE FROM motds WHERE sanitized_tag = ?",
                    motd.getSanitizedTag());
            motds.remove(motd.getSanitizedTag());
        }
        catch(SQLException e)
        {
            Bukkit.getLogger().info("ERROR: MotdManager/deleteMotd: " + e.toString());
        }
    }

    public HashMap<String, Motd> getMotds()
    {
        return motds;
    }
}
