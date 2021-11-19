package com.everneth.emi.events.bot;

import com.everneth.emi.EMI;
import com.everneth.emi.models.WhitelistVote;
import com.everneth.emi.services.VotingService;
import com.everneth.emi.utils.PlayerUtils;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;

public class RoleChangeListener extends ListenerAdapter {
    private final String APPROVE_REACTION = "\u2705";
    private final String REJECT_REACTION = "\u26D4";
    @Override
    public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent event)
    {
        Role pendingRole = event.getGuild().getRoleById(EMI.getPlugin().getConfig().getLong("pending-role-id"));
        Role citizenRole = event.getGuild().getRoleById(EMI.getPlugin().getConfig().getLong("member-role-id"));
        if(event.getRoles().contains(pendingRole))
            event.getGuild().getTextChannelById(EMI.getPlugin().getConfig().getLong("voting-channel-id"))
                    .sendMessage("Heads up @everyone! " + event.getMember().getAsMention() + " has just met requirements.").queue(
                    (msg) -> {
                        VotingService.getService().addVote(msg.getIdLong(), new WhitelistVote(
                                event.getUser().getIdLong(),
                                msg.getIdLong()
                        ));

                        msg.addReaction(APPROVE_REACTION).queue();
                        msg.addReaction(REJECT_REACTION).queue();
                    }
            );
        else if(PlayerUtils.isMember(event.getUser().getIdLong()))
        {
            if(event.getGuild().getMemberById(event.getUser().getIdLong()).getRoles().isEmpty()) {
                event.getGuild().getTextChannelById(EMI.getPlugin().getConfig().getLong("whitelist-channel-id"))
                        .sendMessage("Welcome back " + event.getUser().getAsMention() + "!").queue();
            }
        }
    }
}
