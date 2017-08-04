package com.github.yeriomin.smsscheduler;

import android.app.PendingIntent;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
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
        Log.i(getClass().getName(), "Sending sms " + timestampCreated);
        sendSms(DbHelper.getDbHelper(this).get(timestampCreated));
        WakefulBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void sendSms(SmsModel sms) {
        Long smsId = sms.getTimestampCreated();

        ArrayList<PendingIntent> sentPendingIntents = new ArrayList<PendingIntent>();
        ArrayList<PendingIntent> deliveredPendingIntents = null;

        Intent sentIntent = new Intent(this, SmsSentReceiver.class);
        sentIntent.setAction(smsId.toString());
        sentIntent.putExtra(DbHelper.COLUMN_TIMESTAMP_CREATED, smsId);
        PendingIntent sentPendingIntent = PendingIntent.getBroadcast(this, 0, sentIntent, 0);

        PendingIntent deliveredPendingIntent = null;
        boolean deliveryReports = PreferenceManager
            .getDefaultSharedPreferences(getApplicationContext())
            .getBoolean(SmsSchedulerPreferenceActivity.PREFERENCE_DELIVERY_REPORTS, false)
        ;
        if (deliveryReports) {
            deliveredPendingIntents = new ArrayList<>();
            Intent deliveredIntent = new Intent(this, SmsDeliveredReceiver.class);
            deliveredIntent.setAction(smsId.toString());
            deliveredIntent.putExtra(DbHelper.COLUMN_TIMESTAMP_CREATED, smsId);
            deliveredPendingIntent = PendingIntent.getBroadcast(this, 0, deliveredIntent, 0);
        }

        SmsManager smsManager = SmsManager.getDefault();
        ArrayList<String> mSMSMessage = smsManager.divideMessage(sms.getMessage());
        for (int i = 0; i < mSMSMessage.size(); i++) {
            sentPendingIntents.add(i, sentPendingIntent);
            if (deliveryReports) {
                deliveredPendingIntents.add(i, deliveredPendingIntent);
            }
        }
        smsManager.sendMultipartTextMessage(sms.getRecipientNumber(), null, mSMSMessage, sentPendingIntents, deliveredPendingIntents);
    }
}
