package com.everneth.emi.commands.bot;

import co.aikar.idb.DB;
import co.aikar.idb.DbRow;
import com.everneth.emi.EMI;
import com.everneth.emi.utils.PlayerUtils;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.Role;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitScheduler;

public class UnsyncCommand extends Command {

    public UnsyncCommand() { this.name = "unsync"; }

    @Override
    public void execute(CommandEvent event) {
        // the player does not have a synced account, we can ignore them
        if (!PlayerUtils.isMember(event.getMember().getIdLong()))
            return;

        DbRow playerRow = PlayerUtils.getPlayerRow(event.getMember().getIdLong());
        String playerUsername = playerRow.getString("player_name");
        String altUsername = playerRow.getString("alt_name");

        BukkitScheduler scheduler = EMI.getPlugin().getServer().getScheduler();
        scheduler.callSyncMethod(EMI.getPlugin(), () ->
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "whitelist remove " + playerUsername));
        if (altUsername != null) {
            scheduler.callSyncMethod(EMI.getPlugin(), () ->
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "whitelist remove " + altUsername));
        }

        long guildId = EMI.getPlugin().getConfig().getLong("guild-id");
        Role syncedRole = event.getGuild().getRoleById(EMI.getPlugin().getConfig().getLong("synced-role-id"));
        EMI.getJda().getGuildById(guildId).removeRoleFromMember(event.getMember(), syncedRole);
        // remove the user from the DB so their accounts are not read as already whitelisted
        DB.executeUpdateAsync("DELETE FROM players WHERE discord_id = ?",
                event.getMember().getIdLong());

        event.reply("Your discord has been unsynced and your accounts have been removed from the whitelist. " +
                "Please use `!whitelist <username>` to temporarily add another account to the whitelist so you may re-sync.");
    }
}
