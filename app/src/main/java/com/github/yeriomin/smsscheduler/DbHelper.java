package com.github.yeriomin.smsscheduler;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class DbHelper extends SQLiteOpenHelper {

    private static DbHelper dbHelper;

    private static final String DATABASE_NAME = "SmsScheduler.db";
    private static final int DATABASE_VERSION = 2;

    public static final String TABLE_SMS = "sms";

    public static final String COLUMN_TIMESTAMP_CREATED = "datetimeCreated";
    public static final String COLUMN_TIMESTAMP_SCHEDULED = "datetimeScheduled";
    public static final String COLUMN_RECIPIENT_NUMBER = "recipientNumber";
    public static final String COLUMN_RECIPIENT_NAME = "recipientName";
    public static final String COLUMN_MESSAGE = "message";
    public static final String COLUMN_STATUS = "status";
    public static final String COLUMN_RESULT = "result";
    public static final String COLUMN_SUBSCRIPTION_ID = "subscriptionId";
    public static final String COLUMN_RECURRING_MODE = "recurringMode";

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public DbHelper(Context context, SQLiteDatabase.CursorFactory factory) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }

    static public DbHelper getDbHelper(Context context) {
        if (null == dbHelper) {
            dbHelper = new DbHelper(context);
        }
        return dbHelper;
    }

    static public void closeDbHelper() {
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_SMS +
            "(" +
            COLUMN_TIMESTAMP_CREATED + " BIGINTEGER PRIMARY KEY," +
            COLUMN_TIMESTAMP_SCHEDULED + " BIGINTEGER," +
            COLUMN_RECIPIENT_NUMBER + " TEXT," +
            COLUMN_RECIPIENT_NAME + " TEXT," +
            COLUMN_MESSAGE + " TEXT," +
            COLUMN_STATUS + " TEXT," +
            COLUMN_RESULT + " TEXT," +
            COLUMN_SUBSCRIPTION_ID + " INTEGER," +
            COLUMN_RECURRING_MODE + " TEXT" +
            ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion <= oldVersion) {
            Log.i(getClass().getName(), "newVersion <= oldVersion");
            return;
        }
        if (oldVersion == 1 && newVersion >= 2) {
            Log.i(getClass().getName(), "Upgrading DB! oldVersion == 1 && newVersion >= 2");
            onUpgradeV2(db);
            Log.i(getClass().getName(), "Done");
        }
    }

    private void onUpgradeV2(SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + TABLE_SMS + " ADD COLUMN " + COLUMN_SUBSCRIPTION_ID + " INTEGER NOT NULL DEFAULT 0");
        db.execSQL("ALTER TABLE " + TABLE_SMS + " ADD COLUMN " + COLUMN_RECURRING_MODE + " TEXT NOT NULL DEFAULT \"" + CalendarResolver.RECURRING_NO + "\"");
    }

    public void save(SmsModel sms) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_TIMESTAMP_SCHEDULED, sms.getTimestampScheduled());
        values.put(COLUMN_RECIPIENT_NAME, sms.getRecipientName());
        values.put(COLUMN_RECIPIENT_NUMBER, sms.getRecipientNumber());
        values.put(COLUMN_MESSAGE, sms.getMessage());
        values.put(COLUMN_STATUS, sms.getStatus());
        values.put(COLUMN_RESULT, sms.getResult());
        values.put(COLUMN_SUBSCRIPTION_ID, sms.getSubscriptionId());
        values.put(COLUMN_RECURRING_MODE, sms.getRecurringMode());
        if (sms.getTimestampCreated() > 0) {
            String whereClause = COLUMN_TIMESTAMP_CREATED + "=?";
            String[] whereArgs = new String[] {sms.getTimestampCreated().toString()};
            dbHelper.getWritableDatabase().update(TABLE_SMS, values, whereClause, whereArgs);
        } else {
            long timestampCreated = System.currentTimeMillis();
            sms.setTimestampCreated(timestampCreated);
            values.put(COLUMN_TIMESTAMP_CREATED, timestampCreated);
            dbHelper.getWritableDatabase().insert(TABLE_SMS, null, values);
        }
    }

    public Cursor getCursor() {
        return getCursor("");
    }

    public Cursor getCursor(String status) {
        String[] columns = new String[] { "*", COLUMN_TIMESTAMP_CREATED + " AS _id" };
        String selection = "";
        String[] selectionArgs = new String[]{};
        if (null != status && status.length() > 0) {
            selection = COLUMN_STATUS + "=?";
            selectionArgs = new String[] {status};
        }
        String orderBy = COLUMN_TIMESTAMP_CREATED + " DESC";
        return dbHelper.getReadableDatabase().query(TABLE_SMS, columns, selection, selectionArgs, null, null, orderBy);
    }

    public SmsModel get(long timestampCreated) {
        Cursor cursor = dbHelper.getReadableDatabase().query(
                false,
                TABLE_SMS,
                new String[]{"*", COLUMN_TIMESTAMP_CREATED + " AS _id"},
                COLUMN_TIMESTAMP_CREATED + "=?",
                new String[]{Long.toString(timestampCreated)},
                null,
                null,
                null,
                "1"
        );
        if (cursor != null) {
            ArrayList<SmsModel> results = getObjects(cursor);
            cursor.close();
            if (results.size() > 0) {
                return results.get(0);
            }
        }
        return null;
    }

    public ArrayList<SmsModel> get(String status) {
        Cursor cursor = getCursor(status);
        if (cursor != null) {
            ArrayList<SmsModel> results = getObjects(cursor);
            cursor.close();
            return results;
        }
        return null;
    }

    public void delete(Long timestampCreated) {
        String selection = COLUMN_TIMESTAMP_CREATED + "=?";
        String[] selectionArgs = new String[] {timestampCreated.toString()};
        dbHelper.getReadableDatabase().delete(TABLE_SMS, selection, selectionArgs);
    }

    private ArrayList<SmsModel> getObjects(Cursor cursor) {
        ArrayList<SmsModel> result = new ArrayList<>();
        int indexTimestampCreated = cursor.getColumnIndex(COLUMN_TIMESTAMP_CREATED);
        int indexTimestampScheduled = cursor.getColumnIndex(COLUMN_TIMESTAMP_SCHEDULED);
        int indexRecipientNumber = cursor.getColumnIndex(COLUMN_RECIPIENT_NUMBER);
        int indexRecipientName = cursor.getColumnIndex(COLUMN_RECIPIENT_NAME);
        int indexMessage = cursor.getColumnIndex(COLUMN_MESSAGE);
        int indexStatus = cursor.getColumnIndex(COLUMN_STATUS);
        int indexResult = cursor.getColumnIndex(COLUMN_RESULT);
        int indexSubscriptionId = cursor.getColumnIndex(COLUMN_SUBSCRIPTION_ID);
        int indexRecurringMode = cursor.getColumnIndex(COLUMN_RECURRING_MODE);
        SmsModel object;
        while (cursor.moveToNext()) {
            object = new SmsModel();
            object.setTimestampCreated(cursor.getLong(indexTimestampCreated));
            object.setTimestampScheduled(cursor.getLong(indexTimestampScheduled));
            object.setRecipientNumber(cursor.getString(indexRecipientNumber));
            object.setRecipientName(cursor.getString(indexRecipientName));
            object.setMessage(cursor.getString(indexMessage));
            object.setStatus(cursor.getString(indexStatus));
            object.setResult(cursor.getString(indexResult));
            object.setSubscriptionId(cursor.getInt(indexSubscriptionId));
            object.setRecurringMode(cursor.getString(indexRecurringMode));
            result.add(object);
        }
        return result;
    }
}
