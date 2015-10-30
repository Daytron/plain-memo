package com.github.daytron.plain_memo.view.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.daytron.plain_memo.R;
import com.github.daytron.plain_memo.database.NoteBook;
import com.github.daytron.plain_memo.model.Note;
import com.github.daytron.plain_memo.view.NoteEditActivity;

import java.util.Calendar;
import java.util.Date;
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
        View v = inflater.inflate(R.layout.fragment_note_view,container,false);

        mTitleTextView = (TextView) v.findViewById(R.id.note_title_text_view);
        mTitleTextView.setAllCaps(false);
        mTitleTextView.setText(mNote.getTitle());

        mBodyTextView = (TextView) v.findViewById(R.id.note_body_text_view);
        mBodyTextView.setAllCaps(false);
        mBodyTextView.setText(mNote.getBody());

        mDateTextView = (TextView) v.findViewById(R.id.note_date_text_view);
        mDateTextView.setAllCaps(false);

        updateDate(mNote.getDate());
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
            callEditFragment(true);
            mNewNote = false;
        }
    }

    private void updateDate(Date date) {
        CharSequence dateText = DateFormat.format("cccc, MMMM d, yyyy   h:mm aa", date);
        mDateTextView.setText(dateText);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(ARG_NOTE_IS_NEW,mNewNote);
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
        if (resultCode == Activity.RESULT_CANCELED ) {
            if (requestCode == REQUEST_NOTE_EDIT) {
                boolean isForDeletion = data.getBooleanExtra(
                        NoteEditFragment.EXTRA_NOTE_FOR_DELETION,false);

                if (isForDeletion) {
                    NoteBook.get(getActivity()).deleteNote(mNote);
                    getActivity().finish();
                }
            }
        } else if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_NOTE_EDIT) {
                // Retrieve new changes
                mNote = NoteBook.get(getActivity()).getNote(mNote.getID());
                String[] stringUpdatedData = data.getStringArrayExtra(
                        NoteEditFragment.EXTRA_NOTE_STRING_VALUES);
                mNote.setTitle(stringUpdatedData[0]);
                mNote.setBody(stringUpdatedData[1]);
                mNote.setDate(Calendar.getInstance().getTime());

                // Apply new changes to views
                mTitleTextView.setText(mNote.getTitle());
                mBodyTextView.setText(mNote.getBody());
                updateDate(mNote.getDate());

                // Update database
                NoteBook.get(getActivity()).updateNote(mNote);
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
            case R.id.menu_item_edit_note:
                // start edit fragment
                callEditFragment(false);
                return true;
            case R.id.menu_item_delete_note :
                NoteBook noteBook = NoteBook.get(getActivity());
                noteBook.deleteNote(mNote);
                getActivity().finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void callEditFragment(boolean isNewNote) {
        Intent intent = NoteEditActivity.newIntent(getActivity(), mNote.getID(), isNewNote);
        startActivityForResult(intent, REQUEST_NOTE_EDIT);
    }

}
