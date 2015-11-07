package com.github.daytron.plain_memo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;

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
            NoteBook.get(this).setIsTwoPane(false);
            Intent intent = NotePagerActivity.newIntent(this, note.getID(), isNewNote);
            startActivity(intent);
        } else {
            NoteBook.get(this).setIsTwoPane(true);
            Fragment newNoteDetail = NoteViewFragment.newInstance(note.getID(), isNewNote);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.detail_fragment_container, newNoteDetail)
                    .commit();
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
}
