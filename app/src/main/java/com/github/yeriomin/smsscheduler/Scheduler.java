package com.github.yeriomin.smsscheduler;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import com.github.yeriomin.smsscheduler.activity.SmsSchedulerPreferenceActivity;

import java.text.DateFormat;

public class Scheduler {

    static private final long HOUR = 1000L*60L*60L;

    private Context context;
    private AlarmManager alarmManager;

    public Scheduler(Context context) {
        this.context = context;
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    public void schedule(SmsModel sms) {
        if (null == alarmManager) {
            return;
        }
        Log.i(getClass().getName(), "Scheduling sms to " + DateFormat.getDateTimeInstance().format(sms.getCalendar().getTime()));
        setAlarm(sms.getTimestampScheduled(), getAlarmPendingIntent(sms.getTimestampCreated(), SmsSenderReceiver.class));
        if (PreferenceManager
            .getDefaultSharedPreferences(context)
            .getBoolean(SmsSchedulerPreferenceActivity.PREFERENCE_REMINDERS, false)
        ) {
            setAlarm(sms.getTimestampScheduled() - HOUR, getAlarmPendingIntent(sms.getTimestampCreated(), ReminderReceiver.class));
        }
    }

    public void unschedule(long timestampCreated) {
        if (null == alarmManager) {
            return;
        }
        alarmManager.cancel(getAlarmPendingIntent(timestampCreated, SmsSenderReceiver.class));
        alarmManager.cancel(getAlarmPendingIntent(timestampCreated, ReminderReceiver.class));
    }

    private void setAlarm(long timestamp, PendingIntent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timestamp, intent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, timestamp, intent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, timestamp, intent);
        }
    }

    private PendingIntent getAlarmPendingIntent(long timestampCreated, Class receiverClass) {
        Intent intent = new Intent(context, receiverClass);
        intent.putExtra(DbHelper.COLUMN_TIMESTAMP_CREATED, timestampCreated);
        return PendingIntent.getBroadcast(
            context,
            (int) (timestampCreated / 1000L),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT & Intent.FILL_IN_DATA
        );
    }
}
