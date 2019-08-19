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

    public HashMap<Long, MintProject> getProjects()
    {
        return projects;
    }

    public void addProject(long projectID, MintProject project)
    {
        projects.put(projectID, project);
    }

    public void addProject(MintProject mintProject)
    {
        Long projectID;

        try
        {
            projectID = DB.executeInsert("INSERT INTO mint_project (project_lead, project_name, start_date, complete, focused, description) VALUES (?, ?, ?, ?, ?, ?)",
                    mintProject.getLead(),
                    mintProject.getName(),
                    mintProject.getStartDate(),
                    mintProject.getComplete(),
                    mintProject.getFocused(),
                    mintProject.getDescription());
        }
        catch (SQLException e)
        {
            Bukkit.getLogger().info("MintProjectManager/addProject(MintProject) ERROR: " + e.toString());
            return;
        }
        projects.put(projectID, mintProject);
    }
}
