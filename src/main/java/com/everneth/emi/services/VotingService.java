package com.everneth.emi.services;

import co.aikar.idb.DB;
import co.aikar.idb.DbRow;
import com.everneth.emi.EMI;
import com.everneth.emi.models.WhitelistApp;
import com.everneth.emi.models.WhitelistVote;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class VotingService {
    private HashMap<Long, WhitelistVote> voteMap = new HashMap<>();
    public static VotingService service;

    private VotingService() {}

    public static VotingService getService()
    {
        if(service == null)
        {
            service = new VotingService();
        }
        return service;
    }

    public void addVote(long id, WhitelistVote vote)
    {
        voteMap.put(id, vote);
    }
    public void removeVote(long id)
    {
        voteMap.remove(id);
    }

    public DbRow getAppByDiscordId(long id)
    {
        CompletableFuture<DbRow> futureApp;
        DbRow app = new DbRow();
        futureApp = DB.getFirstRowAsync("SELECT * FROM applications WHERE applicant_discord_id = ?", id);
        try {
            app = futureApp.get();
        }
        catch (Exception e)
        {
            EMI.getPlugin().getLogger().info(e.getMessage());
        }
        return app;
    }

    public static HashMap<Long, WhitelistApp> load()
    {
        HashMap<Long, WhitelistApp> data = new HashMap<>();
        List<DbRow> results = new ArrayList<>();
        try
        {
            results = DB.getResultsAsync("SELECT * FROM applications WHERE is_approved = 0").get();
        }
        catch (Exception e)
        {
            EMI.getPlugin().getLogger().warning(e.getMessage());
        }

        if(!results.isEmpty()) {
            for (DbRow row : results) {
                data.put(row.getLong("applicant_discord_id"), new WhitelistApp(
                        row.getString("mc_ign"),
                        row.getString("location"),
                        row.getInt("age"),
                        row.getString("friend"),
                        row.getString("has_been_banned"),
                        row.getString("looking_for"),
                        row.getString("love_hate"),
                        row.getString("intro"),
                        row.getString("secret_word"),
                        row.getLong("applicant_discord_id"),
                        UUID.fromString(row.getString("mc_uuid"))
                ));
            }
        }
        return data;
    }
}
