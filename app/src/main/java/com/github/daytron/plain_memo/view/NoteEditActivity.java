package com.github.daytron.plain_memo.view;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.github.daytron.plain_memo.R;
import com.github.daytron.plain_memo.view.fragment.NoteEditFragment;

import java.util.UUID;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by ryan on 28/10/15.
 */
public class NoteEditActivity extends SingleFragmentActivity {

    private static final String EXTRA_NOTE_EDIT_ID =
            "com.github.daytron.plain_memo.note_edit_id";
    private static final String EXTRA_NOTE_IS_NEW =
            "com.github.daytron.plain_memo.note_is_new";

    @Override
    protected Fragment createFragment() {
        UUID noteId = (UUID) getIntent().getSerializableExtra(EXTRA_NOTE_EDIT_ID);
        boolean isNewNote = getIntent().getBooleanExtra(EXTRA_NOTE_IS_NEW, false);
        return NoteEditFragment.newInstance(noteId, isNewNote);
    }

    public static Intent newIntent(Context packageContext,  UUID noteId, boolean isNewNote) {
        Intent intent = new Intent(packageContext, NoteEditActivity.class);
        intent.putExtra(EXTRA_NOTE_EDIT_ID, noteId);
        intent.putExtra(EXTRA_NOTE_IS_NEW, isNewNote);
        return intent;
    }

    /**
     * Take care of popping the fragment back stack or finishing the activity
     * as appropriate.
     */
    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

        if (fragment instanceof NoteEditFragment) {
            ((NoteEditFragment) fragment).showConfirmSaveDialog(
                    getSupportActionBar().getThemedContext(), false);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
