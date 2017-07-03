package com.github.yeriomin.smsscheduler;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class Scheduler {

    private Context context;

    public Scheduler(Context context) {
        this.context = context;
    }

    public void schedule(SmsModel sms) {
//        AlarmManager alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//        alarmMgr.set(AlarmManager.RTC_WAKEUP, sms.getTimestampScheduled(), getAlarmPendingIntent(sms));
    }

    public void unschedule(SmsModel sms) {
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmMgr.cancel(getAlarmPendingIntent(sms));
    }

    private PendingIntent getAlarmPendingIntent(SmsModel sms) {
        Intent intent = new Intent(AlarmReceiver.INTENT_FILTER);
        intent.putExtra(DbHelper.COLUMN_TIMESTAMP_CREATED, sms.getTimestampCreated());
        return PendingIntent.getBroadcast(
            context,
            sms.getId(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT & Intent.FILL_IN_DATA
        );
    }

}
