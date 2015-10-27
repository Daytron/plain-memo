package com.github.daytron.plain_memo;

import android.support.v4.app.Fragment;

import com.github.daytron.plain_memo.view.SingleFragmentActivity;
import com.github.daytron.plain_memo.view.fragment.NoteListFragment;

public class NoteListActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new NoteListFragment();
    }

}
