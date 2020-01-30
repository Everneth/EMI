package com.everneth.emi.commands.bot;

import com.everneth.emi.EMI;
import com.everneth.emi.managers.ReportManager;
import com.everneth.emi.models.PostResponse;
import com.everneth.emi.utils.FileUtils;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class CloseReportCommand extends Command {
    public CloseReportCommand() {
        this.name = "close-report";
    }

    @Override
    protected void execute(CommandEvent event) {
        // We need to make sure the command sender has the correct authorized roles.
        // Assume no one has roles
        boolean hasRequiredRoles = false;

        // Get the roles from the member
        List<Role> roleList = event.getMember().getRoles();
        String playerName = event.getTextChannel().getName().substring(0, event.getTextChannel().getName().indexOf('_'));
        ReportManager rm = ReportManager.getReportManager();

        UUID uuid = rm.findReportByChannelId(event.getChannel().getIdLong());
        // Lets check them
        for (Role role : roleList) {
            if (role.getName().equals("Staff")) {
                // Found a required role, no need to continue, break from the loop
                hasRequiredRoles = true;
                break;
            }
        }
        // We've looped through. Do we have the role?
        if (hasRequiredRoles) {
            // Got the role! Lets build a list of messages to clear.
            List<Message> messageList = event.getTextChannel().getIterableHistory().complete();
            // Take our messages and build a string, we'll dump that string into a message file
            // and embed the file into a message
            PostResponse postResponse;
            int statusCode = 0;
            String url = "";
            try {
                 postResponse = transcribeToPost(messageList, playerName, uuid);
                 statusCode = postResponse.getStatusCode();
                 url = postResponse.getUrl();
            }
            catch(IOException e)
            {
                System.out.println(e.getMessage());
            }
            if(statusCode == 200 || statusCode == 201) {
                String msg = "Log from " + playerName + "'s report successfully transmitted to the site.\n\n" +
                        url;
                event.getGuild().getTextChannelById(EMI.getPlugin().getConfig().getLong("staff-channel-id")).sendMessage(msg).queue();
            }
            else {
                File embedFile = transcribeToFile(messageList);
                String msg = "Log from " + playerName + "'s report could not be transmitted to the site. A txt file transcript has been attached.";
                Message message = new MessageBuilder().append(msg).build();
                event.getGuild().getTextChannelById(178247194862682112L).sendMessage(message).addFile(embedFile).queue();
            }

            event.getTextChannel().delete().queue();
        } else {
            // You can;t even use this at all, we're not checking any further
            // TODO: Mute member if attempts are made to use command to spam replies
            event.reply("Sorry dear, you must be a member of staff to use this command. :heart: ");
        }
    }

    private PostResponse transcribeToPost(List<Message> messageList, String playerName, UUID uuid) throws IOException {
        final String URL =
                EMI.getPlugin().getConfig().getString("api-topic-post-url") + "api" +
                        EMI.getPlugin().getConfig().getString("api-topic-post-endpoint");
        final String KEY = EMI.getPlugin().getConfig().getString("api-key");
        final int POSTER = EMI.getPlugin().getConfig().getInt("system-user-id");
        final int FORUM_ID = EMI.getPlugin().getConfig().getInt("report-log-forum-id");

        StringBuilder sb = new StringBuilder();

        List<Message> reverse = reverseList(messageList);

        ReportManager rm = ReportManager.getReportManager();

        String logMsg;

        String postHeader = "<font size=\"16px\"><b>Report submitted by " + playerName + "</b></font><br />";
        sb.append(postHeader);
        for (Message msg : reverse) {
            if (msg.getAuthor().equals(EMI.getJda().getSelfUser())) {
                 logMsg = msg.getContentRaw() + "</br>";
            }
            else {
                logMsg = "<b>" + msg.getMember().getEffectiveName() + "</b>: " + msg.getContentRaw() + "<br />";
            }
            sb.append(logMsg);
        }

        rm.closeReport(uuid);
        rm.removeReport(uuid);

        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(URL);
        List<NameValuePair> params = new ArrayList<NameValuePair>();

        String title = "Report submitted by " + playerName;

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

    private File transcribeToFile(List<Message> messageList) {
        StringBuilder sb = new StringBuilder();

        List<Message> reverse = reverseList(messageList);

        for (Message msg : reverse) {
            String logMsg = msg.getMember().getEffectiveName() + ": " + msg.getContentRaw() + "\n";
            sb.append(logMsg);
        }
        try {
            File fileToEmbed = FileUtils.writeFileFromString("transcript.txt", sb.toString());
            return fileToEmbed;
        } catch (IOException e) {
            EMI.getPlugin().getLogger().warning(e.getMessage());
        }
        return null;
    }

    private static <T> List<T> reverseList(List<T> list) {
        List<T> reverse = new ArrayList<>(list);
        Collections.reverse(reverse);
        return reverse;
    }
}
