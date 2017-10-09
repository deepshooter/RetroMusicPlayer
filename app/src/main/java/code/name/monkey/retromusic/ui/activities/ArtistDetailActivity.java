package code.name.monkey.retromusic.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.transition.TransitionManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.kabouzeid.appthemehelper.util.TintHelper;
import com.retro.musicplayer.backend.model.Artist;
import com.retro.musicplayer.backend.model.Song;
import com.retro.musicplayer.backend.mvp.contract.ArtistDetailContract;
import com.retro.musicplayer.backend.mvp.presenter.ArtistDetailsPresenter;

import java.util.ArrayList;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import code.name.monkey.retromusic.Injection;
import code.name.monkey.retromusic.R;
import code.name.monkey.retromusic.dialogs.AddToPlaylistDialog;
import code.name.monkey.retromusic.glide.ArtistGlideRequest;
import code.name.monkey.retromusic.glide.RetroMusicColoredTarget;
import code.name.monkey.retromusic.helper.MusicPlayerRemote;
import code.name.monkey.retromusic.helper.SortOrder.ArtistSongSortOrder;
import code.name.monkey.retromusic.lastfm.rest.LastFMRestClient;
import code.name.monkey.retromusic.lastfm.rest.model.LastFmArtist;

import code.name.monkey.retromusic.ui.activities.base.AbsSlidingMusicPanelActivity;
import code.name.monkey.retromusic.ui.adapter.artist.ArtistDetailAdapter;
import code.name.monkey.retromusic.util.CustomArtistImageUtil;
import code.name.monkey.retromusic.util.MusicUtil;
import code.name.monkey.retromusic.util.PreferenceUtil;
import code.name.monkey.retromusic.util.Util;
import code.name.monkey.retromusic.util.ViewUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by BlackFootSanji on 10/9/2016.
 */

public class ArtistDetailActivity extends AbsSlidingMusicPanelActivity implements ArtistDetailContract.ArtistsDetailsView {
    public static final String EXTRA_ARTIST_ID = "extra_artist_id";
    private static final String TAG = "ArtistDetailActivity";
    private static final int REQUEST_CODE_SELECT_IMAGE = 9003;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.image)
    ImageView image;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.biography)
    TextView biographyTextView;
    @BindView(R.id.text)
    TextView text;
    @BindView(R.id.root_layout)
    ViewGroup rootLayour;
    @BindView(R.id.play_songs)
    ImageButton playSongs;
    @BindView(R.id.status_bar)
    View mStatusBar;
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.title_container)
    ViewGroup titleContainer;

    private Artist mArtist;
    private LastFMRestClient mLastFMRestClient;
    @Nullable
    private Spanned mBiography;
    @NonNull
    private ArtistDetailsPresenter mArtistDetailsPresenter;
    private ArtistDetailAdapter mArtistDetailAdapter;
    private boolean forceDownload;


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
                .start();
        playSongs.setVisibility(View.VISIBLE);
        playSongs.setEnabled(true);
    }

    private void setUpViews() {
        setupRecyclerView();
        setToolBar();

    }

    @Override
    protected void onCreate(Bundle bundle) {
        setDrawUnderStatusbar(true);
        super.onCreate(bundle);
        ButterKnife.bind(this);


        mLastFMRestClient = new LastFMRestClient(this);

        setBottomBarVisibility(View.GONE);
        ViewUtil.setStatusBarHeight(this, mStatusBar);

        setUpViews();

        supportPostponeEnterTransition();

        int artistID = getIntent().getIntExtra(EXTRA_ARTIST_ID, -1);
        mArtistDetailsPresenter = new ArtistDetailsPresenter(Injection.provideRepository(this),
                this,
                artistID);

    }

    private void setupRecyclerView() {
        mArtistDetailAdapter = new ArtistDetailAdapter(this);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mArtistDetailAdapter);
    }

    @Override
    protected View createContentView() {
        return wrapSlidingMusicPanel(R.layout.activity_artist_details);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_SELECT_IMAGE:
                if (resultCode == RESULT_OK) {
                    CustomArtistImageUtil.getInstance(this).setCustomArtistImage(mArtist, data.getData());
                }
                break;
            default:
                if (resultCode == RESULT_OK) {
                    reload();
                }
                break;
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mArtistDetailsPresenter.subscribe();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mArtistDetailsPresenter.unsubscribe();
    }

    @Override
    public void loading() {
        hide();
    }

    @Override
    public void showEmptyView() {

    }

    @Override
    public void completed() {
        supportStartPostponedEnterTransition();
        new Handler().postDelayed(this::showFab, 700);
    }

    @Override
    public void showArtist(Artist artist) {
        supportStartPostponedEnterTransition();
        setArtist(artist);
    }

    private Artist getArtist() {
        if (mArtist == null) mArtist = new Artist();
        return mArtist;
    }

    private void setArtist(Artist artist) {
        this.mArtist = artist;
        loadArtistImage();

        if (Util.isAllowedToDownloadMetadata(this)) {
            loadBiography();
        }
        title.setText(artist.getName());
        text.setText(MusicUtil.getArtistInfoString(this, artist));

        ArrayList<Object> list = new ArrayList<>();
        list.add("Albums");
        list.add(artist.albums);
        list.add("Songs");
        list.add(artist.getSongs());
        mArtistDetailAdapter.swapData(list);
    }

    private void loadBiography() {
        loadBiography(Locale.getDefault().getLanguage());
    }

    private void loadBiography(@Nullable final String lang) {
        mBiography = null;

        mLastFMRestClient.getApiService()
                .getArtistInfo(getArtist().getName(), lang, null)
                .enqueue(new Callback<LastFmArtist>() {
                    @Override
                    public void onResponse(@NonNull Call<LastFmArtist> call, @NonNull Response<LastFmArtist> response) {
                        final LastFmArtist lastFmArtist = response.body();
                        if (lastFmArtist != null && lastFmArtist.getArtist() != null) {
                            final String bioContent = lastFmArtist.getArtist().getBio().getContent();
                            if (bioContent != null && !bioContent.trim().isEmpty()) {
                                TransitionManager.beginDelayedTransition(titleContainer);
                                biographyTextView.setVisibility(View.VISIBLE);
                                mBiography = Html.fromHtml(bioContent);
                                biographyTextView.setText(mBiography);
                            }
                        }

                        // If the "lang" parameter is set and no biography is given, retry with default language
                        if (mBiography == null && lang != null) {
                            loadBiography(null);
                            return;
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<LastFmArtist> call, @NonNull Throwable t) {
                        t.printStackTrace();
                        mBiography = null;
                    }
                });
    }

    @OnClick(R.id.biography)
    void toggleArtistBiogrphy() {
        TransitionManager.beginDelayedTransition(rootLayour);
        if (biographyTextView.getMaxLines() == 4) {
            biographyTextView.setMaxLines(Integer.MAX_VALUE);
        } else {
            biographyTextView.setMaxLines(4);
        }
    }

    private void setToolBar() {
        toolbar.setTitle("");
        setTitle(R.string.app_name);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void loadArtistImage() {
        ArtistGlideRequest.Builder.from(Glide.with(this), mArtist)
                .forceDownload(forceDownload)
                .generatePalette(this).build()
                .dontAnimate()
                .into(new RetroMusicColoredTarget(image) {
                    @Override
                    public void onColorReady(int color) {
                        setColors(color);
                    }
                });
        forceDownload = false;
    }
  /*  private void loadArtistImage(final boolean forceDownload) {
        if (forceDownload) {
            ArtistSignatureUtil.getInstance(this).updateArtistSignature(getArtist().getName());
        }
        Glide.with(this)
                .load(new ArtistImage(getArtist().getName(), forceDownload))
                .asBitmap()
                .transcode(new BitmapPaletteTranscoder(this), BitmapPaletteWrapper.class)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .placeholder(ContextCompat.getDrawable(this, R.drawable.default_artist_image))
                .signature(ArtistSignatureUtil.getInstance(this).getArtistSignature(getArtist().getName()))
                .dontAnimate()
                .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                .listener(new RequestListener<ArtistImage, BitmapPaletteWrapper>() {
                    @Override
                    public boolean onException(@Nullable Exception e, ArtistImage model, Target<BitmapPaletteWrapper> target, boolean isFirstResource) {
                        if (forceDownload) {
                            Toast.makeText(ArtistDetailActivity.this, e != null ? e.getClass().getSimpleName() : "Error", Toast.LENGTH_SHORT).show();
                        }
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(BitmapPaletteWrapper resource, ArtistImage model, Target<BitmapPaletteWrapper> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        if (forceDownload) {
                            Toast.makeText(ArtistDetailActivity.this, getString(R.string.updated_artist_image), Toast.LENGTH_SHORT).show();
                        }
                        return false;
                    }
                })
                .into(new RetroMusicColoredTarget(image) {
                    @Override
                    public void onColorReady(int color) {
                        setColors(color);
                    }
                });
    }*/

    private void setColors(int color) {
        if (PreferenceUtil.getInstance(this).getAdaptiveColor()) {
            TintHelper.setTintAuto(playSongs, color, true);
        } else {
            //GradientDrawable drawable = (GradientDrawable) playSongs.getDrawable();
            //drawable.setColors(PreferenceUtil.getInstance(this).getGradientColors());

           /* DrawableGradient drawableGradient = new DrawableGradient(GradientDrawable.Orientation.TL_BR,
                    PreferenceUtil.getInstance(this).getGradientColors(), GradientDrawable.OVAL);
            playSongs.setBackground(drawableGradient);*/
        }
    }

    @OnClick({R.id.shuffle_songs, R.id.play_songs})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.shuffle_songs:
                MusicPlayerRemote.openAndShuffleQueue(getArtist().getSongs(), true);
                break;
            case R.id.play_songs:
                showHeartAnimation();
                MusicPlayerRemote.openQueue(getArtist().getSongs(), 0, true);
                break;
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return handleSortOrderMenuItem(item);
    }

    private boolean handleSortOrderMenuItem(@NonNull MenuItem item) {
        String sortOrder = null;
        final ArrayList<Song> songs = getArtist().getSongs();
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
            case android.R.id.home:
                super.onBackPressed();
                return true;
            case R.id.action_colored_footers:
                item.setChecked(!item.isChecked());
                return true;
            /*Sort*/
            case R.id.action_sort_order_title:
                sortOrder = ArtistSongSortOrder.SONG_A_Z;
                break;
            case R.id.action_sort_order_title_desc:
                sortOrder = ArtistSongSortOrder.SONG_Z_A;
                break;
            case R.id.action_sort_order_album:
                sortOrder = ArtistSongSortOrder.SONG_ALBUM;
                break;
            case R.id.action_sort_order_year:
                sortOrder = ArtistSongSortOrder.SONG_YEAR;
                break;
            case R.id.action_sort_order_artist_song_duration:
                sortOrder = ArtistSongSortOrder.SONG_DURATION;
                break;
            case R.id.action_sort_order_date:
                sortOrder = ArtistSongSortOrder.SONG_DATE;
                break;
            case R.id.action_set_artist_image:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, getString(R.string.pick_from_local_storage)), REQUEST_CODE_SELECT_IMAGE);
                return true;
            case R.id.action_reset_artist_image:
                Toast.makeText(ArtistDetailActivity.this, getResources().getString(R.string.updating), Toast.LENGTH_SHORT).show();
                CustomArtistImageUtil.getInstance(ArtistDetailActivity.this).resetCustomArtistImage(mArtist);
                forceDownload = true;
                return true;
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
            case ArtistSongSortOrder.SONG_A_Z:
                sortOrder.findItem(R.id.action_sort_order_title).setChecked(true);
                break;
            case ArtistSongSortOrder.SONG_Z_A:
                sortOrder.findItem(R.id.action_sort_order_title_desc).setChecked(true);
                break;
            case ArtistSongSortOrder.SONG_ALBUM:
                sortOrder.findItem(R.id.action_sort_order_album).setChecked(true);
                break;
            case ArtistSongSortOrder.SONG_YEAR:
                sortOrder.findItem(R.id.action_sort_order_year).setChecked(true);
                break;
            case ArtistSongSortOrder.SONG_DURATION:
                sortOrder.findItem(R.id.action_sort_order_artist_song_duration).setChecked(true);
                break;
            case ArtistSongSortOrder.SONG_DATE:
                sortOrder.findItem(R.id.action_sort_order_date).setChecked(true);
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_artist_detail, menu);
        menu.removeItem(R.id.action_sort_order);
        //setUpSortOrderMenu(sortOrder.getSubMenu());
        return true;
    }

    private void setSaveSortOrder(String sortOrder) {
        PreferenceUtil.getInstance(this).setArtistDetailSongSortOrder(sortOrder);
        reload();
    }

    private void reload() {
        mArtistDetailsPresenter.unsubscribe();
        mArtistDetailsPresenter.subscribe();
    }
}
