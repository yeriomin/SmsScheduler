package com.github.yeriomin.smsscheduler.view;

import android.widget.TimePicker;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class BuilderTime extends Builder {

    @Override
    protected TimePicker getView() {
        return (TimePicker) view;
    }

    @Override
    public TimePicker build() {
        getView().setIs24HourView(android.text.format.DateFormat.is24HourFormat(activity));
        getView().setCurrentHour(sms.getCalendar().get(Calendar.HOUR_OF_DAY));
        getView().setCurrentMinute(sms.getCalendar().get(Calendar.MINUTE));
        getView().setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                sms.getCalendar().set(GregorianCalendar.HOUR_OF_DAY, hourOfDay);
                sms.getCalendar().set(GregorianCalendar.MINUTE, minute);
            }
        });
        return getView();
    }
}
