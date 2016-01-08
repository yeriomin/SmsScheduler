package com.github.yeriomin.smsscheduler.Activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SimpleAdapter;
import android.widget.TimePicker;
import android.widget.Toast;

import com.github.yeriomin.smsscheduler.AlarmReceiver;
import com.github.yeriomin.smsscheduler.R;
import com.github.yeriomin.smsscheduler.DbHelper;
import com.github.yeriomin.smsscheduler.SmsModel;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

public class AddSmsActivity extends Activity {

    final private int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;
    final private String[] permissionsRequired = new String[] {
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_CONTACTS
    };

    private GregorianCalendar timeScheduled = new GregorianCalendar();
    private SmsModel sms;
    private ArrayList<String> permissionsGranted = new ArrayList<String>();

    @Override
    protected void onResume() {
        super.onResume();
        if (permissionsGranted()) {
            buildForm();
        }
    }

    private void buildForm() {
        timeScheduled = new GregorianCalendar();
        String recipient = "", message = "";

        final AutoCompleteTextView formContact = (AutoCompleteTextView) findViewById(R.id.form_input_contact);
        final EditText formMessage = (EditText) findViewById(R.id.form_input_message);
        final TimePicker formTime = (TimePicker) findViewById(R.id.form_time);
        final DatePicker formDate = (DatePicker) findViewById(R.id.form_date);
        final Button buttonCancel = (Button) findViewById(R.id.button_cancel);

        // Filling form with data if provided
        if (sms.getTimestampCreated() > 0) {
            timeScheduled.setTimeInMillis(sms.getTimestampScheduled());
            recipient = sms.getRecipientName().length() > 0
                    ? sms.getRecipientName() + " <" + sms.getRecipientNumber() + ">"
                    : sms.getRecipientNumber()
            ;
            message = sms.getMessage();
        }
        formTime.setIs24HourView(android.text.format.DateFormat.is24HourFormat(this));
        formTime.setCurrentHour(timeScheduled.get(Calendar.HOUR_OF_DAY));
        formTime.setCurrentMinute(timeScheduled.get(Calendar.MINUTE));
        formDate.updateDate(
                timeScheduled.get(GregorianCalendar.YEAR),
                timeScheduled.get(GregorianCalendar.MONTH),
                timeScheduled.get(GregorianCalendar.DAY_OF_MONTH)
        );
        formContact.setText(recipient);
        formMessage.setText(message);
        buttonCancel.setVisibility(sms.getTimestampCreated() > 0 ? View.VISIBLE : View.GONE);
        int stringId = sms.getStatus().contentEquals(sms.STATUS_PENDING)
                ? R.string.form_button_cancel
                : R.string.form_button_delete
                ;
        buttonCancel.setText(getString(stringId));

        // Filling contacts list
        SimpleAdapter adapter = new SimpleAdapter(
                this,
                getContacts(),
                R.layout.item_contact,
                new String[]{"Name", "Phone"},
                new int[]{R.id.account_name, R.id.account_number}
        );
        formContact.setAdapter(adapter);
        formContact.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String, String> recipient = (HashMap<String, String>) parent.getItemAtPosition(position);
                String name = recipient.get("Name"), phone = recipient.get("Phone");
                formContact.setText(name + " <" + phone + ">");
                sms.setRecipientName(name);
                sms.setRecipientNumber(phone);
            }
        });
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!formContact.isPerformingCompletion()) {
                    sms.setRecipientName("");
                    sms.setRecipientNumber(String.valueOf(s));
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
        formContact.addTextChangedListener(watcher);

        // Adding emptiness checks
        TextWatcher watcherEmptiness = new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                final Button button = (Button) findViewById(R.id.button_add);
                button.setEnabled(formContact.getText().length() > 0 && formMessage.getText().length() > 0);
            }
        };
        formContact.addTextChangedListener(watcherEmptiness);
        formMessage.addTextChangedListener(watcherEmptiness);

        // Adding time event listeners
        formTime.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                timeScheduled.set(GregorianCalendar.HOUR_OF_DAY, hourOfDay);
                timeScheduled.set(GregorianCalendar.MINUTE, minute);
            }
        });
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
        long smsId = getSmsId(savedInstanceState);
        if (smsId > 0) {
            sms = DbHelper.getDbHelper(this).get(smsId);
        } else {
            sms = new SmsModel();
        }
    }

    public void scheduleSms(View view) {
        DatePicker formDate = (DatePicker) findViewById(R.id.form_date);
        timeScheduled.set(GregorianCalendar.YEAR, formDate.getYear());
        timeScheduled.set(GregorianCalendar.MONTH, formDate.getMonth());
        timeScheduled.set(GregorianCalendar.DAY_OF_MONTH, formDate.getDayOfMonth());
        if (timeScheduled.getTimeInMillis() < GregorianCalendar.getInstance().getTimeInMillis()) {
            Toast.makeText(getApplicationContext(), getString(R.string.form_validation_datetime), Toast.LENGTH_SHORT).show();
            return;
        }
        sms.setTimestampScheduled(timeScheduled.getTimeInMillis());

        EditText formMessage = (EditText) findViewById(R.id.form_input_message);
        sms.setMessage(formMessage.getText().toString());

        sms.setStatus(SmsModel.STATUS_PENDING);

        DbHelper.getDbHelper(this).save(sms);

        scheduleAlarm(sms);

        goToSmsList();
    }

    public void unscheduleSms(View view) {
        DbHelper.getDbHelper(this).delete(sms.getTimestampCreated());

        unscheduleAlarm(sms);

        goToSmsList();
    }

    private void goToSmsList() {
        Intent goToNextActivity = new Intent(getApplicationContext(), SmsListActivity.class);
        startActivity(goToNextActivity);
    }

    private List<? extends HashMap<String, ?>> getContacts() {
        ArrayList<HashMap<String, String>> contacts = new ArrayList<>();
        Cursor people = getContentResolver().query(
                ContactsContract.Contacts.CONTENT_URI,
                null,
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        );
        while (people.moveToNext()) {
            String contactName = people.getString(people.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            String contactId = people.getString(people.getColumnIndex(ContactsContract.Contacts._ID));
            String hasPhone = people.getString(people.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

            if ((Integer.parseInt(hasPhone) > 0)) {
                Cursor phones = getContentResolver().query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId,
                        null,
                        null
                );
                while (phones.moveToNext()) {
                    String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    HashMap<String, String> NamePhoneType = new HashMap<String, String>();
                    NamePhoneType.put("Name", contactName);
                    NamePhoneType.put("Phone", phoneNumber);
                    contacts.add(NamePhoneType);
                }
                phones.close();
            }
        }
        return contacts;
    }

    private long getSmsId(Bundle savedInstanceState) {
        String smsId = "0";
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                smsId = extras.getString(DbHelper.COLUMN_TIMESTAMP_CREATED);
            }
        } else {
            smsId = (String) savedInstanceState.getSerializable(DbHelper.COLUMN_TIMESTAMP_CREATED);
        }
        return Long.parseLong(smsId);
    }

    private void scheduleAlarm(SmsModel sms) {
        AlarmManager alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmMgr.set(AlarmManager.RTC_WAKEUP, sms.getTimestampScheduled(), getAlarmPendingIntent(sms));
    }

    private void unscheduleAlarm(SmsModel sms) {
        AlarmManager alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmMgr.cancel(getAlarmPendingIntent(sms));
    }
    
    private PendingIntent getAlarmPendingIntent(SmsModel sms) {
        Intent intent = new Intent(AlarmReceiver.INTENT_FILTER);
        intent.putExtra(DbHelper.COLUMN_TIMESTAMP_CREATED, sms.getTimestampCreated());
        return PendingIntent.getBroadcast(
                this,
                sms.getId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT & Intent.FILL_IN_DATA
        );
    }

    private boolean permissionsGranted() {
        boolean granted = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ArrayList<String> permissionsNotGranted = new ArrayList<String>();
            for (int i = 0; i < this.permissionsRequired.length; i++) {
                if (checkSelfPermission(this.permissionsRequired[i]) != PackageManager.PERMISSION_GRANTED) {
                    permissionsNotGranted.add(this.permissionsRequired[i]);
                } else {
                    this.permissionsGranted.add(this.permissionsRequired[i]);
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
            {
                List<String> requiredPermissions = Arrays.asList(this.permissionsRequired);
                String permission;
                for (int i = 0; i < permissions.length; i++) {
                    permission = permissions[i];
                    if (requiredPermissions.contains(permission)
                            && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        this.permissionsGranted.add(permission);
                    }
                }
                if (this.permissionsGranted.size() == this.permissionsRequired.length) {
                    buildForm();
                }
            }
            break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}