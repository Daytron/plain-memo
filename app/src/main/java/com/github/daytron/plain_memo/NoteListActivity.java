package com.github.daytron.plain_memo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.Menu;

import com.github.daytron.plain_memo.database.NoteBook;
import com.github.daytron.plain_memo.model.Note;
import com.github.daytron.plain_memo.view.NotePagerActivity;
import com.github.daytron.plain_memo.view.SingleFragmentActivity;
import com.github.daytron.plain_memo.view.fragment.NoteListFragment;
import com.github.daytron.plain_memo.view.fragment.NoteViewFragment;

import java.util.UUID;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class NoteListActivity extends SingleFragmentActivity
        implements NoteListFragment.Callbacks, NoteViewFragment.Callbacks {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load default settings values
        PreferenceManager.setDefaultValues(this, R.xml.settings_general, false);
    }

    @Override
    protected Fragment createFragment() {
        return new NoteListFragment();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_masterdetail;
    }

    @Override
    public void onNoteSelected(Note note, boolean isNewNote) {
        if (findViewById(R.id.detail_fragment_container) == null) {
            Intent intent = NotePagerActivity.newIntent(this, note.getID(), isNewNote);
            startActivity(intent);
        } else {
            Fragment newNoteDetail = NoteViewFragment.newInstance(note.getID(), isNewNote);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.detail_fragment_container, newNoteDetail)
                    .commit();

            // Auto scroll to last item when creating a new note. A new note is placed
            // at the bottom by the Adapter.
            if (isNewNote) {
                NoteListFragment listFragment = (NoteListFragment)
                        getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                listFragment.updateUI();
                listFragment.scrollToLastItem();
            }
        }
    }

    @Override
    public void onNoteDelete() {
        // Update list
        NoteListFragment listFragment = (NoteListFragment)
                getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        listFragment.updateUI();

        // Remove the deleted note's fragment
        NoteViewFragment fragment = (NoteViewFragment)
                getSupportFragmentManager().findFragmentById(R.id.detail_fragment_container);
        getSupportFragmentManager().beginTransaction().remove(fragment).commit();

        // Set first note in the list as the new detail pane note
        UUID noteId = listFragment.getFirstNoteFromList();
        if (noteId != null) {
            Fragment replacedNote = NoteViewFragment.newInstance(noteId, false);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.detail_fragment_container, replacedNote)
                    .commit();
            // Then auto scroll to first position
            listFragment.scrollToFirstItem();
        }
    }

    @Override
    public void tryToCancelSearchQueryOnNewActions() {
        NoteListFragment listFragment = (NoteListFragment)
                getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        listFragment.clearSearchForTwoPane();
    }

    @Override
    public Note getCurrentNoteDisplayedInDetailFragment() {
        NoteViewFragment viewFragment = (NoteViewFragment)
                getSupportFragmentManager().findFragmentById(R.id.detail_fragment_container);
        return (viewFragment == null) ? null : viewFragment.getCurrentNoteDisplayedForTwoPane();
    }

    /**
     * Initialize the contents of the Activity's standard options menu.  You
     * should place your menu items in to <var>menu</var>.
     * <p/>
     * <p>This is only called once, the first time the options menu is
     * displayed.  To update the menu every time it is displayed, see
     * {@link #onPrepareOptionsMenu}.
     * <p/>
     * <p>The default implementation populates the menu with standard system
     * menu items.  These are placed in the {@link Menu#CATEGORY_SYSTEM} group so that
     * they will be correctly ordered with application-defined menu items.
     * Deriving classes should always call through to the base implementation.
     * <p/>
     * <p>You can safely hold on to <var>menu</var> (and any items created
     * from it), making modifications to it as desired, until the next
     * time onCreateOptionsMenu() is called.
     * <p/>
     * <p>When you add items to the menu, you can implement the Activity's
     * {@link #onOptionsItemSelected} method to handle them there.
     *
     * @param menu The options menu in which you place your items.
     * @return You must return true for the menu to be displayed;
     * if you return false it will not be shown.
     * @see #onPrepareOptionsMenu
     * @see #onOptionsItemSelected
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (NoteBook.get(this).isTwoPane()) {
            getMenuInflater().inflate(R.menu.menu_two_pane, menu);
            return true;
        } else {
            return super.onCreateOptionsMenu(menu);
        }
    }

    @Override
    public void keepSearchViewExpandedIfPreviouslyExpanded() {
        NoteListFragment listFragment = (NoteListFragment)
                getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        listFragment.reQuerySearchViewUponClickingFilteredNote();
    }
}
