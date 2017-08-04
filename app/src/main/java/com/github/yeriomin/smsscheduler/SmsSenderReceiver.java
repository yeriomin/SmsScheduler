package com.github.yeriomin.smsscheduler;

public class SmsSenderReceiver extends WakefulBroadcastReceiver {

    @Override
    protected Class getServiceClass() {
        return SmsSenderService.class;
    }
}
