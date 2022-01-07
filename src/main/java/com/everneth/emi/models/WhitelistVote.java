package com.everneth.emi.models;

import co.aikar.idb.DB;
import com.everneth.emi.EMI;
import net.dv8tion.jda.api.entities.Member;

import java.sql.SQLException;
import java.util.HashSet;

public class WhitelistVote {
    private long applicantDiscordId;
    private boolean completed;
    private long messageId;
    private int id;
    private final HashSet<Member> positiveVoters;
    private final HashSet<Member> negativeVoters;


    public WhitelistVote(long applicantDiscordId, long messageId)
    {
        this.applicantDiscordId = applicantDiscordId;
        this.messageId = messageId;
        this.completed = false;
        this.insertVote();

        this.positiveVoters = new HashSet<>();
        this.negativeVoters = new HashSet<>();
    }

    // Used only in Load()
    public WhitelistVote(long applicantDiscordId, long messageId, boolean completed)
    {
        this.applicantDiscordId = applicantDiscordId;
        this.messageId = messageId;
        this.completed = completed;

        this.positiveVoters = new HashSet<>();
        this.negativeVoters = new HashSet<>();
    }

    public void insertVote() {
        try {
            DB.executeInsert("INSERT INTO votes (applicant_id, message_id, is_active) VALUES (?, ?, ?)",
                    this.applicantDiscordId,
                    this.messageId,
                    1);
        }
        catch (SQLException e)
        {
            EMI.getPlugin().getLogger().warning(e.getMessage());
        }
    }

    public void setInactive() {
        try {
            DB.executeUpdateAsync("UPDATE votes SET is_active = 0 WHERE message_id = ?", this.messageId);
        } catch (Exception e) {
            EMI.getPlugin().getLogger().warning(e.getMessage());
        }
    }

    public long getApplicantDiscordId() {
        return applicantDiscordId;
    }

    public void setApplicantDiscordId(long applicantDiscordId) {
        this.applicantDiscordId = applicantDiscordId;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    public HashSet<Member> getPositiveVoters() { return (HashSet<Member>) this.positiveVoters.clone();}

    public void addPositiveVoter(Member member) {
        this.positiveVoters.add(member);

        // members cannot vote for both options, remove their vote from the other set
        this.negativeVoters.remove(member);
    }

    public HashSet<Member> getNegativeVoters() { return (HashSet<Member>) this.negativeVoters.clone(); }

    public void addNegativeVoter(Member member) {
        this.negativeVoters.add(member);

        // members cannot vote for both options,s remove their vote from the other set
        this.positiveVoters.remove(member);
    }
}
