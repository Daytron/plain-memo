package com.github.daytron.plain_memo.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.github.daytron.plain_memo.database.NoteDbSchema.NoteTable;
import com.github.daytron.plain_memo.model.Note;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by ryan on 27/10/15.
 */
public class NoteBook {

    private static NoteBook sNoteBook = null;

    private Context mContext;
    private SQLiteDatabase mDatabase;
    private NotesDatabaseManager notesDbManager;

    public static NoteBook get(Context appContext) {
        if (sNoteBook == null) {
            sNoteBook = new NoteBook(appContext);
        }

        return sNoteBook;
    }

    private NoteBook(Context context) {
        mContext = context.getApplicationContext();
        notesDbManager = new NotesDatabaseManager(mContext);
        mDatabase = notesDbManager.getDatabase();
    }

    public List<Note> getNotes() {
        List<Note> notes = new ArrayList<>();

        NoteCursorWrapper cursor = queryAllNotes(null,null);

        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                notes.add(cursor.getNote());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }

        return notes;
    }

    private NoteCursorWrapper queryAllNotes(String whereClause, String[] whereArgs) {
        Cursor cursor = mDatabase.query(
                NoteTable.NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                null
        );

        return new NoteCursorWrapper(cursor);
    }

    public Note getNote(UUID uuid) {
        NoteCursorWrapper cursor = queryAllNotes(
                NoteTable.Cols.UUID + " = ?",
                new String[]{uuid.toString()}
        );

        try {
            if (cursor.getCount() == 0) {
                return null;
            }

            cursor.moveToFirst();
            return cursor.getNote();
        } finally {
            cursor.close();
        }
    }

    private static ContentValues getContentValues(Note note) {
        ContentValues values = new ContentValues();
        values.put(NoteTable.Cols.UUID, note.getID().toString());
        values.put(NoteTable.Cols.TITLE, note.getTitle());
        values.put(NoteTable.Cols.BODY, note.getBody());
        values.put(NoteTable.Cols.DATE, note.getDateTime().getTime());

        return values;
    }

    public void addCrime(Note note) {
        ContentValues values = getContentValues(note);

        mDatabase.insert(NoteTable.NAME, null, values);
    }

    public void updateCrime(Note note) {
        String uuidString = note.getID().toString();
        ContentValues values = getContentValues(note);

        mDatabase.update(NoteTable.NAME, values,
                NoteTable.Cols.UUID + " = ?",
                new String[]{uuidString});
    }

    public void deleteCrime(Note note) {
        String uuidString = note.getID().toString();

        mDatabase.delete(NoteTable.NAME,
                NoteTable.Cols.UUID + " = ?",
                new String[]{uuidString});
    }

    public boolean closeDB() {
        return notesDbManager.close();
    }
}
