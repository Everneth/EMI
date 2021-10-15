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
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class ReactionListener extends ListenerAdapter {
    private final String APPROVE_REACTION = "\u2705";
    private final String REJECT_REACTION = "\u26D4";
    @Override
    public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {
        if (VotingService.getService().isVotingMessage(event.getMessageIdLong()) && !event.getUser().isBot()) {
            // ignore events fired for bots and reactions added outside of the voting channel
            if (event.getChannel().getIdLong() == EMI.getPlugin().getConfig().getLong("voting-channel-id")) {
                // is this message even a vote message?
                if (event.getReactionEmote().getEmoji().equals(APPROVE_REACTION) ||
                        event.getReactionEmote().getEmoji().equals(REJECT_REACTION)) {
                    // It is, grab the staff role and a list of staff users
                    Role staffRole = event.getGuild().getRoleById(EMI.getPlugin().getConfig().getLong("staff-role-id"));
                    List<Member> staffMembers = event.getGuild().getMembersWithRoles(staffRole);

                    Message message = event.getChannel().retrieveMessageById(event.getMessageIdLong()).complete();

                    for (MessageReaction reaction : message.getReactions()) {
                        // check majority of any reaction, then identify it
                        double numReactions = reaction.getCount();
                        if (((numReactions-1) / (double) staffMembers.size()) * 100 >= 51) {
                            // we've reached majority, what action do we take
                            if (reaction.getReactionEmote().getEmoji().equals(APPROVE_REACTION)) {
                                String ign = PlayerUtils.getAppRecord(VotingService.getService().getVoteByMessageId(event.getMessageIdLong()).getApplicantDiscordId()).getString("mc_ign");
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        EMI.getPlugin().getServer().dispatchCommand(Bukkit.getConsoleSender(), "whitelist add " + ign);
                                    }
                                }.runTask(EMI.getPlugin());
                                event.getGuild().getTextChannelById(EMI.getPlugin().getConfig().getLong("whitelist-channel-id")).sendMessage(
                                        event.getGuild().getMemberById(VotingService.getService().getVoteByMessageId(event.getMessageIdLong()).getApplicantDiscordId()).getAsMention() + " has been whitelisted! Congrats!").queue();
                                WhitelistAppService.getService().approveWhitelistAppRecord(
                                        VotingService.getService().getVoteByMessageId(event.getMessageIdLong()).getApplicantDiscordId(),
                                        event.getMessageIdLong());
                                VotingService.getService().getVoteByMessageId(event.getMessageIdLong()).updateVote();
                                event.getGuild().getTextChannelById(event.getChannel().getIdLong()).editMessageById(event.getMessageIdLong(),
                                        "The vote is now over. Applicant " +
                                                event.getGuild().getMemberById(VotingService.getService().getVoteByMessageId(event.getMessageIdLong()).getApplicantDiscordId()).getAsMention() +
                                                " accepted.").queue();

                                Role pendingRole = event.getGuild().getRoleById(EMI.getPlugin().getConfig().getLong("pending-role-id"));
                                Role citizenRole = event.getGuild().getRoleById(EMI.getPlugin().getConfig().getLong("member-role-id"));
                                Role syncedRole = event.getGuild().getRoleById(EMI.getPlugin().getConfig().getLong("synced-role-id"));
                                Member memberToEdit = event.getGuild().getMemberById(VotingService.getService().getVoteByMessageId(event.getMessageIdLong()).getApplicantDiscordId());
                                event.getGuild().removeRoleFromMember(memberToEdit, pendingRole).queue();
                                event.getGuild().addRoleToMember(memberToEdit, citizenRole).queue();
                                event.getGuild().addRoleToMember(memberToEdit, syncedRole).queue();
                                VotingService.getService().removeVote(event.getMessageIdLong());
                            } else if (reaction.getReactionEmote().getEmoji().equals(REJECT_REACTION)) {
                                Role pendingRole = event.getGuild().getRoleById(EMI.getPlugin().getConfig().getLong("pending-role-id"));
                                Role applicantRole = event.getGuild().getRoleById(EMI.getPlugin().getConfig().getLong("applicant-role-id"));
                                Member memberToEdit = event.getGuild().getMemberById(VotingService.getService().getVoteByMessageId(event.getMessageIdLong()).getApplicantDiscordId());
                                event.getGuild().removeRoleFromMember(memberToEdit, pendingRole).queue();
                                event.getGuild().addRoleToMember(memberToEdit, applicantRole).queue();
                                event.getGuild().getTextChannelById(event.getChannel().getIdLong()).editMessageById(event.getMessageIdLong(), "The vote is now over. Applicant " +
                                                event.getGuild().getMemberById(VotingService.getService().getVoteByMessageId(event.getMessageIdLong()).getApplicantDiscordId()).getAsMention() +
                                                " denied.").queue();
                                WhitelistAppService.getService().changeRoleToApplicant(VotingService.getService().getVoteByMessageId(event.getMessageIdLong()).getApplicantDiscordId());
                                VotingService.getService().removeVote(event.getMessageIdLong());
                            }
                        }
                    }
                }
                else {
                    // some bozo decided to add a reaction that the bot didn't supply
                    // Get the message using the message id supplied by the event
                    // on the success callback of queue() delete the message
                    event.getChannel().retrieveMessageById(event.getMessageIdLong()).queue(
                            (msg) -> {
                                if (event.getReactionEmote().isEmote()) {
                                    msg.removeReaction(event.getReactionEmote().getEmote(), event.getUser()).queue();
                                } else {
                                    msg.removeReaction(event.getReactionEmote().getEmoji(), event.getUser()).queue();
                                }
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
