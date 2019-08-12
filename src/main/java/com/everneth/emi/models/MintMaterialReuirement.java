package com.everneth.emi.models;

import org.bukkit.Material;

public class MintMaterialReuirement
{
    private Material material;
    private int amount;
    private boolean complete;
    private boolean focused;

    public MintMaterialReuirement(Material material, int amount, boolean complete, boolean focused)
    {
        this.material = material;
        this.amount = amount;
        this.complete = complete;
        this.focused = focused;
    }

    public Material getMaterial()
    {
        return material;
    }

    public void setMaterial(Material material)
    {
        this.material = material;
    }

    public int getAmount()
    {
        return amount;
    }

    public void setAmount(int amount)
    {
        this.amount = amount;
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
