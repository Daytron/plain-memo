package com.github.daytron.plain_memo.view.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
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
import com.github.daytron.plain_memo.database.NoteBook;
import com.github.daytron.plain_memo.model.Note;
import com.github.daytron.plain_memo.util.DateUtil;
import com.github.daytron.plain_memo.view.NotePagerActivity;

import java.util.List;

/**
 * Created by ryan on 27/10/15.
 */
public class NoteListFragment extends Fragment {

    private static final String SAVED_SUBTITLE_VISIBLE = "subtitle";
    private static final int REQUEST_NOTE = 1;

    private LinearLayout mContentLinearLayout;
    private TextView mEmptyTextView;
    private RecyclerView mNoteRecyclerView;
    private NoteListAdapter mAdapter;
    private boolean mSubtitleVisible;

    private boolean isDBClose = false;

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
        int bgColor = ContextCompat.getColor(getActivity(),R.color.colorBackgroundNoteBody);

        mContentLinearLayout = (LinearLayout) view.findViewById(R.id.note_linear_layout_bg);
        mContentLinearLayout.setBackgroundColor(bgColor);

        mEmptyTextView = (TextView) view.findViewById(R.id.note_empty_text_view);
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        ((NoteListActivity)getActivity()).setSupportActionBar(toolbar);

        mNoteRecyclerView = (RecyclerView) view.findViewById(R.id.note_recycler_view);
        mNoteRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab_add);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                Note note = new Note();
                NoteBook.get(getActivity()).addNote(note);

                boolean isNewNote = true;
                Intent intent = NotePagerActivity.newIntent(getActivity(), note.getID(), isNewNote);
                startActivity(intent);
            }
        });

        if (savedInstanceState != null) {
            mSubtitleVisible = savedInstanceState.getBoolean(SAVED_SUBTITLE_VISIBLE);
        }

        updateUI();

        return view;
    }

    private void updateUI() {
        NoteBook noteBook = NoteBook.get(getActivity());
        List<Note> notes = noteBook.getNotes();

        if (noteBook.getNotes().isEmpty()) {
            mEmptyTextView.setVisibility(View.VISIBLE);
        } else {
            mEmptyTextView.setVisibility(View.GONE);
        }

        if (mAdapter == null) {
            mAdapter = new NoteListAdapter(notes);
            mNoteRecyclerView.setAdapter(mAdapter);
        } else  {
            mAdapter.setNotes(notes);
            mAdapter.notifyDataSetChanged();
        }

        updateSubtitle();
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
        isDBClose = NoteBook.get(getActivity()).closeDB();
    }

    /**
     * Called when the fragment is no longer in use.  This is called
     * after {@link #onStop()} and before {@link #onDetach()}.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();

        if (!isDBClose) {
            NoteBook.get(getActivity()).closeDB();
        }
    }

    private class NoteHolder extends RecyclerView.ViewHolder
        implements View.OnClickListener {

        private Note mNote;

        private final TextView mTitleTextView;
        private final TextView mDateTextView;
        private final TextView mTimeTextView;

        public NoteHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            mTitleTextView = (TextView) itemView.findViewById(R.id.list_item_note_title_text_view);
            mDateTextView = (TextView) itemView.findViewById(R.id.list_item_note_date_text_view);
            mTimeTextView = (TextView) itemView.findViewById(R.id.list_item_note_time_text_view);
        }

        public void bindCrime(Note note) {
            mNote = note;
            mTitleTextView.setText(mNote.getTitle());

            if (DateUtils.isToday(mNote.getDate().getTime())) {
                mDateTextView.setText(R.string.today);
            } else {
                mDateTextView.setText(DateUtil.getDateStringLocale(getActivity(),
                        mNote.getDate()));
            }
            mTimeTextView.setText(DateUtil.getTimeStringLocale(getActivity(),
                    mNote.getDate()));
        }

        /**
         * Called when a view has been clicked.
         *
         * @param v The view that was clicked.
         */
        @Override
        public void onClick(View v) {
            Intent intent = NotePagerActivity.newIntent(getActivity(), mNote.getID(),false);
            startActivity(intent);
        }
    }

    private class NoteListAdapter extends RecyclerView.Adapter<NoteHolder> {

        private List<Note> mNotes;

        public NoteListAdapter(List<Note> notes) {
            mNotes = notes;
        }

        /**
         * Called when RecyclerView needs a new {@link ViewHolder} of the given type to represent
         * an item.
         * <p/>
         * This new ViewHolder should be constructed with a new View that can represent the items
         * of the given type. You can either create a new View manually or inflate it from an XML
         * layout file.
         * <p/>
         * The new ViewHolder will be used to display items of the adapter using
         * {@link #onBindViewHolder(ViewHolder, int, List)}. Since it will be re-used to display
         * different items in the data set, it is a good idea to cache references to sub views of
         * the View to avoid unnecessary {@link View#findViewById(int)} calls.
         *
         * @param parent   The ViewGroup into which the new View will be added after it is bound to
         *                 an adapter position.
         * @param viewType The view type of the new View.
         * @return A new ViewHolder that holds a View of the given view type.
         * @see #getItemViewType(int)
         * @see #onBindViewHolder(ViewHolder, int)
         */
        @Override
        public NoteHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.list_item_note, parent, false);

            return new NoteHolder(view);
        }

        /**
         * Called by RecyclerView to display the data at the specified position. This method should
         * update the contents of the {@link ViewHolder#itemView} to reflect the item at the given
         * position.
         * <p/>
         * Note that unlike {@link ListView}, RecyclerView will not call this method
         * again if the position of the item changes in the data set unless the item itself is
         * invalidated or the new position cannot be determined. For this reason, you should only
         * use the <code>position</code> parameter while acquiring the related data item inside
         * this method and should not keep a copy of it. If you need the position of an item later
         * on (e.g. in a click listener), use {@link ViewHolder#getAdapterPosition()} which will
         * have the updated adapter position.
         * <p/>
         * Override {@link #onBindViewHolder(ViewHolder, int, List)} instead if Adapter can
         * handle effcient partial bind.
         *
         * @param holder   The ViewHolder which should be updated to represent the contents of the
         *                 item at the given position in the data set.
         * @param position The position of the item within the adapter's data set.
         */
        @Override
        public void onBindViewHolder(NoteHolder holder, int position) {
            Note note = mNotes.get(position);
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

        MenuItem subtitleItem = menu.findItem(R.id.menu_item_show_subtitle);
        if (mSubtitleVisible) {
            subtitleItem.setTitle(R.string.hide_subtitle);
        } else {
            subtitleItem.setTitle(R.string.show_subtitle);
        }
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
        outState.putBoolean(SAVED_SUBTITLE_VISIBLE, mSubtitleVisible);
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
            case R.id.menu_item_new_crime:
                Note note = new Note();
                NoteBook.get(getActivity()).addNote(note);
                Intent intent = NotePagerActivity.newIntent(getActivity(), note.getID(), true);
                startActivity(intent);
                return true;
            case R.id.menu_item_show_subtitle:
                mSubtitleVisible = !mSubtitleVisible;
                getActivity().supportInvalidateOptionsMenu();
                updateSubtitle();
                return true;
            case R.id.action_settings:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateSubtitle() {
        NoteBook noteBook = NoteBook.get(getActivity());
        int noteCount = noteBook.getNotes().size();
        String subtitle = getString(R.string.subtitle_format, noteCount);

        if (!mSubtitleVisible) {
            subtitle = null;
        }

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        ActionBar actionBar = activity.getSupportActionBar();

        if (actionBar != null) actionBar.setSubtitle(subtitle);
    }
}
