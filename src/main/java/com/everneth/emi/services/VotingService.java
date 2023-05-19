package com.everneth.emi.services;

import co.aikar.idb.DB;
import co.aikar.idb.DbRow;
import com.everneth.emi.EMI;
import com.everneth.emi.models.EMIPlayer;
import com.everneth.emi.models.WhitelistVote;
import com.everneth.emi.models.enums.ConfigMessage;
import com.everneth.emi.models.enums.DiscordRole;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class VotingService {

    private HashMap<Long, WhitelistVote> voteMap = new HashMap<>();
    public static VotingService service;
    private FileConfiguration config = EMI.getPlugin().getConfig();

    private VotingService() {}

    public static VotingService getService()
    {
        if(service == null)
        {
            service = new VotingService();
            service.load();
        }
        return service;
    }
    public void startVote(long messageId, WhitelistVote vote)
    {
        voteMap.put(messageId, vote);
    }

    public void endVote(long messageId, boolean approved, ButtonInteractionEvent event)
    {
        WhitelistVote vote = voteMap.get(messageId);

        Guild guild = EMI.getGuild();
        Member applicant = guild.getMemberById(vote.getApplicantDiscordId());

        if (approved) {
            // Queues have to be delayed to avoid Discord removing the role due to rate limiting
            guild.addRoleToMember(applicant, DiscordRole.CITIZEN.get()).queue();
            guild.addRoleToMember(applicant, DiscordRole.SYNCED.get()).queueAfter(1, TimeUnit.SECONDS);

            guild.getTextChannelById(config.getLong("announcement-channel-id"))
                    .sendMessage(ConfigMessage.APPLICATION_APPROVED.getWithArgs(applicant.getAsMention())).queue();
        }
        else {
            applicant.getUser().openPrivateChannel().queue(privateChannel ->
                    privateChannel.sendMessage(ConfigMessage.APPLICATION_DENIED.getWithArgs(applicant.getEffectiveName()))
                            .queue(null, new ErrorHandler().handle(ErrorResponse.CANNOT_SEND_TO_USER,
                                    error ->
                                        EMI.getGuild().getTextChannelById(config.getLong("voting-channel-id"))
                                                .sendMessage(ConfigMessage.DISCORD_MESSAGE_FAILED.get()).queue()
                                    )));
        }

        // I have yet to figure out a better way of going about disabling buttons, the answer is somewhere within ActionRows which
        // I generally do not understand proper usage of
        List<Button> disabledButtons = new ArrayList<>();
        event.getMessage().getButtons().forEach(button -> disabledButtons.add(button.asDisabled()));
        event.getMessage().editMessage("The vote is now over. Applicant " + applicant.getAsMention() + (approved ? " accepted." : " denied."))
                .setActionRow(disabledButtons).queueAfter(2, TimeUnit.SECONDS);

        guild.removeRoleFromMember(applicant, DiscordRole.PENDING.get()).queueAfter(5, TimeUnit.SECONDS);
        removeVote(messageId);
    }

    public void removeVote(long messageId) {
        // Update the database and remove reference to the whitelist vote from memory
        voteMap.get(messageId).setInactive();
        voteMap.remove(messageId);
    }

    public void removeVoteByDiscordId(long discordId) {
        long messageId = 0;
        for (WhitelistVote vote : voteMap.values()) {
            if (vote.getApplicantDiscordId() == discordId) {
                messageId = vote.getMessageId();
                break;
            }
        }

        if (messageId != 0)
            removeVote(messageId);
    }

    public boolean isVotingMessage(long messageId)
    {
        return this.voteMap.containsKey(messageId);
    }

    public WhitelistVote getVoteByMessageId(long messageId)
    {
        return this.voteMap.get(messageId);
    }

    public void load() {
        List<DbRow> results = new ArrayList<>();
        try
        {
            results = DB.getResultsAsync("SELECT * FROM votes WHERE is_active = 1").get();
        }
        catch (Exception e)
        {
            EMI.getPlugin().getLogger().warning(e.getMessage());
        }
        if(!results.isEmpty()) {
            for (DbRow result : results) {
                long messageId = result.getLong("message_id");
                this.voteMap.put(
                        messageId,
                        new WhitelistVote(
                                result.getLong("applicant_id"),
                                messageId,
                                false)
                );

                EMI.getGuild().getTextChannelById(EMI.getConfigLong("voting-channel-id"))
                        .retrieveMessageById(messageId).queue(message -> {
                            WhitelistVote vote = voteMap.get(messageId);
                            if (message.getEmbeds().size() > 0) {
                                for (MessageEmbed.Field field : message.getEmbeds().get(0).getFields()) {
                                    loadVoters(vote, field);
                                }
                            }
                        }, new ErrorHandler()
                                .handle(ErrorResponse.UNKNOWN_MESSAGE, error -> {
                                    voteMap.get(messageId).setInactive();
                                    voteMap.remove(messageId);
                                    EMI.getPlugin().getLogger().warning("Vote message not found. {id: " + messageId + "}");
                                }));
            }
        }
    }

    private void loadVoters(WhitelistVote vote, MessageEmbed.Field field) {
        String content = field.getValue();

        // use the pattern for mentioned users to find all who have voted
        Pattern pattern = Pattern.compile("<@!?(\\d+)>");
        Matcher matcher = pattern.matcher(content);
        while(matcher.find()) {
            long memberId = Long.parseLong(matcher.group(1));
            Member member = EMI.getGuild().getMemberById(memberId);

            if (field.getName().contains("Yay")) {
                vote.addPositiveVoter(member);
            }
            else if (field.getName().contains("Nay")) {
                vote.addNegativeVoter(member);
            }
        }
    }

    public void onPositiveVoter(ButtonInteractionEvent event) {
        WhitelistVote vote = getVoteByMessageId(event.getMessageIdLong());
        // Vote should be null any time the vote is ended or set inactive
        if (vote == null) {
            disableMessage(event);
            return;
        }
        vote.addPositiveVoter(event.getMember());
        onVote(event, vote);
    }

    public void onNegativeVoter(ButtonInteractionEvent event) {
        WhitelistVote vote = getVoteByMessageId(event.getMessageIdLong());
        // Vote should be null any time the vote is ended or set inactive
        if (vote == null) {
            disableMessage(event);
            return;
        }
        vote.addNegativeVoter(event.getMember());
        onVote(event, vote);
    }

    private void onVote(ButtonInteractionEvent event, WhitelistVote vote) {
        updateVoteEmbed(event);

        Member applicant = event.getGuild().getMemberById(vote.getApplicantDiscordId());
        EMIPlayer applicantPlayer = EMIPlayer.getEmiPlayer(vote.getApplicantDiscordId());
        if (hasMajority(vote.getPositiveVoters(), 51)) {
            String ign = applicantPlayer.getName();
            new BukkitRunnable() {
                @Override
                public void run() {
                    EMI.getPlugin().getServer().dispatchCommand(Bukkit.getConsoleSender(), "whitelist add " + ign);
                }
            }.runTask(EMI.getPlugin());

            endVote(event.getMessageIdLong(), true, event);
        }
        else if (hasMajority(vote.getNegativeVoters(), 50)) {
            endVote(event.getMessageIdLong(), false, event);
        }
    }

    private boolean hasMajority(HashSet<Member> voters, int percentRequired) {
        int staffCount = EMI.getGuild().getMembersWithRoles(DiscordRole.STAFF.get()).size();

        return (voters.size() * 100) / staffCount >= percentRequired;
    }

    private void updateVoteEmbed(ButtonInteractionEvent event) {
        EmbedBuilder builder = new EmbedBuilder(event.getMessage().getEmbeds().get(0));
        builder.clearFields();

        WhitelistVote vote = voteMap.get(event.getMessage().getIdLong());
        String positiveVoters = vote.getPositiveVoters().stream()
                .map(Member::getAsMention)
                .collect(Collectors.joining(" "));
        String negativeVoters = vote.getNegativeVoters().stream()
                .map(Member::getAsMention)
                .collect(Collectors.joining(" "));

        builder.addField("Voted Yay", positiveVoters, false);
        builder.addField("Voted Nay", negativeVoters, false);

        // We wait for the message embed update to complete before proceeding so the cached message is updated
        // This prevents the issue of the last person to vote not being included in the message embed
        event.editMessageEmbeds(builder.build()).queue();
    }

    private void disableMessage(ButtonInteractionEvent event) {
        List<Button> disabledButtons = new ArrayList<>();
        event.getMessage().getButtons().forEach(button -> disabledButtons.add(button.asDisabled()));
        event.editMessage("Something has gone wrong. Vote ended.")
                .setActionRow(disabledButtons).queue();
    }
}
