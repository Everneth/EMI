package com.everneth.emi.models;

import co.aikar.idb.DB;
import co.aikar.idb.DbRow;
import com.everneth.emi.EMI;
import net.dv8tion.jda.api.entities.User;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class EMIPlayer {
    private String uuid;
    private String name;
    private String altName;
    private int id;
    private long discordId;
    private LocalDateTime dateAltAdded;
    private String altUuid;

    public EMIPlayer() {}
    public EMIPlayer(String uuid, String name, String altName)
    {
        this.uuid = uuid;
        this.name = name;
        this.altName = altName;
        this.id = 0;
        this.discordId = 0L;
    }
    public EMIPlayer(String uuid, String name, String altName, int id)
    {
        this.uuid = uuid;
        this.name = name;
        this.altName = altName;
        this.id = id;
        this.discordId = 0L;
    }

    public EMIPlayer(String uuid, String name, int id) {
        this.uuid = uuid;
        this.name = name;
        this.id = id;
    }

    public EMIPlayer(String uuid, String name, String altName, int id, long discordId) {
        this.uuid = uuid;
        this.name = name;
        this.altName = altName;
        this.id = id;
        this.discordId = id;
    }

    public EMIPlayer(String uuid, String name, String altName, int id, long discordId, LocalDateTime dateAltAdded, String altUuid) {
        this.uuid = uuid;
        this.name = name;
        this.altName = altName;
        this.id = id;
        this.discordId = id;
        this.dateAltAdded = dateAltAdded;
        this.altUuid = altUuid;
    }

    public EMIPlayer(String uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public String getUniqueId()
    {
        return this.uuid;
    }

    public String getName()
    {
        return this.name;
    }

    public String getAltName() { return this.altName; }

    public int getId()
    {
        return this.id;
    }
    public long getDiscordId()
    {
        return this.discordId;
    }

    public LocalDateTime getDateAltAdded() {
        return dateAltAdded;
    }

    public void setDateAltAdded(LocalDateTime dateAltAdded) {
        this.dateAltAdded = dateAltAdded;
    }

    public String getAltUuid() {
        return altUuid;
    }

    public void setAltUuid(String altUuid) {
        this.altUuid = altUuid;
    }
    public void setUniqueId(String uuid)
    {
        this.uuid = uuid;
    }
    public void setName(String name)
    {
        this.name = name;
    }
    public void setAltName(String altName) { this.altName = altName; }
    public void setId(int id)
    {
        this.id = id;
    }
    public void setDiscordId(long discordId)
    {
        this.discordId = discordId;
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
            return new EMIPlayer(player.getString("player_uuid"),
                    player.getString("player_name"),
                    player.getString("alt_name"),
                    player.getInt("player_id"),
                    player.getLong("discord_id"),
                    player.get("date_alt_added"),
                    player.getString("alt_uuid"));
        }
        catch (Exception e)
        {
            EMI.getPlugin().getLogger().info(e.getMessage());
            return new EMIPlayer();
        }
    }

    public boolean isEmpty()
    {
        return this.getId() == 0;
    }

    public static List<DbRow> getAllPoints(String name)
    {
        EMIPlayer recipient = EMIPlayer.getEmiPlayer(name);

        List<DbRow> recordsList = new ArrayList<DbRow>();
        try {
            recordsList = DB.getResultsAsync("SELECT charter_point_id, p1.player_name as 'issued_to', p1.player_uuid as 'recipient_uuid', p2.player_name as 'issued_by', p2.player_uuid as 'issuer_uuid', reason, amount, date_issued, date_expired, expunged FROM charter_points c INNER JOIN\n" +
                            "players p1 ON c.issued_to = p1.player_id\n" +
                            "JOIN players p2 ON c.issued_by = p2.player_id WHERE issued_to = ?",
                    recipient.getId()).get();
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }

        return recordsList;
    }

    public static CharterPoint getOnePoint(int id)
    {

        DbRow record = new DbRow();
        try {
            record = DB.getFirstRowAsync("SELECT * FROM charter_points WHERE charter_point_id = ?", id).get();
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        if(record == null)
        {
            return null;
        }
        else
        {
            EMIPlayer issuer = getEmiPlayer(record.getInt("issued_by"));
            EMIPlayer recipient = getEmiPlayer(record.getInt("issued_to"));

            return new CharterPoint(
                    issuer,
                    recipient,
                    record.getString("reason"),
                    record.getInt("amount")
            );
        }
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

    public static boolean syncExists(UUID uuid) {
        EMIPlayer player = EMIPlayer.getEmiPlayer(uuid);
        return player.getDiscordId() != 0;
    }

    public static boolean syncExists(User user) {
        EMIPlayer player = EMIPlayer.getEmiPlayer(user.getIdLong());
        return player.getDiscordId() != 0;
    }

    public static boolean syncExists(long discordId) {
        EMIPlayer player = EMIPlayer.getEmiPlayer(discordId);
        return player.getDiscordId() != 0;
    }

    public static UUID getPlayerUUID(String name) {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet("https://api.mojang.com/users/profiles/minecraft/" + name);
        StringBuffer sb = null;
        try {
            CloseableHttpResponse response = httpclient.execute(httpGet);
            if (response.getStatusLine().getStatusCode() != 204) {
                JSONObject obj = new JSONObject(EntityUtils.toString(response.getEntity()));

                sb = new StringBuffer(obj.getString("id"));
                sb.insert(8, "-");

                sb = new StringBuffer(sb.toString());
                sb.insert(13, "-");

                sb = new StringBuffer(sb.toString());
                sb.insert(18, "-");

                sb = new StringBuffer(sb.toString());
                sb.insert(23, "-");

            } else
                return null;
        } catch (IOException e) {
            EMI.getPlugin().getLogger().warning(e.getMessage());
        }
        return UUID.fromString(sb.toString());
    }
}
