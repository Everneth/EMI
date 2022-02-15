package com.everneth.emi.commands.bot;

import co.aikar.idb.DB;
import co.aikar.idb.DbRow;
import com.everneth.emi.EMI;
import com.everneth.emi.utils.PlayerUtils;
import com.jagrosh.jdautilities.command.SlashCommand;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitScheduler;

public class UnsyncCommand extends SlashCommand {

    public UnsyncCommand() {
        this.name = "unsync";
        this.help = "If you lost access to your minecraft account, use this to remove the sync to it.";

        this.defaultEnabled = false;
        this.enabledRoles = new String[]{EMI.getPlugin().getConfig().getString("synced-role-id")};
    }

    @Override
    public void execute(SlashCommandEvent event) {
        // the player does not have a synced account, we can ignore them
        Long memberId = event.getMember().getIdLong();
        if (!PlayerUtils.isMember(memberId)) {
            event.reply("Your account is not synchronized.").setEphemeral(true).queue();
            return;
        }

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
        EMI.getJda().getGuildById(guildId).removeRoleFromMember(event.getMember(), syncedRole).queue();
        // remove the user from the DB so their accounts are not read as already whitelisted
        DB.executeUpdateAsync("DELETE FROM players WHERE discord_id = ?",
                event.getMember().getIdLong());

        event.reply("Your discord has been unsynced and your accounts have been removed from the whitelist. " +
                "Please use `/whitelist <username>` to temporarily add another account to the whitelist so you may re-sync.").queue();
    }
}
