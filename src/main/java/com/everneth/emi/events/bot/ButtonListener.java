package com.everneth.emi.events.bot;

import com.everneth.emi.services.VotingService;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ButtonListener extends ListenerAdapter {

    @Override
    public void onButtonClick(ButtonClickEvent event) {
        switch (event.getComponentId()) {
            case "confirm":
                VotingService.getService().onPositiveVoter(event);
                break;
            case "deny":
                VotingService.getService().onNegativeVoter(event);
                break;
        }
    }
}
