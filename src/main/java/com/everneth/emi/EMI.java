package com.everneth.emi;

import co.aikar.commands.BukkitCommandManager;
import co.aikar.idb.*;
import com.everneth.emi.api.*;
import com.everneth.emi.commands.*;
import com.everneth.emi.commands.bot.CloseReportCommand;
import com.everneth.emi.commands.bot.ConfirmSyncCommand;
import com.everneth.emi.commands.bot.DenySyncCommand;
import com.everneth.emi.commands.bot.HelpClearCommand;
import com.everneth.emi.commands.comm.CommCommand;
import com.everneth.emi.commands.comp.CompCommand;
import com.everneth.emi.commands.par.CharterCommand;
import com.everneth.emi.events.JoinEvent;
import com.everneth.emi.events.LeaveEvent;
import com.everneth.emi.events.bot.MessageReceivedListener;
import com.everneth.emi.models.EMIPlayer;
import com.everneth.emi.models.MintProject;
import com.everneth.emi.utils.PlayerUtils;
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
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

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

//        ReportManager.getReportManager().loadManager();

        registerCommands();
        registerListeners();
//        initBot();
//        initApi();
        initMintProjects();
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
        commandManager.registerCommand(new ReportReplyCommand());
        commandManager.registerCommand(new GetRepliesCommand());
        commandManager.registerCommand(new MinorHelpCommand());
        commandManager.registerCommand(new DiscordsyncCommand());
        commandManager.registerCommand(new CharterCommand());
    }

    private void initBot()
    {
        CommandClientBuilder builder = new CommandClientBuilder();
        builder.setPrefix(this.getConfig().getString("bot-prefix"));
        builder.setGame(Game.playing(this.getConfig().getString("bot-game")));
        builder.addCommand(new HelpClearCommand());
        builder.addCommand(new ConfirmSyncCommand());
        builder.addCommand(new DenySyncCommand());
        builder.addCommand(new CloseReportCommand());
        builder.setOwnerId(this.getConfig().getString("bot-owner-id"));

        CommandClient client = builder.build();

        try {
            jda = new JDABuilder(config.getString("bot-token"))
                    .addEventListener(client)
                    .addEventListener(new MessageReceivedListener())
                    .build();
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
        getServer().getPluginManager().registerEvents(new LeaveEvent(this), this);
    }

    private void initMintProjects()
    {
        MintProjectManager manager = MintProjectManager.getMintProjectManager();

        ArrayList<DbRow> projects;
        ArrayList<DbRow> tasks;
        ArrayList<DbRow> materials;
        ArrayList<DbRow> workLog;
        ArrayList<DbRow> materialLog;

        try
        {
            projects = new ArrayList<>(DB.getResults("SELECT * FROM mint_project"));
            tasks = new ArrayList<>(DB.getResults("SELECT * FROM mint_task_requirements"));
            materials = new ArrayList<>(DB.getResults("SELECT * FROM mint_material_requirements"));
            workLog = new ArrayList<>(DB.getResults("SELECT * FROM mint_task_log"));
            materialLog = new ArrayList<>(DB.getResults("SELECT * FROM mint_material_log"));
        }
        catch(SQLException e)
        {
            this.getLogger().info("Failed to gather info for mint projects: " + e.toString());
            return;
        }

        for(DbRow projectRow : projects)
        {
            DbRow playerRow = PlayerUtils.getPlayerRow(projectRow.getInt("project_lead"));
            EMIPlayer playerLead = new EMIPlayer(playerRow.getString("player_uuid"), playerRow.getString("player_name"), playerRow.getInt("player_id"));
            Timestamp endDateTime = projectRow.get("end_date");
            String endDate = "";
            if(endDateTime != null)
            {
                endDate = endDateTime.toString();
            }
            MintProject project = new MintProject(
                    playerLead,
                    projectRow.getString("project_name"),
                    projectRow.get("start_date").toString(),
                    endDate,
                    projectRow.getInt("complete"),
                    projectRow.getInt("focused"),
                    projectRow.getString("description"));

//            for(DbRow taskRow : tasks)
//            {
//                if(projectRow.getInt("project_id") != projectRow.getInt("project_id"))
//                {
//                    continue;
//                }
//
//                MintTaskRequirement task = new MintTaskRequirement(taskRow.get("task"), taskRow.getInt("complete"), taskRow.get("focused"));
//                project.getTaskRequirements().put(taskRow.getInt("task_id"), task);
//            }

            manager.addProject(projectRow.getInt("project_id"), project);
        }


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
