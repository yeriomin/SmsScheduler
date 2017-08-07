package com.github.yeriomin.smsscheduler.view;

import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;

import com.github.yeriomin.smsscheduler.CalendarResolver;

import java.text.DateFormatSymbols;
import java.util.Calendar;

public class BuilderRecurringDay extends BuilderSpinner {

    @Override
    protected boolean shouldBeVisible() {
        return !sms.getRecurringMode().equals(CalendarResolver.RECURRING_NO)
            && !sms.getRecurringMode().equals(CalendarResolver.RECURRING_DAILY)
        ;
    }

    @Override
    protected void onAdapterItemSelected(AdapterView<?> parent, View view, int position, long id) {
        CalendarResolver resolver = new CalendarResolver().setCalendar(sms.getCalendar()).setRecurringMode(sms.getRecurringMode()).reset();
        if (isWeekly()) {
            resolver.setWeekDay(position + 1);
        } else {
            resolver.setDayOfMonth(position + 1);
        }
        resolver.advance();
    }

    @Override
    protected int getSelection() {
        return isWeekly() ? sms.getCalendar().get(Calendar.DAY_OF_WEEK) - 1 : sms.getCalendar().get(Calendar.DAY_OF_MONTH) - 1;
    }

    @Override
    public View build() {
        if (isWeekly()) {
            initWeekDays();
        } else {
            initMonthDays();
        }
        return super.build();
    }

    private void initWeekDays() {
        values.clear();
        for (String weekDay: new DateFormatSymbols().getWeekdays()) {
            if (TextUtils.isEmpty(weekDay)) {
                continue;
            }
            values.add(weekDay);
        }
    }

    private void initMonthDays() {
        values.clear();
        for (int i = 1; i <= 31; i++) {
            values.add(Integer.toString(i));
        }
    }

    private boolean isWeekly() {
        return sms.getRecurringMode().equals(CalendarResolver.RECURRING_WEEKLY);
    }
}
