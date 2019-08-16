package com.everneth.emi.models;

public class MintTaskRequirement
{
    private String task;
    private int complete;
    private int focused;

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

    public boolean isComplete()
    {
        return complete != 0;
    }

    public void setComplete(int complete)
    {
        this.complete = complete;
    }

    public boolean isFocused()
    {
        return focused != 0;
    }

    public void setFocused(int focused)
    {
        this.focused = focused;
    }
}
