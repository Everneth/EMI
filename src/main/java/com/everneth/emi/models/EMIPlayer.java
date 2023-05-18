package com.everneth.emi.models;

import co.aikar.idb.DB;
import co.aikar.idb.DbRow;
import com.everneth.emi.EMI;
import com.everneth.emi.models.enums.ConfigMessage;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

public class EMIPlayer {
    private int id;
    private String name;
    private UUID uuid;
    private String altName;
    private UUID altUuid;
    private long discordId;
    private LocalDateTime dateAltAdded;
    private LocalDateTime dateCanNextRefer;
    private EMIPlayer referredBy;
    private LocalDateTime dateReferred;

    public EMIPlayer() {}
    public EMIPlayer(UUID uuid, String name, String altName)
    {
        this.uuid = uuid;
        this.name = name;
        this.altName = altName;
        this.id = 0;
        this.discordId = 0L;
    }

    public EMIPlayer(UUID uuid, String name, String altName, int id, long discordId) {
        this.uuid = uuid;
        this.name = name;
        this.altName = altName;
        this.id = id;
        this.discordId = discordId;
    }

    public EMIPlayer(int id, String name, UUID uuid, String altName, UUID altUuid, LocalDateTime dateAltAdded,
                     long discordId)
    {
        this.id = id;
        this.name = name;
        this.uuid = uuid;
        this.altName = altName;
        this.altUuid = altUuid;
        this.dateAltAdded = dateAltAdded;
        this.discordId = discordId;
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
        if (altUuid == null && altName != null) {
            altUuid = requestUUID(altName);
        }
        return altUuid;
    }

    public String getAltName() { return this.altName; }
    public void setAltName(String altName) {
        this.altName = altName;
    }

    public int getId()
    {
        return this.id;
    }

    public long getDiscordId()
    {
        return this.discordId;
    }
    public void setDiscordId(long discordId) {
        this.discordId = discordId;
    }

    public LocalDateTime getDateAltAdded() {
        return dateAltAdded;
    }

    public boolean isEmpty() {
        return this.id == 0;
    }

    public boolean isSynced() {
        return discordId != 0;
    }

    public LocalDateTime getDateCanNextRefer() {
        if (this.dateCanNextRefer == null) {
            try {
                CompletableFuture<LocalDateTime> row = DB.getFirstColumnAsync("SELECT date_can_next_refer FROM players WHERE player_id = ?", this.id);
                this.dateCanNextRefer = row.get();
            }
            catch (InterruptedException e) {
                EMI.getPlugin().getLogger().warning("Interrupted while getting date referred: " + e.getMessage());
            }
            catch (ExecutionException e) {
                EMI.getPlugin().getLogger().warning("Could not execute query while getting date referred: " + e.getMessage());
            }
        }
        return this.dateCanNextRefer;
    }
    public void setDateCanNextRefer(LocalDateTime dateCanNextRefer) {
        this.dateCanNextRefer = dateCanNextRefer;
    }

    public EMIPlayer getReferredBy() {
        if (this.referredBy == null) {
            try {
                CompletableFuture<Integer> row = DB.getFirstColumnAsync("SELECT referred_by FROM players WHERE player_id = ?", this.id);
                int referredById = row.get();
                this.referredBy = referredById != 0 ? getEmiPlayer(referredById) : null;
            }
            catch (InterruptedException e) {
                EMI.getPlugin().getLogger().warning("Interrupted while getting date referred: " + e.getMessage());
            }
            catch (ExecutionException e) {
                EMI.getPlugin().getLogger().warning("Could not execute query while getting date referred: " + e.getMessage());
            }
        }
        return this.referredBy;
    }
    public void setReferredBy(EMIPlayer referredBy) {
        this.referredBy = referredBy;
    }

    public LocalDateTime getDateReferred() {
        if (this.dateReferred == null)
        {
            try {
                CompletableFuture<LocalDateTime> row = DB.getFirstColumnAsync("SELECT date_referred FROM players WHERE player_id = ?", this.id);
                this.dateReferred = row.get();
            }
            catch (InterruptedException e) {
                EMI.getPlugin().getLogger().warning("Interrupted while getting date referred: " + e.getMessage());
            }
            catch (ExecutionException e) {
                EMI.getPlugin().getLogger().warning("Could not execute query while getting date referred: " + e.getMessage());
            }
        }
        return this.dateReferred;
    }
    public void setDateReferred(LocalDateTime dateReferred) {
        this.dateReferred = dateReferred;
    }

    public AtomicBoolean sendDiscordMessage(String message) {
        AtomicBoolean sent = new AtomicBoolean();
        try {
            getGuildMember().getUser().openPrivateChannel()
                    .flatMap(privateChannel -> privateChannel.sendMessage(message))
                    .queue(discordMessage -> sent.set(true),
                            new ErrorHandler().handle(ErrorResponse.CANNOT_SEND_TO_USER, error ->
                                        EMI.getPlugin().getLogger().warning(ConfigMessage.DISCORD_MESSAGE_FAILED.get())));
        }
        catch (NullPointerException e) {
            EMI.getPlugin().getLogger().warning(ConfigMessage.USER_NOT_FOUND.get());
        }
        return sent;
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
            if (response.getStatusLine().getStatusCode() != 404) {
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

    // This should never return null, at worst it returns an empty player if nothing is found
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
            if (player == null)
                return new EMIPlayer();

            // In the event there is no alternate uuid, we can't try to parse one
            String alt_uuid_string = player.getString("alt_uuid");
            UUID alt_uuid = null;
            if (alt_uuid_string != null)
                alt_uuid = UUID.fromString(alt_uuid_string);

            // The DB row is nullable because of a unique constraint but cannot be null when read in
            Long discordId = player.getLong("discord_id");
            discordId = discordId == null ? 0 : discordId;

            return new EMIPlayer(player.getInt("player_id"),
                    player.getString("player_name"),
                    UUID.fromString(player.getString("player_uuid")),
                    player.getString("alt_name"),
                    alt_uuid,
                    player.get("date_alt_added"),
                    discordId);
        }
        catch (InterruptedException e) {
            EMI.getPlugin().getLogger().info("Interrupted while getting EMIPlayer: " + e.getMessage());
        }
        catch (ExecutionException e) {
            EMI.getPlugin().getLogger().info("Could not execute query to get EMIPlayer: " + e.getMessage());
        }
        return new EMIPlayer();
    }

    public void updatePlayerTable() {
        DB.executeUpdateAsync("UPDATE players SET player_name = ?," +
                        "player_uuid = ?," +
                        "alt_name = ?," +
                        "alt_uuid = ?," +
                        "date_alt_added = ?," +
                        "discord_id = ?," +
                        "date_can_next_refer = ?," +
                        "referred_by = ?," +
                        "date_referred = ? WHERE player_id = ?",
                        name, uuid, altName, altUuid, dateAltAdded, discordId, dateCanNextRefer, referredBy.id, dateReferred, id);
    }

    public List<CharterPoint> getAllPoints()
    {
        List<CharterPoint> recordList = new ArrayList<>();
        try {
            /*List<DbRow> rows = DB.getResultsAsync("""
                            SELECT charter_point_id, p1.player_name as 'issued_to', p1.player_uuid as 'recipient_uuid', p2.player_name as 'issued_by', p2.player_uuid as 'issuer_uuid', reason, amount, date_issued, date_expired, expunged FROM charter_points c INNER JOIN
                            players p1 ON c.issued_to = p1.player_id
                            JOIN players p2 ON c.issued_by = p2.player_id WHERE issued_to = ?""",
                    getId()).get();*/
            List<DbRow> rows = DB.getResultsAsync("SELECT * FROM charter_points WHERE issued_to = ?",
                    this.id).get();

            for (DbRow row : rows) {
                recordList.add(CharterPoint.dbRowToCharterPoint(row));
            }
        }
        catch (Exception e)
        {
            EMI.getPlugin().getLogger().info(e.getMessage());
        }

        return recordList;
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
