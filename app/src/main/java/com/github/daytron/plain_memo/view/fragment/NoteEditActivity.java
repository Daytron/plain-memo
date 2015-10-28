package com.github.daytron.plain_memo.view.fragment;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.github.daytron.plain_memo.view.SingleFragmentActivity;

import java.util.UUID;

/**
 * Created by ryan on 28/10/15.
 */
public class NoteEditActivity extends SingleFragmentActivity {

    private static final String EXTRA_NOTE_EDIT_ID =
            "com.github.daytron.plain_memo.note_edit_id";

    @Override
    protected Fragment createFragment() {
        UUID noteId = (UUID) getIntent().getSerializableExtra(EXTRA_NOTE_EDIT_ID);
        return NoteFragmentEdit.newInstance(noteId);
    }

    public static Intent newIntent(Context packageContext,  UUID noteId) {
        Intent intent = new Intent(packageContext, NoteEditActivity.class);
        intent.putExtra(EXTRA_NOTE_EDIT_ID, noteId);
        return intent;
    }
}
