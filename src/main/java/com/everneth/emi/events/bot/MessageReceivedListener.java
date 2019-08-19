package com.everneth.emi.events.bot;

import co.aikar.idb.DB;
import co.aikar.idb.DbRow;
import com.everneth.emi.EMI;
import com.everneth.emi.ReportManager;
import com.everneth.emi.Utils;

import com.everneth.emi.models.PostResponse;
import com.everneth.emi.models.WhitelistApp;
import com.everneth.emi.services.WhitelistAppService;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MessageReceivedListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        if(event.isFromType(ChannelType.TEXT) && event.getChannel().getName().contains("_staff") && !event.getAuthor().isBot())
        {
            ReportManager rm = ReportManager.getReportManager();
            String channelName = event.getChannel().getName();
            String playerName = channelName.substring(0, channelName.indexOf('_'));

            UUID player_uuid = rm.findReportByChannelId(event.getChannel().getIdLong());

            if(player_uuid != null) {
                OfflinePlayer offlinePlayer = EMI.getPlugin().getServer().getOfflinePlayer(player_uuid);


                if (offlinePlayer.isOnline()) {
                    Player player = offlinePlayer.getPlayer();
                    String name;

                    if(event.getMember().getNickname() == null)
                    {
                        name = event.getMember().getEffectiveName();
                    }
                    else
                    {
                        name = event.getMember().getNickname();
                    }
                    player.sendMessage(Utils.color("&o&d[REPORT]&f<&8" + name + "&f>&7 " + event.getMessage().getContentRaw()));
                } else if (!offlinePlayer.isOnline() && !rm.hasDiscord(player_uuid)) {
                    DbRow report = rm.getReportRecord(player_uuid);
                    String author;
                    if(event.getMember().getNickname() == null)
                    {
                        author = event.getMember().getEffectiveName();
                    }
                    else
                    {
                        author = event.getMember().getNickname();
                    }
                    try {
                        DB.executeInsert("INSERT INTO report_messages (report_id, author, message, msg_read, date_read) " +
                                        "VALUES (?, ?, ?, ?, ?)",
                                report.getInt("report_id"),
                                author,
                                event.getMessage().getContentRaw(),
                                0,
                                null);
                    } catch (SQLException e) {
                        System.out.println(e.getMessage());
                    }
                }
            }
        }
        else if(event.isFromType(ChannelType.PRIVATE) && !event.getAuthor().isBot()) {
            if (!event.getAuthor().isBot() && !event.getMessage().getContentRaw().isEmpty()) {
                WhitelistApp appInProgress = WhitelistAppService.getService().findByDiscordId(event.getAuthor().getIdLong());
                if(appInProgress != null) {
                        if(!appInProgress.isHoldForNextStep())
                        {
                        switch (appInProgress.getStep()) {
                            case 1:
                                event.getPrivateChannel().sendMessage("What is your Minecraft IGN?").queue();
                                appInProgress.setHoldForNextStep(true);
                                break;
                            case 2:
                                event.getPrivateChannel().sendMessage("Where do you live?").queue();
                                appInProgress.setHoldForNextStep(true);
                                break;
                            case 3:
                                event.getPrivateChannel().sendMessage("What is your age? (Must be 13 or older to use Discord!)").queue();
                                appInProgress.setHoldForNextStep(true);
                                break;
                            case 4:
                                event.getPrivateChannel().sendMessage("Do you know someone in our community? If yes, please state who.").queue();
                                appInProgress.setHoldForNextStep(true);
                                break;
                            case 5:
                                event.getPrivateChannel().sendMessage("Have you been banned elsewhere before?").queue();
                                appInProgress.setHoldForNextStep(true);
                                break;
                            case 6:
                                event.getPrivateChannel().sendMessage("What are you looking for in a Minecraft community?").queue();
                                appInProgress.setHoldForNextStep(true);
                                break;
                            case 7:
                                event.getPrivateChannel().sendMessage("What do you love and/or hate about Minecraft?").queue();
                                appInProgress.setHoldForNextStep(true);
                                break;
                            case 8:
                                event.getPrivateChannel().sendMessage("Tell us something about you!").queue();
                                appInProgress.setHoldForNextStep(true);
                                break;
                            case 9:
                                event.getPrivateChannel().sendMessage("What is the secret word?").queue();
                                appInProgress.setHoldForNextStep(true);
                                break;
                            case 10:
                                event.getPrivateChannel().sendMessage("```css The following is your whitelist app```\n").queue();
                                appInProgress.setHoldForNextStep(true);
                                EmbedBuilder eb = new EmbedBuilder();
                                eb.setTitle(appInProgress.getInGameName());
                                eb.setDescription("Discord Name: " + event.getAuthor().getAsTag() + " - Discord ID: " + appInProgress.getDiscordId());
                                eb.addField("Minecraft IGN", appInProgress.getInGameName(), false);
                                eb.addField("Where do  you live?", appInProgress.getLocation(), false);
                                eb.addField("What is your age?", String.valueOf(appInProgress.getAge()), false);
                                eb.addField("Do you know someone in our community", appInProgress.getFriend(), false);
                                eb.addField("Have you been banned elsewhere before?", appInProgress.getBannedElsewhere(), false);
                                eb.addField("What are you looking for in a minecraft community?", appInProgress.getLookingFor(), false);
                                eb.addField("What do you love and/or hate about Minecraft?", appInProgress.getLoveHate(), false);
                                eb.addField("Tell us something about you.", appInProgress.getIntro(), false);
                                eb.addField("What is the secret word?", appInProgress.getSecretWord(), false);

                                event.getPrivateChannel().sendMessage(eb.build()).queue();
                                event.getPrivateChannel().sendMessage("Is this information correct? Please reply **yes** or **no**.").queue();
                                break;
                            case 11:
                                appInProgress.setHoldForNextStep(true);
                                EmbedBuilder eb2 = new EmbedBuilder();
                                eb2.setTitle(appInProgress.getInGameName());
                                eb2.setDescription("Discord Name: " + event.getAuthor().getAsTag() + " - Discord ID: " + appInProgress.getDiscordId());
                                eb2.addField("Minecraft IGN", appInProgress.getInGameName(), false);
                                eb2.addField("Where do  you live?", appInProgress.getLocation(), false);
                                eb2.addField("What is your age?", String.valueOf(appInProgress.getAge()), false);
                                eb2.addField("Do you know someone in our community", appInProgress.getFriend(), false);
                                eb2.addField("Have you been banned elsewhere before?", appInProgress.getBannedElsewhere(), false);
                                eb2.addField("What are you looking for in a minecraft community?", appInProgress.getLookingFor(), false);
                                eb2.addField("What do you love and/or hate about Minecraft?", appInProgress.getLoveHate(), false);
                                eb2.addField("Tell us something about you.", appInProgress.getIntro(), false);
                                eb2.addField("What is the secret word?", appInProgress.getSecretWord(), false);
                                event.getGuild().getTextChannelById(EMI.getPlugin().getConfig().getLong("staff-channel-id")).sendMessage(eb2.build()).queue();
                                event.getGuild().getTextChannelById(EMI.getPlugin().getConfig().getLong("staff-channel-id")).sendMessage("Attempting to transmit application to forums").queue();

                                PostResponse postResponse;
                                int statusCode = 0;
                                String url = "";
                                try {
                                    postResponse = transcribeToPost(appInProgress);
                                    statusCode = postResponse.getStatusCode();
                                    url = postResponse.getUrl();
                                } catch (IOException e) {
                                    System.out.println(e.getMessage());
                                }
                                if (statusCode == 200 || statusCode == 201) {
                                    String msg = appInProgress.getInGameName() + "'s whitelist application successfully transmitted to the site.\n\n" +
                                            url;
                                    event.getGuild().getTextChannelById(EMI.getPlugin().getConfig().getLong("staff-channel-id")).sendMessage(msg).queue();
                                } else {

                                    String msg = appInProgress.getInGameName() + "'s whitelist application could not be transmitted to the site. An embed of the application has been posted.";
                                    event.getGuild().getTextChannelById(EMI.getPlugin().getConfig().getLong("staff-channel-id")).sendMessage(msg).queue();
                                }
                                WhitelistAppService.getService().removeApp(appInProgress.getDiscordId());
                                break;
                        }
                        }
                        else {
                            if (appInProgress.getStep() == 10 && event.getMessage().getContentRaw().toLowerCase().equals("yes")) {
                                WhitelistAppService.getService().addData(appInProgress.getDiscordId(), appInProgress.getStep(), event.getMessage().getContentRaw());
                            } else if (appInProgress.getStep() == 10 && event.getMessage().getContentRaw().toLowerCase().equals("no")) {
                                WhitelistAppService.getService().findByDiscordId(event.getAuthor().getIdLong()).setStep(1);
                            } else if (appInProgress.getStep() == 10 && (!event.getMessage().getContentRaw().toLowerCase().equals("no") || !event.getMessage().getContentRaw().toLowerCase().equals("yes"))) {
                                event.getPrivateChannel().sendMessage("**INVALID INPUT** Please review once more and answer with yes or no!").queue();
                            } else {
                                WhitelistAppService.getService().addData(appInProgress.getDiscordId(), appInProgress.getStep(), event.getMessage().getContentRaw());
                            }
                        }
                    }
                }
            }
        }

    private PostResponse transcribeToPost(WhitelistApp app) throws IOException {
        final String URL =
                EMI.getPlugin().getConfig().getString("api-topic-post-url") + "api" +
                        EMI.getPlugin().getConfig().getString("api-topic-post-endpoint");
        final String KEY = EMI.getPlugin().getConfig().getString("api-key");
        final int POSTER = EMI.getPlugin().getConfig().getInt("system-user-id");
        final int FORUM_ID = EMI.getPlugin().getConfig().getInt("whitelist-app-forum-id");

        StringBuilder sb = new StringBuilder();

        String postHeader = "<font size=\"16px\"><b>Whitelist application submitted by " + app.getInGameName() + "</b></font><br />";
        sb.append(postHeader);

        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(URL);
        List<NameValuePair> params = new ArrayList<NameValuePair>();

        String title = "Report submitted by " + app.getInGameName();

        params.add(new BasicNameValuePair("key", KEY));
        params.add(new BasicNameValuePair("forum", String.valueOf(FORUM_ID)));
        params.add(new BasicNameValuePair("title", title));
        params.add(new BasicNameValuePair("post", sb.toString()));
        params.add(new BasicNameValuePair("author", String.valueOf(POSTER)));

        httpPost.setEntity(new UrlEncodedFormEntity(params, Consts.UTF_8));

        try {
            CloseableHttpResponse response2 = httpclient.execute(httpPost);
            JSONObject obj = new JSONObject(EntityUtils.toString(response2.getEntity()));

            return new PostResponse(response2.getStatusLine().getStatusCode(), obj.getString("url"));
        }
        catch (Exception e)
        {
            EMI.getPlugin().getLogger().info("Something broke: " + e.getMessage());
            return new PostResponse(0, "");
        }
        finally
        {
            httpclient.close();
        }
    }
}
