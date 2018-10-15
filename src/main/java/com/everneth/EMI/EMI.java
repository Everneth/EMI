package com.everneth.EMI;

import org.bukkit.plugin.java.JavaPlugin;

public class EMI extends JavaPlugin {
    @Override
    public void onEnable() {
        getLogger().info("Ministry Interface started.");
    }
    @Override
    public void onDisable() {
        getLogger().info("Ministry Interface stopped.");
    }
}
