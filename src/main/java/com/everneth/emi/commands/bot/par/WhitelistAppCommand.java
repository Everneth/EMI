package com.everneth.emi.commands.bot.par;

import com.everneth.emi.models.WhitelistApp;
import com.everneth.emi.services.WhitelistAppService;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;

import java.util.List;

public class WhitelistAppCommand extends Command {
    public WhitelistAppCommand()
    {
        this.name = "application";
        this.children = new Command[2];
        children[0] = new GetAllApps();
        children[1] = new GetApp();
    }
    @Override
    public void execute(CommandEvent event) {}


    private String convertToMessage(List<WhitelistApp> apps, CommandEvent event)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("```asciidoc\n= Current Application Results =\n");

        for(WhitelistApp app : apps)
        {
            User user = event.getGuild().getMemberById(app.getDiscordId()).getUser();
            String info = user.getName() + "#" + user.getDiscriminator() + " :: Discord ID: " + app.getDiscordId() + "\n";
            sb.append(info);
        }
        sb.append("```");
        return sb.toString();
    }

    private MessageEmbed convertToEmbed(WhitelistApp app, CommandEvent event)
    {
        User user = event.getGuild().getMemberById(app.getDiscordId()).getUser();
        EmbedBuilder eb2 = new EmbedBuilder();
        eb2.setTitle("Discord Whitelist Application for " + app.getInGameName());
        eb2.setDescription("Discord Name: " + user.getAsTag() + " - Discord ID: " + app.getDiscordId());
        eb2.setThumbnail("https://everneth.com/uploads/monthly_2017_05/par-icon.png.7be3a897907506e63716375bda342551.png");
        eb2.addField("Minecraft IGN", app.getInGameName(), false);
        eb2.addField("Where do  you live?", app.getLocation(), false);
        eb2.addField("What is your age?", String.valueOf(app.getAge()), false);
        eb2.addField("Do you know someone in our community", app.getFriend(), false);
        eb2.addField("Have you been banned elsewhere before?", app.getBannedElsewhere(), false);
        eb2.addField("What are you looking for in a minecraft community?", app.getLookingFor(), false);
        eb2.addField("What do you love and/or hate about Minecraft?", app.getLoveHate(), false);
        eb2.addField("Tell us something about you.", app.getIntro(), false);
        eb2.addField("What is the secret word?", app.getSecretWord(), false);
        eb2.setFooter("UUID: " + app.getMinecraftUuid().toString());
        return eb2.build();
    }

    private class GetAllApps extends Command
    {
        private GetAllApps()
        {
            this.name = "all";
            this.guildOnly = true;
        }
        @Override
        protected void execute(CommandEvent event)
        {
            List<WhitelistApp> apps = WhitelistAppService.getService().getAllCurrentApplicants();
            if(apps.isEmpty())
            {
                event.reply("```asciidoc\n [NO RESULTS]```");
            }
            else
            {
                event.reply(convertToMessage(apps, event));
            }
        }
    }

    private class GetApp extends Command
    {
        private GetApp()
        {
            this.name = "get";
            this.guildOnly = true;
            this.arguments = "<name>";
        }
        @Override
        protected void execute(CommandEvent event)
        {
            WhitelistApp app = WhitelistAppService.getService().getSingleApplicant(event.getArgs());
            if(app == null)
            {
                event.reply("```asciidoc\n [NO RESULTS]```");
            }
            else
            {
                event.reply(convertToEmbed(app, event));
            }
        }

    }
}


