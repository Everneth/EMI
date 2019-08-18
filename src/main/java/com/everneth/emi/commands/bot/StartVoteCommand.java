package com.everneth.emi.commands.bot;

import com.everneth.emi.EMI;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;

public class StartVoteCommand extends Command {

    public StartVoteCommand()
    {
        this.name ="startvote";
        this.guildOnly = false;
    }
    @Override
    protected void execute(CommandEvent event)
    {
        Role ministryMember = event.getGuild().getRoleById(455522908521889814L);
        Role staffRole = event.getGuild().getRoleById(EMI.getPlugin().getConfig().getLong("staff-role-id"));
        if(event.getMember().getRoles().contains(ministryMember) || event.getMember().getRoles().contains(staffRole))
        {
            Member member = event.getGuild().getMemberByTag(event.getArgs());
            if(event.getGuild().getMembers().contains(member))
            {
                event.reply(event.getAuthor().getName() + " has requested a vote for " + event.getArgs() +
                        ". 3 positive reactions are required for the vote to be sent to staff.", (message -> {
                            message.addReaction("white_check_mark").queue();
                            message.addReaction("no_entry").queue();
                }));

            }
            else
            {
                event.reply("Could not find this user. Did you @mention them?");
            }

        }
        else
        {
            event.reply("You cannot use this command.");
        }
    }
}
