package com.everneth.emi.events.bot;

import com.everneth.emi.EMI;
import com.everneth.emi.services.VotingService;
import com.everneth.emi.services.WhitelistAppService;
import com.everneth.emi.utils.PlayerUtils;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.RestAction;
import org.bukkit.Bukkit;

import java.util.List;

public class ReactionListener extends ListenerAdapter {
    @Override
    public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event)
    {
        // ignore events fired for bots and reactions added outside of staff chat
        if(!event.getUser().isBot() && event.getChannel().getIdLong() != EMI.getPlugin().getConfig().getLong("staff-channel-id")) {
            // is this message even a vote message?
            if(VotingService.getService().isVotingMessage(event.getMessageIdLong())) {
                if (event.getReactionEmote().getEmoji().equals(":white_check_mark:") ||
                        event.getReactionEmote().getEmoji().equals(":no_entry:")) {
                    // It is, grab the staff role and a list of staff users
                    Role staffRole = event.getGuild().getRoleById(EMI.getPlugin().getConfig().getLong("staff-role-id"));
                    List<Member> staffMembers = event.getGuild().getMembersWithRoles(staffRole);

                    // First check the amount of reactions subtracting 2 for the bots reactions
                    event.getChannel().retrieveMessageById(event.getMessageId()).queue(
                            (msg) -> {
                                for (MessageReaction reaction : msg.getReactions()) {
                                    // check majority of any reaction, then identify it
                                    if ((reaction.getCount() / staffMembers.size()) * 100 >= 51)
                                    {
                                        // we've reached majority, what action do we take
                                        if(reaction.getReactionEmote().getEmoji().equals(":white_check_mark:"))
                                        {
                                            EMI.getPlugin().getServer().dispatchCommand(Bukkit.getConsoleSender(), "whitelist add " +
                                                    PlayerUtils.getPlayerRow(VotingService.getService().getVoteByMessageId(event.getMessageIdLong()).getApplicantDiscordId()).getString("player_ign"));
                                            event.getGuild().getTextChannelById(EMI.getPlugin().getConfig().getLong("whitelist-channel-id")).sendMessage(
                                                    event.getGuild().getMemberById(VotingService.getService().getVoteByMessageId(event.getMessageIdLong()).getApplicantDiscordId()).getAsMention() + " has been whitelisted! Congrats!").queue();
                                            WhitelistAppService.getService().approveWhitelistAppRecord(VotingService.getService().getVoteByMessageId(event.getMessageIdLong()).getApplicantDiscordId());
                                            VotingService.getService().removeVote(event.getMessageIdLong());
                                            event.getGuild().getTextChannelById(event.getChannel().getIdLong()).editMessageById(event.getMessageIdLong(), "The vote is now over. Applicant accepted.").queue();

                                        }
                                        else
                                        {
                                            event.getGuild().getTextChannelById(event.getChannel().getIdLong()).editMessageById(event.getMessageIdLong(), "The vote is now over. Applicant denied.").queue();
                                        }
                                    }
                                }
                            }
                    );

                } else {
                    // some bozo decided to add a reaction that the bot didn't supply
                    // Get the message using the message id supplied by the event
                    // on the success callback of queue() delete the message
                    event.getChannel().retrieveMessageById(event.getMessageIdLong()).queue(
                            (msg) -> {
                                msg.getReactions().remove(event.getReaction());
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
    }
}
