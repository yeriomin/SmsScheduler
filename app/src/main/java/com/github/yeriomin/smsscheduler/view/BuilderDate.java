package com.github.yeriomin.smsscheduler.view;

import android.widget.DatePicker;

import java.util.GregorianCalendar;

public class BuilderDate extends Builder {

    @Override
    protected DatePicker getView() {
        return (DatePicker) view;
    }

    @Override
    public DatePicker build() {
        getView().init(
            sms.getCalendar().get(GregorianCalendar.YEAR),
            sms.getCalendar().get(GregorianCalendar.MONTH),
            sms.getCalendar().get(GregorianCalendar.DAY_OF_MONTH),
            new DatePicker.OnDateChangedListener() {
                @Override
                public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    sms.getCalendar().set(GregorianCalendar.YEAR, year);
                    sms.getCalendar().set(GregorianCalendar.MONTH, monthOfYear);
                    sms.getCalendar().set(GregorianCalendar.DAY_OF_MONTH, dayOfMonth);
                }
            }
        );
        return getView();
    }
}
