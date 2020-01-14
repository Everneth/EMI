package com.everneth.emi.models.mint;

public class MintTask
{
    private long id;
    private long projectID;
    private String task;
    private int complete;
    private int focused;

    public MintTask(long id, long projectID, String task, int complete, int focused)
    {
        this.id = id;
        this.projectID = projectID;
        this.task = task;
        this.complete = complete;
        this.focused = focused;
    }

    public MintTask(long projectID, String task, int complete, int focused)
    {
        this.projectID = projectID;
        this.task = task;
        this.complete = complete;
        this.focused = focused;
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

    public String getTask()
    {
        return task;
    }

    public void setTask(String task)
    {
        this.task = task;
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
}
