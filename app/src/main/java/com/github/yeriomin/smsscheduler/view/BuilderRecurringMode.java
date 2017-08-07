package com.github.yeriomin.smsscheduler.view;

import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.Spinner;

import com.github.yeriomin.smsscheduler.CalendarResolver;
import com.github.yeriomin.smsscheduler.R;
import com.github.yeriomin.smsscheduler.activity.AddSmsActivity;

public class BuilderRecurringMode extends BuilderSpinner {

    static private final String[] modes = new String[] {
        CalendarResolver.RECURRING_NO,
        CalendarResolver.RECURRING_DAILY,
        CalendarResolver.RECURRING_WEEKLY,
        CalendarResolver.RECURRING_MONTHLY,
        CalendarResolver.RECURRING_YEARLY,
    };

    private Spinner recurringDayView;
    private Spinner recurringMonthView;
    private DatePicker dateView;

    public BuilderRecurringMode setRecurringDayView(Spinner recurringDayView) {
        this.recurringDayView = recurringDayView;
        return this;
    }

    public BuilderRecurringMode setRecurringMonthView(Spinner recurringMonthView) {
        this.recurringMonthView = recurringMonthView;
        return this;
    }

    public BuilderRecurringMode setDateView(DatePicker dateView) {
        this.dateView = dateView;
        return this;
    }

    @Override
    public Builder setActivity(AddSmsActivity activity) {
        values.add(activity.getString(R.string.form_recurring_mode_no));
        values.add(activity.getString(R.string.form_recurring_mode_daily));
        values.add(activity.getString(R.string.form_recurring_mode_weekly));
        values.add(activity.getString(R.string.form_recurring_mode_monthly));
        values.add(activity.getString(R.string.form_recurring_mode_yearly));
        return super.setActivity(activity);
    }

    public BuilderRecurringMode() {
        for (int i = 0; i < modes.length; i++) {
            keys.put(modes[i], i);
        }
    }

    @Override
    public View build() {
        refreshDependants();
        return super.build();
    }

    @Override
    protected boolean shouldBeVisible() {
        return true;
    }

    @Override
    protected void onAdapterItemSelected(AdapterView<?> parent, View view, int position, long id) {
        sms.setRecurringMode(modes[position]);
        new CalendarResolver().setCalendar(sms.getCalendar()).setRecurringMode(sms.getRecurringMode()).reset().advance();
        refreshDependants();
    }

    @Override
    protected int getSelection() {
        return keys.get(sms.getRecurringMode());
    }

    private void refreshDependants() {
        new BuilderRecurringDay().setActivity(activity).setSms(sms).setView(recurringDayView).build();
        new BuilderRecurringMonth().setActivity(activity).setSms(sms).setView(recurringMonthView).build();
        dateView.setVisibility(CalendarResolver.RECURRING_NO.equals(sms.getRecurringMode()) ? View.VISIBLE : View.GONE);
    }
}
