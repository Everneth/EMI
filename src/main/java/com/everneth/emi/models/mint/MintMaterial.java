package com.everneth.emi.models.mint;

public class MintMaterial
{
    private String material;
    private long materialID;
    private int amount;
    private int complete;
    private int focused;

    public MintMaterial(long materialID, String material, int amount, int complete, int focused)
    {
        this.material = material;
        this.materialID = materialID;
        this.amount = amount;
        this.complete = complete;
        this.focused = focused;
    }

    public MintMaterial(String material, int amount, int complete, int focused)
    {
        this.material = material;
        this.amount = amount;
        this.complete = complete;
        this.focused = focused;
    }

    public String getMaterial()
    {
        return material;
    }

    public void setMaterial(String material)
    {
        this.material = material;
    }

    public long getMaterialID()
    {
        return materialID;
    }

    public void setMaterialID(long materialID)
    {
        this.materialID = materialID;
    }

    public int getAmount()
    {
        return amount;
    }

    public void setAmount(int amount)
    {
        this.amount = amount;
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
