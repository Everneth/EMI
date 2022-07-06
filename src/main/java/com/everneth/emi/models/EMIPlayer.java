package com.everneth.emi.models;

import co.aikar.idb.DB;
import co.aikar.idb.DbRow;
import com.everneth.emi.EMI;
import com.everneth.emi.Utils;
import com.everneth.emi.models.enums.ConfigMessage;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.bukkit.command.CommandSender;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class EMIPlayer {
    private UUID uuid;
    private String name;
    private UUID altUuid;
    private String altName;
    private int id;
    private long discordId;
    private LocalDateTime dateAltAdded;

    public EMIPlayer() {}
    public EMIPlayer(UUID uuid, String name, String altName)
    {
        this.uuid = uuid;
        this.name = name;
        this.altName = altName;
        this.id = 0;
        this.discordId = 0L;
    }
    public EMIPlayer(UUID uuid, String name, String altName, int id)
    {
        this.uuid = uuid;
        this.name = name;
        this.altName = altName;
        this.id = id;
        this.discordId = 0L;
    }

    public EMIPlayer(UUID uuid, String name, int id) {
        this.uuid = uuid;
        this.name = name;
        this.id = id;
    }

    public EMIPlayer(UUID uuid, String name, String altName, int id, long discordId) {
        this.uuid = uuid;
        this.name = name;
        this.altName = altName;
        this.id = id;
        this.discordId = discordId;
    }

    public EMIPlayer(UUID uuid, String name, String altName, int id, long discordId, LocalDateTime dateAltAdded, UUID altUuid) {
        this.uuid = uuid;
        this.name = name;
        this.altName = altName;
        this.id = id;
        this.discordId = discordId;
        this.dateAltAdded = dateAltAdded;
        this.altUuid = altUuid;
    }

    public EMIPlayer(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public UUID getUuid() {
        if (uuid == null) {
            uuid = requestUUID(name);
        }
        return uuid;
    }

    public String getName()
    {
        return this.name;
    }
    public void setName(String name)
    {
        this.name = name;
    }

    public UUID getAltUuid() {
        if (altUuid == null) {
            uuid = requestUUID(altName);
        }
        return altUuid;
    }

    public String getAltName() { return this.altName; }
    public void setAltName(String altName) { this.altName = altName; }

    public int getId()
    {
        return this.id;
    }

    public long getDiscordId()
    {
        return this.discordId;
    }
    public void setDiscordId(long discordId) { this.discordId = discordId; }

    public LocalDateTime getDateAltAdded() {
        return dateAltAdded;
    }

    public boolean isEmpty() {
        return this.getId() == 0;
    }

    public boolean isSynced() {
        return discordId != 0;
    }

    public boolean sendDiscordMessage(String message) {
        var successWrapper = new Object() { boolean messageSent = false; };
        try {
            getGuildMember().getUser().openPrivateChannel()
                    .flatMap(privateChannel -> privateChannel.sendMessage(message))
                    .queue(discordMessage -> successWrapper.messageSent = true,
                            new ErrorHandler().handle(ErrorResponse.CANNOT_SEND_TO_USER, error ->
                            EMI.getPlugin().getLogger().warning(ConfigMessage.DISCORD_MESSAGE_FAILED.get())));
        }
        catch (NullPointerException e) {
            EMI.getPlugin().getLogger().warning(ConfigMessage.USER_NOT_FOUND.get());
        }
        return successWrapper.messageSent;
    }

    public boolean sendDiscordMessage(String message, CommandSender sender) {
        var successWrapper = new Object() { boolean messageSent = false; };
        try {
            User user = getGuildMember().getUser();
            user.openPrivateChannel()
                    .flatMap(privateChannel -> privateChannel.sendMessage(message))
                    .queue(discordMessage -> {
                        successWrapper.messageSent = true;
                        sender.sendMessage(Utils.color("&a" + "Discord message successfully sent to &f" + user.getName()));
                    }, new ErrorHandler().handle(ErrorResponse.CANNOT_SEND_TO_USER, (error) ->
                            sender.sendMessage(Utils.color("&c") + ConfigMessage.DISCORD_MESSAGE_FAILED.get())));
        }
        catch (NullPointerException e) {
            sender.sendMessage(Utils.color("&c") + ConfigMessage.USER_NOT_FOUND.get());
        }
        return successWrapper.messageSent;
    }

    public Member getGuildMember() {
        return EMI.getGuild().getMemberById(discordId);
    }

    private UUID requestUUID(String name) {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet("https://api.mojang.com/users/profiles/minecraft/" + name);
        StringBuilder sb = null;
        try {
            CloseableHttpResponse response = httpclient.execute(httpGet);
            if (response.getStatusLine().getStatusCode() != 204) {
                JSONObject obj = new JSONObject(EntityUtils.toString(response.getEntity()));

                sb = new StringBuilder(obj.getString("id"));
                sb.insert(8, "-");

                sb = new StringBuilder(sb.toString());
                sb.insert(13, "-");

                sb = new StringBuilder(sb.toString());
                sb.insert(18, "-");

                sb = new StringBuilder(sb.toString());
                sb.insert(23, "-");

            } else
                return null;
        } catch (IOException e) {
            EMI.getPlugin().getLogger().warning(e.getMessage());
        }
        return UUID.fromString(sb.toString());
    }
    
    public static <T> EMIPlayer getEmiPlayer(T t)
    {
        CompletableFuture<DbRow> futurePlayer;
        DbRow player;

        if (t instanceof Integer) {
            futurePlayer = DB.getFirstRowAsync("SELECT * FROM players WHERE player_id = ?", t);
        } else if (t instanceof UUID) {
            futurePlayer = DB.getFirstRowAsync("SELECT * FROM players WHERE ? IN (player_uuid,alt_uuid)", t.toString());
        } else if (t instanceof Long) {
            futurePlayer = DB.getFirstRowAsync("SELECT * FROM players WHERE discord_id = ?", t);
        } else if (t instanceof String) {
            futurePlayer = DB.getFirstRowAsync("SELECT * FROM players WHERE ? IN (player_name,alt_name)", t);
        } else {
            return new EMIPlayer();
        }
        try {
            player = futurePlayer.get();
            return new EMIPlayer(UUID.fromString(player.getString("player_uuid")),
                    player.getString("player_name"),
                    player.getString("alt_name"),
                    player.getInt("player_id"),
                    player.getLong("discord_id"),
                    player.get("date_alt_added"),
                    UUID.fromString(player.getString("alt_uuid")));
        }
        catch (Exception e)
        {
            EMI.getPlugin().getLogger().info(e.getMessage());
            return new EMIPlayer();
        }
    }

    public List<DbRow> getAllPoints()
    {
        List<DbRow> recordsList = new ArrayList<>();
        try {
            recordsList = DB.getResultsAsync("""
                            SELECT charter_point_id, p1.player_name as 'issued_to', p1.player_uuid as 'recipient_uuid', p2.player_name as 'issued_by', p2.player_uuid as 'issuer_uuid', reason, amount, date_issued, date_expired, expunged FROM charter_points c INNER JOIN
                            players p1 ON c.issued_to = p1.player_id
                            JOIN players p2 ON c.issued_by = p2.player_id WHERE issued_to = ?""",
                    getId()).get();
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }

        return recordsList;
    }

    public static DbRow getAppRecord(long discordId)
    {
        CompletableFuture<DbRow> futurePlayer;
        DbRow player = new DbRow();
        futurePlayer = DB.getFirstRowAsync("SELECT * FROM applications WHERE applicant_discord_id = ?", discordId);
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
