package com.everneth.emi.events.bot;

import com.everneth.emi.EMI;
import com.everneth.emi.models.WhitelistVote;
import com.everneth.emi.services.VotingService;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;

public class RoleChangeListener extends ListenerAdapter {
    @Override
    public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent event)
    {
        Role pendingRole = event.getGuild().getRoleById(EMI.getPlugin().getConfig().getLong("pending-role-id"));
        if(event.getRoles().contains(pendingRole))
            event.getGuild().getTextChannelById(EMI.getPlugin().getConfig().getLong("staff-channel-id"))
                    .sendMessage(event.getMember().getAsMention() + " has just met requirements.").queue(
                    (msg) -> {
                        VotingService.getService().addVote(msg.getIdLong(), new WhitelistVote(
                                event.getUser().getIdLong(),
                                msg.getIdLong()
                        ));
                        msg.addReaction(":white_check_mark:").queue();
                        msg.addReaction("no_entry").queue();
                    }
            );
            // notify user
    }
}
