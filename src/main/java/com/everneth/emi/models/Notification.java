package com.everneth.emi.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Notification {

    private String type;
    private String message;
    private Date dateTime;
    private EMIPlayer recipient;
    private EMIPlayer notifier;

    public Notification(EMIPlayer recipient, EMIPlayer notifier, String type, String message, Date dateTime)
    {
        this.recipient = recipient;
        this.notifier = notifier;
        this.type = type;
        this.message = message;
        this.dateTime = dateTime;
    }

    public List<Notification> getAllNotifications()
    {
        List<Notification> notifications = new ArrayList<>();
        return notifications;
    }

    public List<Notification> getAllNotificationsByID(int id) {
        List<Notification> notifications = new ArrayList<>();
        return notifications;
    }
}

