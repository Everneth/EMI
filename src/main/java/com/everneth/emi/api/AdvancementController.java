package com.everneth.emi.api;

import com.everneth.emi.EMI;
import com.everneth.emi.utils.FileUtils;
import spark.Request;
import spark.Response;
import spark.Route;

public class AdvancementController {
    public static final String ADVS_PATH = EMI.getPlugin().getServer().getWorld(
            EMI.getPlugin().getConfig().getString("world-folder")).getWorldFolder().getPath() + "/advancements/";

    public static Route getPlayerAdvs = (Request request, Response response) -> {
        String playerAdvancements = FileUtils.readFileAsString(ADVS_PATH + request.params(":uuid") + ".json");
        response.status(200);
        response.type("application/json");
        return playerAdvancements;
    };
}
