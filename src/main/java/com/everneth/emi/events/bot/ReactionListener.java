package com.everneth.emi.events.bot;

import com.everneth.emi.EMI;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.RestAction;

import java.util.List;

public class ReactionListener extends ListenerAdapter {
    @Override
    public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event)
    {
        // Check and make sure its a valid reaction
        if(event.getReactionEmote().getEmoji().equals(":white_check_mark:") ||
                event.getReactionEmote().getEmoji().equals(":no_entry:"))
        {
            // It is, grab the staff role and a list of staff users
            Role staffRole = event.getGuild().getRoleById(EMI.getPlugin().getConfig().getLong("staff-role-id"));
            List<Member> staffMembers = event.getGuild().getMembersWithRoles(staffRole);

            // First check the amount of reactions subtracting 2 for the bots reactions
            event.getChannel().retrieveMessageById(event.getMessageId()).queue(
                    (msg) -> {
                        int inFavor = 0, against = 0;
                        for(MessageReaction reaction : msg.getReactions())
                        {
                            if(reaction.getReactionEmote().getEmoji().equals(":white_check_mark:"))
                                ++inFavor;
                            else
                                ++against;
                        }
                    }
            );

        }
        else
        {
            // some bozo decided to add a reaction that the bot didn't supply
            // Get the message using the message id supplied by the event
            // on the success callback of queue() delete the message
            event.getChannel().retrieveMessageById(event.getMessageIdLong()).queue(
                    (msg) -> {
                        msg.delete().queue();
                        // Then open up a DM channel with the user and yell at them.
                        event.getUser().openPrivateChannel().queue(
                                (reply) ->
                                {
                                    reply.sendMessage("Please only use the reactions supplied.").queue();
                                }
                        );
                    }
            );
        }
    }
}
