package com.everneth.emi;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *     Class: Utils
 *     Author: Redstonehax (@SterlingHeaton)
 *     Purpose: Debugger utility and chat color utility class
 *
 */

public class Utils
{
    public static String chatTag;

    public static String color(String color)
    {
        return ChatColor.translateAlternateColorCodes('&', color);
    }

    public static void bugTest(String message)
    {
        Bukkit.broadcastMessage(Utils.color("&8[&4BugTest&8]" + "&7 " + message));
    }

    public static void bugTest(int message)
    {
        Bukkit.broadcastMessage(Utils.color("&8[&4BugTest&8]" + "&7 " + message));
    }

    public static String buildMessage(String[] parts, int start, boolean separator)
    {
        StringBuilder message = new StringBuilder();
        for(int count = start; count < parts.length; count++)
        {
            if(separator)
            {
                if(count + 1 == parts.length)
                {
                    message.append(parts[count] + " ");
                }
                else
                {
                    message.append(parts[count] + ", ");
                }
                continue;
            }
            message.append(parts[count] + " ");
        }
        return message.toString();
    }

    public static String getCurrentDate()
    {
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return simpleDateFormat.format(date);
    }
}
