package com.everneth.emi.models;

import co.aikar.idb.DB;

import java.util.UUID;

public class Report {
    private long discordUserId;
    private long channelId;
    private long messageId;
    public Report(long channelId)
    {
        this.discordUserId = 0;
        this.channelId = channelId;
        this.messageId = 0;
    }
    public Report(long channelId, long messageId)
    {
        this.discordUserId = 0;
        this.channelId = channelId;
        this.messageId = messageId;
    }
    public Report(long channelId, long messageId, long discordUserId)
    {
        this.discordUserId = discordUserId;
        this.channelId = channelId;
        this.messageId = messageId;
    }

    public long getDiscordUserId() {
        return discordUserId;
    }

    public void setDiscordUserId(long discordUserId) {
        this.discordUserId = discordUserId;
    }

    public long getChannelId() {
        return channelId;
    }

    public void setChannelId(long channelId) {
        this.channelId = channelId;
    }

    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    public static getReportRecord(UUID uuid)
    {
        
    }
    public static addReportRecord(Report report)
    {

    }
}
