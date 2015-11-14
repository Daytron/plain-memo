package com.github.daytron.plain_memo.unit;

import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.SmallTest;
import android.text.format.DateUtils;

import com.github.daytron.plain_memo.model.Note;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

/**
 * Unit Test class for Note.class
 *
 * Created by ryan on 10/11/15.
 */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class NoteTest {

    private final String SAMPLE_TITLE = "A sample title";
    private final String SAMPLE_BODY = "A sample body";

    private Note mNote;
    private UUID mNoteId;

    @Before
    public void setUp() {
        mNoteId = UUID.randomUUID();

        mNote = new Note(mNoteId);
        mNote.setTitle(SAMPLE_TITLE);
        mNote.setBody(SAMPLE_BODY);
    }

    @Test
    public void testGetId() {
        UUID resultId = mNote.getID();
        assertEquals(resultId, mNoteId);
    }

    @Test
    public void testSetTitle() {
        String givenTitle = "A new title";
        mNote.setTitle(givenTitle);

        String resultTitle = mNote.getTitle();
        assertEquals(givenTitle, resultTitle);
    }

    @Test
    public void testGetTitle() {
        String title = mNote.getTitle();
        assertEquals(title, SAMPLE_TITLE);
    }

    @Test
    public void testSetBody() {
        String givenBody = "A new body";
        mNote.setBody(givenBody);

        String resultBody = mNote.getBody();
        assertEquals(givenBody, resultBody);
    }

    @Test
    public void testGetBody() {
        String body = mNote.getBody();
        assertEquals(body, SAMPLE_BODY);
    }

    @Test
    public void testSetDateCreated() {
        Date givenDate = Calendar.getInstance().getTime();
        mNote.setDateCreated(givenDate);
        Date resultDate = mNote.getDateCreated();
        assertEquals(givenDate, resultDate);
    }

    @Test
    public void testGetDateCreated() {
        Date dateCreated = mNote.getDateCreated();
        boolean isToday = DateUtils.isToday(dateCreated.getTime());
        assertTrue(isToday);
    }

    @Test
    public void testSetGetDateEdited() {
        Date givenDate = Calendar.getInstance().getTime();
        mNote.setDateEdited(givenDate);
        Date resultDate = mNote.getDateEdited();
        assertEquals(givenDate, resultDate);
    }

    @Test
    public void testIsEdited() {
        Calendar futureCalendar = Calendar.getInstance();
        futureCalendar.setTime(mNote.getDateEdited());
        futureCalendar.add(Calendar.DAY_OF_YEAR, 2);

        Date givenDateInFuture = futureCalendar.getTime();
        mNote.setDateEdited(givenDateInFuture);

        boolean isEdited = mNote.isEdited();
        assertTrue(isEdited);
    }

}
