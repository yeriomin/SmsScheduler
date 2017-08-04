package com.github.yeriomin.smsscheduler.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

public class NotificationManagerWrapper {

    private NotificationManager manager;

    static public NotificationBuilder getBuilder(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return new NotificationBuilderO(context);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            return new NotificationBuilderJellybean(context);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return new NotificationBuilderHoneycomb(context);
        } else {
            return new NotificationBuilderLegacy(context);
        }
    }

    public NotificationManagerWrapper(Context context) {
        manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void show(int id, Notification notification) {
        manager.notify(id, notification);
    }

    public void cancel(int id) {
        manager.cancel(id);
    }
}
