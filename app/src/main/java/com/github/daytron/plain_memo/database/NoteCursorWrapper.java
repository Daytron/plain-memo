package com.github.daytron.plain_memo.database;

import android.database.Cursor;
import android.database.CursorWrapper;
import com.github.daytron.plain_memo.database.NoteDbSchema.NoteTable;

import com.github.daytron.plain_memo.model.Note;

import java.util.Date;
import java.util.UUID;

/**
 * Created by ryan on 27/10/15.
 */
class NoteCursorWrapper extends CursorWrapper {

    public NoteCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Note getNote() {
        String uuidString = getString(getColumnIndex(NoteTable.Cols.UUID));
        String title = getString(getColumnIndex(NoteTable.Cols.TITLE));
        String body = getString(getColumnIndex(NoteTable.Cols.BODY));
        long dateCreated = getLong(getColumnIndex(NoteTable.Cols.DATE_CREATED));
        long dateEdited = getLong(getColumnIndex(NoteTable.Cols.DATE_EDITED));

        Note note = new Note(UUID.fromString(uuidString));
        note.setTitle(title);
        note.setBody(body);
        note.setDateCreated(new Date(dateCreated));
        note.setDateEdited(new Date(dateEdited));

        return note;
    }
}
