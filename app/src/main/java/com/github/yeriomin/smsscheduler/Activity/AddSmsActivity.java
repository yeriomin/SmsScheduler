package com.github.yeriomin.smsscheduler.Activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import com.github.yeriomin.smsscheduler.DbHelper;
import com.github.yeriomin.smsscheduler.R;
import com.github.yeriomin.smsscheduler.Scheduler;
import com.github.yeriomin.smsscheduler.SmsModel;
import com.github.yeriomin.smsscheduler.view.BuilderCancel;
import com.github.yeriomin.smsscheduler.view.BuilderContact;
import com.github.yeriomin.smsscheduler.view.BuilderDate;
import com.github.yeriomin.smsscheduler.view.BuilderMessage;
import com.github.yeriomin.smsscheduler.view.BuilderTime;
import com.github.yeriomin.smsscheduler.view.EmptinessTextWatcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;

public class AddSmsActivity extends Activity {

    final public static int RESULT_SCHEDULED = 1;
    final public static int RESULT_UNSCHEDULED = 2;
    final public static String INTENT_SMS_ID = "INTENT_SMS_ID";

    final private static String SMS_STATE = "SMS_STATE";
    final private static int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;
    final private String[] permissionsRequired = new String[] {
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_CONTACTS
    };

    private SmsModel sms;
    private ArrayList<String> permissionsGranted = new ArrayList<>();

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                startActivityForResult(new Intent(this, SmsSchedulerPreferenceActivity.class), 1);
                break;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (permissionsGranted()) {
            buildForm();
        }
    }

    private void buildForm() {
        EditText formMessage = (EditText) findViewById(R.id.form_input_message);
        formMessage = (EditText) new BuilderMessage().setView(formMessage).setSms(sms).build();
        AutoCompleteTextView formContact = (AutoCompleteTextView) findViewById(R.id.form_input_contact);
        formContact = (AutoCompleteTextView) new BuilderContact().setView(formContact).setSms(sms).setActivity(this).build();
        TextWatcher watcherEmptiness = new EmptinessTextWatcher(this, formContact, formMessage);
        formContact.addTextChangedListener(watcherEmptiness);
        formMessage.addTextChangedListener(watcherEmptiness);

        new BuilderTime().setActivity(this).setView(findViewById(R.id.form_time)).setSms(sms).build();
        new BuilderDate().setActivity(this).setView(findViewById(R.id.form_date)).setSms(sms).build();

        new BuilderCancel().setView(findViewById(R.id.button_cancel)).setSms(sms).build();
    }

    @Override
    protected void onPause() {
        super.onPause();
        DbHelper.closeDbHelper();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DbHelper.closeDbHelper();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_sms);

        // Filling existing sms info if possible
        long smsId = getIntent().getLongExtra(INTENT_SMS_ID, 0L);
        if (smsId > 0) {
            Log.i(getClass().getName(), "Sms id passed with intent: " + smsId);
            sms = DbHelper.getDbHelper(this).get(smsId);
        } else if (null != savedInstanceState) {
            Log.i(getClass().getName(), "Getting sms object from saved instance state");
            sms = savedInstanceState.getParcelable(SMS_STATE);
        }
        if (null == sms) {
            Log.i(getClass().getName(), "New sms form");
            sms = new SmsModel();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (null != sms) {
            outState.putParcelable(SMS_STATE, sms);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (null == sms) {
            sms = savedInstanceState.getParcelable(SMS_STATE);
        }
        if (null == sms) {
            sms = new SmsModel();
        }
    }

    public void scheduleSms(View view) {
        if (sms.getTimestampScheduled() < GregorianCalendar.getInstance().getTimeInMillis()) {
            Toast.makeText(getApplicationContext(), getString(R.string.form_validation_datetime), Toast.LENGTH_SHORT).show();
            return;
        }
        DbHelper.getDbHelper(this).save(sms);
        new Scheduler(getApplicationContext()).schedule(sms);
        setResult(RESULT_SCHEDULED, new Intent());
        finish();
    }

    public void unscheduleSms(View view) {
        DbHelper.getDbHelper(this).delete(sms.getTimestampCreated());
        new Scheduler(getApplicationContext()).unschedule(sms);
        setResult(RESULT_UNSCHEDULED, new Intent());
        finish();
    }

    private boolean permissionsGranted() {
        boolean granted = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ArrayList<String> permissionsNotGranted = new ArrayList<>();
            for (String required : this.permissionsRequired) {
                if (checkSelfPermission(required) != PackageManager.PERMISSION_GRANTED) {
                    permissionsNotGranted.add(required);
                } else {
                    this.permissionsGranted.add(required);
                }
            }
            if (permissionsNotGranted.size() > 0) {
                granted = false;
                String[] notGrantedArray = permissionsNotGranted.toArray(new String[permissionsNotGranted.size()]);
                requestPermissions(notGrantedArray, REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
            }
        }
        return granted;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS:
                List<String> requiredPermissions = Arrays.asList(this.permissionsRequired);
                String permission;
                for (int i = 0; i < permissions.length; i++) {
                    permission = permissions[i];
                    if (requiredPermissions.contains(permission) && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        this.permissionsGranted.add(permission);
                    }
                }
                if (this.permissionsGranted.size() == this.permissionsRequired.length) {
                    buildForm();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
