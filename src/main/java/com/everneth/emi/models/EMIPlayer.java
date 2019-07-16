package com.everneth.emi.models;

public class EMIPlayer {
    private String uuid;
    private String name;
    private int id;

    public EMIPlayer(String uuid, String name)
    {
        this.uuid = uuid;
        this.name = name;
        this.id = 0;
    }
    public EMIPlayer(String uuid, String name, int id)
    {
        this.uuid = uuid;
        this.name = name;
        this.id = id;
    }

    public String getUniqueId()
    {
        return this.uuid;
    }

    public String getName()
    {
        return this.name;
    }

    public int getId()
    {
        return this.id;
    }

    public void setUniqueId(String uuid)
    {
        this.uuid = uuid;
    }

    public void setName(String name)
    {
        this.name = name;
    }
    public void setId(int id)
    {
        this.id = id;
    }
}
