package com.everneth.emi.services;

<<<<<<< HEAD
import com.everneth.emi.EMI;
=======
>>>>>>> 0c39a0555f20fd4f125f09fa90d0c43afcfadf11
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
<<<<<<< HEAD
        app.setInProgress(true);
=======
>>>>>>> 0c39a0555f20fd4f125f09fa90d0c43afcfadf11
        appMap.put(id, app);
    }

    public void removeApp(long id)
    {
        appMap.remove(id);
    }

<<<<<<< HEAD
    public WhitelistApp findByDiscordId(long id)
    {
        return appMap.get(id);
    }

=======
>>>>>>> 0c39a0555f20fd4f125f09fa90d0c43afcfadf11
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
<<<<<<< HEAD
                appMap.get(id).setBannedElsewhere(data);
            case 6:
                appMap.get(id).setLookingFor(data);
            case 7:
                appMap.get(id).setLoveHate(data);
            case 8:
                appMap.get(id).setIntro(data);
            case 9:
                appMap.get(id).setSecretWord(data);
            case 10:
                appMap.get(id).setStep(0); // restart app
        }
        appMap.get(id).setStep(appMap.get(id).getStep() + 1);
        if(appMap.get(id).getStep() >= 13)
        {
            appMap.get(id).setInProgress(false);
        }
=======
                appMap.get(id).setLookingFor(data);
            case 6:
                appMap.get(id).setIntro(data);
            case 7:
                appMap.get(id).setSecretWord(data);
        }
        appMap.get(id).setStep(appMap.get(id).getStep() + 1);
>>>>>>> 0c39a0555f20fd4f125f09fa90d0c43afcfadf11
    }
}
