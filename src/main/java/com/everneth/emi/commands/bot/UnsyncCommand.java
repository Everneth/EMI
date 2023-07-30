package com.everneth.emi.commands.bot;

import co.aikar.idb.DB;
import com.everneth.emi.EMI;
import com.everneth.emi.models.EMIPlayer;
import com.everneth.emi.models.enums.DiscordRole;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.entities.Member;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitScheduler;

public class UnsyncCommand extends SlashCommand {

    public UnsyncCommand() {
        this.name = "unsync";
        this.help = "If you lost access to your minecraft account, use this to remove the sync to it.";
    }

    @Override
    public void execute(SlashCommandEvent event) {
        // the player does not have a synced account, we can ignore them
        Member member = event.getMember();
        EMIPlayer emiPlayer = EMIPlayer.getEmiPlayer(member.getIdLong());
        if (!emiPlayer.isSynced()) {
            event.reply("Your account is not synchronized.").setEphemeral(true).queue();
            return;
        }
        else if (!event.getMember().getRoles().contains(DiscordRole.CITIZEN.get())) {
            event.reply("Nice try. You're not a citizen!").setEphemeral(true).queue();
            return;
        }

        EMIPlayer player = EMIPlayer.getEmiPlayer(event.getMember().getIdLong());
        String playerUsername = player.getName();
        String altUsername = player.getAltName();

        BukkitScheduler scheduler = EMI.getPlugin().getServer().getScheduler();
        scheduler.callSyncMethod(EMI.getPlugin(), () ->
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "whitelist remove " + playerUsername));
        if (altUsername != null) {
            scheduler.callSyncMethod(EMI.getPlugin(), () ->
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "whitelist remove " + altUsername));
        }

        EMI.getGuild().removeRoleFromMember(event.getMember(), DiscordRole.SYNCED.get()).queue();
        // Set Discord ID field back to null rather than deleting, the whitelist request will check the requested user is not whitelisted
        DB.executeUpdateAsync("UPDATE players SET discord_id = NULL WHERE discord_id = ?",
                event.getMember().getIdLong());


        event.reply("Your discord has been unsynced and your accounts have been removed from the whitelist. " +
                "Please use `/whitelist <username>` to temporarily add another account to the whitelist so you may re-sync.").queue();
    }
}
