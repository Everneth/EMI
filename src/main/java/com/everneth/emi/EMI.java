package com.everneth.emi;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *     Class: EMI
 *     Author: Faceman (@TptMike)
 *     Purpose: Plugin main class launcher
 *
 */

public class EMI extends JavaPlugin {
    private static EMI plugin;
    Configuration configs;
    @Override
    public void onEnable() {
        plugin = this;
        configs = new Configuration();
        configs.startup();
    }
    @Override
    public void onDisable() {
        configs.shutdown();
    }

    // Next 4 lines are legacy and should eventually phase out once the Configuration class takes over
    public static EMI getPlugin() { return plugin; }
    public static Long getConfigLong(String path) { return plugin.getConfig().getLong(path); }
    public static String getConfigString(String path) { return plugin.getConfig().getString(path); }
    public static Guild getGuild() { return EMI.getJda().getGuildById(plugin.getConfig().getLong("guild-id")); }
    public static JDA getJda() { return Configuration.getJda();  }
}