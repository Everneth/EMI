package com.everneth.emi.models.mint;

import com.everneth.emi.models.EMIPlayer;
import org.bukkit.Material;

public class MintLogMaterial
{
    private EMIPlayer loggedBy;
    private EMIPlayer validatedBy;
    private boolean validated;
    private Material material;
    private int materialsCollected;
    private String logDate;
    private String description;

    public MintLogMaterial(EMIPlayer loggedBy, EMIPlayer validatedBy, boolean validated, Material material, int materialsCollected, String logDate, String description)
    {
        this.loggedBy = loggedBy;
        this.validatedBy = validatedBy;
        this.validated = validated;
        this.material = material;
        this.materialsCollected = materialsCollected;
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

    public Material getMaterial()
    {
        return material;
    }

    public void setMaterial(Material material)
    {
        this.material = material;
    }

    public int getMaterialsCollected()
    {
        return materialsCollected;
    }

    public void setMaterialsCollected(int materialsCollected)
    {
        this.materialsCollected = materialsCollected;
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
