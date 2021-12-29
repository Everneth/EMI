package com.everneth.emi.events.bot;


import co.aikar.idb.DB;
import com.everneth.emi.EMI;
import com.everneth.emi.services.VotingService;
import com.everneth.emi.services.WhitelistAppService;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class GuildLeaveListener extends ListenerAdapter {
    @Override
    public void onGuildMemberRemove(GuildMemberRemoveEvent event)
    {
        Role pendingRole = event.getGuild().getRoleById(EMI.getPlugin().getConfig().getLong("pending-role-id"));
        Role applicantRole = event.getGuild().getRoleById(EMI.getPlugin().getConfig().getLong("applicant-role-id"));
        long userDiscordId = event.getUser().getIdLong();

        if(event.getMember().getRoles().contains(pendingRole))
        {
            // TODO: Remove vote from service, change is_active to 0
            VotingService.getService().removeVote(userDiscordId);
            DB.executeUpdateAsync("UPDATE votes SET is_active = 0 WHERE applicant_id = ?", userDiscordId);
        }
        else if (event.getMember().getRoles().contains(applicantRole))
        {
            // TODO: Remove application from service, change app_active to 0
            WhitelistAppService.getService().removeApp(userDiscordId);
            DB.executeUpdateAsync("UPDATE applications SET app_active = 0 WHERE applicant_discord_id = ?", userDiscordId);
        }
        event.getUser().getIdLong();
    }
}
