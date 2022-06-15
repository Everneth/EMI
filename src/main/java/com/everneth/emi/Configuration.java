package com.everneth.emi;

import co.aikar.commands.BukkitCommandManager;
import co.aikar.idb.*;
import com.everneth.emi.commands.*;
import com.everneth.emi.commands.bot.*;
import com.everneth.emi.commands.bot.par.WhitelistAppCommand;
import com.everneth.emi.commands.mint.MintCommand;
import com.everneth.emi.commands.mint.projects.*;
import com.everneth.emi.commands.par.CharterCommand;
import com.everneth.emi.commands.par.InfoCommand;
import com.everneth.emi.events.JoinEvent;
import com.everneth.emi.events.LeaveEvent;
import com.everneth.emi.events.bot.ButtonListener;
import com.everneth.emi.events.bot.GuildLeaveListener;
import com.everneth.emi.events.bot.MessageReceivedListener;
import com.everneth.emi.events.bot.RoleChangeListener;
import com.everneth.emi.managers.MintProjectManager;
import com.everneth.emi.managers.MotdManager;
import com.everneth.emi.managers.ReportManager;
import com.everneth.emi.models.EMIPlayer;
import com.everneth.emi.models.Motd;
import com.everneth.emi.models.mint.*;
import com.everneth.emi.services.VotingService;
import com.everneth.emi.services.WhitelistService;
import com.everneth.emi.utils.PlayerUtils;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Configuration {

    private static EMI plugin;
    private String configPath;
    private File configFile;
    private static JDA jda;

    private static BukkitCommandManager commandManager;
    private FileConfiguration config = EMI.getPlugin().getConfig();

    public Configuration() {
        String configPath = EMI.getPlugin().getDataFolder() + System.getProperty("file.separator") + "config.yml";
        File configFile = new File(configPath);

    }

    public void startup()
    {
        EMI.getPlugin().getLogger().info("Ministry Interface started.");
        if(!configFile.exists())
        {
            EMI.getPlugin().saveDefaultConfig();
            this.config = EMI.getPlugin().getConfig();
        }
        Utils.chatTag = config.getString("chat-tag");

        DatabaseOptions options = DatabaseOptions.builder().mysql(config.getString("dbuser"), config.getString("dbpass"), config.getString("dbname"), config.getString("dbhost")).build();
        Database db = PooledDatabaseOptions.builder().options(options).createHikariDatabase();
        DB.setGlobalDatabase(db);

        ReportManager.getReportManager().loadManager();

        registerCommands();
        registerListeners();
        //initBot();
        initMint();
        initMotds();
    }
    public void shutdown()
    {
        EMI.getPlugin().getLogger().info("Ministry Interface stopped.");
        DB.close();

        // remove all the registered slash commands from the guild and shutdown
        unregisterCommands(true);
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
    public void reload() {}

    private void registerCommands()
    {
        commandManager = new BukkitCommandManager(plugin);
        commandManager.registerCommand(new MintCommand());
        commandManager.registerCommand(new ReportCommand());
        commandManager.registerCommand(new ReportReplyCommand());
        commandManager.registerCommand(new GetRepliesCommand());
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
            DbRow playerRow = PlayerUtils.getPlayerRow(projectRow.getInt("leader"));
            EMIPlayer playerLead = new EMIPlayer(playerRow.getString("player_uuid"),
                    playerRow.getString("player_name"),
                    playerRow.getString("alt_name"),
                    playerRow.getInt("player_id"));
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
                if (player != null) {
                    EMIPlayer emiPlayer = new EMIPlayer(player.getString("player_uuid"),
                            player.getString("player_name"),
                            player.getString("alt_name"),
                            player.getInt("player_id"));

                    project.getWorkers().add(emiPlayer);
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

                DbRow loggedByRow = PlayerUtils.getPlayerRow(taskLogRow.getInt("logged_by"));
                EMIPlayer loggedBy = null;
                if (loggedByRow != null) {
                    loggedBy = new EMIPlayer(loggedByRow.getString("player_uuid"),
                            loggedByRow.getString("player_name"),
                            loggedByRow.getString("alt_name"),
                            loggedByRow.getInt("player_id"));
                }
                EMIPlayer validatedBy;

                try
                {
                    DbRow validatedByRow = PlayerUtils.getPlayerRow(taskLogRow.getInt("validated_by"));
                    validatedBy = new EMIPlayer(validatedByRow.getString("player_uuid"),
                            validatedByRow.getString("player_name"),
                            validatedByRow.getString("alt_name"),
                            validatedByRow.getInt("player_id"));
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
                EMIPlayer loggedBy = null;
                if (loggedByRow != null) {
                    loggedBy = new EMIPlayer(loggedByRow.getString("player_uuid"),
                            loggedByRow.getString("player_name"),
                            loggedByRow.getString("alt_name"),
                            loggedByRow.getInt("player_id"));
                }
                EMIPlayer validatedBy;

                try
                {
                    DbRow validatedByRow = PlayerUtils.getPlayerRow(materialLogRow.getInt("validated_by"));
                    validatedBy = new EMIPlayer(validatedByRow.getString("player_uuid"),
                            validatedByRow.getString("player_name"),
                            validatedByRow.getString("alt_name"),
                            validatedByRow.getInt("player_id"));
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

    private void initBot()
    {
        CommandClientBuilder builder = new CommandClientBuilder();
        // force the builder to create guild commands to avoid long global registration times
        builder.forceGuildOnly(EMI.getPlugin().getConfig().getLong("guild-id"));

        // the need for a prefix has been deprecated with slash commands
        //builder.setPrefix(this.getConfig().getString("bot-prefix"));

        builder.addSlashCommands(new HelpClearCommand(),
                new CloseReportCommand(),
                new ApplyCommand(),
                new WhitelistAppCommand(),
                new RequestWhitelistCommand(),
                new UnsyncCommand());
        builder.setOwnerId(EMI.getPlugin().getConfig().getString("bot-owner-id"));

        // register our global commands separately
        CommandClientBuilder globalBuilder = new CommandClientBuilder();
        globalBuilder.setActivity(Activity.watching(EMI.getPlugin().getConfig().getString("bot-game")));

        globalBuilder.addSlashCommands(new ConfirmSyncCommand(), new DenySyncCommand());
        globalBuilder.setOwnerId(EMI.getPlugin().getConfig().getString("bot-owner-id"));

        CommandClient client = builder.build();
        CommandClient globalClient = globalBuilder.build();

        try {
            jda = JDABuilder.createDefault(config.getString("bot-token"))
                    .addEventListeners(client, globalClient,
                            new MessageReceivedListener(),
                            new ButtonListener(),
                            new RoleChangeListener(),
                            new GuildLeaveListener())
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .enableIntents(GatewayIntent.GUILD_MEMBERS,GatewayIntent.DIRECT_MESSAGES,GatewayIntent.GUILD_MESSAGES)
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
            plugin.getLogger().info("ERROR: EMI/initMotds: " + e.toString());
            return;
        }

        for(DbRow dbRow : motds)
        {
            Motd motd = new Motd(dbRow.getString("sanitized_tag"), dbRow.getString("tag"), dbRow.getString("message"));
            motdManager.getMotds().put(motd.getSanitizedTag(), motd);
        }
    }

    private void unregisterCommands(boolean keepGlobal)
    {
        Guild guild = EMI.getJda().getGuildById(plugin.getConfig().getLong("guild-id"));

        List<Command> commands = guild.retrieveCommands().complete();
        for (Command command : commands)
            command.delete().complete();

        if (!keepGlobal) {
            List<Command> globalCommands = EMI.getJda().retrieveCommands().complete();
            for (Command command : globalCommands) {
                command.delete().complete();
            }
        }
    }
    public static JDA getJda() { return jda; }
}
