package code.name.monkey.retromusic.ui.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.kabouzeid.appthemehelper.util.TintHelper;
import com.retro.musicplayer.backend.model.Album;
import com.retro.musicplayer.backend.model.Song;
import com.retro.musicplayer.backend.mvp.contract.AlbumDetailsContract;
import com.retro.musicplayer.backend.mvp.presenter.AlbumDetailsPresenter;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import code.name.monkey.retromusic.Injection;
import code.name.monkey.retromusic.R;
import code.name.monkey.retromusic.dialogs.AddToPlaylistDialog;
import code.name.monkey.retromusic.dialogs.DeleteSongsDialog;
import code.name.monkey.retromusic.glide.RetroMusicColoredTarget;
import code.name.monkey.retromusic.glide.SongGlideRequest;
import code.name.monkey.retromusic.glide.palette.BitmapPaletteWrapper;
import code.name.monkey.retromusic.helper.MusicPlayerRemote;
import code.name.monkey.retromusic.helper.SortOrder.AlbumSongSortOrder;


import code.name.monkey.retromusic.ui.activities.base.AbsSlidingMusicPanelActivity;
import code.name.monkey.retromusic.ui.activities.tageditor.AbsTagEditorActivity;
import code.name.monkey.retromusic.ui.activities.tageditor.AlbumTagEditorActivity;
import code.name.monkey.retromusic.ui.adapter.song.SimpleSongAdapter;
import code.name.monkey.retromusic.util.NavigationUtil;
import code.name.monkey.retromusic.util.PreferenceUtil;
import code.name.monkey.retromusic.util.ViewUtil;

/**
 * Created by hemanths on 20/08/17.
 */

public class AlbumDetailsActivity extends AbsSlidingMusicPanelActivity implements AlbumDetailsContract.AlbumDetailsView {
    public static final String EXTRA_ALBUM_ID = "extra_album_id";
    private static final String TAG = "AlbumDetailsActivity";
    private static final int TAG_EDITOR_REQUEST = 2001;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.image)
    ImageView image;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.text)
    TextView text;
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.play_songs)
    ImageButton playSongs;
    @BindView(R.id.status_bar)
    View statusBar;
    @BindView(R.id.shuffle_songs)
    ImageView shuffleSongs;
    @BindView(R.id.container)
    ViewGroup mContainer;
    @NonNull
    private AlbumDetailsPresenter mAlbumDetailsPresenter;
    private Album mAlbum;
    private SimpleSongAdapter mAdapter;

    public void showHeartAnimation() {
        playSongs.clearAnimation();

        playSongs.setScaleX(0.9f);
        playSongs.setScaleY(0.9f);
        playSongs.setVisibility(View.VISIBLE);
        playSongs.setPivotX(playSongs.getWidth() / 2);
        playSongs.setPivotY(playSongs.getHeight() / 2);

        playSongs.animate()
                .setDuration(200)
                .setInterpolator(new DecelerateInterpolator())
                .scaleX(1.1f)
                .scaleY(1.1f)
                .withEndAction(() -> playSongs.animate()
                        .setDuration(200)
                        .setInterpolator(new AccelerateInterpolator())
                        .scaleX(1f)
                        .scaleY(1f)
                        .alpha(1f)
                        .start())
                .start();
    }

    @Override
    protected View createContentView() {
        return wrapSlidingMusicPanel(R.layout.activity_album);
    }

    @OnClick({R.id.shuffle_songs, R.id.play_songs})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.shuffle_songs:
                MusicPlayerRemote.openAndShuffleQueue(mAlbum.songs, true);
                break;
            case R.id.play_songs:
                showHeartAnimation();
                MusicPlayerRemote.openQueue(mAlbum.songs, 0, true);
                break;
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setDrawUnderStatusbar(true);
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        setBottomBarVisibility(View.GONE);

        hide();
        ViewUtil.setStatusBarHeight(this, statusBar);

        setUpToolBar();
        supportPostponeEnterTransition();
        mAlbumDetailsPresenter = new AlbumDetailsPresenter(Injection.provideRepository(this), this, getIntent().getIntExtra(EXTRA_ALBUM_ID, -1));
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAlbumDetailsPresenter.subscribe();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAlbumDetailsPresenter.unsubscribe();
    }

    @Override
    public void loading() {

    }

    @Override
    public void showEmptyView() {

    }

    @Override
    public void completed() {
        new Handler().postDelayed(this::showFab, 700);
    }

    private void hide() {
        playSongs.setScaleX(0);
        playSongs.setScaleY(0);
        playSongs.setEnabled(false);
    }

    private void showFab() {
        playSongs.animate()
                .setDuration(500)
                .setInterpolator(new OvershootInterpolator())
                .scaleX(1)
                .scaleY(1)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);

                    }
                })
                .start();
        playSongs.setVisibility(View.VISIBLE);
        playSongs.setEnabled(true);
    }

    @Override
    public void showList(Album album) {
        mAlbum = album;

        title.setText(album.getTitle());
        text.setText(album.getArtistName());

        loadAlbumCover();
        mAdapter = new SimpleSongAdapter(this, mAlbum.songs, R.layout.item_song);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setNestedScrollingEnabled(false);

    }

    public Album getAlbum() {
        return mAlbum;
    }

    private void setUpToolBar() {
        mToolbar.setTitle("");
        setTitle(R.string.app_name);
        mToolbar.setNavigationOnClickListener(v -> onBackPressed());
        setSupportActionBar(mToolbar);

    }

    private void loadAlbumCover() {
        SongGlideRequest.Builder.from(Glide.with(this), getAlbum().safeGetFirstSong())
                .checkIgnoreMediaStore(this)
                .generatePalette(this).build()
                .dontAnimate()
                .listener(new RequestListener<Object, BitmapPaletteWrapper>() {
                    @Override
                    public boolean onException(Exception e, Object model, Target<BitmapPaletteWrapper> target, boolean isFirstResource) {
                        supportStartPostponedEnterTransition();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(BitmapPaletteWrapper resource, Object model, Target<BitmapPaletteWrapper> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        supportStartPostponedEnterTransition();
                        return false;
                    }
                })
                .into(new RetroMusicColoredTarget(image) {
                    @Override
                    public void onColorReady(int color) {
                        setColors(color);
                    }
                });
    }

    private void setColors(int color) {
        if (PreferenceUtil.getInstance(this).getAdaptiveColor()) {
            TintHelper.setTintAuto(playSongs, color, true);
        }

        //mContainer.setBackgroundColor(color);
        /*if (title != null) {
            title.setTextColor(MaterialValueHelper.getPrimaryTextColor(this, ColorUtil.isColorLight(color)));
        }
        if (text != null) {
            text.setTextColor(MaterialValueHelper.getSecondaryTextColor(this, ColorUtil.isColorLight(color)));
        }*/
        //statusBar.setBackgroundColor(ColorUtil.darkenColor(color));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_album_detail, menu);
        MenuItem sortOrder = menu.findItem(R.id.action_sort_order);
        setUpSortOrderMenu(sortOrder.getSubMenu());
        return true;
    }

   /* @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        final ArrayList<Song> songs = mAdapter.getDataSet();
        switch (id) {
            case R.id.action_play_next:
                MusicPlayerRemote.playNext(songs);
                return true;
            case R.id.action_add_to_current_playing:
                MusicPlayerRemote.enqueue(songs);
                return true;
            case R.id.action_add_to_playlist:
                AddToPlaylistDialog.create(songs).show(getSupportFragmentManager(), "ADD_PLAYLIST");
                return true;
            case R.id.action_delete_from_device:
                DeleteSongsDialog.create(songs).show(getSupportFragmentManager(), "DELETE_SONGS");
                return true;
            case android.R.id.home:
                super.onBackPressed();
                return true;
            case R.id.action_tag_editor:
                Intent intent = new Intent(this, AlbumTagEditorActivity.class);
                intent.putExtra(AbsTagEditorActivity.EXTRA_ID, getAlbum().getId());
                startActivityForResult(intent, TAG_EDITOR_REQUEST);
                return true;
            case R.id.action_go_to_artist:
                NavigationUtil.goToArtist(this, getAlbum().getArtistId());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return handleSortOrderMenuItem(item);
    }

    private boolean handleSortOrderMenuItem(@NonNull MenuItem item) {
        String sortOrder = null;
        final ArrayList<Song> songs = mAdapter.getDataSet();
        switch (item.getItemId()) {
            case R.id.action_play_next:
                MusicPlayerRemote.playNext(songs);
                return true;
            case R.id.action_add_to_current_playing:
                MusicPlayerRemote.enqueue(songs);
                return true;
            case R.id.action_add_to_playlist:
                AddToPlaylistDialog.create(songs).show(getSupportFragmentManager(), "ADD_PLAYLIST");
                return true;
            case R.id.action_delete_from_device:
                DeleteSongsDialog.create(songs).show(getSupportFragmentManager(), "DELETE_SONGS");
                return true;
            case android.R.id.home:
                super.onBackPressed();
                return true;
            case R.id.action_tag_editor:
                Intent intent = new Intent(this, AlbumTagEditorActivity.class);
                intent.putExtra(AbsTagEditorActivity.EXTRA_ID, getAlbum().getId());
                startActivityForResult(intent, TAG_EDITOR_REQUEST);
                return true;
            case R.id.action_go_to_artist:
                NavigationUtil.goToArtist(this, getAlbum().getArtistId());
                return true;
            /*Sort*/
            case R.id.action_sort_order_title:
                sortOrder = AlbumSongSortOrder.SONG_A_Z;
                break;
            case R.id.action_sort_order_title_desc:
                sortOrder = AlbumSongSortOrder.SONG_Z_A;
                break;
            case R.id.action_sort_order_track_list:
                sortOrder = AlbumSongSortOrder.SONG_TRACK_LIST;
                break;
            case R.id.action_sort_order_artist_song_duration:
                sortOrder = AlbumSongSortOrder.SONG_DURATION;
                break;
        }
        if (sortOrder != null) {
            item.setChecked(true);
            setSaveSortOrder(sortOrder);
        }
        return true;
    }

    private String getSavedSortOrder() {
        return PreferenceUtil.getInstance(this).getAlbumDetailSongSortOrder();
    }

    private void setUpSortOrderMenu(@NonNull SubMenu sortOrder) {
        switch (getSavedSortOrder()) {
            case AlbumSongSortOrder.SONG_A_Z:
                sortOrder.findItem(R.id.action_sort_order_title).setChecked(true);
                break;
            case AlbumSongSortOrder.SONG_Z_A:
                sortOrder.findItem(R.id.action_sort_order_title_desc).setChecked(true);
                break;
            case AlbumSongSortOrder.SONG_TRACK_LIST:
                sortOrder.findItem(R.id.action_sort_order_track_list).setChecked(true);
                break;
            case AlbumSongSortOrder.SONG_DURATION:
                sortOrder.findItem(R.id.action_sort_order_artist_song_duration).setChecked(true);
                break;
        }
    }

    private void setSaveSortOrder(String sortOrder) {
        PreferenceUtil.getInstance(this).setAlbumDetailSongSortOrder(sortOrder);
        reload();
    }

    private void reload() {
        mAlbumDetailsPresenter.subscribe();
    }
}
