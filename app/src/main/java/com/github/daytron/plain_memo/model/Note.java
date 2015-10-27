package com.github.daytron.plain_memo.model;

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
        mDateTime = new Date();
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

    public void setDateTime(Date date) {
        mDateTime = date;
    }

    public Date getDateTime() {
        return mDateTime;
    }

    public String getBody() {
        return mBody;
    }

    public void setBody(String body) {
        mBody = body;
    }
}
