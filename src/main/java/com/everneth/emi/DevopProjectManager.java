package com.everneth.emi;

import co.aikar.idb.DB;
import com.everneth.emi.models.devop.DevopProject;
import org.bukkit.Bukkit;

import java.sql.SQLException;
import java.util.HashMap;

public class MintProjectManager
{
    private static MintProjectManager mintProjectManager;
    private MintProjectManager() {}
    private HashMap<Long, DevopProject> projects = new HashMap<>();
    public static MintProjectManager getMintProjectManager()
    {
        if(mintProjectManager == null)
        {
            mintProjectManager = new MintProjectManager();
        }
        return mintProjectManager;
    }

    public DevopProject getProject(String projectName)
    {
        for(DevopProject project : projects.values())
        {
            if(projectName.equalsIgnoreCase(project.getName()))
            {
                return project;
            }
        }
        return null;
    }

    public DevopProject getProject(long projectID)
    {
        return projects.get(projectID);
    }

    public HashMap<Long, DevopProject> getProjects()
    {
        return projects;
    }

    public void addProject(long projectID, DevopProject project)
    {
        projects.put(projectID, project);
    }

    public void addProject(DevopProject devopProject)
    {
        try
        {
            long projectID = DB.executeInsert("INSERT INTO mint_project (leader, name, start_date, end_date, complete, focused, description) VALUES (?, ?, ?, ?, 0, 0, ?)",
                    devopProject.getLeader().getId(),
                    devopProject.getName(),
                    devopProject.getStartDate(),
                    null,
                    devopProject.getDescription());
            devopProject.setId(projectID);
            projects.put(projectID, devopProject);
        }
        catch (SQLException e)
        {
            Bukkit.getLogger().info("MintProjectManager/addProject(MintProject) ERROR: " + e.toString());
        }
    }

    public void switchFocus(DevopProject newFocus, DevopProject formerFocus)
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

    public void unFocus(DevopProject project)
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
