package com.example.social.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;

import com.example.social.fragments.WeekdayFragment;
import com.example.social.model.Week;
import com.example.social.profiles.ProfileManagement;

import java.util.ArrayList;
import java.util.Calendar;

//TODO: Rewrite to Kotlin and RoomDB
public class DbHelper extends SQLiteOpenHelper {
    private Context context;

    private static final int DB_VERSION = 7;
    private static final String DB_NAME = "timetabledb";

    private static final String TIMETABLE = "timetable";
    private static final String TIMETABLE_ODD = "timetable_odd";
    private static final String WEEK_ID = "id";
    private static final String WEEK_SUBJECT = "subject";
    private static final String WEEK_FRAGMENT = "fragment";
    private static final String WEEK_TEACHER = "teacher";
    private static final String WEEK_ROOM = "room";
    private static final String WEEK_FROM_TIME = "fromtime";
    private static final String WEEK_TO_TIME = "totime";
    private static final String WEEK_COLOR = "color";

    public DbHelper(Context context) {
        super(context, getDBName(ProfileManagement.getSelectedProfilePosition()), null, DB_VERSION);
        this.context = context;
    }

    public DbHelper(Context context, int selectedProfile) {
        super(context, getDBName(selectedProfile), null, DB_VERSION);
        this.context = context;
    }

    private DbHelper(Context context, boolean odd) {
        super(context, DB_NAME + "_odd", null, 6);
        this.context = context;
    }

    @NonNull
    public static String getDBName(int selectedProfile) {
        String dbName;
        if (selectedProfile == 0)
            dbName = DB_NAME; //If the app was installed before the profiles were added
        else
            dbName = DB_NAME + "_" + selectedProfile;
        return dbName;
    }

    public void onCreate(@NonNull SQLiteDatabase db) {
        String CREATE_TIMETABLE = "CREATE TABLE IF NOT EXISTS " + TIMETABLE + "("
                + WEEK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + WEEK_SUBJECT + " TEXT,"
                + WEEK_FRAGMENT + " TEXT,"
                + WEEK_TEACHER + " TEXT,"
                + WEEK_ROOM + " TEXT,"
                + WEEK_FROM_TIME + " TEXT,"
                + WEEK_TO_TIME + " TEXT,"
                + WEEK_COLOR + " INTEGER" + ")";

        String CREATE_TIMETABLE_ODD = "CREATE TABLE IF NOT EXISTS " + TIMETABLE_ODD + "("
                + WEEK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + WEEK_SUBJECT + " TEXT,"
                + WEEK_FRAGMENT + " TEXT,"
                + WEEK_TEACHER + " TEXT,"
                + WEEK_ROOM + " TEXT,"
                + WEEK_FROM_TIME + " TEXT,"
                + WEEK_TO_TIME + " TEXT,"
                + WEEK_COLOR + " INTEGER" + ")";

        db.execSQL(CREATE_TIMETABLE);
        db.execSQL(CREATE_TIMETABLE_ODD);
    }

    @Override
    public void onUpgrade(@NonNull SQLiteDatabase db, int oldVersion, int newVersion) {
        onCreate(db);
        switch (oldVersion) {
            default:
            case 6:
                migrateEvenOddWeeks(db);
                break;
        }
    }

    private void migrateEvenOddWeeks(SQLiteDatabase db) {
        String[] keys = new String[]{WeekdayFragment.KEY_MONDAY_FRAGMENT,
                WeekdayFragment.KEY_TUESDAY_FRAGMENT,
                WeekdayFragment.KEY_WEDNESDAY_FRAGMENT,
                WeekdayFragment.KEY_THURSDAY_FRAGMENT,
                WeekdayFragment.KEY_FRIDAY_FRAGMENT,
                WeekdayFragment.KEY_SATURDAY_FRAGMENT,
                WeekdayFragment.KEY_SUNDAY_FRAGMENT};

        ArrayList<Week> oldOddWeeks = new ArrayList<>();
        DbHelper oldDbHelper = new DbHelper(context, true);
        for (String key : keys) {
            oldOddWeeks.addAll(oldDbHelper.getWeek(key, TIMETABLE));
        }

        for (Week week : oldOddWeeks) {
            insertWeek(week, TIMETABLE_ODD, db);
        }
    }

    private String getTimetableTable() {
        return getTimetableTable(Calendar.getInstance());
    }

    private String getTimetableTable(Calendar now) {
        if (PreferenceUtil.isEvenWeek(context, now))
            return TIMETABLE;
        else
            return TIMETABLE_ODD;
    }

    public void insertWeek(Week week) {
        insertWeek(week, getTimetableTable(), this.getWritableDatabase());
    }

    private void insertWeek(@NonNull Week week, String tableName, SQLiteDatabase db) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(WEEK_SUBJECT, week.getSubject());
        contentValues.put(WEEK_FRAGMENT, week.getFragment());
        contentValues.put(WEEK_TEACHER, week.getType());
        contentValues.put(WEEK_ROOM, week.getRoom());
        contentValues.put(WEEK_FROM_TIME, week.getFromTime());
        contentValues.put(WEEK_TO_TIME, week.getToTime());
        contentValues.put(WEEK_COLOR, week.getColor());
        db.insert(tableName, null, contentValues);
        db.update(tableName, contentValues, WEEK_FRAGMENT, null);
//        db.close();
    }

    public void deleteWeekById(@NonNull Week week) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(getTimetableTable(), WEEK_ID + " = ? ", new String[]{String.valueOf(week.getId())});
        db.close();
    }

    public void updateWeek(@NonNull Week week) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(WEEK_SUBJECT, week.getSubject());
        contentValues.put(WEEK_TEACHER, week.getType());
        contentValues.put(WEEK_ROOM, week.getRoom());
        contentValues.put(WEEK_FROM_TIME, week.getFromTime());
        contentValues.put(WEEK_TO_TIME, week.getToTime());
        contentValues.put(WEEK_COLOR, week.getColor());
        db.update(getTimetableTable(), contentValues, WEEK_ID + " = " + week.getId(), null);
        db.close();
    }

    public ArrayList<Week> getWeek(String fragment) {
        return getWeek(fragment, Calendar.getInstance());
    }

    public ArrayList<Week> getWeek(String fragment, Calendar now) {
        return getWeek(fragment, getTimetableTable(now));
    }

    @NonNull
    private ArrayList<Week> getWeek(String fragment, String dbName) {
        SQLiteDatabase db = this.getWritableDatabase();

        ArrayList<Week> weeklist = new ArrayList<>();
        Week week;
        Cursor cursor = db.rawQuery("SELECT * FROM ( SELECT * FROM " + dbName + " ORDER BY " + WEEK_FROM_TIME + " ) WHERE " + WEEK_FRAGMENT + " LIKE '" + fragment + "%'", null);
        while (cursor.moveToNext()) {
            week = new Week();
            week.setId(cursor.getInt(cursor.getColumnIndex(WEEK_ID)));
            week.setFragment(cursor.getString(cursor.getColumnIndex(WEEK_FRAGMENT)));
            week.setSubject(cursor.getString(cursor.getColumnIndex(WEEK_SUBJECT)));
            week.setType(cursor.getString(cursor.getColumnIndex(WEEK_TEACHER)));
            week.setRoom(cursor.getString(cursor.getColumnIndex(WEEK_ROOM)));
            week.setFromTime(cursor.getString(cursor.getColumnIndex(WEEK_FROM_TIME)));
            week.setToTime(cursor.getString(cursor.getColumnIndex(WEEK_TO_TIME)));
            week.setColor(cursor.getInt(cursor.getColumnIndex(WEEK_COLOR)));
            weeklist.add(week);
        }
        return weeklist;
    }
    
}
