package com.everneth.emi.services;

import co.aikar.idb.DB;
import co.aikar.idb.DbRow;

import com.everneth.emi.EMI;
import com.everneth.emi.models.EMIPlayer;
import com.everneth.emi.models.WhitelistVote;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
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
    public void startVote(long id, WhitelistVote vote)
    {
        voteMap.put(id, vote);
    }

    public void endVote(long id, boolean approved, ButtonClickEvent event)
    {
        WhitelistVote vote = voteMap.get(id);
        // Get all the roles that may need to be modified based on the vote outcome
        Role pendingRole = EMI.getGuild().getRoleById(config.getLong("pending-role-id"));
        Role citizenRole = EMI.getGuild().getRoleById(config.getLong("member-role-id"));
        Role syncedRole = EMI.getGuild().getRoleById(config.getLong("synced-role-id"));

        Guild guild = EMI.getGuild();
        DbRow application = EMIPlayer.getAppRecord(vote.getApplicantDiscordId());
        Member applicant = guild.getMemberById(application.getLong("applicant_discord_id"));

        if (approved) {
            guild.addRoleToMember(applicant, citizenRole).queue();
            guild.addRoleToMember(applicant, syncedRole).queue();

            WhitelistAppService.getService().approveWhitelistAppRecord(applicant.getIdLong(), vote.getMessageId());

            guild.getTextChannelById(config.getLong("announcement-channel-id"))
                    .sendMessage(MessageFormat.format(config.getString("application-approved-alert"), applicant.getAsMention())).queue();
        }
        else {
            applicant.getUser().openPrivateChannel().queue(privateChannel ->
                    privateChannel.sendMessage(MessageFormat.format(config.getString("application-denied-alert"), applicant.getEffectiveName()))
                            .queue(null, new ErrorHandler().handle(ErrorResponse.CANNOT_SEND_TO_USER,
                                    error ->
                                        EMI.getGuild().getTextChannelById(config.getLong("voting-channel-id"))
                                                .sendMessage(config.getString("message-send-error")).queue()
                                    )));
        }

        // I have yet to figure out a better way of going about disabling buttons, the answer is somewhere within ActionRows which
        // I generally do not understand proper usage of
        List<Button> disabledButtons = new ArrayList<>();
        event.getMessage().getButtons().forEach(button -> disabledButtons.add(button.asDisabled()));
        event.getMessage().editMessage("The vote is now over. Applicant " + applicant.getAsMention() + (approved ? " accepted." : " denied."))
                .setActionRow(disabledButtons).queueAfter(2, TimeUnit.SECONDS);

        guild.removeRoleFromMember(applicant, pendingRole).queue();
        vote.setInactive();
        voteMap.remove(id);
        WhitelistAppService.getService().removeApp(applicant.getIdLong());
    }

    public void removeVote(long id) { voteMap.remove(id); }

    public DbRow getAppByDiscordId(long id)
    {
        CompletableFuture<DbRow> futureApp;
        DbRow app = new DbRow();
        futureApp = DB.getFirstRowAsync("SELECT * FROM applications WHERE applicant_discord_id = ?", id);
        try {
            app = futureApp.get();
        }
        catch (Exception e)
        {
            EMI.getPlugin().getLogger().info(e.getMessage());
        }
        return app;
    }

    public boolean isVotingMessage(long messageId)
    {
        return this.voteMap.containsKey(messageId);
    }

    public WhitelistVote getVoteByMessageId(long messageId)
    {
        return this.voteMap.get(messageId);
    }

    public long getMessageId(long userid)
    {
        return this.voteMap.get(userid).getMessageId();
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

    public void onPositiveVoter(ButtonClickEvent event) {
        WhitelistVote vote = getVoteByMessageId(event.getMessageIdLong());
        if (vote == null) {
            disableMessage(event);
            return;
        }
        vote.addPositiveVoter(event.getMember());
        onVote(event, vote);
    }

    public void onNegativeVoter(ButtonClickEvent event) {
        WhitelistVote vote = getVoteByMessageId(event.getMessageIdLong());
        if (vote == null) {
            disableMessage(event);
            return;
        }
        vote.addNegativeVoter(event.getMember());
        onVote(event, vote);
    }

    private void onVote(ButtonClickEvent event, WhitelistVote vote) {
        updateVoteEmbed(event);

        DbRow application = EMIPlayer.getAppRecord(getVoteByMessageId(event.getMessage().getIdLong()).getApplicantDiscordId());
        Member applicant = event.getGuild().getMemberById(application.getLong("applicant_discord_id"));

        if (hasMajority(vote.getPositiveVoters(), 51)) {
            String ign = application.getString("mc_ign");
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
        Role staffRole = EMI.getGuild().getRoleById(EMI.getConfigLong("staff-role-id"));
        int staffCount = EMI.getGuild().getMembersWithRoles(staffRole).size();

        return (voters.size() * 100) / staffCount >= percentRequired;
    }

    private void updateVoteEmbed(ButtonClickEvent event) {
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

    private void disableMessage(ButtonClickEvent event) {
        List<Button> disabledButtons = new ArrayList<>();
        event.getMessage().getButtons().forEach(button -> disabledButtons.add(button.asDisabled()));
        event.editMessage("Something has gone wrong. Vote ended.")
                .setActionRow(disabledButtons).queue();
    }
}
