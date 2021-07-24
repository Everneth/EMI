package com.everneth.emi.models.mint;

import com.everneth.emi.models.EMIPlayer;

public class MintLogTask
{
    private long id;
    private final long projectID;
    private final EMIPlayer logger;
    private EMIPlayer validater;
    private int validated;
    private final int timeWorked;
    private final String logDate;
    private final String description;

    public MintLogTask(long id, long projectID, EMIPlayer logger, EMIPlayer validater, int validated, int timeWorked, String logDate, String description)
    {
        this.id = id;
        this.projectID = projectID;
        this.logger = logger;
        this.validater = validater;
        this.validated = validated;
        this.timeWorked = timeWorked;
        this.logDate = logDate;
        this.description = description;
    }

    public MintLogTask(long projectID, EMIPlayer logger, EMIPlayer validater, int validated, int timeWorked, String logDate, String description)
    {
        this.projectID = projectID;
        this.logger = logger;
        this.validater = validater;
        this.validated = validated;
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
