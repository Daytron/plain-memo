package com.github.daytron.plain_memo.model;

import android.support.annotation.NonNull;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

/**
 * Created by ryan on 27/10/15.
 */
public class Note {

    private final UUID mID;
    private String mTitle;
    private String mBody;
    private Date mDateTimeCreated;
    private Date mDateTimeEdited;

    public Note() {
        this(UUID.randomUUID());
    }

    public Note(UUID id) {
        mID = id;
        mDateTimeCreated = Calendar.getInstance().getTime();
        mDateTimeEdited = new Date(mDateTimeCreated.getTime());
    }

    public UUID getID() {
        return mID;
    }

    public void setTitle(@NonNull String title) {
        mTitle = title;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getBody() {
        return mBody;
    }

    public void setBody(@NonNull String body) {
        mBody = body;
    }

    public void setDateCreated(@NonNull Date date) {
        mDateTimeCreated = date;
    }

    public Date getDateCreated() {
        return mDateTimeCreated;
    }

    public void setDateEdited(@NonNull Date date) {
        mDateTimeEdited = date;
    }

    public Date getDateEdited() {
        return mDateTimeEdited;
    }

    public boolean isEdited() {
        Calendar created = Calendar.getInstance();
        created.setTime(mDateTimeCreated);

        Calendar edited = Calendar.getInstance();
        edited.setTime(mDateTimeEdited);

        int value = edited.compareTo(created);
        return value == 1;
    }
}
