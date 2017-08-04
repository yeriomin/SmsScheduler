package com.github.yeriomin.smsscheduler.view;

import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.widget.AutoCompleteTextView;
import android.widget.SimpleAdapter;

import com.github.yeriomin.smsscheduler.R;
import com.github.yeriomin.smsscheduler.activity.AddSmsActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class ContactsTask extends AsyncTask<Void, Void, List<? extends HashMap<String, ?>>> {

    private AddSmsActivity activity;
    private AutoCompleteTextView contactsView;

    public void setContactsView(AutoCompleteTextView contactsView) {
        this.contactsView = contactsView;
    }

    public void setActivity(AddSmsActivity activity) {
        this.activity = activity;
    }

    @Override
    protected void onPostExecute(List<? extends HashMap<String, ?>> contacts) {
        contactsView.setAdapter(new SimpleAdapter(
            activity,
            contacts,
            R.layout.item_contact,
            new String[]{"Name", "Phone"},
            new int[]{R.id.account_name, R.id.account_number}
        ));
    }

    @Override
    protected List<? extends HashMap<String, ?>> doInBackground(Void... voids) {
        HashMap<String, String> names = getNames();

        // Getting phones
        Cursor phones = activity.getContentResolver().query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            new String[] {
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            },
            null,
            null,
            null
        );
        ArrayList<HashMap<String, String>> contacts = new ArrayList<>();
        if (null != phones) {
            int columnIndexId = phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID);
            int columnIndexPhone = phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            while (phones.moveToNext()) {
                String contactId = phones.getString(columnIndexId);
                String phoneNumber = phones.getString(columnIndexPhone);
                HashMap<String, String> NamePhoneType = new HashMap<>();
                NamePhoneType.put("Name", names.get(contactId));
                NamePhoneType.put("Phone", phoneNumber);
                contacts.add(NamePhoneType);
            }
            phones.close();
        }

        return contacts;
    }

    private HashMap<String, String> getNames() {
        HashMap<String, String> names = new HashMap<>();

        // Getting contact names
        String[] projectionPeople = new String[] {
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME
        };
        Cursor people = activity.getContentResolver().query(
            ContactsContract.Contacts.CONTENT_URI,
            projectionPeople,
            null,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        );
        if (null != people) {
            int columnIndexId = people.getColumnIndex(ContactsContract.Contacts._ID);
            int columnIndexName = people.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
            while (people.moveToNext()) {
                names.put(people.getString(columnIndexId), people.getString(columnIndexName));
            }
            people.close();
        }

        return names;
    }
}
