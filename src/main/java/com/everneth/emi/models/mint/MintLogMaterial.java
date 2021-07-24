package com.everneth.emi.models.mint;

import com.everneth.emi.models.EMIPlayer;

public class MintLogMaterial
{
    private long id;
    private final long projectID;
    private final long materialID;
    private final EMIPlayer logger;
    private EMIPlayer validater;
    private int validated;
    private final int materialCollected;
    private final int timeWorked;
    private final String logDate;
    private final String description;

    public MintLogMaterial(long id, long projectID, long materialID, EMIPlayer logger, EMIPlayer validater, int validated, int materialCollected, int timeWorked, String logDate, String description)
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

    public MintLogMaterial(long projectID, long materialID, EMIPlayer logger, EMIPlayer validater, int validated, int materialCollected, int timeWorked, String logDate, String description)
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

    public long getMaterialID()
    {
        return materialID;
    }

    public EMIPlayer getLogger()
    {
        return logger;
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

    public int getTimeWorked()
    {
        return timeWorked;
    }

    public String getLogDate()
    {
        return logDate;
    }

    public String getDescription()
    {
        return description;
    }
}
