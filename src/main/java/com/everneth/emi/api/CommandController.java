package com.everneth.emi.api;

import com.everneth.emi.EMI;
import com.everneth.emi.models.CommandPayload;
import com.google.gson.Gson;
import org.bukkit.Bukkit;
import spark.Request;
import spark.Response;
import spark.Route;

public class CommandController {
    public static Route sendCommandPayload = (Request request, Response response) -> {
        Gson gson = new Gson();
        CommandPayload commandPayload = gson.fromJson(request.body(), CommandPayload.class);
        EMI.getPlugin().getServer().dispatchCommand(Bukkit.getConsoleSender(), commandPayload.toString());
        return null;
    };
}
