package com.github.daytron.plain_memo.view.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.github.daytron.plain_memo.R;
import com.github.daytron.plain_memo.data.GlobalValues;
import com.github.daytron.plain_memo.database.NoteBook;
import com.github.daytron.plain_memo.model.Note;

import java.util.UUID;

/**
 * Created by ryan on 27/10/15.
 */
public class NoteEditFragment extends Fragment {

    public static final String EXTRA_NOTE_FOR_DELETION =
            "com.github.daytron.plain_memo.note_for_deletion";
    public static final String EXTRA_NOTE_IS_NEW =
            "com.github.daytron.plain_memo.note_is_new";
    public static final String EXTRA_NOTE_STRING_VALUES =
            "com.github.daytron.plain_memo.note_values";
    private static final String ARG_NOTE_EDIT_ID = "note_edit_id";
    private static final String ARG_NOTE_IS_NEW = "note_edit_is_new";
    private static final String ARG_NOTE_OFFSET = "note_edit_offset";

    private Note mNote;
    private EditText mTitleField;
    private EditText mBodyField;

    private boolean mNewNote;
    private boolean mUpdated;
    private long mCursorBodyOffset;

    public static NoteEditFragment newInstance(UUID noteId, boolean isNewNote,
                                               long offset) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_NOTE_EDIT_ID, noteId);
        args.putBoolean(ARG_NOTE_IS_NEW, isNewNote);
        args.putLong(ARG_NOTE_OFFSET, offset);

        NoteEditFragment fragment = new NoteEditFragment();
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

        UUID noteId = (UUID) getArguments().getSerializable(ARG_NOTE_EDIT_ID);
        mNewNote = getArguments().getBoolean(ARG_NOTE_IS_NEW);
        mNote = NoteBook.get(getActivity()).getNote(noteId);

        mCursorBodyOffset = getArguments().getLong(ARG_NOTE_OFFSET);

        setHasOptionsMenu(true);
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
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_note_edit, container, false);

        // Change titlebar text
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        ActionBar actionBar = activity.getSupportActionBar();
        String titleText = ((mNewNote) ? getResources().getString(R.string.toolbar_title_new_note):
                getResources().getString(R.string.toolbar_title_edit_note));
        if (actionBar != null) actionBar.setTitle(titleText);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String selectedFontSize = sharedPref
                .getString(getString(R.string.pref_appearance_font_size_key),
                        String.valueOf(GlobalValues.FONT_SIZE_DEFAULT));

        int valueSize = Integer.parseInt(selectedFontSize);
        float fontSize = (float) valueSize;

        mTitleField = (EditText) v.findViewById(R.id.note_title_edit_text);
        mTitleField.setText(mNote.getTitle());
        mTitleField.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);

        mBodyField = (EditText) v.findViewById(R.id.note_body_edit_text);
        mBodyField.setText(mNote.getBody());
        mBodyField.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
        mBodyField.setAllCaps(false);

        if (mNewNote) {
            mTitleField.requestFocus();
        } else {
            mBodyField.requestFocus();
            // Apply offset cursor position from NoteViewFragment
            if (mCursorBodyOffset > 0) {
                mBodyField.setSelection((int) mCursorBodyOffset);
            } else {
                // move cursor in the last position
                // Default behaviour
                mBodyField.setSelection(mBodyField.length());
            }
        }


        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mUpdated = true;
                mNote.setTitle(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mBodyField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mUpdated = true;
                mNote.setBody(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        return v;
    }

    /**
     * Creates a confirm save dialog with three buttons, yes, no and cancel. Each
     * button has predefined functions based on {@link android.content.DialogInterface.OnClickListener}
     * listener object passed as argument.
     *
     * @param listener The DialogInterface.OnClickListener object that can be used as click
     *                 listener for AlertDialog object.
     */
    private void createConfirmSaveDialog(Context context, DialogInterface.OnClickListener listener) {
        final AlertDialog alertDialog = new AlertDialog.Builder(context, R.style.MyAlertDialogStyle)
                .setTitle(R.string.confirm_dialog_save_title)
                .setMessage(R.string.confirm_dialog_save_body)
                .setPositiveButton(R.string.dialog_button_save, listener)
                .setNegativeButton(R.string.dialog_button_delete, listener)
                .setNeutralButton(android.R.string.cancel, listener)
                .create();
        alertDialog.show();
    }

    private void createConfirmCancelDialog(DialogInterface.OnClickListener listener) {
        final AlertDialog alertDialog = new AlertDialog.Builder(
                getActivity(), R.style.MyAlertDialogStyle)
                .setTitle(R.string.confirm_dialog_cancel_title)
                .setMessage(R.string.confirm_dialog_cancel_body)
                .setPositiveButton(R.string.dialog_button_yes, listener)
                .setNegativeButton(R.string.dialog_button_no, listener)
                .create();
        alertDialog.show();
    }

    private void saveNoteFromMenuItem() {
        if (mNote.getTitle() == null || mNote.getTitle().trim().isEmpty()) {
            Toast toast = Toast.makeText(getActivity(), R.string.toast_title_empty,
                    Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, -50);
            toast.show();
        } else {
            sendResult(Activity.RESULT_OK, false);
            getActivity().finish();
        }
    }

    private void showConfirmCancelDialog() {
        if (mNewNote) {
            if (mUpdated) {
                DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                sendResult(Activity.RESULT_CANCELED, true);
                                getActivity().finish();
                                break;
                            default:
                                dialog.cancel();
                                break;
                        }
                    }
                };

                createConfirmCancelDialog(listener);
            } else {
                sendResult(Activity.RESULT_CANCELED, true);
                getActivity().finish();
            }
        } else {
            if (mUpdated) {
                DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                sendResult(Activity.RESULT_CANCELED, false);
                                getActivity().finish();
                                break;
                            default:
                                dialog.cancel();
                                break;
                        }
                    }
                };

                createConfirmCancelDialog(listener);
            } else {
                // If the old note is not updated via field widgets,
                // exit this fragment and do not save in database
                sendResult(Activity.RESULT_CANCELED, false);
                getActivity().finish();
            }
        }
    }

    /**
     * Shows confirm save dialog on various states of this fragment. Accepts boolean flag to
     * distinguish if the call came from pressing back button or from the save menu item button.
     *
     * @param isFromMenuItemPressed boolean flag to differentiate if the method call was made
     *                              either from pressing back button or from the menu save item
     */
    public void showConfirmSaveDialog(Context context) {
        if (mNewNote) {
            if (mNote.getTitle() == null || mNote.getTitle().trim().isEmpty()) {
                if (mNote.getBody() == null || mNote.getBody().trim().isEmpty()) {
                    sendResult(Activity.RESULT_CANCELED, true);
                    getActivity().finish();
                } else {
                    DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    dialog.cancel();

                                    Toast toast = Toast.makeText(getActivity(), R.string.toast_title_empty,
                                            Toast.LENGTH_SHORT);
                                    toast.setGravity(Gravity.CENTER_VERTICAL, 0, -50);
                                    toast.show();
                                    break;
                                case DialogInterface.BUTTON_NEGATIVE:
                                    sendResult(Activity.RESULT_CANCELED, true);
                                    getActivity().finish();
                                default:
                                    dialog.cancel();
                                    break;
                            }
                        }
                    };

                    createConfirmSaveDialog(context, listener);
                }
            } else {
                DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                sendResult(Activity.RESULT_OK, false);
                                getActivity().finish();
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                sendResult(Activity.RESULT_CANCELED, true);
                                getActivity().finish();
                                break;
                            default:
                                dialog.cancel();
                                break;
                        }
                    }
                };

                createConfirmSaveDialog(context, listener);
            }
        } else {
            if (mNote.getTitle().trim().isEmpty()) {
                if (mNote.getBody() == null || mNote.getBody().trim().isEmpty()) {
                    sendResult(Activity.RESULT_CANCELED, false);
                    getActivity().finish();
                } else {
                    DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    dialog.cancel();

                                    Toast toast = Toast.makeText(getActivity(), R.string.toast_title_empty,
                                            Toast.LENGTH_SHORT);
                                    toast.setGravity(Gravity.CENTER_VERTICAL, 0, -50);
                                    toast.show();
                                    break;
                                case DialogInterface.BUTTON_NEGATIVE:
                                    sendResult(Activity.RESULT_CANCELED, false);
                                    getActivity().finish();
                                    break;
                                default:
                                    dialog.cancel();
                                    break;
                            }
                        }
                    };

                    createConfirmSaveDialog(context, listener);
                }
            } else {
                if (mUpdated) {
                    DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    sendResult(Activity.RESULT_OK, false);
                                    getActivity().finish();
                                    break;
                                case DialogInterface.BUTTON_NEGATIVE:
                                    sendResult(Activity.RESULT_CANCELED, false);
                                    getActivity().finish();
                                    break;
                                default:
                                    dialog.cancel();
                                    break;
                            }
                        }
                    };

                    createConfirmSaveDialog(context, listener);
                } else {
                    // If the old note is not updated via field widgets,
                    // exit this fragment and do not save in database
                    sendResult(Activity.RESULT_CANCELED, false);
                    getActivity().finish();
                }
            }
        }
    }

    private void sendResult(int resultCode, boolean forDeletion) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_NOTE_FOR_DELETION, forDeletion);
        intent.putExtra(EXTRA_NOTE_IS_NEW, mNewNote);
        intent.putExtra(EXTRA_NOTE_STRING_VALUES, new String[]{
                mNote.getTitle(), mNote.getBody()
        });
        getActivity().setResult(resultCode, intent);
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
        inflater.inflate(R.menu.fragment_note_edit, menu);
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
            case R.id.menu_item_cancel_note:
                showConfirmCancelDialog();
                return true;
            case R.id.menu_item_save_note:
                saveNoteFromMenuItem();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
