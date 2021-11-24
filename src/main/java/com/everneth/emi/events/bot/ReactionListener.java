package com.everneth.emi.events.bot;

import co.aikar.idb.DbRow;
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
        VotingService votingService = VotingService.getService();
        // Ignore reactions to non-voting messages, reactions from bots, and reactions outside the voting channel
        if (!votingService.isVotingMessage(event.getMessageIdLong()) ||
                event.getUser().isBot() ||
                event.getChannel().getIdLong() != EMI.getPlugin().getConfig().getLong("voting-channel-id"))
            return;

        // Get all the roles that may need to be modified based on the vote outcome
        Role pendingRole = event.getGuild().getRoleById(EMI.getPlugin().getConfig().getLong("pending-role-id"));
        Role citizenRole = event.getGuild().getRoleById(EMI.getPlugin().getConfig().getLong("member-role-id"));
        Role syncedRole = event.getGuild().getRoleById(EMI.getPlugin().getConfig().getLong("synced-role-id"));

        // It is, grab the staff role and a list of staff users
        Role staffRole = event.getGuild().getRoleById(EMI.getPlugin().getConfig().getLong("staff-role-id"));
        List<Member> staffMembers = event.getGuild().getMembersWithRoles(staffRole);

        Message message = event.retrieveMessage().complete();

        MessageReaction reaction = event.getReaction();

        int numReactions = reaction.getCount();
        if (((numReactions - 1) * 100) / staffMembers.size() >= 51) {
            // this reaction has created a majority, determine if the applicant is approved or denied
            DbRow application = PlayerUtils.getAppRecord(votingService.getVoteByMessageId(message.getIdLong()).getApplicantDiscordId());
            Member applicant = event.getGuild().getMemberById(application.getLong("applicant_discord_id"));

            if (reaction.getReactionEmote().getEmoji().equals(APPROVE_REACTION)) {
                String ign = application.getString("mc_ign");
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        EMI.getPlugin().getServer().dispatchCommand(Bukkit.getConsoleSender(), "whitelist add " + ign);
                    }
                }.runTask(EMI.getPlugin());

                event.getGuild().getTextChannelById(EMI.getPlugin().getConfig().getLong("whitelist-channel-id"))
                        .sendMessage(applicant.getAsMention() + " has been whitelisted! Congrats!").queue();

                WhitelistAppService.getService().approveWhitelistAppRecord(applicant.getIdLong(), event.getMessageIdLong());
                votingService.getVoteByMessageId(event.getMessageIdLong()).updateVote();
                message.editMessage("The vote is now over. Applicant " + applicant.getAsMention() + " accepted.").queue();

                event.getGuild().removeRoleFromMember(applicant, pendingRole).queue();
                event.getGuild().addRoleToMember(applicant, citizenRole).queue();
                event.getGuild().addRoleToMember(applicant, syncedRole).queue();

                votingService.removeVote(event.getMessageIdLong());
            }
            else if (reaction.getReactionEmote().getEmoji().equals(REJECT_REACTION)) {
                message.editMessage("The vote is now over. Applicant " + applicant.getAsMention() + " denied.").queue();
                event.getGuild().removeRoleFromMember(applicant, pendingRole).queue();

                votingService.removeVote(event.getMessageIdLong());
            }
        }
    }
}
