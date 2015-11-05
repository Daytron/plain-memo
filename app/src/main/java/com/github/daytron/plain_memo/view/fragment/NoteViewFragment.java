package com.github.daytron.plain_memo.view.fragment;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.format.DateFormat;
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

import com.github.daytron.plain_memo.R;
import com.github.daytron.plain_memo.data.GlobalValues;
import com.github.daytron.plain_memo.database.NoteBook;
import com.github.daytron.plain_memo.model.Note;
import com.github.daytron.plain_memo.util.DateUtil;
import com.github.daytron.plain_memo.view.NoteEditActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by ryan on 27/10/15.
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
    private boolean mSingleTouchEdit;

    public static NoteViewFragment newInstance(UUID noteId, boolean isNewNote) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_NOTE_ID, noteId);
        args.putBoolean(ARG_NOTE_IS_NEW, isNewNote);

        NoteViewFragment fragment = new NoteViewFragment();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Called to do initial creation of a fragment.  This is called after
     * {@link #onAttach(Activity)} and before
     * {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * <p/>
     * <p>Note that this can be called while the fragment's activity is
     * still in the process of being created.  As such, you can not rely
     * on things like the activity's content view hierarchy being initialized
     * at this point.  If you want to do work once the activity itself is
     * created, see {@link #onActivityCreated(Bundle)}.
     *
     * @param savedInstanceState If the fragment is being re-created from
     *                           a previous saved state, this is the state.
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
     * Called to have the fragment instantiate its user interface view.
     * This is optional, and non-graphical fragments can return null (which
     * is the default implementation).  This will be called between
     * {@link #onCreate(Bundle)} and {@link #onActivityCreated(Bundle)}.
     * <p/>
     * <p>If you return a View from here, you will later be called in
     * {@link #onDestroyView} when the view is being released.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate
     *                           any views in the fragment,
     * @param container          If non-null, this is the parent view that the fragment's
     *                           UI should be attached to.  The fragment should not add the view itself,
     *                           but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     * @return Return the View for the fragment's UI, or null.
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_note_view, container, false);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mSingleTouchEdit = sharedPref
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
                if (mSingleTouchEdit) {
                    callEditFragment(false, 0);
                }
            }
        });

        mBodyTextView = (TextView) v.findViewById(R.id.note_body_text_view);
        mBodyTextView.setAllCaps(false);
        mBodyTextView.setText(mNote.getBody());
        mBodyTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
        mBodyTextView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mSingleTouchEdit) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            TextView layout = ((TextView) v);
                            float x = event.getX() + mBodyTextView.getScrollX();
                            float y = event.getY() + mBodyTextView.getScrollY();
                            long offset = layout.getOffsetForPosition(x, y);

                            callEditFragment(false, offset);
                            break;
                    }
                }
                return false;
            }
        });

        mDateTextView = (TextView) v.findViewById(R.id.note_date_text_view);
        mDateTextView.setAllCaps(false);
        mDateTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize -
                GlobalValues.FONT_SIZE_DIFFERENCE);
        mDateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSingleTouchEdit) {
                    callEditFragment(false, 0);
                }
            }
        });

        updateDate();
        return v;
    }

    /**
     * Called when the fragment is visible to the user and actively running.
     * This is generally
     * tied to {@link Activity#onResume() Activity.onResume} of the containing
     * Activity's lifecycle.
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

    private void updateDate() {
        Calendar created = Calendar.getInstance();
        created.setTime(mNote.getDateCreated());
        Calendar edited = Calendar.getInstance();
        edited.setTime(mNote.getDateEdited());

        StringBuilder dateTimeFormatted = new StringBuilder();

        final String SPACE = " ";
        String timeLocaleFormatted;
        if (mNote.isEdited()) {
            timeLocaleFormatted = DateUtil
                    .getTimeStringLocale(getActivity(), mNote.getDateEdited());
            timeLocaleFormatted += SPACE + getString(R.string.note_date_edited);
        } else {
            timeLocaleFormatted = DateUtil
                    .getTimeStringLocale(getActivity(), mNote.getDateCreated());
            timeLocaleFormatted += SPACE + getString(R.string.note_date_created);
        }

        String weekday = DateUtils.formatDateTime(getActivity(),
                mNote.getDateEdited().getTime(),
                DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_ABBREV_WEEKDAY);
        int valueCompareTo = DateUtil.compareToToday(mNote.getDateEdited());
        switch (valueCompareTo) {
            case 0:
                // Today
                dateTimeFormatted.append(weekday)
                        .append(SPACE)
                        .append(getString(R.string.today))
                        .append(SPACE)
                        .append(timeLocaleFormatted);
                break;
            case 1:
                // Yesterday
                dateTimeFormatted.append(weekday)
                        .append(SPACE)
                        .append(getString(R.string.yesterday))
                        .append(SPACE)
                        .append(timeLocaleFormatted);
                break;
            case 2:
                // Last seven days excluding today and yesterday
                // For now case 2 and 3 have the same result format
                String dayAndMonth1 = DateUtils.formatDateTime(getActivity(),
                        mNote.getDateEdited().getTime(),
                        DateUtils.FORMAT_ABBREV_MONTH | DateUtils.FORMAT_NO_YEAR);
                dateTimeFormatted.append(weekday)
                        .append(SPACE)
                        .append(dayAndMonth1)
                        .append(SPACE)
                        .append(timeLocaleFormatted);
                break;
            case 3:
                // Within the same year excluding last seven days
                String dayAndMonth2 = DateUtils.formatDateTime(getActivity(),
                        mNote.getDateEdited().getTime(),
                        DateUtils.FORMAT_ABBREV_MONTH | DateUtils.FORMAT_NO_YEAR);
                dateTimeFormatted.append(weekday)
                        .append(SPACE)
                        .append(dayAndMonth2)
                        .append(SPACE)
                        .append(timeLocaleFormatted);
                break;
            default:
                // Last year
                String dayMonthYear = DateUtils.formatDateTime(getActivity(),
                        mNote.getDateEdited().getTime(),
                        DateUtils.FORMAT_ABBREV_MONTH);
                dateTimeFormatted.append(weekday)
                        .append(SPACE)
                        .append(dayMonthYear)
                        .append(SPACE)
                        .append(timeLocaleFormatted);
        }

        mDateTextView.setText(dateTimeFormatted.toString());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(ARG_NOTE_IS_NEW, mNewNote);
    }

    /**
     * Receive the result from a previous call to
     * {@link #startActivityForResult(Intent, int)}.  This follows the
     * related Activity API as described there in
     * {@link Activity#onActivityResult(int, int, Intent)}.
     *
     * @param requestCode The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who this
     *                    result came from.
     * @param resultCode  The integer result code returned by the child activity
     *                    through its setResult().
     * @param data        An Intent, which can return result data to the caller
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_CANCELED) {
            if (requestCode == REQUEST_NOTE_EDIT) {
                boolean isForDeletion = data.getBooleanExtra(
                        NoteEditFragment.EXTRA_NOTE_FOR_DELETION, false);

                if (isForDeletion) {
                    NoteBook.get(getActivity()).deleteNote(mNote);
                    getActivity().finish();
                }

                Toast.makeText(getActivity(), R.string.toast_note_cancel_save_operation,
                        Toast.LENGTH_SHORT).show();
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
     * Initialize the contents of the Activity's standard options menu.  You
     * should place your menu items in to <var>menu</var>.  For this method
     * to be called, you must have first called {@link #setHasOptionsMenu}.  See
     * {@link Activity#onCreateOptionsMenu(Menu) Activity.onCreateOptionsMenu}
     * for more information.
     *
     * @param menu     The options menu in which you place your items.
     * @param inflater
     * @see #setHasOptionsMenu
     * @see #onPrepareOptionsMenu
     * @see #onOptionsItemSelected
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_note_view, menu);
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
                shareNote();
                return true;
            case R.id.menu_item_edit_note:
                // start edit fragment
                callEditFragment(false, 0);
                return true;
            case R.id.menu_item_delete_note:
                showConfirmDeleteDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

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
        List<ResolveInfo> listOfAppResolveInfo = packageManager.queryIntentActivities(intent,0);

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
                    targetIntent.setClassName(packageName,className);
                    targetIntent.setType("text/plain");
                    targetIntent.putExtra(Intent.EXTRA_TEXT, textData.toString());
                    targetShareIntents.add(targetIntent);
                }
            }
        }

        if(targetShareIntents.size() > 0) {
            Intent chooserIntent = Intent.createChooser(targetShareIntents.remove(
                            targetShareIntents.size() - 1),
                    getResources().getText(R.string.share_to));

            // Populate chooser with new filtered intents
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,
                    targetShareIntents.toArray(new Parcelable[] {}));
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
                        getActivity().finish();
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
