package com.everneth.emi.models;

import java.util.Date;
import java.util.Timer;

public class WhitelistVote {
    private int votesInFavor;
    private int votesAgainst;
    private long applicantDiscordId;
    private boolean completed;
    private Timer timer;
    private long messageId;


    public WhitelistVote(long applicantDiscordId, long messageId)
    {
        this.applicantDiscordId = applicantDiscordId;
        this.messageId = messageId;
        this.timer = new Timer();
        this.completed = false;
        this.votesAgainst = 0;
        this.votesInFavor = 0;
        // Set timer to expire in 24 hours
        // Insert vote after constructed
    }

    public int InsertVote() {return 0;}

    public int UpdateVote() {return 0;}
}
