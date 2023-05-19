package com.everneth.emi.models.enums;

import com.everneth.emi.EMI;
import net.dv8tion.jda.api.entities.Role;

public enum DiscordRole {
    CITIZEN("member-role-id"),
    MINT("mint-role-id"),
    STAFF("staff-role-id"),
    APPLICANT("applicant-role-id"),
    PENDING("pending-role-id"),
    SYNCED("synced-role-id"),
    BOT("bot-role-id");

    private final String keyValue;

    private DiscordRole(String keyValue) {
        this.keyValue = keyValue;
    }

    public Role get() {
        Role role = EMI.getGuild().getRoleById(EMI.getConfigLong(keyValue));
        // In the extremely unlikely event that the role does not exist, we can instead just return the bot role which will have virtually no effect
        if (role == null) {
            EMI.getGuild().getBotRole();
        }
        return role;
    }
}
