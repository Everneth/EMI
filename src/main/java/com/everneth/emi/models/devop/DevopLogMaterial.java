package com.everneth.emi.models.devop;

import com.everneth.emi.models.EMIPlayer;

public class DevopLogMaterial
{
    private long id;
    private long projectID;
    private long materialID;
    private EMIPlayer logger;
    private EMIPlayer validater;
    private int validated;
    private int materialCollected;
    private int timeWorked;
    private String logDate;
    private String description;

    public DevopLogMaterial(long id, long projectID, long materialID, EMIPlayer logger, EMIPlayer validater, int validated, int materialCollected, int timeWorked, String logDate, String description)
    {
        this.id = id;
        this.projectID = projectID;
        this.materialID = materialID;
        this.logger = logger;
        this.validater = validater;
        this.validated = validated;
        this.materialCollected = materialCollected;
        this.timeWorked = timeWorked;
        this.logDate = logDate;
        this.description = description;
    }

    public DevopLogMaterial(long projectID, long materialID, EMIPlayer logger, EMIPlayer validater, int validated, int materialCollected, int timeWorked, String logDate, String description)
    {
        this.projectID = projectID;
        this.materialID = materialID;
        this.logger = logger;
        this.validater = validater;
        this.validated = validated;
        this.materialCollected = materialCollected;
        this.timeWorked = timeWorked;
        this.logDate = logDate;
        this.description = description;
    }

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public long getProjectID()
    {
        return projectID;
    }

    public void setProjectID(long projectID)
    {
        this.projectID = projectID;
    }

    public long getMaterialID()
    {
        return materialID;
    }

    public void setMaterialID(long materialID)
    {
        this.materialID = materialID;
    }

    public EMIPlayer getLogger()
    {
        return logger;
    }

    public void setLogger(EMIPlayer logger)
    {
        this.logger = logger;
    }

    public EMIPlayer getValidater()
    {
        return validater;
    }

    public void setValidater(EMIPlayer validater)
    {
        this.validater = validater;
    }

    public int getValidated()
    {
        return validated;
    }

    public void setValidated(int validated)
    {
        this.validated = validated;
    }

    public int getMaterialCollected()
    {
        return materialCollected;
    }

    public void setMaterialCollected(int materialCollected)
    {
        this.materialCollected = materialCollected;
    }

    public int getTimeWorked()
    {
        return timeWorked;
    }

    public void setTimeWorked(int timeWorked)
    {
        this.timeWorked = timeWorked;
    }

    public String getLogDate()
    {
        return logDate;
    }

    public void setLogDate(String logDate)
    {
        this.logDate = logDate;
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
