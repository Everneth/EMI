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
        try
        {
            long projectID = DB.executeInsert("INSERT INTO mint_project (leader, name, start_date, end_date, complete, focused, description) VALUES (?, ?, ?, ?, 0, 0, ?)",
                    mintProject.getLeader().getId(),
                    mintProject.getName(),
                    mintProject.getStartDate(),
                    null,
                    mintProject.getDescription());
            mintProject.setId(projectID);
            projects.put(projectID, mintProject);
        }
        catch (SQLException e)
        {
            Bukkit.getLogger().info("MintProjectManager/addProject(MintProject) ERROR: " + e.toString());
        }
    }

    public void switchFocus(MintProject newFocus, MintProject formerFocus)
    {
        try
        {
            if(newFocus == formerFocus)
            {
                DB.executeUpdate("UPDATE mint_project SET focused = 0 WHERE project_id = ?",
                        newFocus.getId());
                newFocus.setFocused(0);
                return;
            }

            if(formerFocus != null)
            {
                DB.executeUpdate("UPDATE mint_project SET focused = 0 WHERE project_id = ?",
                        formerFocus.getId());
                formerFocus.setFocused(0);
            }
            DB.executeUpdate("UPDATE mint_project SET focused = 1 WHERE project_id = ?",
                    newFocus.getId());
            newFocus.setFocused(1);
        }
        catch (SQLException e)
        {
            Bukkit.getLogger().info("ERROR: MintProjectManager/switchFocus: " + e.toString());
        }
    }

    public void unFocus(MintProject project)
    {
        try
        {
            DB.executeUpdate("UPDATE mint_project SET focused = 0 WHERE project_id = ?",
                    project.getId());
            project.setFocused(0);
        }
        catch (SQLException e)
        {
            Bukkit.getLogger().info("ERROR: MintProjectManager/unFocus: " + e.toString());
        }
    }
}
