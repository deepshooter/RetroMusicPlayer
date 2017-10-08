package code.name.monkey.retromusic.ui.fragments.mainactivity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

import java.util.ArrayList;

import code.name.monkey.retromusic.Injection;
import code.name.monkey.retromusic.R;
import code.name.monkey.retromusic.model.Playlist;
import code.name.monkey.retromusic.mvp.contract.PlaylistContract;
import code.name.monkey.retromusic.mvp.presenter.PlaylistPresenter;
import code.name.monkey.retromusic.ui.adapter.PlaylistAdapter;
import code.name.monkey.retromusic.ui.fragments.base.AbsLibraryPagerRecyclerViewFragment;
import code.name.monkey.retromusic.util.Util;

/**
 * Created by hemanths on 19/08/17.
 */

public class PlaylistsFragment extends AbsLibraryPagerRecyclerViewFragment<PlaylistAdapter, GridLayoutManager> implements PlaylistContract.PlaylistView {
    private PlaylistPresenter mPlaylistPresenter;

    public static PlaylistsFragment newInstance() {

        Bundle args = new Bundle();

        PlaylistsFragment fragment = new PlaylistsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected GridLayoutManager createLayoutManager() {
        return new GridLayoutManager(getContext(), getGridSize());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mPlaylistPresenter = new PlaylistPresenter(Injection.provideRepository(getContext()), this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @NonNull
    @Override
    protected PlaylistAdapter createAdapter() {
        return new PlaylistAdapter(getLibraryFragment().getMainActivity(), new ArrayList<Playlist>(), R.layout.item_playlist, null);
    }

    public int getGridSize() {
        if (Util.isTablet(getResources())) {
            return Util.isLandscape(getResources()) ? 5 : 3;
        } else if (Util.isLandscape(getResources())) {
            return 4;
        }
        return 2;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getAdapter().getDataSet().isEmpty())
            mPlaylistPresenter.subscribe();
    }

    @Override
    public void onPause() {
        super.onPause();
        mPlaylistPresenter.unsubscribe();
    }

    @Override
    public void onMediaStoreChanged() {
        super.onMediaStoreChanged();
        mPlaylistPresenter.loadPlaylists();
    }

    @Override
    public void loading() {
        if (!getAdapter().getDataSet().isEmpty())
            getProgressBar().setVisibility(View.VISIBLE);
    }

    @Override
    protected int getEmptyMessage() {
        return R.string.no_playlists;
    }

    @Override
    public void showEmptyView() {
        getAdapter().swapDataSet(new ArrayList<Playlist>());
    }

    @Override
    public void completed() {
        getProgressBar().setVisibility(View.GONE);
    }

    @Override
    public void showList(ArrayList<Playlist> songs) {
        getAdapter().swapDataSet(songs);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.removeItem(R.id.action_shuffle_all);
        menu.removeItem(R.id.action_sort_order);
    }

}
