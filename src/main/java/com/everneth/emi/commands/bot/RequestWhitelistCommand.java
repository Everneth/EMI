package com.everneth.emi.commands.bot;


import co.aikar.idb.DbRow;
import com.everneth.emi.services.WhitelistService;
import com.everneth.emi.utils.PlayerUtils;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.Role;
import org.bukkit.entity.Player;

import java.util.List;

public class RequestWhitelistCommand extends Command {
    public RequestWhitelistCommand() { this.name = "whitelist"; }

    @Override
    protected void execute(CommandEvent event) {
        // We need to make sure the command sender has the correct authorized roles.
        // Assume no one has roles
        boolean hasRequiredRoles = false;
        // Get the roles from the member
        List<Role> roleList = event.getMember().getRoles();
        // Lets check them
        for(Role role : roleList)
        {
            if(role.getName().equals("Citizen"))
            {
                // Found a required role, no need to find the other, break from the loop
                hasRequiredRoles = true;
                break;
            }
        }
        if (!hasRequiredRoles) {
            event.reply("You do not have the Citizen role!");
            return;
        }
        DbRow playerRow = PlayerUtils.getPlayerRow(event.getMember().getIdLong());
        if (playerRow != null) {
            event.reply("You are already synced, you do not need to apply for temporary whitelisting.");
            return;
        }
        String username = event.getArgs().split(" ")[0];
        if (username == null || username.isEmpty()) {
            event.reply("You did not provide a name for me to whitelist");
            return;
        }
        if (PlayerUtils.getPlayerRow(username) != null || WhitelistService.getService().isWhitelisted(username)) {
            event.reply("That user is already on the whitelist. If this is an error please contact Staff.");
            return;
        }

        WhitelistService.getService().AddToWhitelistTemporarily(username);
        event.reply("Added you to the whitelist for 5 minutes. Please login to `play.everneth.com` and use `/discord sync <Name#0000>`");
    }
}
