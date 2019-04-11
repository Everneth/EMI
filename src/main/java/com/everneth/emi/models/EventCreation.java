package com.everneth.emi.models;

import com.everneth.emi.Utils;

import java.lang.reflect.Type;

public class EventCreation
{
    public static final String CHATTAG = "&7[&cEvent&6Creator&7] &f";
    public static final String NAMEMESSAGE = CHATTAG + "Enter in the name for your event.";
    public static final String LINKMESSAGE = CHATTAG + "Enter in the link for your event.";
    public static final String DESCRIPTIONMESSAGE = CHATTAG + "Enter in the description for your event.";
    public static final String DATEMESSAGE = CHATTAG + "Enter in the date for your event. &aExample&7: &f2019-12-31 16:59:00";
    public static final String TYPEMESSAGE = CHATTAG + "Enter in the type of event you want."; //TODO Auto populate the existing types in the event_types table.
    public static final String LOCATIONMESSAGE = CHATTAG + "Enter in the location of your event. &aExample&7: &f0 67 0";

    private String name;
    private String date;
    private String link;
    private String description;
    private int type;
    private int playerId;
    private int x;
    private int y;
    private int z;
    private int step;
    private boolean replaceVar;

    public void setData(Type type)
    {
        switch(step)
        {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
        }
    }

    public EventCreation(int playerId)
    {
        this.playerId = playerId;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDate()
    {
        return date;
    }

    public void setDate(String date)
    {
        this.date = date;
    }

    public String getLink()
    {
        return link;
    }

    public void setLink(String link)
    {
        this.link = link;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public int getType()
    {
        return type;
    }

    public void setType(int type)
    {
        this.type = type;
    }

    public int getPlayerId()
    {
        return playerId;
    }

    public int getX()
    {
        return x;
    }

    public void setX(int x)
    {
        this.x = x;
    }

    public int getY()
    {
        return y;
    }

    public void setY(int y)
    {
        this.y = y;
    }

    public int getZ()
    {
        return z;
    }

    public void setZ(int z)
    {
        this.z = z;
    }

    public int getStep()
    {
        return step;
    }

    public void setStep(int step)
    {
        this.step = step;
    }

    public boolean isReplaceVar()
    {
        return replaceVar;
    }

    public void setReplaceVar(boolean replaceVar)
    {
        this.replaceVar = replaceVar;
    }
}
