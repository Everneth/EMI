package com.everneth.emi.models;

public class Motd
{
    private Long id;
    private EMIPlayer player;
    private String tag;
    private String message;

    public Motd(EMIPlayer player, String tag, String message)
    {
        this.player = player;
        this.tag = tag;
        this.message = message;
    }

    public Motd(Long id, EMIPlayer player, String tag, String message)
    {
        this.id = id;
        this.player = player;
        this.tag = tag;
        this.message = message;
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public EMIPlayer getPlayer()
    {
        return player;
    }

    public void setPlayerID(EMIPlayer player)
    {
        this.player = player;
    }

    public String getTag()
    {
        return tag;
    }

    public void setTag(String tag)
    {
        this.tag = tag;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }
}
