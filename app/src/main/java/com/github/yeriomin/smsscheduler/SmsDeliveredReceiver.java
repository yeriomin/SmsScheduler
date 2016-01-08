package com.github.yeriomin.smsscheduler;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SmsDeliveredReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        long smsId = intent.getExtras().getLong(DbHelper.COLUMN_TIMESTAMP_CREATED, 0);
        if (smsId == 0) {
            throw new RuntimeException("No SMS id provided with intent");
        }
        if (getResultCode() == Activity.RESULT_OK) {
            SmsModel sms = DbHelper.getDbHelper(context).get(smsId);
            sms.setStatus(SmsModel.STATUS_DELIVERED);
            DbHelper.getDbHelper(context).save(sms);
        }
    }
}
