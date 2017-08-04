package com.github.yeriomin.smsscheduler.view;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import com.github.yeriomin.smsscheduler.R;
import com.github.yeriomin.smsscheduler.activity.AddSmsActivity;

public class EmptinessTextWatcher implements TextWatcher {
    private AddSmsActivity addSmsActivity;
    private final AutoCompleteTextView formContact;
    private final EditText formMessage;

    public EmptinessTextWatcher(AddSmsActivity addSmsActivity, AutoCompleteTextView formContact, EditText formMessage) {
        this.addSmsActivity = addSmsActivity;
        this.formContact = formContact;
        this.formMessage = formMessage;
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        addSmsActivity.findViewById(R.id.button_add).setEnabled(formContact.getText().length() > 0 && formMessage.getText().length() > 0);
    }
}
