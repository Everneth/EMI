package com.everneth.emi.services;

import com.everneth.emi.EMI;

import com.everneth.emi.models.WhitelistApp;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.MessageChannel;

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
        app.setInProgress(true);
        app.setHoldForNextStep(false);
        appMap.put(id, app);

        MessageBuilder mb = new MessageBuilder();

        EMI.getJda().getGuildById(177976693942779904L).getMemberById(id).getUser().openPrivateChannel().queue(privateChannel ->
                privateChannel.sendMessage("What be ye minecraft IGN?").queue()
        );
    }

    public void removeApp(long id)
    {
        appMap.remove(id);
    }


    public void messageStaffWithEmbed(EmbedBuilder eb2)
    {
        EMI.getJda().getGuildById(EMI.getPlugin().getConfig().getLong("guild-id")).getTextChannelById(EMI.getPlugin().getConfig().getLong("staff-channel-id")).sendMessage(eb2.build()).queue();
        EMI.getJda().getGuildById(EMI.getPlugin().getConfig().getLong("guild-id")).getTextChannelById(EMI.getPlugin().getConfig().getLong("staff-channel-id")).sendMessage("Attempting to transmit application to forums").queue();
    }

    public void messageStaff(String msg)
    {
        EMI.getJda().getGuildById(EMI.getPlugin().getConfig().getLong("guild-id")).getTextChannelById(EMI.getPlugin().getConfig().getLong("staff-channel-id")).sendMessage(msg).queue();
    }

    public WhitelistApp findByDiscordId(long id)
    {
        return appMap.get(id);
    }

    public void addData(long id, int step, String data)
    {
        appMap.get(id).setHoldForNextStep(true);
        switch(step)
        {
            case 1:
                appMap.get(id).setInGameName(data);
                break;
            case 2:
                appMap.get(id).setLocation(data);
                break;
            case 3:
                appMap.get(id).setAge(Integer.valueOf(data));
                break;
            case 4:
                appMap.get(id).setFriend(data);
                break;
            case 5:
                appMap.get(id).setBannedElsewhere(data);
                break;
            case 6:
                appMap.get(id).setLookingFor(data);
                break;
            case 7:
                appMap.get(id).setLoveHate(data);
                break;
            case 8:
                appMap.get(id).setIntro(data);
                break;
            case 9:
                appMap.get(id).setSecretWord(data);
                break;
            case 10:
                //appMap.get(id).setStep(0); // restart app
                break;
        }
        appMap.get(id).setStep(appMap.get(id).getStep() + 1);
        if(appMap.get(id).getStep() >= 13)
        {
            appMap.get(id).setInProgress(false);
        }
    }
}
