package com.everneth.emi;

import co.aikar.commands.BukkitCommandManager;
import co.aikar.idb.*;
import com.everneth.emi.api.*;
import com.everneth.emi.commands.bot.CloseReportCommand;
import com.everneth.emi.commands.bot.ConfirmSyncCommand;
import com.everneth.emi.commands.bot.DenySyncCommand;
import com.everneth.emi.commands.bot.HelpClearCommand;
import com.everneth.emi.commands.comm.CommCommand;
import com.everneth.emi.commands.comp.CompCommand;
import com.everneth.emi.commands.DevopCommand;
import com.everneth.emi.commands.CharterCommand;
import com.everneth.emi.commands.playerassistance.*;
import com.everneth.emi.events.JoinEvent;
import com.everneth.emi.events.LeaveEvent;
import com.everneth.emi.events.bot.MessageReceivedListener;
import com.everneth.emi.managers.DevopProjectManager;
import com.everneth.emi.managers.ReportManager;
import com.everneth.emi.models.*;
import com.everneth.emi.models.devop.*;
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

        ReportManager.getReportManager().loadManager();

        registerCommands();
        registerListeners();
        initBot();
//        initApi();
        initDevop();
    }
    @Override
    public void onDisable() {
        getLogger().info("Ministry Interface stopped.");
        DB.close();

        DevopProjectManager manager = DevopProjectManager.getDevopProjectManager();
        for(DevopProject project : manager.getProjects().values())
        {
            for(DevopLogMaterial logMaterial : project.getQueuedValidateMaterial().values())
            {
                project.getMaterialLogValidation().put(logMaterial.getId(), logMaterial);
            }

            for(DevopLogTask logTask : project.getQueuedValidateTask().values())
            {
                project.getTaskLogValidation().put(logTask.getId(), logTask);
            }
        }
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
        commandManager.registerCommand(new DevopCommand());
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

    private void initDevop()
    {
        DevopProjectManager manager = DevopProjectManager.getDevopProjectManager();

        ArrayList<DbRow> projects;
        ArrayList<DbRow> tasks;
        ArrayList<DbRow> materials;
        ArrayList<DbRow> taskLog;
        ArrayList<DbRow> materialLog;
        ArrayList<DbRow> workers;

        try
        {
            projects = new ArrayList<>(DB.getResults("SELECT * FROM devop_project"));
            tasks = new ArrayList<>(DB.getResults("SELECT * FROM devop_task"));
            materials = new ArrayList<>(DB.getResults("SELECT * FROM devop_material"));
            taskLog = new ArrayList<>(DB.getResults("SELECT * FROM devop_log_task"));
            materialLog = new ArrayList<>(DB.getResults("SELECT * FROM devop_log_material"));
            workers = new ArrayList<>(DB.getResults("SELECT * FROM devop_log_join"));
        }
        catch(SQLException e)
        {
            this.getLogger().info("Failed to gather info for devop projects: " + e.toString());
            return;
        }

        for(DbRow projectRow : projects)
        {
            DbRow playerRow = PlayerUtils.getPlayerRow(projectRow.getInt("leader"));
            EMIPlayer playerLead = new EMIPlayer(playerRow.getString("player_uuid"), playerRow.getString("player_name"), playerRow.getInt("player_id"));
            Timestamp endDateTime = projectRow.get("end_date");
            String endDate = null;

            if(endDateTime != null)
            {
                endDate = endDateTime.toString();
            }

            DevopProject project = new DevopProject(
                    projectRow.getInt("project_id"),
                    playerLead,
                    projectRow.getString("name"),
                    projectRow.get("start_date").toString(),
                    endDate,
                    projectRow.getInt("complete"),
                    projectRow.getInt("focused"),
                    projectRow.getString("description"));

            for(DbRow worker : workers)
            {
                if(!projectRow.getInt("project_id").equals(worker.getInt("project_id")))
                {
                    continue;
                }

                DbRow player = PlayerUtils.getPlayerRow(worker.getInt("player_id"));
                EMIPlayer emiPlayer = new EMIPlayer(player.getString("player_uuid"), player.getString("player_name"), player.getInt("player_id"));

                project.getWorkers().add(emiPlayer);
            }

            for(DbRow taskRow : tasks)
            {
                if(!projectRow.getInt("project_id").equals(taskRow.getInt("project_id")))
                {
                    continue;
                }

                DevopTask task = new DevopTask(
                        taskRow.getInt("task_id"),
                        taskRow.getInt("project_id"),
                        taskRow.getString("task"),
                        taskRow.getInt("complete"),
                        taskRow.getInt("focused"));
                project.getTasks().put(task.getId(), task);

                if(task.getFocused() == 1)
                {
                    project.setFocusedTask(task);
                }
            }

            for(DbRow materialRow : materials)
            {
                if(!projectRow.getInt("project_id").equals(materialRow.getInt("project_id")))
                {
                    continue;
                }

                DevopMaterial material = new DevopMaterial(
                        materialRow.getInt("material_id"),
                        materialRow.getInt("project_id"),
                        materialRow.getString("material"),
                        materialRow.getInt("total"),
                        materialRow.getInt("collected"),
                        materialRow.getInt("complete"),
                        materialRow.getInt("focused"));
                project.getMaterials().put(material.getId(), material);

                if(material.getFocused() == 1)
                {
                    project.setFocusedMaterial(material);
                }
            }

            for(DbRow taskLogRow : taskLog)
            {
                if(!projectRow.getInt("project_id").equals(taskLogRow.getInt("project_id")))
                {
                    continue;
                }

                DbRow loggedByRow = PlayerUtils.getPlayerRow(taskLogRow.getInt("logged_by"));
                EMIPlayer loggedBy = new EMIPlayer(loggedByRow.getString("player_uuid"), loggedByRow.getString("player_name"), loggedByRow.getInt("player_id"));
                EMIPlayer validatedBy;

                try
                {
                    DbRow validatedByRow = PlayerUtils.getPlayerRow(taskLogRow.getInt("validated_by"));
                    validatedBy = new EMIPlayer(validatedByRow.getString("player_uuid"), validatedByRow.getString("player_name"), validatedByRow.getInt("player_id"));
                }
                catch (NullPointerException e)
                {
                    validatedBy = null;
                }

                DevopLogTask log = new DevopLogTask(
                        taskLogRow.getInt("log_id"),
                        taskLogRow.getInt("project_id"),
                        loggedBy,
                        validatedBy,
                        taskLogRow.getInt("validated"),
                        taskLogRow.getInt("time_worked"),
                        taskLogRow.get("log_date").toString(),
                        taskLogRow.getString("description"));

                if(log.getValidated() == 1)
                {
                    project.getTaskLog().put(log.getId(), log);
                }
                else
                {
                    project.getTaskLogValidation().put(log.getId(), log);
                }
            }

            for(DbRow materialLogRow : materialLog)
            {
                if(!projectRow.getInt("project_id").equals(materialLogRow.getInt("project_id")))
                {
                    continue;
                }

                DbRow loggedByRow = PlayerUtils.getPlayerRow(materialLogRow.getInt("logged_by"));
                EMIPlayer loggedBy = new EMIPlayer(loggedByRow.getString("player_uuid"), loggedByRow.getString("player_name"), loggedByRow.getInt("player_id"));
                EMIPlayer validatedBy;

                try
                {
                    DbRow validatedByRow = PlayerUtils.getPlayerRow(materialLogRow.getInt("validated_by"));
                    validatedBy = new EMIPlayer(validatedByRow.getString("player_uuid"), validatedByRow.getString("player_name"), validatedByRow.getInt("player_id"));
                }
                catch (NullPointerException e)
                {
                    validatedBy = null;
                }

                DevopLogMaterial log = new DevopLogMaterial(
                        materialLogRow.getInt("log_id"),
                        materialLogRow.getInt("project_id"),
                        materialLogRow.getInt("material_id"),
                        loggedBy,
                        validatedBy,
                        materialLogRow.getInt("validated"),
                        materialLogRow.getInt("material_collected"),
                        materialLogRow.getInt("time_worked"),
                        materialLogRow.get("log_date").toString(),
                        materialLogRow.getString("description"));

                if(log.getValidated() == 1)
                {
                    project.getMaterialLog().put(log.getId(), log);
                }
                else
                {
                    project.getMaterialLogValidation().put(log.getId(), log);
                }
            }
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