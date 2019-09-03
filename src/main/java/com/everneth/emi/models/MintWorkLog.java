package com.everneth.emi.models;

public class MintWorkLog
{
    private EMIPlayer loggedBy;
    private EMIPlayer validatedBy;
    private long workID;
    private int validated;
    private String workLength;
    private String logDate;
    private String description;

    public MintWorkLog(EMIPlayer loggedBy, EMIPlayer validatedBy, int validated, String workLength, String logDate, String description)
    {
        this.loggedBy = loggedBy;
        this.validatedBy = validatedBy;
        this.validated = validated;
        this.workLength = workLength;
        this.logDate = logDate;
        this.description = description;
    }

    public MintWorkLog(long workID, EMIPlayer loggedBy, EMIPlayer validatedBy, int validated, String workLength, String logDate, String description)
    {
        this.loggedBy = loggedBy;
        this.validatedBy = validatedBy;
        this.workID = workID;
        this.validated = validated;
        this.workLength = workLength;
        this.logDate = logDate;
        this.description = description;
    }

    public long getWorkID()
    {
        return workID;
    }

    public void setWorkID(long workID)
    {
        this.workID = workID;
    }

    public EMIPlayer getLoggedBy()
    {
        return loggedBy;
    }

    public void setLoggedBy(EMIPlayer loggedBy)
    {
        this.loggedBy = loggedBy;
    }

    public EMIPlayer getValidatedBy()
    {
        return validatedBy;
    }

    public void setValidatedBy(EMIPlayer validatedBy)
    {
        this.validatedBy = validatedBy;
    }

    public int getValidated()
    {
        return validated;
    }

    public void setValidated(int validated)
    {
        this.validated = validated;
    }

    public String getWorkLength()
    {
        return workLength;
    }

    public void setWorkLength(String workLength)
    {
        this.workLength = workLength;
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
