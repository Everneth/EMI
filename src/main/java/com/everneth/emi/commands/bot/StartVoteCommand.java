package com.everneth.emi.commands.bot;

import com.everneth.emi.EMI;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;

import javax.swing.text.html.Option;
import java.io.CharArrayReader;
import java.sql.Array;
import java.util.ArrayList;

public class StartVoteCommand extends SlashCommand {

    public StartVoteCommand()
    {
        this.name = "startvote";
        this.help = "Forcefully start a whitelist vote for a user";

        this.options = new ArrayList<>();
        this.options.add(new OptionData(OptionType.USER, "user", "The user you would like to start a vote for."));
        this.guildOnly = false;
    }
    @Override
    protected void execute(SlashCommandEvent event)
    {
        Role ministryMember = event.getGuild().getRoleById(455522908521889814L);
        Role staffRole = event.getGuild().getRoleById(EMI.getPlugin().getConfig().getLong("staff-role-id"));
        if(event.getMember().getRoles().contains(ministryMember) || event.getMember().getRoles().contains(staffRole))
        {
            Member member = event.getGuild().getMemberByTag(event.getOption("name").getAsString());
            if(event.getGuild().getMembers().contains(member))
            {
                event.reply(event.getUser().getName() + " has requested a vote for " + event.getOption("name").getAsString() +
                        ". 3 positive reactions are required for the vote to be sent to staff.").queue();

            }
            else
            {
                event.reply("Could not find this user. Did you @mention them?").setEphemeral(true).queue();
            }

        }
        else
        {
            event.reply("You cannot use this command.").setEphemeral(true).queue();
        }
    }
}
