package com.everneth.emi.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import com.everneth.emi.EMI;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

@CommandAlias("report")
public class DiscordsyncCommand extends BaseCommand {
    Plugin plugin = EMI.getPlugin();
    public void onDiscordsync(CommandSender sender, String discordDetails)
    {
        Player player = (Player)sender;
        List<Member> memberList = EMI.getJda().getGuildById(plugin.getConfig().getLong("guild-id")).getMembers();
        List<User> userList = new ArrayList<User>();
        int poundIndex = discordDetails.indexOf('#');
        String name = discordDetails.substring(0, poundIndex);
        String discriminator = discordDetails.substring(poundIndex + 1);

        for(Member member : memberList)
        {
            if(member.getUser().getName().equals(name))
                userList.add(member.getUser());
        }

        if(userList.isEmpty())
            sender.sendMessage("User not found! Please check your details and try again. If this is your second attempt, please contact Comms.");
        else
        {
            if(userList.size() == 1)
            {
                if(userList.get(0).getDiscriminator().equals(discriminator)) {
                    sender.sendMessage("Please check your Discord DMs to verify your account.");
                    userList.get(0).openPrivateChannel().queue((channel) ->
                            {
                                channel.sendMessage(sender.getName() + " is attempting to their minecraft account with our Discord guild. if this is you, please use !!confirmsync to complete the account synchronization. If this is not done by you, please forward this message to staff immediately. Thank you!").queue();
                            }
                    );
                }
                else
                {
                    sender.sendMessage("User not found! Please check your details and try again. If this is your second attempt, please contact Comms.");
                }
            }
            else
            {
                boolean userFound = false;
                for(User user : userList)
                {
                    if(user.getName().equals(name) && user.getDiscriminator().equals(discriminator))
                    {
                        userFound = true;
                        user.openPrivateChannel().queue((channel) ->
                                {
                                    channel.sendMessage(sender.getName() + " is attempting to their minecraft account with our Discord guild. if this is you, please use !!confirmsync to complete the account synchronization. If this is not done by you, please forward this message to staff immediately. Thank you!").queue();
                                }
                        );
                    }
                }
                if(!userFound)
                {
                    sender.sendMessage("User not found! Please check your details and try again. If this is your second attempt, please contact Comms.");
                }
            }
        }
    }
}