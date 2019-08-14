package com.everneth.emi.models;

import java.util.ArrayList;
import java.util.HashMap;

public class MintProject
{
    private EMIPlayer lead;
    private String name;
    private String startDate;
    private String endDate;
    private boolean complete;
    private boolean focused;
    private String description;
    private HashMap<Long, MintWorkLog> workLog = new HashMap<>();
    private HashMap<Long, MintMaterialLog> materialLog = new HashMap<>();
    private HashMap<Long, MintTaskRequirement> taskRequirements = new HashMap<>();
    private HashMap<Long, MintMaterialReuirement> materialRequirements = new HashMap<>();

    public MintProject(EMIPlayer lead, String name, String startDate, String endDate, boolean complete, boolean focused, String description)
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
        return complete;
    }

    public void setComplete(boolean complete)
    {
        this.complete = complete;
    }

    public boolean isFocused()
    {
        return focused;
    }

    public void setFocused(boolean focused)
    {
        this.focused = focused;
    }

    public HashMap<Long, MintWorkLog> getWorkLog()
    {
        return workLog;
    }

    public void setWorkLog(HashMap<Long, MintWorkLog> workLog)
    {
        this.workLog = workLog;
    }

    public HashMap<Long, MintMaterialLog> getMaterialLog()
    {
        return materialLog;
    }

    public void setMaterialLog(HashMap<Long, MintMaterialLog> materialLog)
    {
        this.materialLog = materialLog;
    }

    public HashMap<Long, MintTaskRequirement> getTaskRequirements()
    {
        return taskRequirements;
    }

    public void setTaskRequirements(HashMap<Long, MintTaskRequirement> taskRequirements)
    {
        this.taskRequirements = taskRequirements;
    }

    public HashMap<Long, MintMaterialReuirement> getMaterialRequirements()
    {
        return materialRequirements;
    }

    public void setMaterialRequirements(HashMap<Long, MintMaterialReuirement> materialRequirements)
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
