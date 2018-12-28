package com.everneth.emi;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

/**
 *     Class: Utils
 *     Author: Redstpmejax (@SterlingHeaton)
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
}
