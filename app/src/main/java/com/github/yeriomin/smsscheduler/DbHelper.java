package com.github.yeriomin.smsscheduler;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Calendar;

public class DbHelper extends SQLiteOpenHelper {

    private static DbHelper dbHelper;

    private static final String DATABASE_NAME = "SmsScheduler.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_SMS = "sms";

    public static final String COLUMN_TIMESTAMP_CREATED = "datetimeCreated";
    public static final String COLUMN_TIMESTAMP_SCHEDULED = "datetimeScheduled";
    public static final String COLUMN_RECIPIENT_NUMBER = "recipientNumber";
    public static final String COLUMN_RECIPIENT_NAME = "recipientName";
    public static final String COLUMN_MESSAGE = "message";
    public static final String COLUMN_STATUS = "status";
    public static final String COLUMN_RESULT = "result";

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
        String CREATE_SMS_TABLE = "CREATE TABLE " + TABLE_SMS +
                "(" +
                COLUMN_TIMESTAMP_CREATED + " BIGINTEGER PRIMARY KEY," +
                COLUMN_TIMESTAMP_SCHEDULED + " BIGINTEGER," +
                COLUMN_RECIPIENT_NUMBER + " TEXT," +
                COLUMN_RECIPIENT_NAME + " TEXT," +
                COLUMN_MESSAGE + " TEXT," +
                COLUMN_STATUS + " TEXT," +
                COLUMN_RESULT + " TEXT" +
                ")";
        db.execSQL(CREATE_SMS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_SMS);
            onCreate(db);
        }
    }

    public void save(SmsModel sms) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_TIMESTAMP_SCHEDULED, sms.getTimestampScheduled());
        values.put(COLUMN_RECIPIENT_NAME, sms.getRecipientName());
        values.put(COLUMN_RECIPIENT_NUMBER, sms.getRecipientNumber());
        values.put(COLUMN_MESSAGE, sms.getMessage());
        values.put(COLUMN_STATUS, sms.getStatus());
        values.put(COLUMN_RESULT, sms.getResult());
        if (sms.getTimestampCreated() > 0) {
            String whereClause = COLUMN_TIMESTAMP_CREATED + "=?";
            String[] whereArgs = new String[] {sms.getTimestampCreated().toString()};
            dbHelper.getWritableDatabase().update(TABLE_SMS, values, whereClause, whereArgs);
        } else {
            long timestampCreated = Calendar.getInstance().getTimeInMillis();
            sms.setTimestampCreated(timestampCreated);
            values.put(COLUMN_TIMESTAMP_CREATED, timestampCreated);
            dbHelper.getWritableDatabase().insert(TABLE_SMS, null, values);
        }
    }

    public Cursor getCursor() {
        String selection = "";
        return getCursor(selection);
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
            SmsModel result = null;
            if (cursor.moveToFirst()) {
                result = buildObject(cursor);
            }
            cursor.close();
            return result;
        }
        return null;
    }

    public ArrayList<SmsModel> get(String status) {
        Cursor cursor = getCursor(status);
        if (cursor != null) {
            ArrayList<SmsModel> result = new ArrayList<>();
            while (cursor.moveToNext()) {
                result.add(buildObject(cursor));
            }
            cursor.close();
            return result;
        }
        return null;
    }

    public void delete(Long timestampCreated) {
        String selection = COLUMN_TIMESTAMP_CREATED + "=?";
        String[] selectionArgs = new String[] {timestampCreated.toString()};
        dbHelper.getReadableDatabase().delete(TABLE_SMS, selection, selectionArgs);
    }

    private SmsModel buildObject(Cursor cursor) {
        SmsModel result = new SmsModel();
        result.setTimestampCreated(cursor.getLong(cursor.getColumnIndex(COLUMN_TIMESTAMP_CREATED)));
        result.setTimestampScheduled(cursor.getLong(cursor.getColumnIndex(COLUMN_TIMESTAMP_SCHEDULED)));
        result.setRecipientNumber(cursor.getString(cursor.getColumnIndex(COLUMN_RECIPIENT_NUMBER)));
        result.setRecipientName(cursor.getString(cursor.getColumnIndex(COLUMN_RECIPIENT_NAME)));
        result.setMessage(cursor.getString(cursor.getColumnIndex(COLUMN_MESSAGE)));
        result.setStatus(cursor.getString(cursor.getColumnIndex(COLUMN_STATUS)));
        result.setResult(cursor.getString(cursor.getColumnIndex(COLUMN_RESULT)));
        return result;
    }
}
