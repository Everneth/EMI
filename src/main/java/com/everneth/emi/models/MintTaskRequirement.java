package com.everneth.emi.models;

public class MintTaskRequirement
{
    private String task;
    private long taskID;
    private int complete;
    private int focused;

    public MintTaskRequirement(long taskID, String task, int complete, int focused)
    {
        this.taskID = taskID;
        this.task = task;
        this.complete = complete;
        this.focused = focused;
    }

    public MintTaskRequirement(String task, int complete, int focused)
    {
        this.task = task;
        this.complete = complete;
        this.focused = focused;
    }

    public String getTask()
    {
        return task;
    }

    public void setTask(String task)
    {
        this.task = task;
    }

    public long getTaskID()
    {
        return taskID;
    }

    public void setTaskID(long taskID)
    {
        this.taskID = taskID;
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
