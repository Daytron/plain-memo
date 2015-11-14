package com.github.daytron.plain_memo.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.github.daytron.plain_memo.R;
import com.github.daytron.plain_memo.database.NoteBook;
import com.github.daytron.plain_memo.model.Note;
import com.github.daytron.plain_memo.view.fragment.NoteViewFragment;

import java.util.List;
import java.util.UUID;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Activity for hosting {@link NoteViewFragment} via {@link FragmentStatePagerAdapter}.
 */
public class NotePagerActivity extends AppCompatActivity {

    private static final String EXTRA_NOTE_ID =
            "com.github.daytron.plain_memo.note_id";
    private static final String EXTRA_NOTE_IS_NEW =
            "com.github.daytron.plain_memo.note_is_new";

    private ViewPager mViewPager;
    private List<Note> mNotes;

    public static Intent newIntent(Context packageContext, UUID noteId,
                                   boolean isNewNote) {
        Intent intent = new Intent(packageContext, NotePagerActivity.class);
        intent.putExtra(EXTRA_NOTE_ID, noteId);
        intent.putExtra(EXTRA_NOTE_IS_NEW, isNewNote);
        return intent;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Perform initialization of all fragments and loaders.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_pager);

        final UUID noteId = (UUID) getIntent().getSerializableExtra(EXTRA_NOTE_ID);
        final boolean isNewNote = getIntent().getBooleanExtra(EXTRA_NOTE_IS_NEW, false);

        mViewPager = (ViewPager) findViewById(R.id.activity_note_pager_view_pager);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mNotes = NoteBook.get(this).getNotes();
        FragmentManager fragmentManager = getSupportFragmentManager();
        mViewPager.setAdapter(new FragmentStatePagerAdapter(fragmentManager) {
            @Override
            public Fragment getItem(int position) {
                Note note = mNotes.get(position);
                if (noteId.equals(note.getID())) {
                    return NoteViewFragment.newInstance(note.getID(), isNewNote);
                } else {
                    return NoteViewFragment.newInstance(note.getID(), false);
                }

            }

            @Override
            public int getCount() {
                return mNotes.size();
            }
        });

        for (int i = 0; i < mNotes.size(); i++) {
            if (mNotes.get(i).getID().equals(noteId)) {
                mViewPager.setCurrentItem(i);
                break;
            }
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
