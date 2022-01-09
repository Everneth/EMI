package com.everneth.emi.commands.bot.par;

import com.everneth.emi.EMI;
import com.everneth.emi.models.WhitelistApp;
import com.everneth.emi.services.WhitelistAppService;
import com.jagrosh.jdautilities.command.SlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.ArrayList;
import java.util.List;

public class WhitelistAppCommand extends SlashCommand {
    public WhitelistAppCommand()
    {
        this.name = "application";
        this.help = "All commands pertaining to user applications";

        this.children = new SlashCommand[]{new GetAllApps(), new GetApp()};
    }

    @Override
    public void execute(SlashCommandEvent event) {}


    private String convertToMessage(List<WhitelistApp> apps, SlashCommandEvent event)
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

    private MessageEmbed convertToEmbed(WhitelistApp app, SlashCommandEvent event)
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

    private class GetAllApps extends SlashCommand
    {
        private GetAllApps()
        {
            this.name = "all";
            this.help = "Gets the applications for all current applicants.";

            this.defaultEnabled = false;
            this.enabledRoles = new String[]{EMI.getPlugin().getConfig().getString("staff-role-id")};
        }
        @Override
        protected void execute(SlashCommandEvent event)
        {
            List<WhitelistApp> apps = WhitelistAppService.getService().getAllCurrentApplicants();
            if(apps.isEmpty())
            {
                event.reply("```asciidoc\n [NO RESULTS]```").queue();
            }
            else
            {
                event.reply(convertToMessage(apps, event)).queue();
            }
        }
    }

    private class GetApp extends SlashCommand
    {
        private GetApp()
        {
            this.name = "get";
            this.help = "Get an application for an applicant";

            this.options = new ArrayList<>();
            this.options.add(new OptionData(OptionType.STRING, "id", "The user's unique identifier").setRequired(true));

            this.defaultEnabled = false;
            this.enabledRoles = new String[]{EMI.getPlugin().getConfig().getString("staff-role-id")};
        }
        @Override
        protected void execute(SlashCommandEvent event)
        {
            long id = Long.parseLong(event.getOption("id").getAsString());
            WhitelistApp app;
            if(id == 0L)
                app = WhitelistAppService.getService().getSingleApplicant(event.getMember().getIdLong());
            else
                app = WhitelistAppService.getService().getSingleApplicant(id);

            if(app == null) {
                event.reply("```asciidoc\n [NO RESULTS]```").queue();
            }
            else {
                event.replyEmbeds(convertToEmbed(app, event)).queue();
            }
        }

    }

    private class ReloadApps extends SlashCommand
    {
        private ReloadApps() {
            this.name = "reload";
            this.help = "Reload all active applications into memory from the table.";

            this.defaultEnabled = false;
            this.enabledRoles = new String[]{EMI.getPlugin().getConfig().getString("staff-role-id")};
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            WhitelistAppService.getService().load();

            event.reply("All active whitelist applications have been reloaded.").setEphemeral(true).queue();
        }
    }
}


