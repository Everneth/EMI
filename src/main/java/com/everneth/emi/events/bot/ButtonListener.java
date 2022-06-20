package com.everneth.emi.events.bot;

import com.everneth.emi.services.VotingService;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ButtonListener extends ListenerAdapter {

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
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
