package com.github.daytron.plain_memo.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.github.daytron.plain_memo.data.GlobalValues;
import com.github.daytron.plain_memo.database.NoteDbSchema.NoteTable;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

/**
 * Subclass of DatabaseManager for creating and upgrading notes database.
 */
public class NotesDatabaseManager extends DatabaseManager {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "NOTE_DB";

    public NotesDatabaseManager(Context appContext) {
        super(appContext, DATABASE_NAME, DATABASE_VERSION);
    }

    @Override
    protected void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + NoteTable.NAME + "(" +
                " _id integer primary key autoincrement, " +
                NoteTable.Cols.UUID + ", " +
                NoteTable.Cols.TITLE + ", " +
                NoteTable.Cols.BODY + ", " +
                NoteTable.Cols.DATE_CREATED + ", " +
                NoteTable.Cols.DATE_EDITED + ")");

        db.insert(NoteTable.NAME, null, createNewWelcomeNote());
    }

    /**
     * Prepare to add a note on first install to welcome user.
     *
     * @return ContentValues object
     */
    private ContentValues createNewWelcomeNote() {
        ContentValues values = new ContentValues();

        Date date = Calendar.getInstance().getTime();
        values.put(NoteTable.Cols.UUID, UUID.randomUUID().toString());
        values.put(NoteTable.Cols.TITLE, GlobalValues.WELCOME_TITLE);
        values.put(NoteTable.Cols.BODY, GlobalValues.WELCOME_BODY);
        values.put(NoteTable.Cols.DATE_CREATED, date.getTime());
        values.put(NoteTable.Cols.DATE_EDITED, date.getTime());

        return values;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older notes table if existed
        db.execSQL("DROP TABLE IF EXISTS " + NoteTable.NAME);

        // create fresh notes table
        this.onCreate(db);
    }
}
