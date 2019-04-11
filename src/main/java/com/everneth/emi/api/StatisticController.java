package com.everneth.emi.api;

import com.everneth.emi.EMI;
import com.everneth.emi.utils.FileUtils;
import spark.Request;
import spark.Response;
import spark.Route;

public class StatisticController {
    public static final String STATS_PATH = EMI.getPlugin().getServer().getWorld(
            EMI.getPlugin().getConfig().getString("world-folder")).getWorldFolder().getPath() + "/stats/";

    public static Route getPlayerStats = (Request request, Response response) -> {
        String playerStats = FileUtils.readFileAsString(STATS_PATH + request.params(":uuid") + ".json");
        response.status(200);
        response.type("application/json");
        return playerStats;
    };
}
