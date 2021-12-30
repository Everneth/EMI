package com.everneth.emi.managers;

import co.aikar.idb.DB;
import net.dv8tion.jda.api.entities.User;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 *     Class: DiscordSyncManager
 *     Author: Faceman (@TptMike)
 *     Purpose: Manage the actively loaded sync requests in memory. This container is not reloaded upon
 *     server restart. Any sync requests will need to be resent
 */

public final class DiscordSyncManager {
    private static DiscordSyncManager dsm;
    private HashMap<UUID, User> userMap = new HashMap<UUID, User>();;
    private DiscordSyncManager() {}
    private CompletableFuture<Integer> futurePlayerId;
    public static DiscordSyncManager getDSM()
    {
        if(dsm == null)
        {
            dsm = new DiscordSyncManager();
        }
        return dsm;
    }
    public void addSyncRequest(Player player, User discordUser)
    {
        this.userMap.put(player.getUniqueId(), discordUser);
    }
    public void removeSyncRequest(Player player)
    {
        this.userMap.remove(player.getUniqueId());
    }
    public void removeSyncRequest(String uuid)
    {
        this.userMap.remove(UUID.fromString(uuid));
    }
    public User findSyncRequest(User user)
    {
        return getKeyFromValue(this.userMap, user);
    }
    public User findSyncRequest(Player player)
    {
        return userMap.get(player.getUniqueId());
    }
    public UUID findSyncRequestUUID(User user)
    {
        return getKeyFromValueUUID(this.userMap, user);
    }

    private User getKeyFromValue(Map hm, User user)
    {
        for (Object o : hm.keySet())
        {
            if(hm.get((UUID) o).equals(user))
            {
                return (User) hm.get(o);
            }
        }
        return null;
    }

    private UUID getKeyFromValueUUID(Map hm, User user)
    {
        for (Object o : hm.keySet())
        {
            if(hm.get((UUID) o).equals(user))
            {
                return (UUID) o;
            }
        }
        return null;
    }

    public int syncAccount(User user)
    {
        DiscordSyncManager dsm = DiscordSyncManager.getDSM();
        int playerId = 0;
        futurePlayerId = DB.executeUpdateAsync("UPDATE players SET discord_id = ? " +
                "WHERE player_uuid = ?", user.getIdLong(), dsm.findSyncRequestUUID(user).toString());
        // get the results from the future
        try {
            playerId = futurePlayerId.get();
        }
        catch (Exception e)
        {
            System.out.print(e.getMessage());
        }
        return playerId;
    }
}
