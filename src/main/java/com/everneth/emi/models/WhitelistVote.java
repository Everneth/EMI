package com.everneth.emi.models;

import co.aikar.idb.DB;
import com.everneth.emi.EMI;

import java.sql.SQLException;

public class WhitelistVote {
    private long applicantDiscordId;
    private boolean completed;
    private long messageId;
    private int id;


    public WhitelistVote(long applicantDiscordId, long messageId)
    {
        this.applicantDiscordId = applicantDiscordId;
        this.messageId = messageId;
        this.completed = false;
        this.insertVote();
    }

    // Used only in Load()
    public WhitelistVote(long applicantDiscordId, long messageId, boolean completed)
    {
        this.applicantDiscordId = applicantDiscordId;
        this.messageId = messageId;
        this.completed = completed;
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

    public void updateVote() {
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
}
