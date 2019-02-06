package com.everneth.emi;

import net.dv8tion.jda.core.entities.User;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 *     Class: DiscordSyncManager
 *     Author: Faceman (@TptMike)
 *     Purpose: Manage the actively loaded sync requests in memory. This container is not reloaded upon
 *     server restart. Any sync requests will need to be resent
 */

public class DiscordSyncManager {
    private static final DiscordSyncManager dsm = new DiscordSyncManager();
    private HashMap<UUID, User> userMap;
    private DiscordSyncManager()
    {
        userMap = new HashMap<UUID, User>();
    }
    public static DiscordSyncManager getDSM()
    {
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
}
