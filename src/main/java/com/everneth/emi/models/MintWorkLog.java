package com.everneth.emi.models;

public class MintWorkLog
{
    private EMIPlayer loggedBy;
    private EMIPlayer validatedBy;
    private boolean validated;
    private int workLength;
    private String logDate;
    private String description;

    public MintWorkLog(EMIPlayer loggedBy, EMIPlayer validatedBy, boolean validated, int workLength, String logDate, String description)
    {
        this.loggedBy = loggedBy;
        this.validatedBy = validatedBy;
        this.validated = validated;
        this.workLength = workLength;
        this.logDate = logDate;
        this.description = description;
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

    public boolean isValidated()
    {
        return validated;
    }

    public void setValidated(boolean validated)
    {
        this.validated = validated;
    }

    public int getWorkLength()
    {
        return workLength;
    }

    public void setWorkLength(int workLength)
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
