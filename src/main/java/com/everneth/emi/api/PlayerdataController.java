package com.everneth.emi.api;

import com.everneth.emi.EMI;
import com.everneth.emi.models.PlayerJson;

import com.google.gson.Gson;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jnbt.CompoundTag;
import org.jnbt.NBTInputStream;
import org.jnbt.Tag;
import spark.Request;
import spark.Response;
import spark.Route;

import java.io.FileInputStream;
import java.util.Map;
import java.util.UUID;

public class PlayerdataController {
    public static final String DATA_PATH = EMI.getPlugin().getServer().getWorld(
            EMI.getPlugin().getConfig().getString("world-folder")).getWorldFolder().getPath() + "/playerdata/";

    public static Route getPlayerData = (Request request, Response response) -> {
        // Get the UUID from the API call
        UUID uuid = UUID.fromString(request.params(":uuid"));

        //Read in the file to an NBT input stream
        final NBTInputStream input = new NBTInputStream(new FileInputStream(DATA_PATH + uuid.toString() + ".dat"));

        //The first tag is a compound root tag, leads read it in and then close the stream
        final CompoundTag originalTopLevelTag = (CompoundTag) input.readTag();
        input.close();

        //Stuff the top level tag into a map so we can retrieve tags by key
        Map<String, Tag> originalData = originalTopLevelTag.getValue();

        //We want the last and first seen date, we can use the UUID from the API call
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);

        //Build our data to an POJO class for serialization
        PlayerJson playerToJson = new PlayerJson();

        playerToJson.setUuid(uuid);
        playerToJson.setName(player.getName());
        playerToJson.setHealth(Double.valueOf(originalData.get("Health").getValue().toString()));
        playerToJson.setLevel(Integer.valueOf(originalData.get("XpLevel").getValue().toString()));
        playerToJson.setFirstPlayed(player.getFirstPlayed());
        playerToJson.setLastPlayed(player.getLastPlayed());

        Gson gson = new Gson();

        //Serialize and send!
        return gson.toJson(playerToJson);
    };
}
