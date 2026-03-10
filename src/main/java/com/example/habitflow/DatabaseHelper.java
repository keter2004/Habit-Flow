// DatabaseHelper.java
package com.example.habitflow;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "habitflow.db";
    private static final int DATABASE_VERSION = 1;

    // Users table
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PASSWORD = "password"; // Note: In real apps, hash passwords

    // Habits table
    private static final String TABLE_HABITS = "habits";
    private static final String COLUMN_HABIT_ID = "habit_id";
    private static final String COLUMN_HABIT_NAME = "name";

    // Check-ins table (for daily tracking)
    private static final String TABLE_CHECKINS = "checkins";
    private static final String COLUMN_CHECKIN_ID = "checkin_id";
    private static final String COLUMN_HABIT_FOREIGN_ID = "habit_id";
    private static final String COLUMN_DATE = "date"; // e.g., "2026-03-05"
    private static final String COLUMN_DONE = "done"; // 1 for yes, 0 for no

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_EMAIL + " TEXT UNIQUE, "
                + COLUMN_PASSWORD + " TEXT)";
        db.execSQL(createUsersTable);

        String createHabitsTable = "CREATE TABLE " + TABLE_HABITS + " ("
                + COLUMN_HABIT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_HABIT_NAME + " TEXT)";
        db.execSQL(createHabitsTable);

        String createCheckinsTable = "CREATE TABLE " + TABLE_CHECKINS + " ("
                + COLUMN_CHECKIN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_HABIT_FOREIGN_ID + " INTEGER, "
                + COLUMN_DATE + " TEXT, "
                + COLUMN_DONE + " INTEGER, "
                + "FOREIGN KEY(" + COLUMN_HABIT_FOREIGN_ID + ") REFERENCES " + TABLE_HABITS + "(" + COLUMN_HABIT_ID + "))";
        db.execSQL(createCheckinsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HABITS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHECKINS);
        onCreate(db);
    }

    // User methods
    public boolean addUser(String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PASSWORD, password); // TODO: Hash in production
        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    public boolean checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_ID},
                COLUMN_EMAIL + "=? AND " + COLUMN_PASSWORD + "=?",
                new String[]{email, password}, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        return count > 0;
    }

    // Habit methods
    public void addHabit(Habit habit) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_HABIT_NAME, habit.getName());
        db.insert(TABLE_HABITS, null, values);
    }

    public List<Habit> getAllHabits() {
        List<Habit> habits = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_HABITS, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_HABIT_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HABIT_NAME));
                habits.add(new Habit(id, name));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return habits;
    }

    public void updateHabit(Habit habit) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_HABIT_NAME, habit.getName());
        db.update(TABLE_HABITS, values, COLUMN_HABIT_ID + "=?", new String[]{String.valueOf(habit.getId())});
    }

    public void deleteHabit(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_HABITS, COLUMN_HABIT_ID + "=?", new String[]{String.valueOf(id)});
        // Also delete checkins
        db.delete(TABLE_CHECKINS, COLUMN_HABIT_FOREIGN_ID + "=?", new String[]{String.valueOf(id)});
    }

    // Check-in methods
    public void addOrUpdateCheckin(int habitId, String date, boolean done) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_HABIT_FOREIGN_ID, habitId);
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_DONE, done ? 1 : 0);

        // Check if exists
        Cursor cursor = db.query(TABLE_CHECKINS, new String[]{COLUMN_CHECKIN_ID},
                COLUMN_HABIT_FOREIGN_ID + "=? AND " + COLUMN_DATE + "=?",
                new String[]{String.valueOf(habitId), date}, null, null, null);

        if (cursor.getCount() > 0) {
            db.update(TABLE_CHECKINS, values, COLUMN_HABIT_FOREIGN_ID + "=? AND " + COLUMN_DATE + "=?",
                    new String[]{String.valueOf(habitId), date});
        } else {
            db.insert(TABLE_CHECKINS, null, values);
        }
        cursor.close();
    }

    public boolean isChecked(int habitId, String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_CHECKINS, new String[]{COLUMN_DONE},
                COLUMN_HABIT_FOREIGN_ID + "=? AND " + COLUMN_DATE + "=?",
                new String[]{String.valueOf(habitId), date}, null, null, null);
        if (cursor.moveToFirst()) {
            boolean done = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DONE)) == 1;
            cursor.close();
            return done;
        }
        cursor.close();
        return false;
    }

    public List<String> getCheckedDatesForHabit(int habitId) {
        List<String> dates = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_CHECKINS, new String[]{COLUMN_DATE},
                COLUMN_HABIT_FOREIGN_ID + "=? AND " + COLUMN_DONE + "=1",
                new String[]{String.valueOf(habitId)}, null, null, COLUMN_DATE + " ASC");
        if (cursor.moveToFirst()) {
            do {
                dates.add(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return dates;
    }
}