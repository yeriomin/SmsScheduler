package com.github.yeriomin.smsscheduler;

import android.app.Activity;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;

import com.github.yeriomin.smsscheduler.Activity.SmsListActivity;

public class SmsSentService extends IntentService {

    private final static String SERVICE_NAME = "SmsSentService";

    public SmsSentService() {
        super(SERVICE_NAME);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Context context = getApplicationContext();
        long smsId = intent.getExtras().getLong(DbHelper.COLUMN_TIMESTAMP_CREATED, 0);
        if (smsId == 0) {
            throw new RuntimeException("No SMS id provided with intent");
        }
        SmsModel sms = DbHelper.getDbHelper(context).get(smsId);
        String errorId = "";
        String errorString = "";
        String title = context.getString(R.string.notification_title_failure);
        String message = "";
        sms.setStatus(SmsModel.STATUS_FAILED);

        switch (intent.getExtras().getInt(SmsSentReceiver.RESULT_CODE, 0)) {
            case Activity.RESULT_OK:
                title = context.getString(R.string.notification_title_success);
                message = context.getString(R.string.notification_message_success, sms.getRecipientName());
                sms.setStatus(SmsModel.STATUS_SENT);
                break;
            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                errorId = SmsModel.ERROR_GENERIC;
                errorString = context.getString(R.string.error_generic);
                break;
            case SmsManager.RESULT_ERROR_NO_SERVICE:
                errorId = SmsModel.ERROR_NO_SERVICE;
                errorString = context.getString(R.string.error_no_service);
                break;
            case SmsManager.RESULT_ERROR_NULL_PDU:
                errorId = SmsModel.ERROR_NULL_PDU;
                errorString = context.getString(R.string.error_null_pdu);
                break;
            case SmsManager.RESULT_ERROR_RADIO_OFF:
                errorId = SmsModel.ERROR_RADIO_OFF;
                errorString = context.getString(R.string.error_radio_off);
                break;
            default:
                errorId = SmsModel.ERROR_UNKNOWN;
                errorString = context.getString(R.string.error_unknown);
                break;
        }
        if (errorId.length() > 0) {
            sms.setResult(errorId);
            message = context.getString(R.string.notification_message_failure, sms.getRecipientName(), errorString);
        }
        DbHelper.getDbHelper(context).save(sms);
        notify(context, intent, title, message, sms.getId());
    }

    private void notify(Context context, Intent intent, String title, String message, int id) {
        Intent myIntent = new Intent(context, SmsListActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, myIntent, 0);
        Notification notification = NotificationUtil.createNotification(context, pendingIntent, title, message, R.drawable.ic_notification);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(id, notification);
    }
}
