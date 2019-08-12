package com.everneth.emi.models;

public class MintTaskRequirement
{
    private String task;
    private boolean complete;
    private boolean focused;

    public MintTaskRequirement(String task, boolean complete, boolean focused)
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
        return complete;
    }

    public void setComplete(boolean complete)
    {
        this.complete = complete;
    }

    public boolean isFocused()
    {
        return focused;
    }

    public void setFocused(boolean focused)
    {
        this.focused = focused;
    }
}
