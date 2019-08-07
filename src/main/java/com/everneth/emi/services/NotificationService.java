package com.everneth.emi.services;

public class NotificationService {

    private static NotificationService ns;

    private NotificationService() {}

    public static NotificationService getNotificationService()
    {
        if(ns == null)
        {
            ns = new NotificationService();
        }
        return ns;
    }

    public static void startService()
    {

    }

    public static void stopService()
    {

    }

}
