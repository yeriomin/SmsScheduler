package com.github.yeriomin.smsscheduler;

import android.app.Activity;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.util.Log;

import com.github.yeriomin.smsscheduler.activity.SmsListActivity;
import com.github.yeriomin.smsscheduler.notification.NotificationManagerWrapper;

public class SmsSentService extends SmsIntentService {

    public SmsSentService() {
        super("SmsSentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        super.onHandleIntent(intent);
        if (timestampCreated == 0) {
            return;
        }
        Log.i(getClass().getName(), "Notifying that sms " + timestampCreated + " is sent");
        SmsModel sms = DbHelper.getDbHelper(this).get(timestampCreated);
        String errorId = "";
        String errorString = "";
        String title = getString(R.string.notification_title_failure);
        String message = "";
        sms.setStatus(SmsModel.STATUS_FAILED);

        switch (intent.getIntExtra(SmsSentReceiver.RESULT_CODE, 0)) {
            case Activity.RESULT_OK:
                title = getString(R.string.notification_title_success);
                message = getString(R.string.notification_message_success, sms.getRecipientName());
                sms.setStatus(SmsModel.STATUS_SENT);
                break;
            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                errorId = SmsModel.ERROR_GENERIC;
                errorString = getString(R.string.error_generic);
                break;
            case SmsManager.RESULT_ERROR_NO_SERVICE:
                errorId = SmsModel.ERROR_NO_SERVICE;
                errorString = getString(R.string.error_no_service);
                break;
            case SmsManager.RESULT_ERROR_NULL_PDU:
                errorId = SmsModel.ERROR_NULL_PDU;
                errorString = getString(R.string.error_null_pdu);
                break;
            case SmsManager.RESULT_ERROR_RADIO_OFF:
                errorId = SmsModel.ERROR_RADIO_OFF;
                errorString = getString(R.string.error_radio_off);
                break;
            default:
                errorId = SmsModel.ERROR_UNKNOWN;
                errorString = getString(R.string.error_unknown);
                break;
        }
        if (errorId.length() > 0) {
            sms.setResult(errorId);
            message = getString(R.string.notification_message_failure, sms.getRecipientName(), errorString);
        }
        DbHelper.getDbHelper(this).save(sms);
        notify(this, title, message, sms.getId());
        WakefulBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void notify(Context context, String title, String message, int id) {
        Notification notification = NotificationManagerWrapper.getBuilder(context)
            .setIntent(new Intent(context, SmsListActivity.class))
            .setMessage(message)
            .setTitle(title)
            .build()
        ;
        new NotificationManagerWrapper(context).show(id, notification);
    }
}
