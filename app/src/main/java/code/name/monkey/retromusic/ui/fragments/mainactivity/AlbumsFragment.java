package code.name.monkey.retromusic.ui.fragments.mainactivity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.transition.TransitionManager;
import android.support.v7.widget.GridLayoutManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

import java.util.ArrayList;

import code.name.monkey.retromusic.Injection;
import code.name.monkey.retromusic.R;
import code.name.monkey.retromusic.helper.SortOrder.AlbumSortOrder;
import code.name.monkey.retromusic.model.Album;
import code.name.monkey.retromusic.mvp.contract.AlbumContract;
import code.name.monkey.retromusic.mvp.presenter.AlbumPresenter;
import code.name.monkey.retromusic.ui.adapter.album.AlbumAdapter;
import code.name.monkey.retromusic.ui.fragments.base.AbsLibraryPagerRecyclerViewCustomGridSizeFragment;
import code.name.monkey.retromusic.util.PreferenceUtil;

/**
 * Created by hemanths on 12/08/17.
 */

public class AlbumsFragment extends AbsLibraryPagerRecyclerViewCustomGridSizeFragment<AlbumAdapter, GridLayoutManager> implements AlbumContract.AlbumView {
    public static final String TAG = AlbumsFragment.class.getSimpleName();

    private AlbumPresenter albumPresenter;

    public static AlbumsFragment newInstance() {
        Bundle args = new Bundle();
        AlbumsFragment fragment = new AlbumsFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    protected GridLayoutManager createLayoutManager() {
        return new GridLayoutManager(getActivity(), getGridSize());
    }

    @NonNull
    @Override
    protected AlbumAdapter createAdapter() {
        int itemLayoutRes = getItemLayoutRes();
        notifyLayoutResChanged(itemLayoutRes);
        ArrayList<Album> dataSet = getAdapter() == null ? new ArrayList<Album>() : getAdapter().getDataSet();
        return new AlbumAdapter(
                getLibraryFragment().getMainActivity(),
                dataSet,
                itemLayoutRes,
                loadUsePalette(),
                getLibraryFragment());
    }

    @Override
    protected int getEmptyMessage() {
        return R.string.no_albums;
    }

    @Override
    public boolean loadUsePalette() {
        return PreferenceUtil.getInstance(getActivity()).albumColoredFooters();
    }

    @Override
    protected void setUsePalette(boolean usePalette) {
        getAdapter().usePalette(usePalette);
    }

    @Override
    protected void setGridSize(int gridSize) {
        getLayoutManager().setSpanCount(gridSize);
        getAdapter().notifyDataSetChanged();
    }

    @Override
    protected int loadGridSize() {
        return PreferenceUtil.getInstance(getActivity()).getAlbumGridSize(getActivity());
    }

    @Override
    protected void saveGridSize(int gridSize) {
        PreferenceUtil.getInstance(getActivity()).setAlbumGridSize(gridSize);
    }

    @Override
    protected int loadGridSizeLand() {
        return PreferenceUtil.getInstance(getActivity()).getAlbumGridSizeLand(getActivity());
    }

    @Override
    protected void saveGridSizeLand(int gridSize) {
        PreferenceUtil.getInstance(getActivity()).setAlbumGridSizeLand(gridSize);
    }

    @Override
    protected void saveUsePalette(boolean usePalette) {
        PreferenceUtil.getInstance(getActivity()).setAlbumColoredFooters(usePalette);
    }

    @Override
    public void onMediaStoreChanged() {
        albumPresenter.loadAlbums();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        albumPresenter = new AlbumPresenter(Injection.provideRepository(getContext()), this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onResume() {
        super.onResume();
        if (getAdapter().getDataSet().isEmpty())
            albumPresenter.subscribe();
    }

    @Override
    public void onPause() {
        super.onPause();
        albumPresenter.unsubscribe();
    }

    @Override
    public void loading() {
        TransitionManager.beginDelayedTransition(getRecyclerView());
        if (!getAdapter().getDataSet().isEmpty())
            getProgressBar().setVisibility(View.VISIBLE);
    }

    @Override
    public void showEmptyView() {
        getAdapter().swapDataSet(new ArrayList<Album>());
    }

    @Override
    public void completed() {
        getProgressBar().setVisibility(View.GONE);
    }

    @Override
    public void showList(ArrayList<Album> albums) {
        getAdapter().swapDataSet(albums);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return handleSortOrderMenuItem(item);
    }

    private boolean handleSortOrderMenuItem(@NonNull MenuItem item) {
        String sortOrder = null;
        switch (item.getItemId()) {
            case R.id.action_sort_order_album:
                sortOrder = AlbumSortOrder.ALBUM_A_Z;
                break;
            case R.id.action_sort_order_album_desc:
                sortOrder = AlbumSortOrder.ALBUM_Z_A;
                break;
            case R.id.action_sort_order_artist:
                sortOrder = AlbumSortOrder.ALBUM_ARTIST;
                break;
        }
        if (sortOrder != null) {
            item.setChecked(true);
            setSaveSortOrder(sortOrder);
        }
        return true;
    }

    private void setSaveSortOrder(String sortOrder) {
        PreferenceUtil.getInstance(getContext()).setAlbumSortOrder(sortOrder);
        albumPresenter.loadAlbums();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem sortOrder = menu.findItem(R.id.action_sort_order);
        setUpSortOrderMenu(sortOrder.getSubMenu());
    }

    private void setUpSortOrderMenu(SubMenu subMenu) {
        subMenu.removeItem(R.id.action_sort_order_title_desc);
        subMenu.removeItem(R.id.action_sort_order_title);
        subMenu.removeItem(R.id.action_sort_order_date);
        subMenu.removeItem(R.id.action_sort_order_duration);
        subMenu.removeItem(R.id.action_sort_order_year);
        subMenu.removeItem(R.id.action_sort_order_artist_desc);

        switch (getSavedSortOrder()) {
            case AlbumSortOrder.ALBUM_A_Z:
                subMenu.findItem(R.id.action_sort_order_album).setChecked(true);
                break;
            case AlbumSortOrder.ALBUM_Z_A:
                subMenu.findItem(R.id.action_sort_order_album_desc).setChecked(true);
                break;
            case AlbumSortOrder.ALBUM_ARTIST:
                subMenu.findItem(R.id.action_sort_order_artist).setChecked(true);
                break;
        }

    }

    private String getSavedSortOrder() {
        return PreferenceUtil.getInstance(getContext()).getAlbumSortOrder();
    }
}
