package com.github.yeriomin.smsscheduler.view;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;

import com.github.yeriomin.smsscheduler.R;

import java.util.HashMap;

public class BuilderContact extends Builder {

    @Override
    protected AutoCompleteTextView getView() {
        return (AutoCompleteTextView) view;
    }

    @Override
    public AutoCompleteTextView build() {
        ContactsTask task = new ContactsTask();
        task.setActivity(activity);
        task.setContactsView(getView());
        task.execute();
        getView().setText(!TextUtils.isEmpty(sms.getRecipientName())
            ? activity.getString(R.string.contact_format, sms.getRecipientName(), sms.getRecipientNumber())
            : sms.getRecipientNumber()
        );
        getView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String, String> recipient = (HashMap<String, String>) parent.getItemAtPosition(position);
                String name = recipient.get("Name"), phone = recipient.get("Phone");
                getView().setText(activity.getString(R.string.contact_format, name, phone));
                sms.setRecipientName(name);
                sms.setRecipientNumber(phone);
            }
        });
        getView().addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!getView().isPerformingCompletion()) {
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
        });
        return getView();
    }
}
