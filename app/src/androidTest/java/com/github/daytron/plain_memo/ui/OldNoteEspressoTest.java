package com.github.daytron.plain_memo.ui;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.widget.RecyclerView;
import android.test.suitebuilder.annotation.LargeTest;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.clearText;
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
 * Espresso test for many UI events of old notes.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class OldNoteEspressoTest {

    public static final String A_SAMPLE_TITLE_TEXT = "A sample title text";
    public static final String A_SAMPLE_BODY_TEXT = "A sample body text";

    @Rule
    public ActivityTestRule<NoteListActivity> mActivityRule =
            new ActivityTestRule<>(NoteListActivity.class);

    @Before
    public void beforeEachTest() {
        EspressoHelpers.verifyCurrentScreenIsHome();
        EspressoHelpers.createNewNoteFromHomeThenStayInNoteViewScreen(A_SAMPLE_TITLE_TEXT);
        EspressoHelpers.clickEditMenu(A_SAMPLE_TITLE_TEXT);
    }

    @After
    public void afterEachTest() {
        // Return to home
        pressBack();

        EspressoHelpers.openAndDeleteNote(A_SAMPLE_TITLE_TEXT);
        // Verify that it return back to note list screen
        EspressoHelpers.verifyCurrentScreenIsHome();
    }

    /***************
     * Actual Tests
     ***************/

    @Test
    public void testPressDiscardMenuNotUpdated() {
        // Find discard menu button and click
        onView(withId(R.id.menu_item_discard_note)).perform(click());
    }

    @Test
    public void testPressDiscardMenuUpdatedOptionalWithNoTitle() {
        // Find EditText title and clear title
        onView(withId(R.id.note_title_edit_text)).perform(clearText());

        // Find EditText body and start typing text into it
        onView(withId(R.id.note_body_edit_text)).perform(typeText(A_SAMPLE_BODY_TEXT));

        // Find discard menu button and click
        onView(withId(R.id.menu_item_discard_note)).perform(click());

        // Verify Discard dialog is displayed
        onView(withText(R.string.confirm_dialog_discard_changes)).inRoot(isDialog())
                .check(matches(isDisplayed()));

        // Click no button from the dialog
        onView(withId(android.R.id.button2)).perform(click());

        // Run discard dialog again
        onView(withId(R.id.menu_item_discard_note)).perform(click());

        // Verify Discard dialog is displayed
        onView(withText(R.string.confirm_dialog_discard_changes)).inRoot(isDialog())
                .check(matches(isDisplayed()));

        // Click yes button from the dialog to discard the new note
        onView(withId(android.R.id.button1)).perform(click());
    }

    @Test
    public void testBackPressNotUpdated() {
        // Close soft keyboard if open
        Espresso.closeSoftKeyboard();

        pressBack();
    }

    @Test
    public void testBackPressNotUpdatedWithTitle() {
        // Find EditText body and start typing text into it
        onView(withId(R.id.note_body_edit_text)).perform(typeText(A_SAMPLE_BODY_TEXT));

        // Close soft keyboard if open
        Espresso.closeSoftKeyboard();

        pressBack();
    }

    @Test
    public void testBackPressNotUpdatedWithNoTitle() {
        // Find EditText title and clear title
        onView(withId(R.id.note_title_edit_text)).perform(clearText());

        // Find EditText body and start typing text into it
        onView(withId(R.id.note_body_edit_text)).perform(typeText(A_SAMPLE_BODY_TEXT));

        // Close soft keyboard if open
        Espresso.closeSoftKeyboard();

        pressBack();

        // Verify Discard dialog is displayed
        onView(withText(R.string.confirm_dialog_discard_changes)).inRoot(isDialog())
                .check(matches(isDisplayed()));

        // Click no button from the dialog
        onView(withId(android.R.id.button2)).perform(click());

        // Run discard dialog again
        pressBack();

        // Verify Discard dialog is displayed
        onView(withText(R.string.confirm_dialog_discard_changes)).inRoot(isDialog())
                .check(matches(isDisplayed()));

        // Click yes button from the dialog to discard the new note
        onView(withId(android.R.id.button1)).perform(click());
    }
}
