package com.everneth.emi;

import co.aikar.idb.DB;
import com.everneth.emi.models.mint.MintProject;
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

    public MintProject getProject(long projectID)
    {
        return projects.get(projectID);
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
                    mintProject.getLead().getId(),
                    mintProject.getName(),
                    mintProject.getStartDate(),
                    mintProject.getComplete(),
                    mintProject.getFocused(),
                    mintProject.getDescription());
        }
        catch (SQLException e)
        {
            Bukkit.getLogger().info("MintProjectManager/addProject(MintProject) ERROR: " + e.toString());
            Bukkit.getLogger().info("MintProjectManager/addProject(MintProject) ERROR: " + mintProject.toString());
            return;
        }
        mintProject.setProjectID(projectID);
        projects.put(projectID, mintProject);
    }

    public void switchFocus(MintProject newFocus, MintProject formerFocus)
    {
        try
        {
            if(formerFocus != null)
            {
                DB.executeUpdate("UPDATE mint_project SET focused = 0 WHERE project_id = ?",
                        formerFocus.getProjectID());
                formerFocus.setFocused(0);
            }
            DB.executeUpdate("UPDATE mint_project SET focused = 1 WHERE project_id = ?",
                    newFocus.getProjectID());
            newFocus.setFocused(1);
        }
        catch (SQLException e)
        {
            Bukkit.getLogger().info("ERROR: MintProjectManager/switchFocus: " + e.toString());
        }
    }
}
