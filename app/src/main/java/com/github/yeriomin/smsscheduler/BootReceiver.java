package com.github.yeriomin.smsscheduler;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;
import java.util.Iterator;

public class BootReceiver extends BroadcastReceiver {

    private Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            this.context = context;
            ArrayList<SmsModel> pendingSms = getPendingSms();
            Iterator<SmsModel> i = pendingSms.iterator();
            SmsModel sms;
            while (i.hasNext()) {
                sms = i.next();
                scheduleAlarm(sms);
            }
        }
    }

    private ArrayList<SmsModel> getPendingSms() {
        return DbHelper.getDbHelper(context).get(SmsModel.STATUS_PENDING);
    }

    private void scheduleAlarm(SmsModel sms) {
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(AlarmReceiver.INTENT_FILTER);
        intent.putExtra(DbHelper.COLUMN_TIMESTAMP_CREATED, sms.getTimestampCreated());
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, sms.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT & Intent.FILL_IN_DATA);
        alarmMgr.set(AlarmManager.RTC_WAKEUP, sms.getTimestampScheduled(), alarmIntent);
    }
}
