package com.everneth.emi.events.bot;


import co.aikar.idb.DB;
import com.everneth.emi.models.enums.DiscordRole;
import com.everneth.emi.services.VotingService;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class GuildLeaveListener extends ListenerAdapter {
    @Override
    public void onGuildMemberRemove(GuildMemberRemoveEvent event)
    {
        long userDiscordId = event.getUser().getIdLong();

        if(event.getMember().getRoles().contains(DiscordRole.PENDING.get()))
        {
            VotingService.getService().removeVote(userDiscordId);
        }
    }
}
