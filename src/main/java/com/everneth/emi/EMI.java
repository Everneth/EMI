package com.everneth.emi;

import co.aikar.commands.BukkitCommandManager;
import co.aikar.idb.*;
import com.everneth.emi.commands.*;
import com.everneth.emi.commands.bot.*;
import com.everneth.emi.commands.bot.par.WhitelistAppCommand;
import com.everneth.emi.commands.mint.projects.*;
import com.everneth.emi.commands.par.CharterCommand;
import com.everneth.emi.events.JoinEvent;
import com.everneth.emi.events.LeaveEvent;
import com.everneth.emi.events.bot.GuildLeaveListener;
import com.everneth.emi.events.bot.MessageReceivedListener;
import com.everneth.emi.events.bot.ReactionListener;
import com.everneth.emi.events.bot.RoleChangeListener;
import com.everneth.emi.managers.MintProjectManager;
import com.everneth.emi.managers.MotdManager;
import com.everneth.emi.managers.ReportManager;
import com.everneth.emi.models.*;
import com.everneth.emi.models.mint.*;
import com.everneth.emi.utils.PlayerUtils;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.everneth.emi.commands.mint.MintCommand;

import java.io.File;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;

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
        initMint();
        initMotds();
    }
    @Override
    public void onDisable() {
        getLogger().info("Ministry Interface stopped.");
        DB.close();

        MintProjectManager manager = MintProjectManager.getMintProjectManager();
        for(MintProject project : manager.getProjects().values())
        {
            for(MintLogMaterial logMaterial : project.getQueuedValidateMaterial().values())
            {
                project.getMaterialLogValidation().put(logMaterial.getId(), logMaterial);
            }

            for(MintLogTask logTask : project.getQueuedValidateTask().values())
            {
                project.getTaskLogValidation().put(logTask.getId(), logTask);
            }
        }
    }

    private void registerCommands()
    {
        commandManager = new BukkitCommandManager(this);
        commandManager.registerCommand(new MintCommand());
        commandManager.registerCommand(new ReportCommand());
        commandManager.registerCommand(new ReportReplyCommand());
        commandManager.registerCommand(new GetRepliesCommand());
        commandManager.registerCommand(new SupportCommand());
        commandManager.registerCommand(new DiscordsyncCommand());
        commandManager.registerCommand(new CharterCommand());
        commandManager.registerCommand(new MintProjectCommands());
        commandManager.registerCommand(new MintMaterialCommands());
        commandManager.registerCommand(new MintTaskCommands());
        commandManager.registerCommand(new MintValidationCommands());
        commandManager.registerCommand(new MintViewCommands());
        commandManager.registerCommand(new MotdCommand());
    }

    private void initBot()
    {
        CommandClientBuilder builder = new CommandClientBuilder();
        builder.setPrefix(this.getConfig().getString("bot-prefix"));
        builder.setActivity(Activity.listening(this.getConfig().getString("bot-game")));
        builder.addCommand(new HelpClearCommand());
        builder.addCommand(new ConfirmSyncCommand());
        builder.addCommand(new DenySyncCommand());
        builder.addCommand(new CloseReportCommand());
        builder.addCommand(new ApplyCommand());
        builder.addCommand(new WhitelistAppCommand());
        builder.setOwnerId(this.getConfig().getString("bot-owner-id"));

        CommandClient client = builder.build();

        try {
            jda = JDABuilder.createDefault(config.getString("bot-token"))
                    .addEventListeners(client)
                    .addEventListeners(new MessageReceivedListener())
                    .addEventListeners(new ReactionListener())
                    .addEventListeners(new RoleChangeListener())
                    .addEventListeners(new GuildLeaveListener())
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .enableIntents(GatewayIntent.GUILD_MEMBERS)
                    .enableIntents(GatewayIntent.DIRECT_MESSAGES)
                    .build();
            jda.awaitReady();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    private void registerListeners()
    {
        getServer().getPluginManager().registerEvents(new JoinEvent(this), this);
        getServer().getPluginManager().registerEvents(new LeaveEvent(this), this);
    }

    private void initMint()
    {
        MintProjectManager manager = MintProjectManager.getMintProjectManager();

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
            LocalDateTime endDateTime = projectRow.get("end_date");
            String endDate = null;

            if(endDateTime != null)
            {
                endDate = endDateTime.toString();
            }

            MintProject project = new MintProject(
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

                MintTask task = new MintTask(
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

                MintMaterial material = new MintMaterial(
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

                MintLogTask log = new MintLogTask(
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

                MintLogMaterial log = new MintLogMaterial(
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

    public void initMotds()
    {
        MotdManager motdManager = MotdManager.getMotdManager();

        ArrayList<DbRow> motds;

        try
        {
            motds = new ArrayList<>(DB.getResults("SELECT * FROM motds"));
        }
        catch(SQLException e)
        {
            this.getLogger().info("ERROR: EMI/initMotds: " + e.toString());
            return;
        }

        for(DbRow dbRow : motds)
        {
            Motd motd = new Motd(dbRow.getString("sanitized_tag"), dbRow.getString("tag"), dbRow.getString("message"));
            motdManager.getMotds().put(motd.getSanitizedTag(), motd);
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