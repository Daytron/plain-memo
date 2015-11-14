package com.github.daytron.plain_memo.ui;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.contrib.RecyclerViewActions;

import com.github.daytron.plain_memo.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.isDialog;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Espresso test for UI events in the NoteListFragment
 */
class EspressoHelpers {

    private EspressoHelpers(){}

    public static void clickFabNewNoteButtonAndVerifyAction() {
        // Find fab button and click
        onView(withId(R.id.fab_add)).perform(click());

        // Verify the NoteEditFragment is displayed via title EditText widget
        onView((withId(R.id.note_title_edit_text))).check(matches(isDisplayed()));

        // Close soft keyboard if open
        Espresso.closeSoftKeyboard();
    }

    public static void deleteNoteFromView(String title) {
        // Verify the note clicked is the note opened in NoteViewFragment
        onView(withText(title)).check(matches(isDisplayed()));

        // Verify Delete menu item is displayed and click
        onView(withId(R.id.menu_item_delete_note))
                .check(matches(isDisplayed()))
                .perform(click());

        // Confirm delete dialog is shown
        onView(withText(R.string.confirm_dialog_delete_title)).inRoot(isDialog())
                .check(matches(isDisplayed()));

        // Click delete button on delete dialog
        onView(withId(android.R.id.button1)).perform(click());
    }

    public static void openANoteFromListAndVerify(String title) {
        onView(withId(R.id.note_recycler_view))
                .check(matches(isDisplayed()))
                .perform(RecyclerViewActions.actionOnItem(
                        hasDescendant(withText(title)), scrollTo()))
                .perform(RecyclerViewActions.actionOnItem(
                        hasDescendant(withText(title)), click()));

        onView(withText(title)).check(matches(isDisplayed()));
    }

    public static void verifyCurrentScreenIsHome() {
        onView(withContentDescription("More options")).check(matches(isDisplayed()));
    }

    public static void enterATitleFromEditNoteScreenAndSave(String title) {
        // Find EditText title and start typing text into it
        onView(withId(R.id.note_title_edit_text)).perform(typeText(title));

        // Close soft keyboard if open
        Espresso.closeSoftKeyboard();

        // Find save menu button and click
        onView(withId(R.id.menu_item_save_note)).perform(click());
    }

    public static void createNewNoteFromHomeThenStayInNoteViewScreen(String title) {
        verifyCurrentScreenIsHome();
        clickFabNewNoteButtonAndVerifyAction();
        enterATitleFromEditNoteScreenAndSave(title);

        // Verify note created
        onView(withText(title)).check(matches(isDisplayed()));
    }

    public static void openAndDeleteNote(String title) {
        verifyCurrentScreenIsHome();
        openANoteFromListAndVerify(title);

        deleteNoteFromView(title);
    }

    public static void clickEditMenu(String title) {
        // Verify we are in note view of the given title
        onView(withText(title)).check(matches(isDisplayed()));

        // Find edit menu icon is displayed then click it
        onView(withId(R.id.menu_item_edit_note)).perform(click());

        // Verify we are in edit screen
        onView(withText(title)).check(matches(isDisplayed()));

        // Close soft keyboard if open
        Espresso.closeSoftKeyboard();
    }
}
