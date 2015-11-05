package com.github.daytron.plain_memo.view.fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
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
import android.widget.ListView;
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

/**
 * Created by ryan on 27/10/15.
 */
public class NoteListFragment extends Fragment implements SearchView.OnQueryTextListener {

    private final String ARG_QUERY_SEARCH_STRING = "query_search_string";
    private final String ARG_SEARCHVIEW_MENU_EXPANDED = "searchview_menu_expanded";

    private LinearLayout mContentLinearLayout;
    private TextView mEmptyTextView;
    private RecyclerView mNoteRecyclerView;
    private NoteListAdapter mAdapter;
    private List<Note> mListOfNotes;
    private List<Note> mFilteredNotes;
    private SearchView mSearchView;
    private String mCurrentFilterQuery;

    private boolean mSubtitleVisible;
    private boolean isDBClose = false;
    private boolean mSearchViewMenuItemExpanded = false;

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
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_note_list, container, false);

        // ContextCompat allows app to choose between pre SDK 23 and above for getColor method
        int bgColor = ContextCompat.getColor(getActivity(), R.color.colorBackgroundNoteBody);

        mContentLinearLayout = (LinearLayout) view.findViewById(R.id.note_linear_layout_bg);
        mContentLinearLayout.setBackgroundColor(bgColor);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String selectedFontSize = sharedPref
                .getString(getString(R.string.pref_appearance_font_size_key),
                        String.valueOf(GlobalValues.FONT_SIZE_DEFAULT));

        int valueSize = Integer.parseInt(selectedFontSize);
        float fontSize = (float) valueSize;

        mEmptyTextView = (TextView) view.findViewById(R.id.note_empty_text_view);
        mEmptyTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        ((NoteListActivity) getActivity()).setSupportActionBar(toolbar);

        mNoteRecyclerView = (RecyclerView) view.findViewById(R.id.note_recycler_view);
        mNoteRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab_add);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Note note = new Note();
                NoteBook.get(getActivity()).addNote(note);

                Intent intent = NotePagerActivity.newIntent(getActivity(), note.getID(), true);
                startActivity(intent);
            }
        });

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

    private void updateUI() {
        // Update list (for new changes)
        mListOfNotes = NoteBook.get(getActivity()).getNotes();

        if (mListOfNotes.isEmpty()) {
            mEmptyTextView.setVisibility(View.VISIBLE);
        } else {
            mEmptyTextView.setVisibility(View.GONE);
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
     * Called to ask the fragment to save its current dynamic state, so it
     * can later be reconstructed in a new instance of its process is
     * restarted.  If a new instance of the fragment later needs to be
     * created, the data you place in the Bundle here will be available
     * in the Bundle given to {@link #onCreate(Bundle)},
     * {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}, and
     * {@link #onActivityCreated(Bundle)}.
     * <p/>
     * <p>This corresponds to {@link Activity#onSaveInstanceState(Bundle)
     * Activity.onSaveInstanceState(Bundle)} and most of the discussion there
     * applies here as well.  Note however: <em>this method may be called
     * at any time before {@link #onDestroy()}</em>.  There are many situations
     * where a fragment may be mostly torn down (such as when placed on the
     * back stack with no UI showing), but its state will not be saved until
     * its owning activity actually needs to save its state.
     *
     * @param outState Bundle in which to place your saved state.
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ARG_QUERY_SEARCH_STRING, mCurrentFilterQuery);

        mSearchViewMenuItemExpanded = !(mSearchView != null && mSearchView.isIconified());
        outState.putBoolean(ARG_SEARCHVIEW_MENU_EXPANDED, mSearchViewMenuItemExpanded);
    }

    /**
     * Called when the fragment's activity has been created and this
     * fragment's view hierarchy instantiated.  It can be used to do final
     * initialization once these pieces are in place, such as retrieving
     * views or restoring state.  It is also useful for fragments that use
     * {@link #setRetainInstance(boolean)} to retain their instance,
     * as this callback tells the fragment when it is fully associated with
     * the new activity instance.  This is called after {@link #onCreateView}
     * and before {@link #onViewStateRestored(Bundle)}.
     *
     * @param savedInstanceState If the fragment is being re-created from
     *                           a previous saved state, this is the state.
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            // Restore any previous active search filter
            mCurrentFilterQuery = savedInstanceState.getString(ARG_QUERY_SEARCH_STRING);
            mSearchViewMenuItemExpanded = savedInstanceState
                    .getBoolean(ARG_SEARCHVIEW_MENU_EXPANDED);
        }
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

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mSubtitleVisible = sharedPref
                .getBoolean(getString(R.string.pref_appearance_show_num_notes_key), true);

        updateUI();
    }

    /**
     * Called when the Fragment is no longer resumed.  This is generally
     * tied to {@link Activity#onPause() Activity.onPause} of the containing
     * Activity's lifecycle.
     */
    @Override
    public void onPause() {
        super.onPause();
        isDBClose = NoteBook.get(getActivity()).closeDatabase();
    }

    /**
     * Called when the fragment is no longer in use.  This is called
     * after {@link #onStop()} and before {@link #onDetach()}.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        // Make sure to close database, since onPause is not always
        // guaranteed to be called.
        if (!isDBClose) {
            NoteBook.get(getActivity()).closeDatabase();
        }
    }

    private class NoteHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private Note mNote;

        private final TextView mTitleTextView;
        private final TextView mDateTextView;

        public NoteHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            mTitleTextView = (TextView) itemView.findViewById(R.id.list_item_note_title_text_view);
            mDateTextView = (TextView) itemView.findViewById(R.id.list_item_note_date_text_view);
        }

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
         * Called when a view has been clicked.
         *
         * @param v The view that was clicked.
         */
        @Override
        public void onClick(View v) {
            Intent intent = NotePagerActivity.newIntent(getActivity(), mNote.getID(), false);
            startActivity(intent);
        }
    }

    private class NoteListAdapter extends RecyclerView.Adapter<NoteHolder> {

        private List<Note> mNotes;

        public NoteListAdapter(List<Note> notes) {
            mNotes = new ArrayList<>(notes);
        }

        /**
         * Called when RecyclerView needs a new {@link android.support.v7.widget.RecyclerView.ViewHolder}
         * of the given type to represent an item.
         * <p/>
         * This new ViewHolder should be constructed with a new View that can represent the items
         * of the given type. You can either create a new View manually or inflate it from an XML
         * layout file.
         * <p/>
         * The new ViewHolder will be used to display items of the adapter using
         * {@link #onBindViewHolder(RecyclerView.ViewHolder, int, List)}. Since it will be re-used to display
         * different items in the data set, it is a good idea to cache references to sub views of
         * the View to avoid unnecessary {@link View#findViewById(int)} calls.
         *
         * @param parent   The ViewGroup into which the new View will be added after it is bound to
         *                 an adapter position.
         * @param viewType The view type of the new View.
         * @return A new ViewHolder that holds a View of the given view type.
         * @see #getItemViewType(int)
         * @see #onBindViewHolder(NoteHolder, int)
         */
        @Override
        public NoteHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.list_item_note, parent, false);

            return new NoteHolder(view);
        }

        /**
         * Called by RecyclerView to display the data at the specified position. This method should
         * update the contents of the {@link android.support.v7.widget.RecyclerView.ViewHolder#itemView}
         * to reflect the item at the given position.
         * <p/>
         * Note that unlike {@link ListView}, RecyclerView will not call this method
         * again if the position of the item changes in the data set unless the item itself is
         * invalidated or the new position cannot be determined. For this reason, you should only
         * use the <code>position</code> parameter while acquiring the related data item inside
         * this method and should not keep a copy of it. If you need the position of an item later
         * on (e.g. in a click listener), use
         * {@link android.support.v7.widget.RecyclerView.ViewHolder#getAdapterPosition()} which will
         * have the updated adapter position.
         * <p/>
         * Override {@link #onBindViewHolder(RecyclerView.ViewHolder, int, List)} instead if Adapter can
         * handle effcient partial bind.
         *
         * @param holder   The ViewHolder which should be updated to represent the contents of the
         *                 item at the given position in the data set.
         * @param position The position of the item within the adapter's data set.
         */
        @Override
        public void onBindViewHolder(NoteHolder holder, int position) {
            final Note note = mNotes.get(position);
            holder.bindCrime(note);
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

        inflater.inflate(R.menu.fragment_note_list, menu);

        // Get the SearchView and set its listener
        final MenuItem searchViewItem = menu.findItem(R.id.menu_item_search_note);
        mSearchView = (SearchView) MenuItemCompat.getActionView(searchViewItem);
        mSearchView.setOnQueryTextListener(this);

        // Apply search filter when configuration is changed (e.g. screen rotation)
        // only when searchview is active from previous configuration or screen
        if (mCurrentFilterQuery != null && mSearchViewMenuItemExpanded) {
            mSearchView.setQuery(mCurrentFilterQuery, false);
            mSearchView.setFocusable(true);
            mSearchView.setIconified(false);
            mSearchView.requestFocusFromTouch();
        }
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
     * @param newText the new content of the query text field.
     * @return false if the SearchView should perform the default action of showing any
     * suggestions if available, true if the action was handled by the listener.
     */
    @Override
    public boolean onQueryTextChange(String newText) {
        mCurrentFilterQuery = newText.trim();
        mFilteredNotes = filter(mListOfNotes, mCurrentFilterQuery);
        mAdapter.animateTo(mFilteredNotes);
        mNoteRecyclerView.scrollToPosition(0);
        return true;
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
            final String titleText = note.getTitle().toLowerCase();
            if (titleText.contains(queryString)) {
                filteredNoteList.add(note);
            }
        }
        return filteredNoteList;
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
                Intent intent = NotePagerActivity.newIntent(getActivity(), note.getID(), true);
                startActivity(intent);
                return true;
            case R.id.action_settings:
                Intent intentPreference = new Intent(getActivity(), UserPreferenceActivity.class);
                startActivity(intentPreference);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateSubtitle() {
        NoteBook noteBook = NoteBook.get(getActivity());
        int noteCount = noteBook.getNotes().size();
        String subtitle = getResources().getQuantityString(R.plurals.subtitle_plural,
                noteCount, noteCount);

        if (!mSubtitleVisible || noteCount == 0) {
            subtitle = null;
        }

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar != null) actionBar.setSubtitle(subtitle);
    }
}
