package com.everneth.emi.models;

import java.util.ArrayList;
import java.util.HashMap;

public class MintProject
{
    private EMIPlayer lead;
    private String name;
    private String startDate;
    private String endDate;
    private int complete;
    private int focused;
    private String description;
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

    public boolean isComplete()
    {
        return complete != 0;
    }

    public void setComplete(int complete)
    {
        this.complete = complete;
    }

    public boolean isFocused()
    {
        return focused != 0;
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
}
