package com.github.yeriomin.smsscheduler;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import android.util.Log;

import com.github.yeriomin.smsscheduler.activity.SmsSchedulerPreferenceActivity;

import java.util.ArrayList;

public class SmsSenderService extends SmsIntentService {

    public SmsSenderService() {
        super("SmsSenderService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        super.onHandleIntent(intent);
        if (timestampCreated == 0) {
            return;
        }
        SmsModel sms = DbHelper.getDbHelper(this).get(timestampCreated);
        Log.i(getClass().getName(), "Sending sms " + timestampCreated);
        sendSms(sms);
        String recurringMode = sms.getRecurringMode();
        if (!TextUtils.isEmpty(recurringMode) && !recurringMode.equals(CalendarResolver.RECURRING_NO)) {
            Log.i(getClass().getName(), "Scheduling next sms");
            scheduleNextSms(sms);
        }
        WakefulBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void sendSms(SmsModel sms) {
        ArrayList<PendingIntent> sentPendingIntents = new ArrayList<>();
        ArrayList<PendingIntent> deliveredPendingIntents = new ArrayList<>();
        PendingIntent sentPendingIntent = getPendingIntent(sms.getTimestampCreated(), SmsSentReceiver.class);
        PendingIntent deliveredPendingIntent = getPendingIntent(sms.getTimestampCreated(), SmsDeliveredReceiver.class);

        SmsManager smsManager = getSmsManager(sms.getSubscriptionId());
        ArrayList<String> smsMessage = smsManager.divideMessage(sms.getMessage());
        boolean deliveryReports = PreferenceManager
            .getDefaultSharedPreferences(getApplicationContext())
            .getBoolean(SmsSchedulerPreferenceActivity.PREFERENCE_DELIVERY_REPORTS, false)
        ;
        for (int i = 0; i < smsMessage.size(); i++) {
            sentPendingIntents.add(i, sentPendingIntent);
            if (deliveryReports) {
                deliveredPendingIntents.add(i, deliveredPendingIntent);
            }
        }
        smsManager.sendMultipartTextMessage(
            sms.getRecipientNumber(),
            null,
            smsMessage,
            sentPendingIntents,
            deliveryReports ? deliveredPendingIntents : null
        );
    }

    private PendingIntent getPendingIntent(long smsId, Class receiverClass) {
        Intent intent = new Intent(this, receiverClass);
        intent.setAction(Long.toString(smsId));
        intent.putExtra(DbHelper.COLUMN_TIMESTAMP_CREATED, smsId);
        return PendingIntent.getBroadcast(this, 0, intent, 0);
    }

    private SmsManager getSmsManager(int subscriptionId) {
        SmsManager smsManager = SmsManager.getDefault();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1) {
            return smsManager;
        }
        SubscriptionManager subscriptionManager = (SubscriptionManager) getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
        if (null == subscriptionManager) {
            return smsManager;
        }
        if (null == subscriptionManager.getActiveSubscriptionInfo(subscriptionId)) {
            return smsManager;
        }
        return SmsManager.getSmsManagerForSubscriptionId(subscriptionId);
    }

    private void scheduleNextSms(SmsModel sms) {
        long oldTimestamp = sms.getTimestampScheduled();
        new CalendarResolver().setCalendar(sms.getCalendar()).setRecurringMode(sms.getRecurringMode()).advance();
        if (oldTimestamp == sms.getTimestampScheduled()) {
            Log.w(getClass().getName(), "No valid next date found");
        } else {
            DbHelper.getDbHelper(this).save(sms);
            new Scheduler(getApplicationContext()).schedule(sms);
        }
    }
}
