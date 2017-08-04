package com.github.yeriomin.smsscheduler;

import android.content.Intent;

public class SmsDeliveredService extends SmsIntentService {

    public SmsDeliveredService() {
        super("SmsDeliveredService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        super.onHandleIntent(intent);
        if (timestampCreated == 0) {
            return;
        }
        SmsModel sms = DbHelper.getDbHelper(this).get(timestampCreated);
        sms.setStatus(SmsModel.STATUS_DELIVERED);
        DbHelper.getDbHelper(this).save(sms);
        WakefulBroadcastReceiver.completeWakefulIntent(intent);
    }
}
