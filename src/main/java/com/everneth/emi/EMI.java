package com.everneth.emi;

import co.aikar.commands.BukkitCommandManager;
import co.aikar.idb.*;
import com.everneth.emi.managers.MintProjectManager;
import com.everneth.emi.models.mint.*;
import com.everneth.emi.services.WhitelistService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.bukkit.plugin.java.JavaPlugin;


import java.util.List;

/**
 *     Class: EMI
 *     Author: Faceman (@TptMike)
 *     Purpose: Plugin main class launcher
 *
 */

public class EMI extends JavaPlugin {
    private static EMI plugin;
    private static BukkitCommandManager commandManager;
    Configuration configs = new Configuration(this);
    private static JDA jda;
    @Override
    public void onEnable() {
        configs.startup();
    }
    @Override
    public void onDisable() {
        configs.shutdown();
    }

    // Next 4 lines are legacy and should eventually phase out once the Configuration class takes over
    public static EMI getPlugin()
    {
        return plugin;
    }
    public static Long getConfigLong(String path) { return plugin.getConfig().getLong(path); }
    public static Guild getGuild() { return Configuration.getJda().getGuildById(plugin.getConfig().getLong("guild-id")); }
    public static JDA getJda()
    {
        return jda;
    }

}