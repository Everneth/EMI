package com.everneth.emi.managers;

import co.aikar.idb.DB;
import com.everneth.emi.models.devop.DevopProject;
import org.bukkit.Bukkit;

import java.sql.SQLException;
import java.util.HashMap;

public class DevopProjectManager
{
    private static DevopProjectManager devopProjectManager;
    private DevopProjectManager() {}
    private final HashMap<Long, DevopProject> projects = new HashMap<>();

    public static DevopProjectManager getDevopProjectManager()
    {
        if(devopProjectManager == null)
        {
            devopProjectManager = new DevopProjectManager();
        }
        return devopProjectManager;
    }

    /**
     * This method gets the project if it exists.
     *
     * @param projectName Input for the project name
     *
     * @return Returns the project object or null if it doesnt exist
     */
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

    /**
     * This method gets all of the projects currently in memory.
     *
     * @return Returns a hashmap of all of the projects
     */
    public HashMap<Long, DevopProject> getProjects()
    {
        return projects;
    }

    /**
     * This method adds a project to the project list when the server first starts up.
     *
     * @param projectID Input for the projectID
     * @param project   Input for the project object
     */
    public void addProject(long projectID, DevopProject project)
    {
        projects.put(projectID, project);
    }

    /**
     * This method adds a project to the database and memory upon creation.
     *
     * @param devopProject Input for the project
     */
    public void addProject(DevopProject devopProject)
    {
        try
        {
            long projectID = DB.executeInsert("INSERT INTO devop_project (leader, name, start_date, end_date, complete, focused, description) VALUES (?, ?, ?, ?, 0, 0, ?)",
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
            Bukkit.getLogger().info("DevopProjectManager/addProject(DevopProject) ERROR: " + e.toString());
        }
    }

    /**
     * This method switches the focus for the project in the database and memory.
     *
     * @param newFocus    Input for the new project
     * @param formerFocus Input for the current project
     */
    public void switchFocus(DevopProject newFocus, DevopProject formerFocus)
    {
        try
        {
            if(newFocus == formerFocus)
            {
                DB.executeUpdate("UPDATE devop_project SET focused = 0 WHERE project_id = ?",
                        newFocus.getId());
                newFocus.setFocused(0);
                return;
            }

            if(formerFocus != null)
            {
                DB.executeUpdate("UPDATE devop_project SET focused = 0 WHERE project_id = ?",
                        formerFocus.getId());
                formerFocus.setFocused(0);
            }
            DB.executeUpdate("UPDATE devop_project SET focused = 1 WHERE project_id = ?",
                    newFocus.getId());
            newFocus.setFocused(1);
        }
        catch (SQLException e)
        {
            Bukkit.getLogger().info("ERROR: DevopProjectManager/switchFocus: " + e.toString());
        }
    }

    /**
     * This method unfocuses the current project in the database and memory.
     *
     * @param project Input for the project
     */
    public void unFocus(DevopProject project)
    {
        try
        {
            DB.executeUpdate("UPDATE devop_project SET focused = 0 WHERE project_id = ?",
                    project.getId());
            project.setFocused(0);
        }
        catch (SQLException e)
        {
            Bukkit.getLogger().info("ERROR: DevopProjectManager/unFocus: " + e.toString());
        }
    }
}
