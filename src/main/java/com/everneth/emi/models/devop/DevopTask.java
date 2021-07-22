package com.everneth.emi.models.devop;

public class DevopTask
{
    private long id;
    private final long projectID;
    private final String task;
    private int complete;
    private int focused;

    public DevopTask(long id, long projectID, String task, int complete, int focused)
    {
        this.id = id;
        this.projectID = projectID;
        this.task = task;
        this.complete = complete;
        this.focused = focused;
    }

    public DevopTask(long projectID, String task, int complete, int focused)
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

    public String getTask()
    {
        return task;
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
