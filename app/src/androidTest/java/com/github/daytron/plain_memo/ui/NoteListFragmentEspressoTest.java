package com.github.daytron.plain_memo.ui;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.github.daytron.plain_memo.NoteListActivity;
import com.github.daytron.plain_memo.R;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Created by ryan on 10/11/15.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class NoteListFragmentEspressoTest {

    @Rule
    public ActivityTestRule<NoteListActivity> mActivityRule =
            new ActivityTestRule<>(NoteListActivity.class);

    @Test
    public void testIfNoteListLoadsProperly() {
        onView(withText("Plain Memo")).check(matches(isDisplayed()));
    }

    @Test
    public void testAddNewNoteViaMenuItem() {
        // Find add note menu item and click
        onView(withContentDescription("More options")).perform(click());
        onView(withText(R.string.new_note)).perform(click());

        // Verify the NoteEditFragment is displayed via title EditText widget
        onView((withId(R.id.note_title_edit_text))).check(matches(isDisplayed()));

        // Make sure to trigger auto delete new note for cleanup
        pressBack();
    }


}
