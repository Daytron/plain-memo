package com.github.daytron.plain_memo.view.fragment;

import android.app.Activity;
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
 * Fragment class for editing note.
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
     * {@inheritDoc}
     * <p/>
     * Retrieve details of the {@link Note} that was viewed from {@link NoteViewFragment} that
     * is about to be edited.
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
     * {@inheritDoc}
     * <p/>
     * Inflate the necessary UI widgets for this fragment and apply user preference from the
     * settings. Apply necessary listener behaviors when user entered text.
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_note_edit, container, false);

        // Change titlebar text
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        ActionBar actionBar = activity.getSupportActionBar();
        String titleText = ((mNewNote) ? getResources().getString(R.string.toolbar_title_new_note) :
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
        mTitleField.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize
                + GlobalValues.FONT_SIZE_DIFFERENCE);

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
     * Creates a confirm discard note dialog with two action buttons, yes and no. Each
     * button has predefined functions based on {@link android.content.DialogInterface.OnClickListener}
     * listener object passed as argument.
     *
     * @param listener              Custom listener for various behaviour of the dialog action buttons.
     * @param resourceDialogMessage String resource as the dialog content text.
     */
    private void createConfirmDiscardDialog(DialogInterface.OnClickListener listener,
                                            int resourceDialogMessage) {
        final AlertDialog alertDialog = new AlertDialog.Builder(
                getActivity(), R.style.MyAlertDialogStyle)
                .setMessage(resourceDialogMessage)
                .setPositiveButton(R.string.dialog_button_yes, listener)
                .setNegativeButton(R.string.dialog_button_no, listener)
                .create();
        alertDialog.show();
    }

    /**
     * Initiate save note process via menu item. If no title is found, belay save and
     * notify user. Otherwise, set result to save note.
     */
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

    /**
     * Initiate discard note process via menu item. Pressing no button in dialog, cancels the dialog.
     * Pressing yes triggers various behaviours depending on the state of the note. The
     * following are the behaviours outline for pressing discard button in menu item:
     * <p/>
     * <ul>
     * <li>If it is a new note and it is not updated,
     * set result to discard the new note and return to home.</li>
     * <li>If it is a new note and it is updated, it launches discard dialog.
     * Pressing yes sets result to discard the new note and return to home screen.</li>
     * <li>If it is an old note and note is not updated, return back to note view screen.</li>
     * <li>If it is an old note and note is updated, it launches discard dialog.
     * Pressing yes sets result to  discard new changes and revert to its previous state.
     * Return to note view screen.</li>
     * </ul>
     */
    private void discardNoteFromMenuItem() {
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

                createConfirmDiscardDialog(listener,
                        R.string.confirm_dialog_discard_new_note);
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

                createConfirmDiscardDialog(listener,
                        R.string.confirm_dialog_discard_changes);
            } else {
                // If the old note is not updated via field widgets,
                // exit this fragment and do not save in database
                sendResult(Activity.RESULT_CANCELED, false);
                getActivity().finish();
            }
        }
    }

    /**
     * Initiate discard note process via back press. Pressing no button in dialog, cancels the dialog.
     * Pressing yes triggers various behaviours depending on the state of the note. The
     * following are the behaviours outline for pressing back button:
     * <p/>
     * <ul>
     * <li>If it is a new note and it is not updated,
     * set result to discard the new note and return to home.</li>
     * <li>If it is a new note and it is updated with no title, it launches discard dialog.
     * Pressing yes sets result to discard the new note and return to home screen.</li>
     * <li>If it is a new note and it is updated with title, Set the result to save.</li>
     * <li>If it is an old note and note is not updated, return back to note view screen.</li>
     * <li>If it is an old note and note is updated with no title, it launches discard dialog.
     * Pressing yes sets result to  discard new changes and revert to its previous state.
     * Return to note view screen.</li>
     * <li>If it is an old note and note is updated with title, set result to save.</li>
     * </ul>
     */
    public void discardNoteFromBackButton() {
        if (mNewNote) {
            if (mUpdated) {
                if (mNote.getTitle() == null || mNote.getTitle().trim().isEmpty()) {
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

                    createConfirmDiscardDialog(listener,
                            R.string.confirm_dialog_discard_new_note);
                } else {
                    sendResult(Activity.RESULT_OK, false);
                    getActivity().finish();
                }
            } else {
                sendResult(Activity.RESULT_CANCELED, true);
                getActivity().finish();
            }
        } else {
            if (mUpdated) {
                if (mNote.getTitle().trim().isEmpty()) {
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

                    createConfirmDiscardDialog(listener,
                            R.string.confirm_dialog_discard_changes);
                } else {
                    sendResult(Activity.RESULT_OK, false);
                    getActivity().finish();
                }
            } else {
                // If the old note is not updated via field widgets,
                // exit this fragment and do not save in database
                sendResult(Activity.RESULT_CANCELED, false);
                getActivity().finish();
            }

        }
    }

    /**
     * Apply result based on result code and boolean flag for note deletion parameters given.
     *
     * @param resultCode  The result code
     * @param forDeletion boolean flag for note deletion. True if the note is to be deleted.
     *                    Otherwise, false.
     */
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
     * {@inheritDoc}
     * <p/>
     * Inflate the menu options for this fragment.
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_note_edit, menu);
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
            case R.id.menu_item_discard_note:
                discardNoteFromMenuItem();
                return true;
            case R.id.menu_item_save_note:
                saveNoteFromMenuItem();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
