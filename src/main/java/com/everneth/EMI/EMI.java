package com.everneth.EMI;

import co.aikar.commands.BukkitCommandManager;
import org.bukkit.plugin.java.JavaPlugin;

public class EMI extends JavaPlugin {

    private static EMI plugin;
    private static BukkitCommandManager commandManager;


    @Override
    public void onEnable() {
        plugin = this;
        getLogger().info("Ministry Interface started.");

        registerCommands();
    }
    @Override
    public void onDisable() {
        getLogger().info("Ministry Interface stopped.");
    }

    private void registerCommands()
    {
        commandManager = new BukkitCommandManager(this);
        commandManager.enableUnstableAPI("help");
        commandManager.getCommandContexts().registerContext();

    }
}
