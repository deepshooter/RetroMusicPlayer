package code.name.monkey.retromusic.ui.fragments.player.normal;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.kabouzeid.appthemehelper.util.ATHUtil;
import com.retro.musicplayer.backend.model.Song;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import code.name.monkey.retromusic.R;
import code.name.monkey.retromusic.helper.MusicPlayerRemote;

import code.name.monkey.retromusic.ui.fragments.base.AbsPlayerFragment;
import code.name.monkey.retromusic.ui.fragments.player.PlayerAlbumCoverFragment;
import code.name.monkey.retromusic.util.LyricUtil;
import code.name.monkey.retromusic.util.MusicUtil;
import code.name.monkey.retromusic.util.PreferenceUtil;
import code.name.monkey.retromusic.util.ToolbarColorizeHelper;
import code.name.monkey.retromusic.util.Util;
import code.name.monkey.retromusic.util.ViewUtil;
import code.name.monkey.retromusic.views.DrawableGradient;

/**
 * Created by hemanths on 18/08/17.
 */

public class PlayerFragment extends AbsPlayerFragment implements PlayerAlbumCoverFragment.Callbacks {
    @BindView(R.id.player_toolbar)
    Toolbar mToolbar;
    @BindView(R.id.gradient_background)
    View colorBackground;
    @Nullable
    @BindView(R.id.toolbar_container)
    FrameLayout toolbarContainer;

    private AsyncTask updateIsFavoriteTask;
    private AsyncTask updateLyricsAsyncTask;
    private int lastColor;
    private PlayerPlaybackControlsFragment mPlaybackControlsFragment;
    private PlayerAlbumCoverFragment playerAlbumCoverFragment;
    private Unbinder unbinder;
    private ValueAnimator valueAnimator;


    public static PlayerFragment newInstance() {
        Bundle args = new Bundle();
        PlayerFragment fragment = new PlayerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private void colorize(int i) {
        if (valueAnimator != null) valueAnimator.cancel();

        valueAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), android.R.color.transparent, i);
        valueAnimator.addUpdateListener(animation -> {
            GradientDrawable drawable = new DrawableGradient(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    new int[]{(int) animation.getAnimatedValue(), android.R.color.transparent},
                    0);
            if (colorBackground != null) {
                colorBackground.setBackground(drawable);
            }
        });
        valueAnimator.setDuration(ViewUtil.PHONOGRAPH_ANIM_TIME).start();
    }

    @Override
    @ColorInt
    public int getPaletteColor() {
        return lastColor;
    }

    @Override
    public void onShow() {
        mPlaybackControlsFragment.show();
    }

    @Override
    public void onHide() {
        mPlaybackControlsFragment.hide();
        onBackPressed();
    }

    @Override
    public void onResume() {
        super.onResume();
        checkToggleToolbar(toolbarContainer);
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    @Override
    public void onColorChanged(int color) {
        mPlaybackControlsFragment.setDark(color);
        getCallbacks().onPaletteColorChanged();
        lastColor = color;

        if (PreferenceUtil.getInstance(getContext()).getAdaptiveColor()) colorize(color);
    }

    @Override
    protected void toggleFavorite(Song song) {
        super.toggleFavorite(song);
        if (song.id == MusicPlayerRemote.getCurrentSong().id) {
            //if (MusicUtil.isFavorite(getActivity(), song)) {
            //this.playerAlbumCoverFragment.showHeartAnimation();
            //}
            updateIsFavorite();
        }
    }

    @Override
    public void onFavoriteToggled() {
        toggleFavorite(MusicPlayerRemote.getCurrentSong());
    }

    @Override
    public void onDestroyView() {

        if (updateLyricsAsyncTask != null && !updateLyricsAsyncTask.isCancelled()) {
            updateLyricsAsyncTask.cancel(true);
        }
        if (updateIsFavoriteTask != null && !updateIsFavoriteTask.isCancelled()) {
            updateIsFavoriteTask.cancel(true);
        }
        super.onDestroyView();
        unbinder.unbind();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpSubFragments();
        setUpPlayerToolbar();


    }

    private void setUpSubFragments() {
        mPlaybackControlsFragment = (PlayerPlaybackControlsFragment) getChildFragmentManager().findFragmentById(R.id.playback_controls_fragment);

        playerAlbumCoverFragment = (PlayerAlbumCoverFragment) getChildFragmentManager().findFragmentById(R.id.player_album_cover_fragment);
        playerAlbumCoverFragment.setCallbacks(this);
    }

    private void setUpPlayerToolbar() {
        mToolbar.inflateMenu(R.menu.menu_player);
        mToolbar.setNavigationOnClickListener(v -> getActivity().onBackPressed());
        mToolbar.setOnMenuItemClickListener(this);

        ToolbarColorizeHelper.colorizeToolbar(mToolbar,
                ATHUtil.resolveColor(getContext(), R.attr.iconColor),
                getActivity());

       /* ToolbarContentTintHelper.setToolbarContentColor(getContext(),
                mToolbar,
                ATHUtil.resolveColor(getContext(), R.attr.iconColor),
                ThemeStore.textColorPrimary(getContext()),
                ThemeStore.textColorSecondary(getContext()),
                ATHUtil.resolveColor(getContext(), R.attr.iconColor));*/
    }

    @Override
    public void onServiceConnected() {
        //updateQueue();
        //updateCurrentSong();
        updateIsFavorite();
        updateLyrics();
    }

    @Override
    public void onPlayingMetaChanged() {
        //updateCurrentSong();
        //updateQueuePosition();
        updateIsFavorite();
        updateLyrics();
    }

    @Override
    public void onQueueChanged() {
        //updateQueue();
    }

    @Override
    public void onMediaStoreChanged() {
        //updateQueue();
    }

    @SuppressLint("StaticFieldLeak")
    private void updateLyrics() {
        if (updateLyricsAsyncTask != null) updateLyricsAsyncTask.cancel(false);
        final Song song = MusicPlayerRemote.getCurrentSong();
        updateLyricsAsyncTask = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mToolbar.getMenu().removeItem(R.id.action_show_lyrics);
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                return LyricUtil.isLrcFileExist(song.title, song.artistName);

            }

            @Override
            protected void onPostExecute(Boolean l) {
                if (l) {
                    Activity activity = getActivity();
                    if (mToolbar != null && activity != null)
                        if (mToolbar.getMenu().findItem(R.id.action_show_lyrics) == null) {
                            int color = ATHUtil.resolveColor(activity, R.attr.iconColor, Color.TRANSPARENT);
                            Drawable drawable = Util.getTintedVectorDrawable(activity, R.drawable.ic_comment_text_outline_white_24dp, color);
                            mToolbar.getMenu()
                                    .add(Menu.NONE, R.id.action_show_lyrics, Menu.NONE, R.string.action_show_lyrics)
                                    .setIcon(drawable)
                                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                        }
                } else {
                    if (mToolbar != null) {
                        mToolbar.getMenu().removeItem(R.id.action_show_lyrics);
                    }
                }

            }
        }.execute();
    }

    @SuppressLint("StaticFieldLeak")
    private void updateIsFavorite() {
        if (updateIsFavoriteTask != null) updateIsFavoriteTask.cancel(false);
        updateIsFavoriteTask = new AsyncTask<Song, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Song... params) {
                Activity activity = getActivity();
                if (activity != null) {
                    return MusicUtil.isFavorite(getActivity(), params[0]);
                } else {
                    cancel(false);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Boolean isFavorite) {
                Activity activity = getActivity();
                if (activity != null) {
                    int res = isFavorite ? R.drawable.ic_favorite_white_24dp : R.drawable.ic_favorite_border_white_24dp;
                    int color = ATHUtil.resolveColor(activity, R.attr.iconColor, Color.TRANSPARENT);
                    Drawable drawable = Util.getTintedVectorDrawable(activity, res, color);
                    mToolbar.getMenu().findItem(R.id.action_toggle_favorite)
                            .setIcon(drawable)
                            .setTitle(isFavorite ? getString(R.string.action_remove_from_favorites) : getString(R.string.action_add_to_favorites));
                }
            }
        }.execute(MusicPlayerRemote.getCurrentSong());
    }
}
