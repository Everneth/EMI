package com.everneth.emi.services;

import co.aikar.idb.DB;
import co.aikar.idb.DbRow;
import com.everneth.emi.EMI;
import com.everneth.emi.models.WhitelistApp;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class VotingService {
    private HashMap<Long, WhitelistApp> voteMap = new HashMap<>();
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

    public void addVote(long id)
    {}
    public void clearVotes()
    {
        service.voteMap.clear();
    }

    public DbRow getAppByDiscordId(long id)
    {
        CompletableFuture<DbRow> futureApp;
        DbRow app = new DbRow();
        futureApp = DB.getFirstRowAsync("SELECT * FROM applications WHERE discord_id = ?", id);
        try {
            app = futureApp.get();
        }
        catch (Exception e)
        {
            EMI.getPlugin().getLogger().info(e.getMessage());
        }
        return app;
    }
/*
    private WhitelistApp buildApp(DbRow app)
    {
        return new WhitelistApp(
                app.getString("player_ign"),
                app.getString("player_location"),
                app.getInt("player_age"),
                app.getString("player_friend"),
                app.getString("player_lookingfor"),
                app.getString("player_interests"),
                app.getString("player_intro"),
                app.getString("secret_word"),
                app.getLong("player_discordId"),
                UUID.fromString(app.getString("player_uuid"))
        );
    }
    
 */
}
