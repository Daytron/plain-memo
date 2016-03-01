package com.github.daytron.plain_memo.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Toast;

import com.github.daytron.plain_memo.BuildConfig;
import com.github.daytron.plain_memo.R;
import com.github.daytron.plain_memo.database.NoteBook;
import com.jaredrummler.android.device.DeviceName;

import java.util.Calendar;
import java.util.List;

/**
 * Preference activity for handling settings view.
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

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.preference_headers, target);
    }

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
         * the first production release.
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
            StringBuilder aboutStringBuilder = new StringBuilder();
            aboutStringBuilder
                    .append("Copyright © ");

            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);

            aboutStringBuilder.append(year)
                    .append(" ")
                    .append(getString(R.string.app_developer));

            String aboutTitle = getString(R.string.app_name) + " " + BuildConfig.VERSION_NAME;

            Preference aboutPref = findPreference(getString(R.string.pref_general_about_key));
            aboutPref.setSummary(aboutStringBuilder.toString());
            aboutPref.setTitle(aboutTitle);
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equalsIgnoreCase(getString(R.string.pref_appearance_font_size_key))) {
                Preference preference = findPreference(key);
                preference.setSummary(((ListPreference) preference).getEntry());
            }
        }

        /**
         * Implement intent for sending feedback.
         */
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

            PackageManager packageManager = getActivity().getPackageManager();
            // Look for available applications that has ACTION_SENDTO intent
            List<ResolveInfo> listOfAppResolveInfo = packageManager
                    .queryIntentActivities(emailIntent, 0);

            if (!listOfAppResolveInfo.isEmpty()) {
                try {
                    startActivity(Intent.createChooser(emailIntent,
                            getString(R.string.feedback_client_selection_title)));
                    getActivity().finish();
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(getActivity(),
                            getString(R.string.feedback_email_no_client), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getActivity(),
                        getString(R.string.feedback_email_no_client), Toast.LENGTH_SHORT).show();
            }


        }
    }
}
