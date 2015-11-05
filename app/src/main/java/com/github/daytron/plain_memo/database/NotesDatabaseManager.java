package com.github.daytron.plain_memo.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.github.daytron.plain_memo.database.NoteDbSchema.NoteTable;

/**
 * Created by ryan on 27/10/15.
 */
public class NotesDatabaseManager extends DatabaseManager {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "NOTE_DB";

    public NotesDatabaseManager(Context appContext) {
        super(appContext, DATABASE_NAME, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + NoteTable.NAME + "(" +
                " _id integer primary key autoincrement, " +
                NoteTable.Cols.UUID + ", " +
                NoteTable.Cols.TITLE + ", " +
                NoteTable.Cols.BODY + ", " +
                NoteTable.Cols.DATE_CREATED + ", " +
                NoteTable.Cols.DATE_EDITED + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older notes table if existed
        db.execSQL("DROP TABLE IF EXISTS " + NoteTable.NAME);

        // create fresh notes table
        this.onCreate(db);
    }
}
