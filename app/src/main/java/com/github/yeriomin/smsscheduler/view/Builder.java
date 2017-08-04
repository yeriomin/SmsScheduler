package com.github.yeriomin.smsscheduler.view;

import android.view.View;

import com.github.yeriomin.smsscheduler.SmsModel;
import com.github.yeriomin.smsscheduler.activity.AddSmsActivity;

public abstract class Builder {

    protected View view;
    protected SmsModel sms;
    protected AddSmsActivity activity;

    abstract protected View getView();
    abstract public View build();

    public Builder setView(View view) {
        this.view = view;
        return this;
    }

    public Builder setSms(SmsModel sms) {
        this.sms = sms;
        return this;
    }

    public Builder setActivity(AddSmsActivity activity) {
        this.activity = activity;
        return this;
    }
}
