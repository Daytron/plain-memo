package com.github.daytron.plain_memo.view;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.github.daytron.plain_memo.R;
import com.github.daytron.plain_memo.database.NoteBook;
import com.github.daytron.plain_memo.model.Note;
import com.github.daytron.plain_memo.view.fragment.NoteListFragment;
import com.github.daytron.plain_memo.view.fragment.NoteViewFragment;


/**
 * Abstract class for handling fragments to its subclasses Activities.
 */
public abstract class SingleFragmentActivity extends AppCompatActivity {

    protected abstract Fragment createFragment();

    @LayoutRes
    public int getLayoutResId() {
        return R.layout.activity_fragment;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResId());

        boolean isTwoPane = false;

        if (findViewById(R.id.detail_fragment_container) != null) {
            isTwoPane = true;
            NoteBook.get(this).setIsTwoPane(true);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (findViewById(R.id.fragment_container) != null) {

            FragmentManager fm = getSupportFragmentManager();
            Fragment fragment = fm.findFragmentById(R.id.fragment_container);

            if (savedInstanceState == null) {
                if (fragment == null) {
                    fragment = createFragment();

                    fm.beginTransaction()
                            .add(R.id.fragment_container, fragment)
                            .commit();

                    // Auto load first note into the detail fragment container
                    if (isTwoPane) {
                        Note firstNote = NoteBook.get(this).getFirstNote();

                        if (firstNote != null) {
                            Fragment replacedNote = NoteViewFragment
                                    .newInstance(firstNote.getID(), false);
                            fm.beginTransaction()
                                    .replace(R.id.detail_fragment_container, replacedNote)
                                    .commit();
                        }
                    }
                }
            }

            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_add);
            fab.setOnClickListener((NoteListFragment) fragment);
        }
    }
}
