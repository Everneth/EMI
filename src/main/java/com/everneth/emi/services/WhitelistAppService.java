package com.everneth.emi.services;

import co.aikar.idb.DB;
import co.aikar.idb.DbRow;
import com.everneth.emi.EMI;
import com.everneth.emi.models.EMIPlayer;
import com.everneth.emi.models.WhitelistApp;
import com.everneth.emi.models.enums.DiscordRole;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.managers.GuildManager;
import org.apache.commons.lang.StringUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;


public class WhitelistAppService {
    private static WhitelistAppService service;
    private HashMap<Long, WhitelistApp> appMap = new HashMap<>();
    private WhitelistAppService() {}

    public static WhitelistAppService getService()
    {
        if(service == null)
        {
            service = new WhitelistAppService();
            service.load();
        }
        return service;
    }

    // this is a direct copy of getAllCurrentApplicants until refactoring can be done on that method
    public void load() {
        List<DbRow> results = new ArrayList<>();
        try {
            results = DB.getResultsAsync("SELECT * FROM applications WHERE app_active = ?", 1).get();
        }
        catch(Exception e)
        {
            EMI.getPlugin().getLogger().warning("Error retrieving current applications. Error msg: " + e.getMessage());
        }
        for(DbRow result : results) {
            appMap.put(result.getLong("applicant_discord_id"),
                    new WhitelistApp(result.getString("mc_ign"),
                            result.getString("location"),
                            result.getInt("age"),
                            result.getString("friend"),
                            result.getString("looking_for"),
                            result.getString("has_been_banned"),
                            result.getString("love_hate"),
                            result.getString("intro"),
                            result.getString("secret_word"),
                            result.getLong("applicant_discord_id"),
                            UUID.fromString(result.getString("mc_uuid"))
                    ));
        }
    }

    public void addApp(long id, WhitelistApp app)
    {
        app.setStep(1);
        app.setDiscordId(id);
        app.setInProgress(true);
        app.setHoldForNextStep(false);
        appMap.put(id, app);
    }

    public void removeApp(long discordId)
    {
        // set the application inactive in the db and remove from memory
        appMap.remove(discordId);
        DB.executeUpdateAsync("UPDATE applications SET app_active = 0 WHERE applicant_discord_id = ?",
                discordId);
    }

    public void changeRoleToApplicant(long discordId)
    {
        GuildManager manager = EMI.getJda().getGuildById(EMI.getPlugin().getConfig().getLong("guild-id")).getManager();
        manager.getGuild().addRoleToMember(manager.getGuild().getMemberById(discordId), DiscordRole.APPLICANT.get()).queue();
    }

    public void messageStaffWithEmbed(EmbedBuilder eb2)
    {
        EMI.getJda().getGuildById(EMI.getPlugin().getConfig().getLong("guild-id")).getTextChannelById(EMI.getPlugin().getConfig().getLong("voting-channel-id"))
                .sendMessageEmbeds(eb2.build()).queue();
    }

    public void messageStaff(String msg)
    {
        EMI.getJda().getGuildById(EMI.getPlugin().getConfig().getLong("guild-id")).getTextChannelById(EMI.getPlugin().getConfig().getLong("voting-channel-id"))
                .sendMessage(msg).queue();
    }

    public WhitelistApp findByDiscordId(long id)
    {
        return appMap.get(id);
    }

    public void addApplicationRecord(WhitelistApp application)
    {
        try {
            DB.executeInsert("INSERT INTO applications (" +
                            "mc_ign, location, age, friend, looking_for, has_been_banned, is_approved, love_hate, intro, secret_word, mc_uuid, applicant_discord_id) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    application.getInGameName(),
                    application.getLocation(),
                    application.getAge(),
                    application.getFriend(),
                    application.getLookingFor(),
                    application.getBannedElsewhere(),
                    0,
                    application.getLoveHate(),
                    application.getIntro(),
                    application.getSecretWord(),
                    application.getMinecraftUuid().toString(),
                    application.getDiscordId());
        } catch(SQLException e)
        {
            EMI.getPlugin().getLogger().severe(e.getMessage());
        }
    }

    public void updateApplicationRecord(WhitelistApp application)
    {
            DB.executeUpdateAsync("UPDATE applications " +
                    "SET mc_ign = ?," +
                    " location = ?," +
                    " age = ?," +
                    " friend = ?," +
                    " looking_for = ?," +
                    " has_been_banned = ?," +
                    " is_approved = ?," +
                    " love_hate = ?," +
                    " intro = ?," +
                    " secret_word = ?," +
                    " mc_uuid = ?," +
                    " applicant_discord_id = ?" +
                    " WHERE applicant_discord_id = ?",
                    application.getInGameName(),
                    application.getLocation(),
                    application.getAge(),
                    application.getFriend(),
                    application.getLookingFor(),
                    application.getBannedElsewhere(),
                    0,
                    application.getLoveHate(),
                    application.getIntro(),
                    application.getSecretWord(),
                    application.getMinecraftUuid(),
                    application.getDiscordId(),
                    application.getDiscordId());
    }

    public void approveWhitelistAppRecord(long id, long msgid)
    {
        DbRow playerToAdd = EMIPlayer.getAppRecord(id);
        DB.executeUpdateAsync("UPDATE applications SET is_approved = 1 WHERE applicant_discord_id = ?",
                id);
        try {
            DB.executeInsert("INSERT INTO players (player_name, player_uuid, discord_id) " +
                            "VALUES (?, ?, ?)",
                    playerToAdd.getString("mc_ign"),
                    playerToAdd.getString("mc_uuid"),
                    id);
        }
        catch (SQLException e)
        {
            EMI.getPlugin().getLogger().warning("Error inserting new player record. Output: " + e.getMessage());
        }
    }

    public WhitelistApp getSingleApplicant(String discordDetails)
    {
        int poundIndex = discordDetails.indexOf('#');
        String name = discordDetails.substring(0, poundIndex);
        String discriminator = discordDetails.substring(poundIndex + 1);

        DbRow result = new DbRow();

        try {
            result = DB.getFirstRowAsync("SELECT * FROM applications WHERE applicant_discord_id = ?",
                    EMI.getJda().getUserByTag(name, discriminator).getIdLong()).get();
        }
        catch(Exception e)
        {
            EMI.getPlugin().getLogger().warning("Error retrieving single application: Error msg: " + e.getMessage());
        }

        return dbRowToApp(result);
    }

    public WhitelistApp getSingleApplicant(long discordId)
    {
        DbRow result = new DbRow();
        try {
            result = DB.getFirstRowAsync("SELECT * FROM applications WHERE applicant_discord_id = ?",
                    discordId).get();
        }
        catch(Exception e)
        {
            EMI.getPlugin().getLogger().warning("Error retrieving single application: Error msg: " + e.getMessage());
        }

        return dbRowToApp(result);
    }

    public WhitelistApp getSingleApplicant(UUID minecraftUuid)
    {

        DbRow result = new DbRow();

        try {
            result = DB.getFirstRowAsync("SELECT * FROM applications WHERE mc_uuid = ?",
                    minecraftUuid.toString()).get();
        }
        catch(Exception e)
        {
            EMI.getPlugin().getLogger().warning("Error retrieving single application: Error msg: " + e.getMessage());
        }

        if(result == null)
            return null;
        else
            return dbRowToApp(result);
    }

    private WhitelistApp dbRowToApp(DbRow appRecord)
    {
        return new WhitelistApp(
                appRecord.getString("mc_ign"),
                appRecord.getString("location"),
                appRecord.getInt("age"),
                appRecord.getString("friend"),
                appRecord.getString("has_been_banned"),
                appRecord.getString("looking_for"),
                appRecord.getString("love_hate"),
                appRecord.getString("intro"),
                appRecord.getString("secret_word"),
                appRecord.getLong("applicant_discord_id"),
                UUID.fromString(appRecord.getString("mc_uuid"))
        );
    }

    public List<WhitelistApp> getAllCurrentApplicants()
    {
        List<WhitelistApp> applicants = new ArrayList<>();
        List<DbRow> results = new ArrayList<>();
        try {
            results = DB.getResultsAsync("SELECT * FROM applications WHERE app_active = ?", 1).get();
        }
        catch(Exception e)
        {
            EMI.getPlugin().getLogger().warning("Error retrieving current applications. Error msg: " + e.getMessage());
        }
        for(DbRow result : results)
            applicants.add(new WhitelistApp(
                    result.getString("mc_ign"),
                    result.getString("location"),
                    result.getInt("age"),
                    result.getString("friend"),
                    result.getString("looking_for"),
                    result.getString("has_been_banned"),
                    result.getString("love_hate"),
                    result.getString("intro"),
                    result.getString("secret_word"),
                    result.getLong("applicant_discord_id"),
                    UUID.fromString(result.getString("mc_uuid"))
            ));
        return applicants;
    }

    public boolean appExists(UUID minecraftUuid) {
        WhitelistApp app = getSingleApplicant(minecraftUuid);
        return app != null;
    }

    public boolean appExists(Long discordId) {
        return appMap.containsKey(discordId);
    }

    public void addData(long id, int step, String data)
    {
        WhitelistApp app = appMap.get(id);
        app.setHoldForNextStep(true);

        switch(step)
        {
            case 1:
                // the username containing any spaces will cause an exception, the easiest solution is to simply replace any spaces
                data = data.trim().replace(" ", "_");
                EMIPlayer player= new EMIPlayer();
                player.setName(data);
                if (player.getUuid() != null) {
                    app.setMinecraftUuid(player.getUuid());
                    app.setInGameName(data);
                }
                break;
            case 2:
                app.setLocation(data);
                break;
            case 3:
                if (StringUtils.isNumeric(data)) {
                    app.setAge(Integer.valueOf(data));
                }
                break;
            case 4:
                app.setFriend(data);
                break;
            case 5:
                app.setBannedElsewhere(data);
                break;
            case 6:
                app.setLookingFor(data);
                break;
            case 7:
                app.setLoveHate(data);
                break;
            case 8:
                app.setIntro(data);
                break;
            case 9:
                app.setSecretWord(data);
                break;
        }

        if (app.getStep() == 1 && app.getMinecraftUuid() == null) {
            app.setStep(1);
        }
        else if (app.getStep() == 3 && app.getAge() == 0) {
            app.setStep(3);
        }
        else if (app.getStep() >= 13)
        {
            app.setInProgress(false);
        }
        else {
            app.setStep(app.getStep() + 1);
        }
    }
}
