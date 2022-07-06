package com.everneth.emi.models.enums;

import com.everneth.emi.EMI;
import net.dv8tion.jda.api.entities.Role;

public enum DiscordRole {
    CITIZEN("member-role-id"),
    MINT("mint-role-id"),
    STAFF("staff-role-id"),
    APPLICANT("applicant-role-id"),
    PENDING("pending-role-id"),
    SYNCED("synced-role-id");

    private final String keyValue;

    private DiscordRole(String keyValue) {
        this.keyValue = keyValue;
    }

    public Role get() {
        return EMI.getGuild().getRoleById(EMI.getConfigLong(keyValue));
    }
}
