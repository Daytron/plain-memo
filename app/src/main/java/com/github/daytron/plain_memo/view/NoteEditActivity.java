package com.github.daytron.plain_memo.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.github.daytron.plain_memo.R;
import com.github.daytron.plain_memo.view.fragment.NoteEditFragment;

import java.util.UUID;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Host Activity class for {@link NoteEditFragment}
 */
public class NoteEditActivity extends AppCompatActivity {

    private static final String EXTRA_NOTE_EDIT_ID =
            "com.github.daytron.plain_memo.note_edit_id";
    private static final String EXTRA_NOTE_IS_NEW =
            "com.github.daytron.plain_memo.note_is_new";
    private static final String EXTRA_NOTE_IS_FOR_BODY_OFFSET =
            "com.github.daytron.plain_memo.note_is_for_body_offset";
    private static final String EXTRA_NOTE_TOUCH_OFFSET =
            "com.github.daytron.plain_memo.note_touch_offset";

    /**
     * {@inheritDoc}
     * <p/>
     * Load the {@link NoteEditFragment} into the view.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_edit);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (findViewById(R.id.fragment_container) != null) {
            FragmentManager fm = getSupportFragmentManager();
            Fragment fragment = fm.findFragmentById(R.id.fragment_container);

            if (savedInstanceState == null) {
                if (fragment == null) {
                    UUID noteId = (UUID) getIntent().getSerializableExtra(EXTRA_NOTE_EDIT_ID);
                    boolean isNewNote = getIntent().getBooleanExtra(EXTRA_NOTE_IS_NEW, false);
                    boolean isForBodyOffset = getIntent()
                            .getBooleanExtra(EXTRA_NOTE_IS_FOR_BODY_OFFSET, false);
                    long offset = getIntent().getLongExtra(EXTRA_NOTE_TOUCH_OFFSET, 0);
                    fragment = NoteEditFragment.newInstance(noteId, isNewNote, isForBodyOffset,
                            offset);

                    fm.beginTransaction()
                            .add(R.id.fragment_container, fragment)
                            .commit();
                }
            }
        }
    }

    /**
     * Static helper method for building the Intent to start this activity.
     *
     * @param packageContext The application context
     * @param noteId         id of the note as UUID object
     * @param isNewNote      boolean flag if the note is newly created (true) or not (false)
     * @param offset         Optional touch position offset use to place the edit cursor in the EditText
     * @return Intent built
     */
    public static Intent newIntent(Context packageContext, UUID noteId, boolean isNewNote,
                                   boolean isForBodyOffset, long offset) {
        Intent intent = new Intent(packageContext, NoteEditActivity.class);
        intent.putExtra(EXTRA_NOTE_EDIT_ID, noteId);
        intent.putExtra(EXTRA_NOTE_IS_NEW, isNewNote);
        intent.putExtra(EXTRA_NOTE_IS_FOR_BODY_OFFSET, isForBodyOffset);
        intent.putExtra(EXTRA_NOTE_TOUCH_OFFSET, offset);
        return intent;
    }

    /**
     * Takes care of handling unsaved note in the editor view.
     */
    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

        if (fragment instanceof NoteEditFragment) {
            ((NoteEditFragment) fragment).discardNoteFromBackButton();
        } else {
            super.onBackPressed();
        }
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Attach Calligraphy for this activity.
     */
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
