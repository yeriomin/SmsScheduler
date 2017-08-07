package com.github.yeriomin.smsscheduler;

import android.util.Log;

import java.util.Calendar;

public class CalendarResolver {

    public static final String RECURRING_NO = "RECURRING_NO";
    public static final String RECURRING_DAILY = "RECURRING_DAILY";
    public static final String RECURRING_WEEKLY = "RECURRING_WEEKLY";
    public static final String RECURRING_MONTHLY = "RECURRING_MONTHLY";
    public static final String RECURRING_YEARLY = "RECURRING_YEARLY";

    private Calendar calendar;
    private String recurringMode;

    public CalendarResolver setCalendar(Calendar calendar) {
        this.calendar = calendar;
        return this;
    }

    public CalendarResolver setRecurringMode(String recurringMode) {
        this.recurringMode = recurringMode;
        return this;
    }

    public CalendarResolver setWeekDay(int day) {
        if (day < 1 || day > 7) {
            Log.e(getClass().getName(), "Week day number must be from 1 to 7");
            return this;
        }
        calendar.set(Calendar.DAY_OF_MONTH, Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        while (past() || calendar.get(Calendar.DAY_OF_WEEK) != day) {
            calendar.add(Calendar.DATE, 1);
        }
        return this;
    }

    public CalendarResolver setDayOfMonth(int day) {
        calendar.set(Calendar.DAY_OF_MONTH, day);
        return this;
    }

    public CalendarResolver setMonth(int month) {
        Calendar temp = Calendar.getInstance();
        temp.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
        temp.set(Calendar.MONTH, month - 1);
        if (calendar.get(Calendar.DAY_OF_MONTH) > temp.getActualMaximum(Calendar.DAY_OF_MONTH)) {
            calendar.set(Calendar.DAY_OF_MONTH, temp.getActualMaximum(Calendar.DAY_OF_MONTH));
        }
        calendar.set(Calendar.MONTH, month - 1);
        return this;
    }

    public CalendarResolver reset() {
        calendar.set(Calendar.DAY_OF_MONTH, Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.MONTH, Calendar.getInstance().get(Calendar.MONTH));
        calendar.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));
        return this;
    }

    public CalendarResolver advance() {
        while (past()) {
            try {
                step();
            } catch (IllegalArgumentException e) {
                Log.w(getClass().getName(), e.getMessage());
                break;
            }
        }
        return this;
    }

    private void step() {
        switch (recurringMode) {
            case RECURRING_DAILY:
                calendar.add(Calendar.DATE, 1);
                break;
            case RECURRING_WEEKLY:
                calendar.add(Calendar.DATE, 7);
                break;
            case RECURRING_MONTHLY:
                calendar.add(Calendar.MONTH, 1);
                break;
            case RECURRING_YEARLY:
                calendar.add(Calendar.YEAR, 1);
                break;
            default:
                throw new IllegalArgumentException("Unsupported recurring mode: " + recurringMode);
        }
    }

    private boolean past() {
        return Calendar.getInstance().getTimeInMillis() > calendar.getTimeInMillis();
    }
}
