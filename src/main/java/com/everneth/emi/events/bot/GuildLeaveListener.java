package com.everneth.emi.events.bot;


import co.aikar.idb.DB;
import com.everneth.emi.EMI;
import com.everneth.emi.models.enums.DiscordRole;
import com.everneth.emi.services.VotingService;
import com.everneth.emi.services.WhitelistAppService;
import net.dv8tion.jda.api.entities.Role;
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
            DB.executeUpdateAsync("UPDATE votes SET is_active = 0 WHERE applicant_id = ?", userDiscordId);
        }
        else if (event.getMember().getRoles().contains(DiscordRole.APPLICANT.get()))
        {
            WhitelistAppService.getService().removeApp(userDiscordId);
            DB.executeUpdateAsync("UPDATE applications SET app_active = 0 WHERE applicant_discord_id = ?", userDiscordId);
        }
        event.getUser().getIdLong();
    }
}
