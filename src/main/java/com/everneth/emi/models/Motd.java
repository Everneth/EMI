package com.everneth.emi.models;

public class Motd
{
    private String sanitizedTag;
    private String tag;
    private String message;

    public Motd(String sanitizedTag, String tag, String message)
    {
        this.sanitizedTag = sanitizedTag;
        this.tag = tag;
        this.message = message;
    }

    public String displayMotd()
    {
        return ("&7[" + tag + "&7] &f" + message);
    }

    public String getSanitizedTag()
    {
        return sanitizedTag;
    }

    public void setSanitizedTag(String sanitizedTag)
    {
        this.sanitizedTag = sanitizedTag;
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
