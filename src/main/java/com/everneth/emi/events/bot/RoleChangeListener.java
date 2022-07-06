package com.everneth.emi.events.bot;

import com.everneth.emi.EMI;
import com.everneth.emi.models.WhitelistVote;
import com.everneth.emi.models.enums.DiscordRole;
import com.everneth.emi.services.VotingService;
import com.everneth.emi.services.WhitelistAppService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.interactions.components.Button;

import java.awt.*;

public class RoleChangeListener extends ListenerAdapter {
    private final String APPROVE_REACTION = "\u2705";
    private final String REJECT_REACTION = "\u26D4";

    @Override
    public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent event) {
        if (event.getRoles().contains(DiscordRole.PENDING.get())) {
            // the user does not have an application, let's remove the pending role from them
            if (!WhitelistAppService.getService().appExists(event.getMember().getIdLong())) {
                EMI.getGuild().removeRoleFromMember(event.getMember(), DiscordRole.PENDING.get()).queue();
                return;
            }

            event.getGuild().getTextChannelById(EMI.getPlugin().getConfig().getLong("voting-channel-id"))
                    .sendMessage("Heads up @everyone! " + event.getMember().getAsMention() + " has just met requirements.")
                    .setEmbeds(createVoteEmbed(event.getMember()))
                    .setActionRow(Button.success("confirm", Emoji.fromUnicode(APPROVE_REACTION)),
                            Button.danger("deny", Emoji.fromUnicode(REJECT_REACTION)))
                    .queue((msg) ->
                            {
                                VotingService.getService().startVote(msg.getIdLong(), new WhitelistVote(
                                        event.getUser().getIdLong(),
                                        msg.getIdLong()
                                ));
                            }
                    );
            event.getGuild().removeRoleFromMember(event.getMember(), DiscordRole.APPLICANT.get()).queue();
        }
    }

    private MessageEmbed createVoteEmbed(Member member) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Whitelist Vote for " + member.getEffectiveName());
        builder.setThumbnail(member.getEffectiveAvatarUrl());
        builder.setColor(Color.ORANGE);
        builder.setDescription("This is how everyone has voted so far:");
        builder.addField("Voted Yay", "*Nobody*", false);
        builder.addField("Voted Nay", "*Nobody*", false);

        return builder.build();
    }
}
