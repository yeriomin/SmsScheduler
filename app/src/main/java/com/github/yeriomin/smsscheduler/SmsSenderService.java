package com.github.yeriomin.smsscheduler;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;

import com.github.yeriomin.smsscheduler.Activity.SmsSchedulerPreferenceActivity;

import java.util.ArrayList;

public class SmsSenderService extends IntentService {

    private final static String SERVICE_NAME = "SmsSenderService";

    public SmsSenderService() {
        super(SERVICE_NAME);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        long smsId = intent.getExtras().getLong(DbHelper.COLUMN_TIMESTAMP_CREATED, 0);
        if (smsId == 0) {
            throw new RuntimeException("No SMS id provided with intent");
        }
        SmsModel sms = DbHelper.getDbHelper(this).get(smsId);
        sendSms(sms);
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
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean deliveryReports = prefs.getBoolean(SmsSchedulerPreferenceActivity.PREFERENCE_DELIVERY_REPORTS, false);
        if (deliveryReports) {
            deliveredPendingIntents = new ArrayList<PendingIntent>();
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
