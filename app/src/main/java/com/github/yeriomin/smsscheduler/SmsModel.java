package com.github.yeriomin.smsscheduler;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Calendar;

public class SmsModel implements Parcelable {

    public static final String ERROR_UNKNOWN = "UNKNOWN";
    public static final String ERROR_GENERIC = "GENERIC";
    public static final String ERROR_NO_SERVICE = "NO_SERVICE";
    public static final String ERROR_NULL_PDU = "NULL_PDU";
    public static final String ERROR_RADIO_OFF = "RADIO_OFF";

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_SENT = "SENT";
    public static final String STATUS_DELIVERED = "DELIVERED";
    public static final String STATUS_FAILED = "FAILED";

    private long timestampCreated;
    private String recipientNumber;
    private String recipientName;
    private String message;
    private String status = STATUS_PENDING;
    private int subscriptionId;
    private String recurringMode = CalendarResolver.RECURRING_NO;

    private String result = "";
    private Calendar calendar = Calendar.getInstance();

    public SmsModel() {
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 1);
    }

    public int getId() {
        return (int) (getTimestampCreated() / 1000);
    }

    public Long getTimestampCreated() {
        return timestampCreated;
    }

    public void setTimestampCreated(long timestampCreated) {
        this.timestampCreated = timestampCreated;
    }

    public Long getTimestampScheduled() {
        return calendar.getTimeInMillis();
    }

    public void setTimestampScheduled(long timestampScheduled) {
        calendar.setTimeInMillis(timestampScheduled);
    }

    public String getRecipientNumber() {
        return recipientNumber;
    }

    public void setRecipientNumber(String recipientNumber) {
        this.recipientNumber = recipientNumber;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public int getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(int subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public String getRecurringMode() {
        return recurringMode;
    }

    public void setRecurringMode(String recurringMode) {
        this.recurringMode = recurringMode;
    }

    public Calendar getCalendar() {
        return calendar;
    }

    public SmsModel(Parcel in) {
        timestampCreated = in.readLong();
        calendar.setTimeInMillis(in.readLong());
        recipientNumber = in.readString();
        recipientName = in.readString();
        message = in.readString();
        status = in.readString();
        result = in.readString();
        subscriptionId = in.readInt();
        recurringMode = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(timestampCreated);
        dest.writeLong(calendar.getTimeInMillis());
        dest.writeString(recipientNumber);
        dest.writeString(recipientName);
        dest.writeString(message);
        dest.writeString(status);
        dest.writeString(result);
        dest.writeInt(subscriptionId);
        dest.writeString(recurringMode);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public SmsModel createFromParcel(Parcel in) {
            return new SmsModel(in);
        }

        public SmsModel[] newArray(int size) {
            return new SmsModel[size];
        }
    };
}
