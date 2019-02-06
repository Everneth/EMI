package com.everneth.emi.commands.bot;

import co.aikar.idb.DB;
import co.aikar.idb.DbRow;
import com.everneth.emi.DiscordSyncManager;
import com.everneth.emi.EMI;
import com.everneth.emi.models.Motd;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.core.entities.User;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 *     Class: ConfirmSyncCommand
 *     Author: Faceman (@TptMike)
 *     Purpose: The JDA bot !!comfirmsync command that adds the users discord ID to EMIs player table
 */

public class ConfirmSyncCommand extends Command {
    private CompletableFuture<DbRow> playerOjbectFuture;
    private DbRow playerRow;
    private CompletableFuture<Integer> futurePlayerId;
    private DiscordSyncManager dsm = DiscordSyncManager.getDSM();
    public ConfirmSyncCommand()
    {
        this.name = "confirmsync";
    }
    @Override
    protected void execute(CommandEvent event)
    {
        User toFind = dsm.findSyncRequest(event.getAuthor());
        if(toFind == null)
        {
            event.replyInDm("No sync request exists for your account.");
        }
        else
        {
            if(syncExists(event.getAuthor()))
            {
                event.replyInDm("You have already synced this account. If this is in error, please contact staff.");
            }
            else
            {
                int playerId = syncAccount(event.getAuthor());
                if(playerId == 0)
                {
                    event.replyInDm("ERROR: Could not sync account.");
                }
                else
                {
                    dsm.removeSyncRequest(this.getPlayerRow(playerId).getString("player_uuid"));
                    event.getMember().getRoles().add(event.getGuild().getRoleById(EMI.getPlugin().getConfig().getLong("synced-role-id")));
                    event.replyInDm("You're account has successfully synced with our system!");
                }
            }
        }
    }
    private boolean syncExists(User user)
    {
        playerRow = new DbRow();
        playerOjbectFuture = DB.getFirstRowAsync("SELECT * FROM players\n" +
                "WHERE player_uuid = ?", this.dsm.findSyncRequestUUID(user).toString());
        // get the results from the future
        try {
            playerRow = playerOjbectFuture.get();
        }
        catch (Exception e)
        {
            System.out.print(e.getMessage());
        }
        return playerRow.isEmpty();
    }
    private int syncAccount(User user)
    {
        int playerId = 0;
        futurePlayerId = DB.executeUpdateAsync("UPDATE players SET discord_id = ?\n" +
                "WHERE player_uuid = ?", user.getIdLong(), this.dsm.findSyncRequestUUID(user).toString());
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
    private DbRow getPlayerRow(int id)
    {
        CompletableFuture<DbRow> futurePlayer;
        DbRow player = new DbRow();
        futurePlayer = DB.getFirstRowAsync("SELECT * FROM players WHERE player_id = ?", id);
        try {
            player = futurePlayer.get();
        }
        catch (Exception e)
        {
            EMI.getPlugin().getLogger().info(e.getMessage());
        }
        return player;
    }
}
