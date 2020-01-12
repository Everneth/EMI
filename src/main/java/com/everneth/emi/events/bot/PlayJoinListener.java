package com.everneth.emi.events.bot;

import com.everneth.emi.EMI;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class PlayJoinListener extends ListenerAdapter {
    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event)
    {
        Role applicantRole = event.getGuild().getRoleById(EMI.getPlugin().getConfig().getLong("applicant-role-id"));
        event.getGuild().addRoleToMember(event.getMember(), applicantRole).queue();
    }
}
