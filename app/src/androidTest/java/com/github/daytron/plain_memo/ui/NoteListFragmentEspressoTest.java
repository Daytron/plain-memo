package com.github.daytron.plain_memo.ui;

import android.support.test.espresso.Espresso;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.github.daytron.plain_memo.NoteListActivity;
import com.github.daytron.plain_memo.R;

import junit.framework.AssertionFailedError;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Custom helpers for Espresso tests.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class NoteListFragmentEspressoTest {

    private final String[] NEW_NOTE_TITLES = {
            "My Shopping List", "My Class Schedule", "Sold items on Ebay"};

    @Rule
    public ActivityTestRule<NoteListActivity> mActivityRule =
            new ActivityTestRule<>(NoteListActivity.class);


    @Before
    public void beforeEachTest() {
        // Verify the current view is the home screen
        EspressoHelpers.verifyCurrentScreenIsHome();
    }

    @After
    public void afterEachTest() {
        // Verify that the last screen is the home screen
        // Making sure any other tests will not end up in other screen
        EspressoHelpers.verifyCurrentScreenIsHome();
    }


    /*****************
     * Helper methods
     *****************/

    private void fillNewNotesForSearch() {
        for (String title : NEW_NOTE_TITLES) {
            EspressoHelpers.clickFabNewNoteButtonAndVerifyAction();

            // Find EditText title and start typing text into it
            onView(withId(R.id.note_title_edit_text)).perform(typeText(title));

            // Find save menu button and click
            onView(withId(R.id.menu_item_save_note)).perform(click());

            pressBack();
            EspressoHelpers.verifyCurrentScreenIsHome();
        }
    }

    private void deleteAllAddedNotes() {
        for (String title : NEW_NOTE_TITLES) {
            EspressoHelpers.openANoteFromListAndVerify(title);
            EspressoHelpers.deleteNoteFromView(title);
            EspressoHelpers.verifyCurrentScreenIsHome();
        }
    }

    /***************
     * Actual Tests
     ***************/

    @Test
    public void testAddNewNoteViaMenuItem() {
        // Find add note menu item and click
        onView(withContentDescription("More options")).perform(click());
        onView(withText(R.string.new_note)).perform(click());

        // Verify the NoteEditFragment is displayed via title EditText widget
        onView((withId(R.id.note_title_edit_text))).check(matches(isDisplayed()));

        // Close soft keyboard if open
        Espresso.closeSoftKeyboard();

        // Make sure to trigger auto delete new note for cleanup
        pressBack();
    }

    @Test
    public void testSearch() {
        // Initialise
        fillNewNotesForSearch();

        // Verify SearchView is not expanded yet
        onView(withContentDescription("Search")).check(matches(isDisplayed()));

        // Expand SearchView
        onView(withId(R.id.menu_item_search_note)).perform(click());

        // Verify SearchView is expanded
        onView(withId(R.id.search_src_text)).check(matches(isDisplayed()));
        onView(withId(R.id.search_close_btn)).check(matches(isDisplayed()));

        // Enter query
        String queryText = "My";
        onView(withId(R.id.search_src_text)).perform(typeText(queryText));

        // Verify filtered items
        onView(withText(NEW_NOTE_TITLES[0])).check(matches(isDisplayed()));
        onView(withText(NEW_NOTE_TITLES[1])).check(matches(isDisplayed()));

        // Verify the third note is not displayed
        try {
            onView(withText(NEW_NOTE_TITLES[2])).check(matches(isCompletelyDisplayed()));
            throw new AssertionFailedError("Third note is still displayed");
        } catch (AssertionFailedError error) { /* Do nothing */ }

        // Close SearchView
        onView(withId(R.id.search_src_text)).perform(clearText());
        onView(withId(R.id.search_close_btn)).perform(click());

        // Cleanup
        deleteAllAddedNotes();
    }

    @Test
    public void testLoadSettingsView() {
        // Find add note menu item and click
        onView(withContentDescription("More options")).perform(click());
        onView(withText(R.string.action_settings)).perform(click());

        // Verify the Settings screen
        onView(withText(R.string.pref_general)).check(matches(isDisplayed()));
        onView(withText(R.string.pref_appearance)).check(matches(isDisplayed()));

        // Open General settings
        onView(withText(R.string.pref_general)).perform(click());
        onView(withText(R.string.pref_general_send_feedback_title))
                .check(matches(isDisplayed()));
        pressBack();

        // Open Appearance settings
        onView(withText(R.string.pref_appearance)).perform(click());
        onView(withText(R.string.pref_appearance_font_size_title))
                .check(matches(isDisplayed()));
        pressBack();

        // Exit settings
        pressBack();
    }

    @Test
    public void testEditNoteAndSave() {
        EspressoHelpers.clickFabNewNoteButtonAndVerifyAction();

        String title = "test Edit note";
        EspressoHelpers.enterATitleFromEditNoteScreenAndSave(title);

        // Verify creation
        onView(withText(title)).check(matches(isDisplayed()));

        // Cleanup via delete
        EspressoHelpers.deleteNoteFromView(title);
    }

}
