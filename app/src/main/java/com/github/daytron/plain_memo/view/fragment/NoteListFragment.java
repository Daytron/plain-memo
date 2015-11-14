package com.github.daytron.plain_memo.view.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.daytron.plain_memo.NoteListActivity;
import com.github.daytron.plain_memo.R;
import com.github.daytron.plain_memo.data.GlobalValues;
import com.github.daytron.plain_memo.database.NoteBook;
import com.github.daytron.plain_memo.model.Note;
import com.github.daytron.plain_memo.settings.UserPreferenceActivity;
import com.github.daytron.plain_memo.util.DateUtil;
import com.github.daytron.plain_memo.view.NotePagerActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Fragment class for displaying a list view of {@link Note} objects.
 */
public class NoteListFragment extends Fragment implements SearchView.OnQueryTextListener,
        View.OnClickListener, SearchView.OnCloseListener {

    private final String ARG_QUERY_SEARCH_STRING = "query_search_string";
    private final String ARG_SEARCHVIEW_MENU_EXPANDED = "searchview_menu_expanded";
    private final String ARG_SELECTED_NOTE_HIGHLIGHT = "selected_note_highlight";

    private TextView mEmptyListTextView;
    private RecyclerView mNoteRecyclerView;
    private NoteListAdapter mAdapter;
    private List<Note> mListOfNotes;
    private List<Note> mFilteredNotes;
    private SearchView mSearchView;
    private String mCurrentFilterQuery;

    private boolean mSubtitleVisible;
    private boolean mIsDBClose = false;
    private boolean mTwoPaneKeepSearchbarExpandedTriggered = false;
    private boolean mTwoPaneJustRotatedScreen = false;
    private boolean mTwoPaneClickingNoteToExpandSearch;
    private boolean mSearchViewMenuItemExpanded = false;

    private int mSelectedNotePos = 0;

    private Callbacks mCallbacks;

    /**
     * Required interface for communication to the fragment manager and to the
     * NoteViewFragment for handling small and large screen configurations.
     */
    public interface Callbacks {
        /**
         * Notify {@link android.support.v4.app.FragmentManager} to load the selected note
         * into the view depending on the screen size configuration (Single pane ot Two-Pane setup).
         *
         * @param note      The {@link Note} to view
         * @param isNewNote Boolean flag for handling newly created note
         */
        void onNoteSelected(Note note, boolean isNewNote);

        /**
         * Extract the current note displayed in the Detail layout of Two-Pane setup.
         *
         * @return The {@link Note} extracted
         */
        Note getCurrentNoteDisplayedInDetailFragment();
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Activate options menu for this fragment.
     *
     * @param savedInstanceState If the fragment is being re-created from
     *                           a previous saved state, this is the state.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Inflate the necessary UI widgets for this fragment and apply user preference from the
     * settings. Implement behavior for incoming {@link Intent} for {@link Intent#ACTION_SEND}.
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_note_list, container, false);

        // ContextCompat allows app to choose between pre SDK 23 and above for getColor method
        int bgColor = ContextCompat.getColor(getActivity(), R.color.colorBackgroundNoteListBody);

        LinearLayout contentLinearLayout = (LinearLayout) view.findViewById(R.id.note_linear_layout_bg);
        contentLinearLayout.setBackgroundColor(bgColor);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String selectedFontSize = sharedPref
                .getString(getString(R.string.pref_appearance_font_size_key),
                        String.valueOf(GlobalValues.FONT_SIZE_DEFAULT));

        int valueSize = Integer.parseInt(selectedFontSize);
        float fontSize = (float) valueSize;

        mEmptyListTextView = (TextView) view.findViewById(R.id.note_empty_text_view);
        mEmptyListTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);

        mNoteRecyclerView = (RecyclerView) view.findViewById(R.id.note_recycler_view);
        mNoteRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        updateUI();

        // Detecting intent data from other apps
        Intent intentReceived = getActivity().getIntent();
        String action = intentReceived.getAction();
        String type = intentReceived.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null &&
                "text/plain".equals(type)) {
            String sharedText = intentReceived.getStringExtra(Intent.EXTRA_TEXT);
            if (sharedText != null) {
                Note newNote = new Note();
                newNote.setBody(sharedText);
                NoteBook.get(getActivity()).addNote(newNote);

                Intent intent = NotePagerActivity.newIntent(getActivity(),
                        newNote.getID(), true);
                startActivity(intent);
            }
        }

        return view;
    }

    /**
     * Called when fab icon button has been clicked. Creates a new note.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        Note note = new Note();
        NoteBook.get(getActivity()).addNote(note);

        clearSearchForTwoPane();
        mCallbacks.onNoteSelected(note, true);
    }

    /**
     * Update the UI widgets and bind updated data to reflect user action.
     */
    public void updateUI() {
        // Update list (for new changes)
        mListOfNotes = NoteBook.get(getActivity()).getNotes();

        if (mListOfNotes.isEmpty()) {
            mEmptyListTextView.setVisibility(View.VISIBLE);
        } else {
            mEmptyListTextView.setVisibility(View.GONE);
        }

        if (mAdapter == null) {
            mAdapter = new NoteListAdapter(new ArrayList<>(mListOfNotes));
            mNoteRecyclerView.setAdapter(mAdapter);
        } else {
            if (mFilteredNotes != null &&
                    mSearchView != null &&
                    !mSearchView.isIconified() &&
                    mCurrentFilterQuery != null &&
                    !mCurrentFilterQuery.isEmpty()) {
                // Updates filtered notes list
                // This is for updating the note list when the user
                // created a note in the middle of search query event
                // upon return to this fragment the list is updated
                // Whether the user created a new note or not,
                // The note list is re-filtered
                mFilteredNotes = filter(mListOfNotes, mCurrentFilterQuery);
                mAdapter.setNotes(mFilteredNotes);
            } else {
                mAdapter.setNotes(new ArrayList<>(mListOfNotes));
            }
            mAdapter.notifyDataSetChanged();
        }

        updateSubtitle();
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Initialise Callbacks instance member upon attaching this fragment.
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallbacks = (Callbacks) context;
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
     * Called to ask the fragment to save its current dynamic state, so it
     * can later be reconstructed in a new instance of its process is
     * restarted.
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ARG_QUERY_SEARCH_STRING, mCurrentFilterQuery);

        mSearchViewMenuItemExpanded = !(mSearchView != null && mSearchView.isIconified());
        outState.putBoolean(ARG_SEARCHVIEW_MENU_EXPANDED, mSearchViewMenuItemExpanded);

        if (NoteBook.get(getActivity()).isTwoPane()) {
            outState.putInt(ARG_SELECTED_NOTE_HIGHLIGHT, mSelectedNotePos);
        }
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Re-establishes the save information from last configuration. Any active search will be
     * re-queried to maintain action consistency.
     *
     * @see #onSaveInstanceState(Bundle)
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            // Restore any previous active search filter
            mCurrentFilterQuery = savedInstanceState.getString(ARG_QUERY_SEARCH_STRING);
            mSearchViewMenuItemExpanded = savedInstanceState
                    .getBoolean(ARG_SEARCHVIEW_MENU_EXPANDED);

            if (NoteBook.get(getActivity()).isTwoPane()) {
                mSelectedNotePos = savedInstanceState.getInt(ARG_SELECTED_NOTE_HIGHLIGHT);
                mNoteRecyclerView.scrollToPosition(mSelectedNotePos);
            }
        }
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Apply user settings preference and update UI data upon resuming this fragment into view.
     */
    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mSubtitleVisible = sharedPref
                .getBoolean(getString(R.string.pref_appearance_show_num_notes_key), true);

        updateUI();
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Try to close the database.
     */
    @Override
    public void onPause() {
        super.onPause();
        mIsDBClose = NoteBook.get(getActivity()).closeDatabase();
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Close the database if {@link #onPause()} is not called.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        // Make sure to close database, since onPause is not always
        // guaranteed to be called.
        if (!mIsDBClose) {
            NoteBook.get(getActivity()).closeDatabase();
        }
    }

    /**
     * Inner class responsible for handling and binding data of a single {@link Note} list item UI.
     */
    private class NoteHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private Note mNote;

        private final TextView mTitleTextView;
        private final TextView mDateTextView;
        private final LinearLayout mLinearLayout;

        private final int mSelectedColor;
        private final int mDefaultBgColor;

        public NoteHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            mSelectedColor = ContextCompat.getColor(getActivity(), R.color.colorNoteSelectedItem);
            mDefaultBgColor = ContextCompat.getColor(getActivity(), R.color.colorBackgroundNoteListBody);

            mTitleTextView = (TextView) itemView.findViewById(R.id.list_item_note_title_text_view);
            if (NoteBook.get(getActivity()).isTwoPane()) {
                int imgResource = R.drawable.ic_note_icon_light;
                mTitleTextView.setCompoundDrawablesWithIntrinsicBounds(imgResource, 0, 0, 0);
            } else {
                int imgResource = R.drawable.ic_note_icon;
                mTitleTextView.setCompoundDrawablesWithIntrinsicBounds(imgResource, 0, 0, 0);
            }

            mDateTextView = (TextView) itemView.findViewById(R.id.list_item_note_date_text_view);
            mLinearLayout = (LinearLayout) itemView.findViewById(R.id.list_item_linearlayout);
        }

        /**
         * binds data into the item view.
         *
         * @param note The {@link Note} data to bind.
         */
        public void bindCrime(Note note) {
            SharedPreferences sharedPref = PreferenceManager
                    .getDefaultSharedPreferences(getActivity());
            String selectedFontSize = sharedPref
                    .getString(getString(R.string.pref_appearance_font_size_key),
                            String.valueOf(GlobalValues.FONT_SIZE_DEFAULT));

            int valueSize = Integer.parseInt(selectedFontSize);
            float fontSize = (float) valueSize;

            mTitleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
            mDateTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP,
                    fontSize - GlobalValues.FONT_SIZE_DIFFERENCE);

            mNote = note;
            mTitleTextView.setText(mNote.getTitle());

            int comparedValue = DateUtil.compareToToday(mNote.getDateEdited());

            switch (comparedValue) {
                case 0:
                    // Today
                    mDateTextView.setText(
                            DateUtils.formatDateTime(getActivity(), mNote.getDateCreated().getTime(),
                                    DateUtils.FORMAT_SHOW_TIME)
                    );
                    break;
                case 1:
                    // Yesterday
                    mDateTextView.setText(R.string.yesterday);
                    break;
                case 2:
                    // Last seven days excluding today and yesterday
                    mDateTextView.setText(
                            DateUtils.formatDateTime(getActivity(), mNote.getDateCreated().getTime(),
                                    DateUtils.FORMAT_SHOW_WEEKDAY |
                                            DateUtils.FORMAT_ABBREV_WEEKDAY)
                    );
                    break;
                case 3:
                    // Within the same year excluding last seven days
                    mDateTextView.setText(
                            DateUtils.formatDateTime(getActivity(), mNote.getDateCreated().getTime(),
                                    DateUtils.FORMAT_ABBREV_MONTH | DateUtils.FORMAT_NO_YEAR)
                    );
                    break;
                default:
                    // Last year
                    mDateTextView.setText(
                            DateFormat.getDateFormat(getActivity()).format(mNote.getDateCreated())
                    );
            }
        }

        /**
         * Apply necessary background color to represent item selection and deselection for
         * Two-Pane configuration only.
         *
         * @param isSelected Boolean value if the particular ViewHolder is the selected note.
         */
        public void setSelection(boolean isSelected) {
            if (isSelected) {
                mLinearLayout.setBackgroundColor(mSelectedColor);
                int imgResource = R.drawable.ic_note_icon;
                mTitleTextView.setCompoundDrawablesWithIntrinsicBounds(imgResource, 0, 0, 0);
            } else {
                mLinearLayout.setBackgroundColor(mDefaultBgColor);
                int imgResource = R.drawable.ic_note_icon_light;
                mTitleTextView.setCompoundDrawablesWithIntrinsicBounds(imgResource, 0, 0, 0);
            }
        }

        /**
         * Called when an item view has been clicked.
         *
         * @param v The view that was clicked.
         */
        @Override
        public void onClick(View v) {
            if (NoteBook.get(getActivity()).isTwoPane()) {
                int oldPos = mSelectedNotePos;
                mSelectedNotePos = getLayoutPosition();
                mAdapter.notifyItemChanged(oldPos);
                mAdapter.notifyItemChanged(mSelectedNotePos);

                // trigger flag for auto expand searchbar when loading a detail fragment
                // A workaround for toolbar keeps refreshing whenever a detail fragment is loaded
                mTwoPaneClickingNoteToExpandSearch = !mSearchView.isIconified();
            }

            mCallbacks.onNoteSelected(mNote, false);
        }
    }

    /**
     * Retrieves {@link UUID} of the first {@link Note} from the list.
     *
     * @return UUID object of the first note from the list.
     */
    public UUID getFirstNoteFromList() {
        return ((mAdapter == null) ? null : mAdapter.getFirstItem());
    }

    /**
     * Inner class responsible for handling {@link android.support.v7.widget.RecyclerView.ViewHolder}
     * objects in the {@link RecyclerView}.
     */
    private class NoteListAdapter extends RecyclerView.Adapter<NoteHolder> {

        private List<Note> mNotes;
        private final boolean isMasterDetail;

        public NoteListAdapter(List<Note> notes) {
            mNotes = new ArrayList<>(notes);

            // Save processing expense by saving the device setup config as
            // instance variable, instead of accessing NoteBook
            // every time onBindViewHolder is called
            isMasterDetail = NoteBook.get(getActivity()).isTwoPane();
        }

        public UUID getFirstItem() {
            if (getItemCount() > 0) {
                return mNotes.get(0).getID();
            } else {
                return null;
            }
        }

        public int getPositionByNote(Note note) {
            for (int i = 0; i < mNotes.size(); i++) {
                if (mNotes.get(i).getID().equals(note.getID())) {
                    return i;
                }
            }

            return -1;
        }
        
        /**
         * {@inheritDoc}
         * <p/>
         * Inflates the view for the {@link android.support.v7.widget.RecyclerView.ViewHolder}.
         */
        @Override
        public NoteHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.list_item_note, parent, false);

            return new NoteHolder(view);
        }

        /**
         * {@inheritDoc}
         * <p/>
         * Binds data for each {@link android.support.v7.widget.RecyclerView.ViewHolder}.
         */
        @Override
        public void onBindViewHolder(NoteHolder holder, int position) {
            final Note note = mNotes.get(position);
            holder.bindCrime(note);

            // Only apply item highlight/selection on master detail config
            if (isMasterDetail) {
                holder.setSelection(mSelectedNotePos == position);
            }
        }

        /**
         * Returns the total number of items in the data set hold by the adapter.
         *
         * @return The total number of items in this adapter.
         */
        @Override
        public int getItemCount() {
            return mNotes.size();
        }

        /**
         * Sets a new set of Notes to the {@link RecyclerView}.
         *
         * @param notes List of notes as {@link List} object.
         */
        public void setNotes(List<Note> notes) {
            mNotes = notes;
        }

        /**
         * Remove a note from the list of notes with the given position.
         *
         * @param position The position in the list to remove the note.
         * @return The removed note.
         */
        public Note removeItem(int position) {
            final Note note = mNotes.remove(position);
            notifyItemRemoved(position);
            return note;
        }

        /**
         * Add a note to the list of notes with the given position.
         *
         * @param position The position in the list to add the note.
         * @param note     The note to be added.
         */
        public void addItem(int position, Note note) {
            mNotes.add(position, note);
            notifyItemInserted(position);
        }

        /**
         * Move note from the given fromPosition to toPosition in the notes container, mNotes.
         *
         * @param fromPosition The position to move from.
         * @param toPosition   The position to move to.
         */
        public void moveItem(int fromPosition, int toPosition) {
            final Note note = mNotes.remove(fromPosition);
            mNotes.add(toPosition, note);
            notifyItemMoved(fromPosition, toPosition);
        }

        /**
         * A convenience method to call the three move methods which are process in order. The order
         * in which they are called is important. Unmatched notes are removed at first, then
         * return/re-add any matched note that has been removed from previous action (e.g. different
         * string query) to the list of notes and finally rearranged their positions according to
         * the filtered list.
         *
         * @param notes The filtered list of notes as {@link List} object.
         */
        public void animateTo(List<Note> notes) {
            applyAndAnimateRemovals(notes);
            applyAndAnimateAdditions(notes);
            applyAndAnimateMovedItems(notes);
        }

        /**
         * Iterates through the filtered list and remove any unmatched note from the notes
         * container, mNotes. Iterates starts from the bottom of the list going up to
         * avoid the need to keep track of the offset when moving their positions later on.
         *
         * @param newModels The filtered list of notes as {@link List} object.
         */
        private void applyAndAnimateRemovals(List<Note> newModels) {
            for (int i = mNotes.size() - 1; i >= 0; i--) {
                final Note model = mNotes.get(i);
                if (!newModels.contains(model)) {
                    removeItem(i);
                }
            }
        }

        /**
         * Iterates through the filtered list and verify that the notes container, mNotes
         * does have the note from the filtered list. If not, add it to mNotes.
         *
         * @param newModels The filtered list of notes as {@link List} object.
         */
        private void applyAndAnimateAdditions(List<Note> newModels) {
            for (int i = 0, count = newModels.size(); i < count; i++) {
                final Note model = newModels.get(i);
                if (!mNotes.contains(model)) {
                    addItem(i, model);
                }
            }
        }

        /**
         * Detects the difference between filtered notes and original notes in the position of
         * the notes and move the note items from their original positions to their respective
         * filtered positions.
         * <p/>
         * It is imperative to call this method after which {@link #applyAndAnimateRemovals(List)}
         * and {@link #applyAndAnimateAdditions(List)} methods are called first. Moving notes
         * without calling these methods first can be fatal to the whole operation of filtering.
         *
         * @param newModels The filtered list of notes that is needed to retrieve the required
         *                  positions as {@link List} object.
         */
        private void applyAndAnimateMovedItems(List<Note> newModels) {
            for (int toPosition = newModels.size() - 1; toPosition >= 0; toPosition--) {
                final Note note = newModels.get(toPosition);
                final int fromPosition = mNotes.indexOf(note);
                if (fromPosition >= 0 && fromPosition != toPosition) {
                    moveItem(fromPosition, toPosition);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * Attaches the {@link SearchView} into the {@link android.support.v7.widget.Toolbar}. Re-apply
     * filter state from its last active state from the last configuration.
     *
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (!NoteBook.get(getActivity()).isTwoPane()) {
            inflater.inflate(R.menu.menu_note_list, menu);
        }

        // Get the SearchView and set its listener
        final MenuItem searchViewItem = menu.findItem(R.id.menu_item_search_note);
        mSearchView = (SearchView) MenuItemCompat.getActionView(searchViewItem);
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setOnCloseListener(this);

        // Apply search filter when configuration is changed (e.g. screen rotation)
        // only when searchview is active from previous configuration or screen
        if (mCurrentFilterQuery != null && mSearchViewMenuItemExpanded) {
            if (NoteBook.get(getActivity()).isTwoPane()) {
                mTwoPaneJustRotatedScreen = true;
            }

            mSearchView.setQuery(mCurrentFilterQuery, false);
            mSearchView.setFocusable(true);
            mSearchView.setIconified(false);
            mSearchView.requestFocusFromTouch();

            if (NoteBook.get(getActivity()).isTwoPane()) {
                // Select back to its previous selection from its last screen orientation
                mAdapter.notifyItemChanged(mSelectedNotePos);
                mNoteRecyclerView.scrollToPosition(mSelectedNotePos);
                mTwoPaneJustRotatedScreen = false;
            }
        }
    }

    /**
     * Re-establish search state when user click a list item in the Two-Pane configuration.
     *
     * @see NoteViewFragment.Callbacks#keepSearchViewExpandedIfPreviouslyExpanded()
     * @see NoteListActivity#keepSearchViewExpandedIfPreviouslyExpanded()
     */
    public void reQuerySearchViewUponClickingFilteredNote() {
        if (mSearchView.isIconified() && mTwoPaneClickingNoteToExpandSearch) {
            mTwoPaneKeepSearchbarExpandedTriggered = true;

            mSearchView.setFocusable(true);
            mSearchView.setIconified(false);
            mSearchView.setQuery(mCurrentFilterQuery, false);
            mSearchView.requestFocusFromTouch();

            mTwoPaneKeepSearchbarExpandedTriggered = false;
        }
    }

    /**
     * The user is attempting to close the SearchView.
     *
     * @return true if the listener wants to override the default behavior of clearing the
     * text field and dismissing it, false otherwise.
     */
    @Override
    public boolean onClose() {
        mSearchViewMenuItemExpanded = false;

        return false;
    }

    /**
     * Called when the user submits the query. This could be due to a key press on the
     * keyboard or due to pressing a submit button.
     * The listener can override the standard behavior by returning true
     * to indicate that it has handled the submit request. Otherwise return false to
     * let the SearchView handle the submission by launching any associated intent.
     *
     * @param query the query text that is to be submitted
     * @return true if the query has been handled by the listener, false to let the
     * SearchView perform the default action.
     */
    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    /**
     * Called when the query text is changed by the user.
     *
     * For single pane view, the filter applies normally. But for two-pane view, it also handles
     * item highlight selection.
     *
     * @param newText the new content of the query text field.
     * @return false if the SearchView should perform the default action of showing any
     * suggestions if available, true if the action was handled by the listener.
     */
    @Override
    public boolean onQueryTextChange(String newText) {
        // Remove the previous highlight when filtering only if triggered by the user manually
        // Some events requires to keep the state of the selection e.g. screen rotation which
        // automatically calls this method using setQuery method and keeping searchbar expanded
        // when a detail fragment is loaded where the searchbar is previously active.
        // Note: loading a detail fragment causes for toolbar to be reloaded, loosing its previous
        // state
        if (NoteBook.get(getActivity()).isTwoPane() &&
                !mTwoPaneKeepSearchbarExpandedTriggered &&
                !mTwoPaneJustRotatedScreen) {
            int oldPos = mSelectedNotePos;
            mSelectedNotePos = -1;
            mAdapter.notifyItemChanged(oldPos);
        }

        mCurrentFilterQuery = newText.trim();
        mFilteredNotes = filter(mListOfNotes, mCurrentFilterQuery);
        mAdapter.animateTo(mFilteredNotes);

        // Do not trigger when trying to regain SearchView's state through
        // reQuerySearchViewUponClickingFilteredNote() method and
        // when rotating screen with SearchView expanded
        if (!mTwoPaneKeepSearchbarExpandedTriggered &&
                !mTwoPaneJustRotatedScreen) {
            mNoteRecyclerView.scrollToPosition(0);
        }

        if (NoteBook.get(getActivity()).isTwoPane() &&
                !mTwoPaneKeepSearchbarExpandedTriggered &&
                !mTwoPaneJustRotatedScreen) {
            // Handles item highlight when search query is empty.
            // Select and highlight the first item after refreshing the list
            if (newText.trim().isEmpty()) {
                int pos = getPositionOfDetailedFragment();
                if (pos > -1) {
                    mSelectedNotePos = pos;
                    mAdapter.notifyItemChanged(pos);
                    mNoteRecyclerView.scrollToPosition(pos);
                }
            }
        }

        updateSubtitle();
        return true;
    }

    private int getPositionOfDetailedFragment() {
        if (mCallbacks != null) {
            Note noteDisplayed = mCallbacks.getCurrentNoteDisplayedInDetailFragment();
            if (noteDisplayed != null) {
                return mAdapter.getPositionByNote(noteDisplayed);
            }
        }

        return -1;
    }

    /**
     * For two-pane configuration, scrolls to the first item and select it.
     *
     * @see NoteViewFragment.Callbacks#onNoteDelete()
     * @see NoteListActivity#onNoteDelete()
     */
    public void scrollToFirstItem() {
        if (mAdapter.getItemCount() > 0) {
            mNoteRecyclerView.scrollToPosition(0);

            // Prepare to be automatically highlighted
            int oldPos = mSelectedNotePos;
            mSelectedNotePos = 0;
            mAdapter.notifyItemChanged(mSelectedNotePos);
            mAdapter.notifyItemChanged(oldPos);
        }
    }

    /**
     * For two-pane configuration, scrolls to the last item and select it.
     *
     * @see com.github.daytron.plain_memo.view.fragment.NoteListFragment.Callbacks#onNoteSelected(Note, boolean)
     * @see NoteListActivity#onNoteSelected(Note, boolean)
     */
    public void scrollToLastItem() {
        int totalItems = mAdapter.getItemCount();
        if (totalItems > 0) {
            mNoteRecyclerView.scrollToPosition(totalItems - 1);

            // Prepare to be automatically highlighted
            int oldPos = mSelectedNotePos;
            mSelectedNotePos = totalItems - 1;
            mAdapter.notifyItemChanged(mSelectedNotePos);
            mAdapter.notifyItemChanged(oldPos);
        }
    }

    /**
     * Filter the list based on the string entered by the user via {@link SearchView} widget
     * in the toolbar. Filter only by letter and note case sensitive. Any letter match in the
     * note's title is displayed in the filtered view.
     *
     * @param notes       The list of notes to be filtered as {@link List} object.
     * @param queryString The query search text as {@link String}.
     * @return The filtered list as {@link List} object.
     */
    private List<Note> filter(List<Note> notes, String queryString) {
        queryString = queryString.toLowerCase();

        final List<Note> filteredNoteList = new ArrayList<>();
        for (Note note : notes) {
            if (note.getTitle() == null) {
                continue;
            }
            final String titleText = note.getTitle().toLowerCase();
            if (titleText.contains(queryString)) {
                filteredNoteList.add(note);
            }
        }
        return filteredNoteList;
    }

    /**
     * Use to cancel search query for new actions such as add new note, delete, edit and etc
     * (for two-pane configuration only).
     */
    public void clearSearchForTwoPane() {
        if (NoteBook.get(getActivity()).isTwoPane()) {
            // Make sure this is false, to keep SearchView close when new
            // detail is loaded after deleting the current detail fragment,
            // that will call reQuerySearchViewUponClickingFilteredNote() method
            mTwoPaneClickingNoteToExpandSearch = false;

            // Clear any filtered note by requerying empty string
            onQueryTextChange("");

            // Try closing the SearchView
            mSearchView.setIconified(true);
            mSearchView.onActionViewCollapsed();
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
            case R.id.menu_item_new_note:
                Note note = new Note();
                NoteBook.get(getActivity()).addNote(note);

                clearSearchForTwoPane();

                mCallbacks.onNoteSelected(note, true);
                return true;
            case R.id.action_settings:
                clearSearchForTwoPane();
                Intent intentPreference = new Intent(getActivity(), UserPreferenceActivity.class);
                startActivity(intentPreference);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateSubtitle() {
        int noteCount = mAdapter.getItemCount();
        String subtitle;

        if (!mSubtitleVisible) {
            subtitle = null;
        } else if (noteCount == 0) {
            subtitle = getResources().getString(R.string.zero_notes);
        } else {
            subtitle = getResources().getQuantityString(R.plurals.subtitle_plural,
                    noteCount, noteCount);
        }

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar != null) actionBar.setSubtitle(subtitle);
    }
}
