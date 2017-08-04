package com.github.yeriomin.smsscheduler;

public class ReminderReceiver extends WakefulBroadcastReceiver {

    @Override
    protected Class getServiceClass() {
        return ReminderService.class;
    }
}
