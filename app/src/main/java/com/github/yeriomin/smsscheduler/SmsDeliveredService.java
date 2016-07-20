package com.github.yeriomin.smsscheduler;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

public class SmsDeliveredService extends IntentService {

    private final static String SERVICE_NAME = "SmsDeliveredService";

    public SmsDeliveredService() {
        super(SERVICE_NAME);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        long smsId = intent.getExtras().getLong(DbHelper.COLUMN_TIMESTAMP_CREATED, 0);
        if (smsId == 0) {
            throw new RuntimeException("No SMS id provided with intent");
        }
        Context context = getApplicationContext();
        SmsModel sms = DbHelper.getDbHelper(context).get(smsId);
        sms.setStatus(SmsModel.STATUS_DELIVERED);
        DbHelper.getDbHelper(context).save(sms);
    }
}
