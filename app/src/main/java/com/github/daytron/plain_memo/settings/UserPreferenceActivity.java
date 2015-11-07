package com.github.daytron.plain_memo.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Toast;

import com.github.daytron.plain_memo.BuildConfig;
import com.github.daytron.plain_memo.R;
import com.github.daytron.plain_memo.data.GlobalValues;
import com.github.daytron.plain_memo.database.NoteBook;
import com.jaredrummler.android.device.DeviceName;

import java.util.Calendar;
import java.util.List;

/**
 * Created by ryan on 02/11/15.
 */
public class UserPreferenceActivity extends AppCompatPreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupToolbarForTwoPane();
    }

    private void setupToolbarForTwoPane() {
        Toolbar toolbar;
        ViewGroup root = (ViewGroup) findViewById(android.R.id.list).getParent().getParent().getParent();
        AppBarLayout appBarLayout = (AppBarLayout)LayoutInflater.from(this)
                .inflate(R.layout.settings_toolbar, root, false);

        root.addView(appBarLayout,0);
        toolbar = (Toolbar) root.findViewById(R.id.setting_toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.action_settings);
    }

    /**
     * This hook is called whenever an item in your options menu is selected.
     * The default implementation simply returns false to have the normal
     * processing happen (calling the item's Runnable or sending a message to
     * its Handler as appropriate).  You can use this method for any items
     * for which you would like to do processing without those other
     * facilities.
     * <p/>
     * <p>Derived classes should call through to the base class for it to
     * perform the default menu handling.</p>
     *
     * @param item The menu item that was selected.
     * @return boolean Return false to allow normal menu processing to
     * proceed, true to consume it here.
     * @see #onCreateOptionsMenu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Called when the activity needs its list of headers build.  By
     * implementing this and adding at least one item to the list, you
     * will cause the activity to run in its modern fragment mode.  Note
     * that this function may not always be called; for example, if the
     * activity has been asked to display a particular fragment without
     * the header list, there is no need to build the headers.
     * <p/>
     * <p>Typical implementations will use {@link #loadHeadersFromResource}
     * to fill in the list from a resource.
     *
     * @param target The list in which to place the headers.
     */
    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.preference_headers, target);
    }

    /**
     * Subclasses should override this method and verify that the given fragment is a valid type
     * to be attached to this activity. The default implementation returns <code>true</code> for
     * apps built for <code>android:targetSdkVersion</code> older than
     * {@link Build.VERSION_CODES#KITKAT}. For later versions, it will throw an exception.
     *
     * @param fragmentName the class name of the Fragment about to be attached to this activity.
     * @return true if the fragment class name is valid for this Activity and false otherwise.
     */
    @Override
    protected boolean isValidFragment(String fragmentName) {
        return UserHeaderFragment.class.getName().equals(fragmentName);
    }

    public static class UserHeaderFragment extends PreferenceFragment implements
            SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);


            String settingsVal = getArguments().getString(getString(R.string.action_settings));
            if (getString(R.string.pref_general).equals(settingsVal)) {
                addPreferencesFromResource(R.xml.settings_general);

                Preference sendFeedbackPreference = findPreference(
                        getString(R.string.pref_general_send_feedback_key)
                );
                sendFeedbackPreference.setOnPreferenceClickListener(
                        new Preference.OnPreferenceClickListener() {
                            @Override
                            public boolean onPreferenceClick(Preference preference) {
                                sendFeedbackEmail();
                                return true;
                            }
                        });

                // Only change settings title in the toolbar if single pane
                if (!NoteBook.get(getActivity()).isTwoPane()) {
                    ((UserPreferenceActivity) getActivity())
                            .getSupportActionBar()
                            .setTitle(getString(R.string.pref_general));
                }


                initChangelogPref();
                initAboutPreference();
            } else {
                addPreferencesFromResource(R.xml.settings_appearance);

                // Only change settings title in the toolbar if single pane
                if (!NoteBook.get(getActivity()).isTwoPane()) {
                    ((UserPreferenceActivity) getActivity())
                            .getSupportActionBar()
                            .setTitle(getString(R.string.pref_appearance));
                }
            }
        }

        /**
         * Disable Changelog preference if the release version is 1, since this is
         * the first release.
         */
        private void initChangelogPref() {
            if (BuildConfig.VERSION_CODE == 1) {
                Preference changelogPref = findPreference(
                        getString(R.string.pref_general_changelog_key)
                );
                changelogPref.setEnabled(false);
            }
        }

        private void initAboutPreference() {
            String copyright = "Copyright \u00a9 " +
                    getString(R.string.app_release_year);
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            if (!getString(R.string.app_release_year).equalsIgnoreCase(
                    Integer.toString(year)
            )) {
                copyright += "-" + calendar;
            }
            copyright += " " + getString(R.string.app_developer);

            String aboutTitle = getString(R.string.app_name) + " " + BuildConfig.VERSION_NAME;

            Preference aboutPref = findPreference(getString(R.string.pref_general_about_key));
            aboutPref.setSummary(copyright);
            aboutPref.setTitle(aboutTitle);
        }

        /**
         * Called when the fragment is visible to the user and actively running.
         * This is generally
         * tied to {@link android.app.Activity#onResume() Activity.onResume} of the containing
         * Activity's lifecycle.
         */
        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);
        }

        /**
         * Called when the Fragment is no longer resumed.  This is generally
         * tied to {@link android.app.Activity#onPause() Activity.onPause} of the containing
         * Activity's lifecycle.
         */
        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);
        }

        /**
         * Called when a shared preference is changed, added, or removed. This
         * may be called even if a preference is set to its existing value.
         * <p/>
         * <p>This callback will be run on your main thread.
         *
         * @param sharedPreferences The {@link SharedPreferences} that received
         *                          the change.
         * @param key               The key of the preference that was changed, added, or
         */
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equalsIgnoreCase(getString(R.string.pref_appearance_font_size_key))) {
                Preference preference = findPreference(key);
                preference.setSummary(((ListPreference) preference).getEntry());
            }
        }

        private void sendFeedbackEmail() {
            String deviceName = DeviceName.getDeviceName();
            String header = getString(R.string.feedback_email_header) + " " +
                    getString(R.string.app_name) + " " + BuildConfig.VERSION_NAME;

            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            String uriString = "mailto:"
                    + Uri.encode(getString(R.string.app_support_email))
                    + "?subject=" + Uri.encode(header) +
                    "&body=" + Uri.encode(getString(R.string.feedback_email_body_hi))
                    + "%0D%0A%0D%0A"
                    + Uri.encode(String.format(getString(R.string.feedback_email_body_content),
                    deviceName, Build.VERSION.RELEASE))
                    + "%0D%0A%0D%0A";
            Uri uri = Uri.parse(uriString);
            emailIntent.setData(uri);

            try {
                startActivity(Intent.createChooser(emailIntent,
                        getString(R.string.feedback_client_selection_title)));
                getActivity().finish();
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(getActivity(),
                        getString(R.string.feedback_email_no_client), Toast.LENGTH_SHORT).show();
            }

        }
    }
}
