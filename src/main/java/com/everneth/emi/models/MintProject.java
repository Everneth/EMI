package com.everneth.emi.models;

import co.aikar.idb.DB;
import com.everneth.emi.Utils;
import org.bukkit.Bukkit;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class MintProject
{
    private long projectID;
    private EMIPlayer lead;
    private String name;
    private String startDate;
    private String endDate;
    private int complete;
    private int focused;
    private String description;
    private ArrayList<EMIPlayer> workers = new ArrayList<>();
    private HashMap<Integer, MintWorkLog> workLog = new HashMap<>();
    private HashMap<Integer, MintMaterialLog> materialLog = new HashMap<>();
    private HashMap<Integer, MintTaskRequirement> taskRequirements = new HashMap<>();
    private HashMap<Integer, MintMaterialReuirement> materialRequirements = new HashMap<>();

    public MintProject(EMIPlayer lead, String name, String startDate, String endDate, int complete, int focused, String description)
    {
        this.lead = lead;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.complete = complete;
        this.focused = focused;
        this.description = description;
    }

    public MintProject(long projectID, EMIPlayer lead, String name, String startDate, String endDate, int complete, int focused, String description)
    {
        this.projectID = projectID;
        this.lead = lead;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.complete = complete;
        this.focused = focused;
        this.description = description;
    }

    public void addWorker(EMIPlayer emiPlayer)
    {
        try
        {
            DB.executeInsert("INSERT INTO mint_project_join_log (player_id, project_id, join_date) VALUES (?, ?, ?)",
                    emiPlayer.getId(),
                    projectID, Utils.getCurrentDate());
            workers.add(emiPlayer);
        }
        catch (SQLException e)
        {
            Bukkit.getLogger().info("ERROR: MintProject/addPlayer: " + e.toString());
        }
    }

    public void complete()
    {
        try
        {
            DB.executeUpdate("UPDATE mint_project SET end_date = ?, complete = 1 WHERE project_id = ?",
                    Utils.getCurrentDate(),
                    projectID);
            complete = 1;
        }
        catch (SQLException e)
        {
            Bukkit.getLogger().info("ERROR: MintProject/complete: " + e.toString());
        }
    }

    public long getProjectID()
    {
        return projectID;
    }

    public void setProjectID(long projectID)
    {
        this.projectID = projectID;
    }

    public EMIPlayer getLead()
    {
        return lead;
    }

    public void setLead(EMIPlayer lead)
    {
        this.lead = lead;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getStartDate()
    {
        return startDate;
    }

    public void setStartDate(String startDate)
    {
        this.startDate = startDate;
    }

    public String getEndDate()
    {
        return endDate;
    }

    public void setEndDate(String endDate)
    {
        this.endDate = endDate;
    }

    public int getComplete()
    {
        return complete;
    }

    public void setComplete(int complete)
    {
        this.complete = complete;
    }

    public int getFocused()
    {
        return focused;
    }

    public void setFocused(int focused)
    {
        this.focused = focused;
    }

    public HashMap<Integer, MintWorkLog> getWorkLog()
    {
        return workLog;
    }

    public void setWorkLog(HashMap<Integer, MintWorkLog> workLog)
    {
        this.workLog = workLog;
    }

    public HashMap<Integer, MintMaterialLog> getMaterialLog()
    {
        return materialLog;
    }

    public void setMaterialLog(HashMap<Integer, MintMaterialLog> materialLog)
    {
        this.materialLog = materialLog;
    }

    public HashMap<Integer, MintTaskRequirement> getTaskRequirements()
    {
        return taskRequirements;
    }

    public void setTaskRequirements(HashMap<Integer, MintTaskRequirement> taskRequirements)
    {
        this.taskRequirements = taskRequirements;
    }

    public HashMap<Integer, MintMaterialReuirement> getMaterialRequirements()
    {
        return materialRequirements;
    }

    public void setMaterialRequirements(HashMap<Integer, MintMaterialReuirement> materialRequirements)
    {
        this.materialRequirements = materialRequirements;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public ArrayList<EMIPlayer> getWorkers()
    {
        return workers;
    }

    public void setWorkers(ArrayList<EMIPlayer> workers)
    {
        this.workers = workers;
    }

    @Override
    public String toString()
    {
        return "MintProject{" + "lead=" + lead + ", name='" + name + '\'' + ", startDate='" + startDate + '\'' + ", endDate='" + endDate + '\'' + ", complete=" + complete + ", focused=" + focused + ", description='" + description + '\'' + ", workLog=" + workLog + ", materialLog=" + materialLog + ", taskRequirements=" + taskRequirements + ", materialRequirements=" + materialRequirements + '}';
    }
}
