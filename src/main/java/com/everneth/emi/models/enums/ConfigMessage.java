package com.everneth.emi.models.enums;

import com.everneth.emi.EMI;

import java.text.MessageFormat;

public enum ConfigMessage {
    /**
     * For use when a Discord user cannot be found in the guild
     */
    USER_NOT_FOUND("user-not-found-error"),
    /**
     * For use when an EMIPlayer cannot be found in the database
     */
    PLAYER_NOT_FOUND("player-not-found-error"),
    /**
     * For use when a Discord message could not be sent to a user
     */
    DISCORD_MESSAGE_FAILED("message-send-error"),
    /**
     * For use when notifying the staff member how many points have been accumulated by the
     * user being issued points
     */
    POINTS_ACCRUED("points-accumulated-alert"),
    /**
     * For use when notifying a user via Discord that they have been issued points as well as the accompanying
     * punishment and length
     */
    POINTS_GAINED_WARNING("issued-point-alert"),
    /**
     * For use when notifying a user via Discord that they have had points removed and their punishment has
     * been adjusted accordingly
     */
    POINTS_REMOVED_WARNING("removed-point-alert"),
    /**
     * For use when notifying a Discord user that a player is attempting to synchronize their Minecraft
     * account to their Discord account
     */
    ACCOUNT_SYNCED("account-sync-alert"),
    /**
     * For use when notifying a Discord user that their Discord account has been unsynced from their Minecraft account
     */
    ACCOUNT_UNSYNCED("account-unsync-alert"),
    /**
     * For use when notifying a user that their application has been approved and they have been whitelisted
     */
    APPLICATION_APPROVED("application-approved-alert"),
    /**
     * For use when notifying a user that their application has been denied
     */
    APPLICATION_DENIED("application-denied-alert"),
    /**
     * For use when no ban reason has been specified
     */
    DEFAULT_BAN_REASON("default-ban-reason"),
    /**
     * For use when a referrer gets a point per section IV c
     */
    FRIEND_ACCOUNTABILITY_REASON("friend-accountability-reason");
    private final String keyValue;

    ConfigMessage(String keyValue) {
        this.keyValue = keyValue;
    }

    public String get() {
        String message = EMI.getPlugin().getConfig().getString(keyValue);
        if (message == null) {
            message = "Internal error occurred. " + keyValue + " not found. Please contact an admin.";
        }

        return message;
    }

    public String getWithArgs(Object... args) {
        return MessageFormat.format(this.get(), args);
    }
}
