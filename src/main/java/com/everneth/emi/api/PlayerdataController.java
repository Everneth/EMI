package com.everneth.emi.api;

import com.everneth.emi.EMI;
import com.everneth.emi.models.PlayerJson;
import com.everneth.emi.utils.FileUtils;
import com.google.gson.Gson;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.UUID;

public class PlayerdataController {
    public static final String DATA_PATH = EMI.getPlugin().getServer().getWorld(
            EMI.getPlugin().getConfig().getString("world-folder")).getWorldFolder().getPath() + "/playerdata/";

    public static Route getPlayerData = (Request request, Response response) -> {
        //String playerData = FileUtils.readFileAsString(DATA_PATH + request.params(":uuid") + ".dat");
        UUID uuid = UUID.fromString(request.params(":uuid"));

        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);

        PlayerJson playerToJson = new PlayerJson();

        playerToJson.setUuid(player.getUniqueId());
        playerToJson.setName(player.getName());
        playerToJson.setHealth(((Player) player).getHealth());
        playerToJson.setLevel(((Player) player).getLevel());
        playerToJson.setFirstPlayed(player.getFirstPlayed());
        playerToJson.setLastPlayed(player.getLastPlayed());

        response.status(200);
        response.type("application/json");

        Gson gson = new Gson();
        return gson.toJson(playerToJson);
    };
}
