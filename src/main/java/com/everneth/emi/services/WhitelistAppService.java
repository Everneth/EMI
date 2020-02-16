package com.everneth.emi.services;

import co.aikar.idb.DB;
import co.aikar.idb.DbRow;
import com.everneth.emi.EMI;

import com.everneth.emi.models.WhitelistApp;
import com.everneth.emi.utils.PlayerUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.managers.GuildManager;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;
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
        }
        return service;
    }

    public void addApp(long id, WhitelistApp app)
    {
        app.setStep(1);
        app.setDiscordId(id);
        app.setInProgress(true);
        app.setHoldForNextStep(false);
        appMap.put(id, app);

        MessageBuilder mb = new MessageBuilder();

        EMI.getJda().getGuildById(EMI.getPlugin().getConfig().getLong("guild-id")).getMemberById(id).getUser().openPrivateChannel().queue(privateChannel ->
                privateChannel.sendMessage("What be ye minecraft IGN?").queue()
        );
    }

    public void removeApp(long id)
    {
        appMap.remove(id);
    }

    public void changeRoleToApplicant(long discordId)
    {
        GuildManager manager = EMI.getJda().getGuildById(EMI.getPlugin().getConfig().getLong("guild-id")).getManager();
        Role applicant = manager.getGuild().getRoleById(EMI.getPlugin().getConfig().getLong("applicant-role-id"));
        manager.getGuild().addRoleToMember(manager.getGuild().getMemberById(discordId), applicant).queue();
    }

    public void messageStaffWithEmbed(EmbedBuilder eb2)
    {
        EMI.getJda().getGuildById(EMI.getPlugin().getConfig().getLong("guild-id")).getTextChannelById(EMI.getPlugin().getConfig().getLong("staff-channel-id")).sendMessage(eb2.build()).queue();
        EMI.getJda().getGuildById(EMI.getPlugin().getConfig().getLong("guild-id")).getTextChannelById(EMI.getPlugin().getConfig().getLong("staff-channel-id")).sendMessage("Attempting to transmit application to forums").queue();
    }

    public void messageStaff(String msg)
    {
        EMI.getJda().getGuildById(EMI.getPlugin().getConfig().getLong("guild-id")).getTextChannelById(EMI.getPlugin().getConfig().getLong("staff-channel-id")).sendMessage(msg).queue();
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

    public void approveWhitelistAppRecord(long id, long msgid)
    {
        DbRow playerToAdd = PlayerUtils.getAppRecord(id);
        DB.executeUpdateAsync("UPDATE applications SET is_approved = 1 WHERE applicant_discord_id = ?",
                id);
        try {
            DB.executeInsert("INSERT INTO players (player_name, player_uuid, member_id, discord_id) " +
                            "VALUES (?, ?, ?, ?)",
                    playerToAdd.getString("mc_ign"),
                    playerToAdd.getString("mc_uuid"),
                    null,
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
            results = DB.getResultsAsync("SELECT * FROM applications WHERE is_approved = ?", 0).get();
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

    public void addData(long id, int step, String data)
    {
        appMap.get(id).setHoldForNextStep(true);
        switch(step)
        {
            case 1:
                CloseableHttpClient httpclient = HttpClients.createDefault();
                HttpGet httpGet = new HttpGet("https://api.mojang.com/users/profiles/minecraft/" + data);
                try {
                    CloseableHttpResponse response = httpclient.execute(httpGet);
                    if(response.getStatusLine().getStatusCode() != 204)
                    {
                        JSONObject obj = new JSONObject(EntityUtils.toString(response.getEntity()));

                        StringBuffer sb = new StringBuffer(obj.getString("id"));
                        sb.insert(8, "-");

                        sb = new StringBuffer(sb.toString());
                        sb.insert(13, "-");

                        sb = new StringBuffer(sb.toString());
                        sb.insert(18, "-");

                        sb = new StringBuffer(sb.toString());
                        sb.insert(23, "-");
                        appMap.get(id).setMinecraftUuid(UUID.fromString(sb.toString()));
                        appMap.get(id).setInGameName(data);
                    }
                    else
                        return;
                }
                catch(IOException e)
                {
                    EMI.getPlugin().getLogger().warning(e.getMessage());
                }
                break;
            case 2:
                appMap.get(id).setLocation(data);
                break;
            case 3:
                appMap.get(id).setAge(Integer.valueOf(data));
                break;
            case 4:
                appMap.get(id).setFriend(data);
                break;
            case 5:
                appMap.get(id).setBannedElsewhere(data);
                break;
            case 6:
                appMap.get(id).setLookingFor(data);
                break;
            case 7:
                appMap.get(id).setLoveHate(data);
                break;
            case 8:
                appMap.get(id).setIntro(data);
                break;
            case 9:
                appMap.get(id).setSecretWord(data);
                break;
            case 10:
                //appMap.get(id).setStep(0); // restart app
                break;
        }
        appMap.get(id).setStep(appMap.get(id).getStep() + 1);
        if(appMap.get(id).getStep() >= 13)
        {
            appMap.get(id).setInProgress(false);
        }
    }
}
