package com.everneth.emi.models.devop;

public class DevopMaterial
{
    private long id;
    private final long projectID;
    private final String material;
    private final int total;
    private int collected;
    private int complete;
    private int focused;

    public DevopMaterial(long id, long projectID, String material, int total, int collected, int complete, int focused)
    {
        this.id = id;
        this.projectID = projectID;
        this.material = material;
        this.total = total;
        this.collected = collected;
        this.complete = complete;
        this.focused = focused;
    }

    public DevopMaterial(long projectID, String material, int total, int collected, int complete, int focused)
    {
        this.projectID = projectID;
        this.material = material;
        this.total = total;
        this.collected = collected;
        this.complete = complete;
        this.focused = focused;
    }

    private boolean validated;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public String getMaterial()
    {
        return material;
    }

    public int getTotal()
    {
        return total;
    }

    public int getCollected()
    {
        return collected;
    }

    public void setCollected(int collected)
    {
        this.collected = collected;
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

    public boolean isValidated()
    {
        return validated;
    }

    public void setValidated(boolean validated)
    {
        this.validated = validated;
    }
}
