package com.github.daytron.plain_memo.model;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

/**
 * Created by ryan on 27/10/15.
 */
public class Note {

    private UUID mID;
    private String mTitle;
    private String mBody;
    private Date mDateTime;

    public Note() {
        this(UUID.randomUUID());
    }

    public Note(UUID id) {
        mID = id;
        mDateTime = Calendar.getInstance().getTime();
    }

    public UUID getID() {
        return mID;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setDate(Date date) {
        mDateTime = date;
    }

    public Date getDate() {
        return mDateTime;
    }

    public String getBody() {
        return mBody;
    }

    public void setBody(String body) {
        mBody = body;
    }
}
