package com.github.daytron.plain_memo.view.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.github.daytron.plain_memo.NoteListActivity;
import com.github.daytron.plain_memo.R;
import com.github.daytron.plain_memo.data.GlobalValues;
import com.github.daytron.plain_memo.database.NoteBook;
import com.github.daytron.plain_memo.model.Note;
import com.github.daytron.plain_memo.util.DateUtil;
import com.github.daytron.plain_memo.view.NoteEditActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

/**
 * Fragment class for viewing individual note.
 */
public class NoteViewFragment extends Fragment {

    private static final String ARG_NOTE_ID = "note_id";
    private static final String ARG_NOTE_IS_NEW = "note_is_new";

    private static final int REQUEST_NOTE_EDIT = 1;

    private Note mNote;
    private TextView mTitleTextView;
    private TextView mDateTextView;
    private TextView mBodyTextView;

    private boolean mNewNote;
    private boolean mSingleTouchEditEnabled;
    private boolean mIsLongPress;

    private Callbacks mCallbacks;

    /**
     * Required interface to bridge communication between {@link NoteViewFragment} and
     * {@link NoteListFragment} objects through {@link com.github.daytron.plain_memo.NoteListActivity}.
     * Necessary for Tablet screen interface.
     */
    public interface Callbacks {
        /**
         * Notify {@link NoteListFragment} to update its list upon note deletion and replace
         * the deleted note fragment with the top most Note item in the list.
         */
        void onNoteDelete();

        /**
         * Keeps {@link android.support.v7.widget.SearchView} expanded with its query result when
         * toolbar is refreshed to keep consistent with its state behavior. A workaround fix when
         * the toolbar is refreshed upon replacing the {@link NoteViewFragment}, resetting the
         * state of the {@link android.support.v7.widget.SearchView}.
         */
        void keepSearchViewExpandedIfPreviouslyExpanded();

        /**
         * Notify {@link NoteListFragment} to try close {@link android.support.v7.widget.SearchView}
         * widget it is currently active and open.
         */
        void tryToCancelSearchQueryOnNewActions();
    }

    public static NoteViewFragment newInstance(UUID noteId, boolean isNewNote) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_NOTE_ID, noteId);
        args.putBoolean(ARG_NOTE_IS_NEW, isNewNote);

        NoteViewFragment fragment = new NoteViewFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private final View.OnLongClickListener mHoldPressedListener = new View.OnLongClickListener() {

        @Override
        public boolean onLongClick(View pView) {
            // Do something when your hold starts here.
            mIsLongPress = true;
            return true;
        }
    };

    private final View.OnTouchListener mTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            view.onTouchEvent(event);
            // When the touch is released.
            if (event.getAction() == MotionEvent.ACTION_UP) {
                // Detect if the view is currently long pressed.
                if (mIsLongPress) {
                    // Do something when the button is released.
                    mIsLongPress = false;
                } else {
                    if (mSingleTouchEditEnabled) {
                        TextView layout = ((TextView) view);
                        float x = event.getX() + mBodyTextView.getScrollX();
                        float y = event.getY() + mBodyTextView.getScrollY();
                        long offset = layout.getOffsetForPosition(x, y);

                        callEditFragment(false, offset);
                    }
                }
            }
            return false;
        }
    };

    /**
     * {@inheritDoc}
     * <p/>
     * Retrieve details of the {@link Note} that was clicked from {@link NoteListFragment} that
     * is about to be displayed.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UUID noteId = (UUID) getArguments().getSerializable(ARG_NOTE_ID);
        mNote = NoteBook.get(getActivity()).getNote(noteId);
        setHasOptionsMenu(true);

        if (savedInstanceState != null) {
            // Retrieve old value of mNewNote when this view is recreated
            // This is used again when this fragment came back to top stack
            // when user press back button from NoteEditFragment
            // IMPORTANT flag to prevent triggering callEditFragment() method in
            // onResume when this fragment comes back on view
            // Ideally the fragment object is save in the stack when on same orientation
            // so no need to save this at all
            // But problem arise if orientation is changed, all views are recreated thus
            // the need to save this boolean value
            mNewNote = savedInstanceState.getBoolean(ARG_NOTE_IS_NEW);
        } else {
            // Retrieve mNewNote from previous fragment (NoteListFragment)
            // for the first time
            mNewNote = getArguments().getBoolean(ARG_NOTE_IS_NEW);
        }
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Inflate the necessary UI widgets for this fragment and apply user preference from the
     * settings. Apply necessary listener behaviors when user tap the title, date or the note itself
     * for the single touch edit feature.
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_note_view, container, false);

        mIsLongPress = false;

        if (!NoteBook.get(getActivity()).isTwoPane()) {
            // Remove titlebar text if single pane only
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            ActionBar actionBar = activity.getSupportActionBar();
            if (actionBar != null) actionBar.setDisplayShowTitleEnabled(false);
        }

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mSingleTouchEditEnabled = sharedPref
                .getBoolean(getString(R.string.pref_appearance_single_tap_edit_key), true);
        String selectedFontSize = sharedPref
                .getString(getString(R.string.pref_appearance_font_size_key),
                        String.valueOf(GlobalValues.FONT_SIZE_DEFAULT));

        int valueSize = Integer.parseInt(selectedFontSize);
        float fontSize = (float) valueSize;

        mTitleTextView = (TextView) v.findViewById(R.id.note_title_text_view);
        mTitleTextView.setAllCaps(false);
        mTitleTextView.setText(mNote.getTitle());
        mTitleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize +
                GlobalValues.FONT_SIZE_DIFFERENCE);
        mTitleTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSingleTouchEditEnabled) {
                    callEditFragment(false, 0);
                }
            }
        });

        mBodyTextView = (TextView) v.findViewById(R.id.note_body_text_view);
        mBodyTextView.setAllCaps(false);
        mBodyTextView.setText(mNote.getBody());
        mBodyTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);

        mBodyTextView.setOnLongClickListener(mHoldPressedListener);
        mBodyTextView.setOnTouchListener(mTouchListener);

        mDateTextView = (TextView) v.findViewById(R.id.note_date_text_view);
        mDateTextView.setAllCaps(false);
        mDateTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize -
                GlobalValues.FONT_SIZE_DIFFERENCE);
        mDateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSingleTouchEditEnabled) {
                    callEditFragment(false, 0);
                }
            }
        });

        updateDate();
        return v;
    }

    /**
     * Retrieve current note displayed.
     *
     * @return {@link Note} currently displayed in two-pane configuration.
     * @see NoteListFragment.Callbacks#getCurrentNoteDisplayedInDetailFragment()
     * @see NoteListActivity#getCurrentNoteDisplayedInDetailFragment()
     */
    public Note getCurrentNoteDisplayedForTwoPane() {
        return mNote;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Initialise Callbacks instance member upon attaching this fragment.
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (NoteBook.get(getActivity()).isTwoPane()) {
            mCallbacks = (Callbacks) context;
        }

    }

    /**
     * {@inheritDoc}
     * <p/>
     * Nullify the Callbacks instance member when this fragment is about to detach.
     */
    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Load {@link NoteEditFragment} into the view when the {@link Note} in question is newly created.
     */
    @Override
    public void onResume() {
        super.onResume();

        // Forward to note edit fragment if request new note
        // Triggered at this stage after all views are initialised, so
        // that when the user return back to this view in the stack through
        // back button, the views are already setup and inflated.
        // Allowing for easy binding new data changes
        if (mNewNote) {
            callEditFragment(true, 0);
            mNewNote = false;
        }
    }

    /**
     * Formats the date displayed in the note view. Displays updated date if the note is updated.
     * Otherwise, created date is displayed instead. The format of the date displayed changes
     * depending on the current date this note is viewed.
     */
    private void updateDate() {
        StringBuilder dateTimeFormatted;

        int valueCompareTo = DateUtil.compareToToday(mNote.getDateEdited());
        switch (valueCompareTo) {
            case 0:
                // Today
                dateTimeFormatted = buildFullDateString(getString(R.string.today));
                break;
            case 1:
                // Yesterday
                dateTimeFormatted = buildFullDateString(getString(R.string.yesterday));
                break;
            case 2:
                // Last seven days excluding today and yesterday
                // For now case 2 and 3 have the same result format
                String dayAndMonth1 = DateUtils.formatDateTime(getActivity(),
                        mNote.getDateEdited().getTime(),
                        DateUtils.FORMAT_ABBREV_MONTH | DateUtils.FORMAT_NO_YEAR);
                dateTimeFormatted = buildFullDateString(dayAndMonth1);
                break;
            case 3:
                // Within the same year excluding last seven days
                String dayAndMonth2 = DateUtils.formatDateTime(getActivity(),
                        mNote.getDateEdited().getTime(),
                        DateUtils.FORMAT_ABBREV_MONTH | DateUtils.FORMAT_NO_YEAR);
                dateTimeFormatted = buildFullDateString(dayAndMonth2);
                break;
            default:
                // Last year
                String dayMonthYear = DateUtils.formatDateTime(getActivity(),
                        mNote.getDateEdited().getTime(),
                        DateUtils.FORMAT_ABBREV_MONTH);
                dateTimeFormatted = buildFullDateString(dayMonthYear);
        }

        mDateTextView.setText(dateTimeFormatted.toString());
    }

    /**
     * Helper method to build the formatted date and time. It determines which date to format.
     *
     * @param dateFormat The formatted {@link String} date only.
     * @return {@link StringBuilder} object, represents the formatted complete date and time.
     */
    private StringBuilder buildFullDateString(String dateFormat) {
        String weekday = DateUtils.formatDateTime(getActivity(),
                mNote.getDateEdited().getTime(),
                DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_ABBREV_WEEKDAY);

        String timeLocaleFormatted;
        if (mNote.isEdited()) {
            timeLocaleFormatted = getString(R.string.note_date_time_format,
                    DateUtil.getTimeStringLocale(getActivity(), mNote.getDateEdited()),
                    getString(R.string.note_date_edited));
        } else {
            timeLocaleFormatted = getString(R.string.note_date_time_format,
                    DateUtil.getTimeStringLocale(getActivity(), mNote.getDateCreated()),
                    getString(R.string.note_date_created));
        }

        StringBuilder builder = new StringBuilder();
        builder.append(getString(
                R.string.note_date_full_format,
                weekday,
                dateFormat,
                timeLocaleFormatted
        ));
        return builder;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Called to ask the fragment to save its current dynamic state, so it
     * can later be reconstructed in a new instance of its process is
     * restarted.
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(ARG_NOTE_IS_NEW, mNewNote);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Handles result returned back from {@link NoteViewFragment} for note saving and deletion.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_CANCELED) {
            if (requestCode == REQUEST_NOTE_EDIT) {
                boolean isForDeletion = data.getBooleanExtra(
                        NoteEditFragment.EXTRA_NOTE_FOR_DELETION, false);

                if (isForDeletion) {
                    NoteBook.get(getActivity()).deleteNote(mNote);

                    // Since in two pane NoteViewFragment and NoteListFragment
                    // share the same activity, no need to destroy the activity.
                    if (NoteBook.get(getActivity()).isTwoPane()) {
                        mCallbacks.onNoteDelete();
                    } else {
                        getActivity().finish();
                    }
                }
            }
        } else if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_NOTE_EDIT) {
                boolean isNewNote = data.getBooleanExtra(
                        NoteEditFragment.EXTRA_NOTE_IS_NEW, false);

                // Retrieve new changes
                mNote = NoteBook.get(getActivity()).getNote(mNote.getID());
                String[] stringUpdatedData = data.getStringArrayExtra(
                        NoteEditFragment.EXTRA_NOTE_STRING_VALUES);
                mNote.setTitle(stringUpdatedData[0]);
                mNote.setBody(stringUpdatedData[1]);

                if (!isNewNote) {
                    // Only update the edited date for old notes
                    mNote.setDateEdited(Calendar.getInstance().getTime());
                }

                // Apply new changes to views
                mTitleTextView.setText(mNote.getTitle());
                mBodyTextView.setText(mNote.getBody());
                updateDate();

                // Update database
                NoteBook.get(getActivity()).updateNote(mNote);

                Toast.makeText(getActivity(), R.string.toast_note_save,
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * {@inheritDoc}
     * <p/>
     * For two-pane configuration, when the toolbar is refreshed, this method is called.
     * Notify {@link NoteListFragment} that the toolbar is refreshed and re-query search if
     * currently active. Otherwise for single pane view, inflate the menu view for single-pane.
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (NoteBook.get(getActivity()).isTwoPane()) {
            mCallbacks.keepSearchViewExpandedIfPreviouslyExpanded();
        } else {
            inflater.inflate(R.menu.menu_note_view, menu);
        }
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
     * perform the default menu handling.
     *
     * @param item The menu item that was selected.
     * @return boolean Return false to allow normal menu processing to
     * proceed, true to consume it here.
     * @see #onCreateOptionsMenu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_share_note:
                cancelSearchQueryFromTwoPane();
                shareNote();
                return true;
            case R.id.menu_item_edit_note:
                cancelSearchQueryFromTwoPane();
                // start edit fragment
                callEditFragment(false, 0);
                return true;
            case R.id.menu_item_delete_note:
                cancelSearchQueryFromTwoPane();
                showConfirmDeleteDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Cancel and close search activity for two-pane view via {@link Callbacks}.
     */
    private void cancelSearchQueryFromTwoPane() {
        if (NoteBook.get(getActivity()).isTwoPane()) {
            mCallbacks.tryToCancelSearchQueryOnNewActions();
        }
    }

    /**
     * Intent behavior for sharing notes.
     */
    private void shareNote() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("text/plain");

        // Prepare text data
        StringBuilder textData = new StringBuilder();
        textData.append(mNote.getTitle())
                .append(". ")
                .append(mNote.getBody());

        PackageManager packageManager = getActivity().getPackageManager();
        // Look for available applications that has ACTION_SEND intent
        List<ResolveInfo> listOfAppResolveInfo = packageManager.queryIntentActivities(intent, 0);

        String packageNameOfAppToHide = "com.github.daytron.plain_memo";
        ArrayList<Intent> targetShareIntents = new ArrayList<>();

        if (!listOfAppResolveInfo.isEmpty()) {
            for (ResolveInfo currentInfo : listOfAppResolveInfo) {
                String packageName = currentInfo.activityInfo.packageName;
                String className = currentInfo.activityInfo.name;

                // Only allow any other apps except this application
                if (!packageNameOfAppToHide.equalsIgnoreCase(packageName)) {
                    Intent targetIntent = new Intent(Intent.ACTION_SEND);
                    targetIntent.setPackage(packageName);
                    targetIntent.setClassName(packageName, className);
                    targetIntent.setType("text/plain");
                    targetIntent.putExtra(Intent.EXTRA_TEXT, textData.toString());
                    targetShareIntents.add(targetIntent);
                }
            }
        }

        if (targetShareIntents.size() > 0) {
            Intent chooserIntent = Intent.createChooser(targetShareIntents.remove(
                            targetShareIntents.size() - 1),
                    getResources().getText(R.string.share_to));

            // Populate chooser with new filtered intents
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,
                    targetShareIntents.toArray(new Parcelable[]{}));
            startActivity(chooserIntent);
        } else {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.share_no_app_found),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void callEditFragment(boolean isNewNote, long offset) {
        Intent intent = NoteEditActivity.newIntent(getActivity(), mNote.getID(), isNewNote,
                offset);
        startActivityForResult(intent, REQUEST_NOTE_EDIT);
    }

    private void showConfirmDeleteDialog() {
        String msgBody = getString(R.string.confirm_dialog_delete_body)
                + " \"" + mNote.getTitle() + "\"?";

        AlertDialog dialog = new AlertDialog.Builder(getActivity(), R.style.MyAlertDialogStyle)
                .setTitle(R.string.confirm_dialog_delete_title)
                .setMessage(msgBody)
                .setPositiveButton(R.string.dialog_button_delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        NoteBook noteBook = NoteBook.get(getActivity());
                        noteBook.deleteNote(mNote);

                        if (NoteBook.get(getActivity()).isTwoPane()) {
                            mCallbacks.onNoteDelete();
                        } else {
                            getActivity().finish();
                        }

                        Toast.makeText(getActivity(), R.string.toast_note_deletion,
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .create();
        dialog.show();
    }

}
