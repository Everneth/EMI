package com.everneth.emi;

import co.aikar.commands.BukkitCommandManager;
import co.aikar.idb.DB;
import co.aikar.idb.Database;
import co.aikar.idb.DatabaseOptions;
import co.aikar.idb.PooledDatabaseOptions;
import com.everneth.emi.commands.ReportCommand;
import com.everneth.emi.commands.comm.CommCommand;
import com.everneth.emi.commands.comp.CompCommand;
import com.everneth.emi.events.JoinEvent;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.everneth.emi.commands.mint.MintCommand;

import javax.security.auth.login.LoginException;

public class EMI extends JavaPlugin {

    private static EMI plugin;
    private static BukkitCommandManager commandManager;
    private static JDA jda;
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
        initBot();
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
        commandManager.registerCommand(new ReportCommand(jda));
    }

    private void loadConfig()
    {
        config.addDefault("dbhost", "localhost:3306");
        config.addDefault("dbname", "emi");
        config.addDefault("dbuser", "admin_emi");
        config.addDefault("dbpass", "secret");
        config.addDefault("dbprefix", "ev_");
        config.addDefault("bot-token", "PASTE-TOKEN-HERE");
        config.options().copyDefaults(true);
    }

    private void initBot()
    {
        try {
            jda = new JDABuilder(config.getString("bot-token")).build();
            jda.awaitReady();
        }
        catch(LoginException e)
        {
            e.printStackTrace();
        }
        catch(InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    private void registerListeners()
    {
        getServer().getPluginManager().registerEvents(new JoinEvent(this), this);
    }


    public static EMI getPlugin()
    {
        return plugin;
    }
}
