package com.github.daytron.plain_memo.ui;

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
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Created by ryan on 10/11/15.
 */
public class EspressoHelpers {

    private EspressoHelpers(){}

    public static void clickFabNewNoteButtonAndVerifyAction() {
        // Find fab button and click
        onView(withId(R.id.fab_add)).perform(click());

        // Verify the NoteEditFragment is displayed via title EditText widget
        onView((withId(R.id.note_title_edit_text))).check(matches(isDisplayed()));
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

        // Click save button on delete dialog
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
        onView(withText("Plain Memo")).check(matches(isDisplayed()));
    }

    public static void enterATitleFromEditNoteScreenAndSave(String title) {
        // Find EditText title and start typing text into it
        onView(withId(R.id.note_title_edit_text)).perform(typeText(title));

        // Find save menu button and click
        onView(withId(R.id.menu_item_save_note)).perform(click());
    }
}
