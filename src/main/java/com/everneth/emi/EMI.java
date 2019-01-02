package com.everneth.emi;

import co.aikar.commands.BukkitCommandManager;
import co.aikar.idb.DB;
import co.aikar.idb.Database;
import co.aikar.idb.DatabaseOptions;
import co.aikar.idb.PooledDatabaseOptions;
import com.everneth.emi.api.*;
import com.everneth.emi.commands.ReportCommand;
import com.everneth.emi.commands.bot.HelpClearCommand;
import com.everneth.emi.commands.comm.CommCommand;
import com.everneth.emi.commands.comp.CompCommand;
import com.everneth.emi.events.JoinEvent;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.everneth.emi.commands.mint.MintCommand;
import spark.Spark;

import static spark.Spark.*;
import javax.security.auth.login.LoginException;

import java.io.File;

import static spark.Spark.get;
import static spark.Spark.port;

/**
 *     Class: EMI
 *     Author: Faceman (@TptMike)
 *     Purpose: Plugin main class launcher
 *
 */

public class EMI extends JavaPlugin {

    private static EMI plugin;
    private static BukkitCommandManager commandManager;
    private static JDA jda;
    FileConfiguration config = getConfig();
    String configPath = getDataFolder() + System.getProperty("file.separator") + "config.yml";
    File configFile = new File(configPath);
    @Override
    public void onEnable() {
        plugin = this;

        getLogger().info("Ministry Interface started.");
        if(!configFile.exists())
        {
            this.saveDefaultConfig();
        }

        Utils.chatTag = config.getString("chat-tag");

        DatabaseOptions options = DatabaseOptions.builder().mysql(config.getString("dbuser"), config.getString("dbpass"), config.getString("dbname"), config.getString("dbhost")).build();
        Database db = PooledDatabaseOptions.builder().options(options).createHikariDatabase();
        DB.setGlobalDatabase(db);

        registerCommands();
        registerListeners();
        initBot();
        initApi();
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
        commandManager.registerCommand(new ReportCommand());
    }

    private void initBot()
    {
        CommandClientBuilder builder = new CommandClientBuilder();
        builder.setPrefix(this.getConfig().getString("bot-prefix"));
        builder.setGame(Game.playing(this.getConfig().getString("bot-game")));
        builder.addCommand(new HelpClearCommand());
        builder.setOwnerId(this.getConfig().getString("bot-owner-id"));

        CommandClient client = builder.build();

        try {
            jda = new JDABuilder(config.getString("bot-token")).addEventListener(client).build();
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

    private void initApi()
    {
        port(this.getConfig().getInt("api-port"));
        get(Path.Web.ONE_STATS, StatisticController.getPlayerStats);
        get(Path.Web.ONE_DATA, PlayerdataController.getPlayerData);
        get(Path.Web.ONE_ADV, AdvancementController.getPlayerAdvs);
        post(Path.Web.EXECUTE_COMMAND, CommandController.sendCommandPayload);
        get("*", (request, response) -> "404 not found!!");

        Spark.exception(Exception.class, (exception, request, response) -> {exception.printStackTrace();});
    }

    private void registerListeners()
    {
        getServer().getPluginManager().registerEvents(new JoinEvent(this), this);
    }


    public static EMI getPlugin()
    {
        return plugin;
    }

    public static JDA getJda()
    {
        return jda;
    }
}
