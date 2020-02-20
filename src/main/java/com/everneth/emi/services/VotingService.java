package com.everneth.emi.services;

import co.aikar.idb.DB;
import co.aikar.idb.DbRow;

import com.everneth.emi.EMI;
import com.everneth.emi.models.WhitelistVote;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
            service.load();
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
    public boolean isVotingMessage(long messageId)
    {
        return this.voteMap.containsKey(messageId);
    }

    public WhitelistVote getVoteByMessageId(long messageId)
    {
        return this.voteMap.get(messageId);
    }

    public long getMessageId(long userid)
    {
        return this.voteMap.get(userid).getMessageId();
    }

    public HashMap<Long, WhitelistVote> load()
    {
        HashMap<Long, WhitelistVote> data = new HashMap<>();
        List<DbRow> results = new ArrayList<>();
        try
        {
            results = DB.getResultsAsync("SELECT * FROM votes WHERE is_active = 1").get();
        }
        catch (Exception e)
        {
            EMI.getPlugin().getLogger().warning(e.getMessage());
        }
        if(!results.isEmpty())
        {
            for(DbRow result : results)
            {
                this.voteMap.put(
                        result.getLong("message_id"),
                        new WhitelistVote(
                                result.getLong("applicant_id"),
                                result.getLong("message_id"),
                                false)
                        );
            }
        }
        return data;
    }
}
