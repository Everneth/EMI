package com.everneth.emi;

import co.aikar.commands.BukkitCommandManager;
import co.aikar.idb.DB;
import co.aikar.idb.Database;
import co.aikar.idb.DatabaseOptions;
import co.aikar.idb.PooledDatabaseOptions;
import com.everneth.emi.commands.comm.CommCommand;
import com.everneth.emi.commands.comp.CompCommand;
import com.everneth.emi.events.JoinEvent;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.everneth.emi.commands.mint.MintCommand;

public class EMI extends JavaPlugin {

    private static EMI plugin;
    private static BukkitCommandManager commandManager;
    FileConfiguration config = getConfig();


    @Override
    public void onEnable() {
        plugin = this;
        getLogger().info("Ministry Interface started.");
        loadConfig();
        saveConfig();

        DatabaseOptions options = DatabaseOptions.builder().mysql(config.getString("dbuser"), config.getString("dbpass"), config.getString("dbname"), config.getString("dbhost")).build();
        Database db = PooledDatabaseOptions.builder().options(options).createHikariDatabase();
        DB.setGlobalDatabase(db);

        registerCommands();
        registerListeners();
    }
    @Override
    public void onDisable() {
        getLogger().info("Ministry Interface stopped.");
        DB.close();
    }

    private void registerCommands()
    {
        commandManager = new BukkitCommandManager(this);
        commandManager.registerCommand(new MintCommand());
        commandManager.registerCommand(new CommCommand());
        commandManager.registerCommand(new CompCommand());
    }

    private void loadConfig()
    {
        config.addDefault("dbhost", "localhost:3306");
        config.addDefault("dbname", "emi");
        config.addDefault("dbuser", "admin_emi");
        config.addDefault("dbpass", "secret");
        config.addDefault("dbprefix", "ev_");
        config.options().copyDefaults(true);
    }

    private void registerListeners()
    {
        getServer().getPluginManager().registerEvents(new JoinEvent(), this);
    }


    public static EMI getPlugin()
    {
        return plugin;
    }
}
