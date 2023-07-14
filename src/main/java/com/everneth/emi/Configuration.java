package com.everneth.emi;

import co.aikar.commands.BukkitCommandManager;
import co.aikar.idb.*;
import com.everneth.emi.commands.*;
import com.everneth.emi.commands.bot.*;
import com.everneth.emi.commands.mint.MintCommand;
import com.everneth.emi.commands.mint.projects.*;
import com.everneth.emi.commands.par.CharterCommand;
import com.everneth.emi.commands.par.InfoCommand;
import com.everneth.emi.events.JoinEvent;
import com.everneth.emi.events.LeaveEvent;
import com.everneth.emi.events.bot.ButtonListener;
import com.everneth.emi.events.bot.GuildLeaveListener;
import com.everneth.emi.events.bot.RoleChangeListener;
import com.everneth.emi.managers.MintProjectManager;
import com.everneth.emi.managers.MotdManager;
import com.everneth.emi.managers.ReportManager;
import com.everneth.emi.models.EMIPlayer;
import com.everneth.emi.models.Motd;
import com.everneth.emi.models.mint.*;
import com.everneth.emi.services.VotingService;
import com.everneth.emi.services.WhitelistService;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class Configuration {

    private String configPath;
    private String cachePath;
    private File configFile;
    private File cacheDir;
    private static JDA jda;

    private static BukkitCommandManager commandManager;
    private FileConfiguration config;

    public Configuration() {
        configPath = EMI.getPlugin().getDataFolder() + File.separator + "config.yml";
        cachePath = EMI.getPlugin().getDataFolder() + File.separator + "cache" + File.separator;
        configFile = new File(configPath);
        cacheDir = new File(cachePath);
    }

    public void startup()
    {
        EMI.getPlugin().getLogger().info("Ministry Interface started.");
        if(!configFile.exists())
        {
            EMI.getPlugin().saveDefaultConfig();
        }
        this.config = EMI.getPlugin().getConfig();

        if(!cacheDir.exists())
        {
            try { Files.createDirectory(Paths.get(cachePath)); }
            catch (IOException e) { EMI.getPlugin().getLogger().severe(e.getMessage()); }
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
    public void shutdown()
    {
        EMI.getPlugin().getLogger().info("Ministry Interface stopped.");
        DB.close();

        // remove all the registered slash commands from the guild and shutdown
        //unregisterCommands(true);
        EMI.getJda().shutdown();

        // In the event someone requested temporary whitelisting less than 5 minutes before a server shutdown,
        // we want to remove them so that they're not permanently on the whitelist
        WhitelistService.getService().removeAllFromWhitelist();

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
    public void reload()
    {
        try
        {
            config.load(configFile);
        }
        catch (Exception e)
        {
            EMI.getPlugin().getLogger().severe(e.getMessage());
        }
    }

    private void registerCommands()
    {
        commandManager = new BukkitCommandManager(EMI.getPlugin());
        commandManager.registerCommand(new MintCommand());
        commandManager.registerCommand(new ReportCommand());
        commandManager.registerCommand(new SupportCommand());
        commandManager.registerCommand(new DiscordSyncCommands());
        commandManager.registerCommand(new CharterCommand());
        commandManager.registerCommand(new MintProjectCommands());
        commandManager.registerCommand(new MintMaterialCommands());
        commandManager.registerCommand(new MintTaskCommands());
        commandManager.registerCommand(new MintValidationCommands());
        commandManager.registerCommand(new MintViewCommands());
        commandManager.registerCommand(new MotdCommand());
        commandManager.registerCommand(new AltAccountCommands());
        commandManager.registerCommand(new InfoCommand());
    }

    private void registerListeners()
    {
        Plugin plugin = EMI.getPlugin();
        EMI.getPlugin().getServer().getPluginManager().registerEvents(new JoinEvent(plugin), plugin);
        EMI.getPlugin().getServer().getPluginManager().registerEvents(new LeaveEvent(plugin), plugin);
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
            EMI.getPlugin().getLogger().info("Failed to gather info for devop projects: " + e.toString());
            return;
        }

        for(DbRow projectRow : projects)
        {
            EMIPlayer playerLead = EMIPlayer.getEmiPlayer(projectRow.getInt("leader"));
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

                EMIPlayer player = EMIPlayer.getEmiPlayer(worker.getInt("player_id"));
                if (!player.isEmpty()) {
                    project.getWorkers().add(player);
                }
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

                EMIPlayer loggedBy = EMIPlayer.getEmiPlayer(taskLogRow.getInt("logged_by"));
                EMIPlayer validatedBy;

                try
                {
                    validatedBy = EMIPlayer.getEmiPlayer(taskLogRow.getInt("validated_by"));
                }
                catch (NullPointerException e)
                {
                    validatedBy = new EMIPlayer();
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

                EMIPlayer loggedBy = EMIPlayer.getEmiPlayer(materialLogRow.getInt("logged_by"));
                EMIPlayer validatedBy;

                try
                {
                    validatedBy = EMIPlayer.getEmiPlayer(materialLogRow.getInt("validated_by"));
                }
                catch (NullPointerException e)
                {
                    validatedBy = new EMIPlayer();
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

    private void initBot()
    {
        CommandClientBuilder builder = new CommandClientBuilder();
        // force the builder to create guild commands to avoid long global registration times
        builder.forceGuildOnly(EMI.getPlugin().getConfig().getLong("guild-id"));

        // the need for a prefix has been deprecated with slash commands
        //builder.setPrefix(this.getConfig().getString("bot-prefix"));

        builder.addSlashCommands(new HelpClearCommand(),
                new CloseReportCommand(),
                new RequestWhitelistCommand(),
                new UnsyncCommand());
        builder.setOwnerId(EMI.getPlugin().getConfig().getString("bot-owner-id"));

        // register our global commands separately
        CommandClientBuilder globalBuilder = new CommandClientBuilder();
        String status = EMI.getPlugin().getConfig().getString("bot-status");
        globalBuilder.setActivity(Activity.watching(status));

        globalBuilder.addSlashCommands(new ConfirmSyncCommand(), new DenySyncCommand());
        globalBuilder.setOwnerId(EMI.getPlugin().getConfig().getString("bot-owner-id"));

        CommandClient client = builder.build();
        CommandClient globalClient = globalBuilder.build();

        try {
            jda = JDABuilder.createDefault(config.getString("bot-token"))
                    .addEventListeners(client, globalClient,
                            new ButtonListener(),
                            new RoleChangeListener(),
                            new GuildLeaveListener())
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .enableIntents(GatewayIntent.GUILD_MEMBERS,
                            GatewayIntent.DIRECT_MESSAGES,
                            GatewayIntent.GUILD_MESSAGES,
                            GatewayIntent.MESSAGE_CONTENT)
                    .build();
            EMI.getJda().awaitReady();
            Guild guild = EMI.getJda().getGuildById(EMI.getPlugin().getConfig().getLong("guild-id"));

            //send an API request for all Everneth guild members on startup, which will then be stored in the cache
            guild.loadMembers();

            // call the service to force it to load the votes
            VotingService.getService();

            // cache the help channel history so message history persists through a reset
            guild.getTextChannelById(EMI.getPlugin().getConfig().getLong("help-channel-id")).getHistoryFromBeginning(100);
        }
        catch(Exception e)
        {
            e.printStackTrace();
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
            EMI.getPlugin().getLogger().info("ERROR: EMI/initMotds: " + e.toString());
            return;
        }

        for(DbRow dbRow : motds)
        {
            Motd motd = new Motd(dbRow.getString("sanitized_tag"), dbRow.getString("tag"), dbRow.getString("message"));
            motdManager.getMotds().put(motd.getSanitizedTag(), motd);
        }
    }
    public static JDA getJda() { return jda; }
}
