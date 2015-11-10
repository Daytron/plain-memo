package com.github.daytron.plain_memo.ui;

import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.widget.RecyclerView;
import android.test.suitebuilder.annotation.LargeTest;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.isDialog;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

import com.github.daytron.plain_memo.NoteListActivity;
import com.github.daytron.plain_memo.R;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Espresso test for many UI events of adding new note.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class AddNewNoteEspressoTest {

    public static final String A_SAMPLE_TITLE_TEXT = "A sample title text";

    @Rule
    public ActivityTestRule<NoteListActivity> mActivityRule =
            new ActivityTestRule<>(NoteListActivity.class);

    @Before
    public void beforeEachTest() {
        EspressoHelpers.verifyCurrentScreenIsHome();
        EspressoHelpers.clickFabNewNoteButtonAndVerifyAction();
    }

    @After
    public void afterEachTest() {
        // Verify that it return back to note list screen
        EspressoHelpers.verifyCurrentScreenIsHome();
    }

    /***************
     * Actual Tests
     ***************/

    @Test
    public void testCancelNoteViaMenuCancelWithNoText() {
        // Find cancel menu button and click
        onView(withId(R.id.menu_item_cancel_note)).perform(click());
    }

    @Test
    public void testCancelNoteViaBackPress() {
        pressBack();
    }

    @Test
    public void testCancelNoteViaMenuCancelWithTitleTextClickNoAndYes() {
        String sampleNoteTitle = "This is sample Title";

        // Find EditText title and start typing text into it
        onView(withId(R.id.note_title_edit_text)).perform(typeText(sampleNoteTitle));

        // Find cancel menu button and click
        onView(withId(R.id.menu_item_cancel_note)).perform(click());

        // Verify Cancel dialog is displayed
        onView(withText(R.string.confirm_dialog_cancel_title)).inRoot(isDialog())
                .check(matches(isDisplayed()));

        // Click no button from the dialog
        onView(withId(android.R.id.button2)).perform(click());

        // Run cancel dialog again
        onView(withId(R.id.menu_item_cancel_note)).perform(click());

        // Verify Cancel dialog is displayed
        onView(withText(R.string.confirm_dialog_cancel_title)).inRoot(isDialog())
                .check(matches(isDisplayed()));

        // Click yes button from the dialog to cancel and cleanup note
        onView(withId(android.R.id.button1)).perform(click());
    }

    @Test
    public void testCreateNoteAndOpenItAgainFromList() {
        EspressoHelpers.enterATitleFromEditNoteScreenAndSave(A_SAMPLE_TITLE_TEXT);
        pressBack();

        // Verify RecyclerView is displayed and click the newly created note to open it
        EspressoHelpers.openANoteFromListAndVerify(A_SAMPLE_TITLE_TEXT);

        // Cleanup
        EspressoHelpers.deleteNoteFromView(A_SAMPLE_TITLE_TEXT);
    }

    @Test
    public void testSaveNoteViaMenuSaveClickSave() {
        EspressoHelpers.enterATitleFromEditNoteScreenAndSave(A_SAMPLE_TITLE_TEXT);

        // Cleanup process delete extra note created
        EspressoHelpers.deleteNoteFromView(A_SAMPLE_TITLE_TEXT);
    }

}
