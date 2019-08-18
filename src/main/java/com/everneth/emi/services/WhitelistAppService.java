package com.everneth.emi.services;

import com.everneth.emi.models.WhitelistApp;

import java.util.HashMap;

public class WhitelistAppService {
    private static WhitelistAppService service;
    private HashMap<Long, WhitelistApp> appMap = new HashMap<>();
    private WhitelistAppService() {}

    public static WhitelistAppService getService()
    {
        if(service == null)
        {
            service = new WhitelistAppService();
        }
        return service;
    }

    public void addApp(long id, WhitelistApp app)
    {
        app.setStep(1);
        app.setDiscordId(id);
        appMap.put(id, app);
    }

    public void removeApp(long id)
    {
        appMap.remove(id);
    }

    public void addData(long id, int step, String data)
    {
        switch(step)
        {
            case 1:
                appMap.get(id).setInGameName(data);
            case 2:
                appMap.get(id).setLocation(data);
            case 3:
                appMap.get(id).setAge(Integer.valueOf(data));
            case 4:
                appMap.get(id).setFriend(data);
            case 5:
                appMap.get(id).setLookingFor(data);
            case 6:
                appMap.get(id).setIntro(data);
            case 7:
                appMap.get(id).setSecretWord(data);
        }
        appMap.get(id).setStep(appMap.get(id).getStep() + 1);
    }
}
