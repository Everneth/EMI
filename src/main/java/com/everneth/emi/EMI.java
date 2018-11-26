package com.everneth.emi;

import co.aikar.commands.BukkitCommandManager;
import co.aikar.idb.DB;
import co.aikar.idb.Database;
import co.aikar.idb.DatabaseOptions;
import co.aikar.idb.PooledDatabaseOptions;
import com.everneth.emi.api.AdvancementController;
import com.everneth.emi.api.Path;
import com.everneth.emi.api.PlayerdataController;
import com.everneth.emi.api.StatisticController;
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

import static spark.Spark.get;
import static spark.Spark.port;

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

    private void loadConfig()
    {
        config.addDefault("dbhost", "localhost:3306");
        config.addDefault("dbname", "emi");
        config.addDefault("dbuser", "admin_emi");
        config.addDefault("dbpass", "secret");
        config.addDefault("dbprefix", "ev_");
        config.addDefault("bot-token", "PASTE-TOKEN-HERE");
        config.addDefault("report-channel", 0);
        config.addDefault("chat-tag", "&7&6EMI&7]");
        config.addDefault("root-report-msg", 0);
        config.addDefault("bot-owner-id", 0);
        config.addDefault("world-folder", "world");
        config.options().copyDefaults(true);
    }

    private void initBot()
    {
        CommandClientBuilder builder = new CommandClientBuilder();
        builder.setPrefix("!!");
        builder.setGame(Game.playing("Nursing your ailments, love."));
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
        port(7598);
        get(Path.Web.ONE_STATS, StatisticController.getPlayerStats);
        get(Path.Web.ONE_DATA, PlayerdataController.getPlayerData);
        get(Path.Web.ONE_ADV, AdvancementController.getPlayerAdvs);
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
