package com.github.daytron.plain_memo;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;

import com.github.daytron.plain_memo.view.SingleFragmentActivity;
import com.github.daytron.plain_memo.view.fragment.NoteListFragment;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class NoteListActivity extends SingleFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load default settings values
        PreferenceManager.setDefaultValues(this, R.xml.settings_general,false);
    }

    @Override
    protected Fragment createFragment() {
        return new NoteListFragment();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
