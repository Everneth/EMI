package com.everneth.emi.models.mint;

import com.everneth.emi.models.EMIPlayer;

public class MIntLogTask
{
    private long id;
    private long projectID;
    private EMIPlayer logger;
    private EMIPlayer validater;
    private int validated;
    private int timeWorked;
    private String logDate;
    private String description;

    public MIntLogTask(long id, long projectID, EMIPlayer logger, EMIPlayer validater, int validated, int timeWorked, String logDate, String description)
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

    public MIntLogTask(long projectID, EMIPlayer logger, EMIPlayer validater, int validated, int timeWorked, String logDate, String description)
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

    public long getProjectID()
    {
        return projectID;
    }

    public void setProjectID(long projectID)
    {
        this.projectID = projectID;
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
