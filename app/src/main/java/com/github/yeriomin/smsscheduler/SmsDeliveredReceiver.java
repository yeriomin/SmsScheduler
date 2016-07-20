package com.github.yeriomin.smsscheduler;

import android.content.Context;
import android.content.Intent;

public class SmsDeliveredReceiver extends WakefulBroadcastReceiver {

    static public final String RESULT_CODE = "resultCode";

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, SmsDeliveredService.class);
        service.putExtras(intent.getExtras());
        service.putExtra(RESULT_CODE, getResultCode());
        startWakefulService(context, service);
    }
}
