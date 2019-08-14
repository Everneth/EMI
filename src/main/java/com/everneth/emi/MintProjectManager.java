package com.everneth.emi;

import co.aikar.idb.DB;
import com.everneth.emi.models.MintProject;
import org.bukkit.Bukkit;

import java.sql.SQLException;
import java.util.HashMap;

public class MintProjectManager
{
    private static MintProjectManager mintProjectManager;
    private MintProjectManager() {}
    private HashMap<Long, MintProject> projects = new HashMap<>();
    public static MintProjectManager getMintProjectManager()
    {
        if(mintProjectManager == null)
        {
            mintProjectManager = new MintProjectManager();
        }
        return mintProjectManager;
    }

    public MintProject getProject(String projectName)
    {
        for(MintProject project : projects.values())
        {
            if(projectName.equalsIgnoreCase(project.getName()))
            {
                return project;
            }
        }
        return null;
    }

    public void addProject(MintProject mintProject)
    {
        Long projectID;

        try
        {
            projectID = DB.executeInsert("INSERT INTO mint_projects (project_lead, project_name, start_date, complete, focused, description VALUES (?, ?, ?, ?, ?, ?)",
                    mintProject.getLead(),
                    mintProject.getName(),
                    mintProject.getStartDate(),
                    mintProject.isComplete(),
                    mintProject.isFocused(),
                    mintProject.getDescription());
        }
        catch (SQLException e)
        {
            Bukkit.getLogger().info(e.toString());
            return;
        }
        projects.put(projectID, mintProject);
    }
}
