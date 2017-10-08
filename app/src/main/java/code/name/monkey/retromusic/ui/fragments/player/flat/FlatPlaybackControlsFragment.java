package code.name.monkey.retromusic.ui.fragments.player.flat;

import android.animation.ObjectAnimator;
import android.graphics.PorterDuff;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.kabouzeid.appthemehelper.ThemeStore;
import com.kabouzeid.appthemehelper.util.ATHUtil;
import com.kabouzeid.appthemehelper.util.ColorUtil;
import com.kabouzeid.appthemehelper.util.MaterialValueHelper;
import com.kabouzeid.appthemehelper.util.TintHelper;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import code.name.monkey.retromusic.R;
import code.name.monkey.retromusic.helper.MusicPlayerRemote;
import code.name.monkey.retromusic.helper.MusicProgressViewUpdateHelper;
import code.name.monkey.retromusic.helper.MusicProgressViewUpdateHelper.Callback;
import code.name.monkey.retromusic.helper.PlayPauseButtonOnClickHandler;
import code.name.monkey.retromusic.misc.SimpleOnSeekbarChangeListener;
import code.name.monkey.retromusic.model.Song;
import code.name.monkey.retromusic.service.MusicService;
import code.name.monkey.retromusic.ui.fragments.base.AbsMusicServiceFragment;
import code.name.monkey.retromusic.util.MusicUtil;
import code.name.monkey.retromusic.util.PreferenceUtil;
import code.name.monkey.retromusic.views.PlayPauseDrawable;

/**
 * Created by hemanths on 30/08/17.
 */

public class FlatPlaybackControlsFragment extends AbsMusicServiceFragment implements Callback {
    private static final String TAG = "FlatPlaybackControls";
    @BindView(R.id.text)
    TextView mText;
    @BindView(R.id.title)
    TextView mTitle;
    @BindView(R.id.playback_controls)
    ViewGroup viewGroup;
    @BindView(R.id.player_song_total_time)
    TextView mSongTotalTime;
    @BindView(R.id.player_song_current_progress)
    TextView mPlayerSongCurrentProgress;
    @BindView(R.id.player_repeat_button)
    ImageButton mPlayerRepeatButton;
    @BindView(R.id.player_shuffle_button)
    ImageButton mPlayerShuffleButton;
    @BindView(R.id.player_play_pause_button)
    ImageButton mPlayerPlayPauseFab;
    Unbinder unbinder;
    @BindView(R.id.player_progress_slider)
    SeekBar progressSlider;
    private int lastPlaybackControlsColor;
    private int lastDisabledPlaybackControlsColor;
    private MusicProgressViewUpdateHelper progressViewUpdateHelper;
    private PlayPauseDrawable playerFabPlayPauseDrawable;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        progressViewUpdateHelper = new MusicProgressViewUpdateHelper(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_flat_player_playback_controls, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpMusicControllers();
    }

    @Override
    public void onResume() {
        super.onResume();
        progressViewUpdateHelper.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        progressViewUpdateHelper.stop();
    }

    @Override
    public void onUpdateProgressViews(int progress, int total) {
        progressSlider.setMax(total);

        ObjectAnimator animator = ObjectAnimator.ofInt(progressSlider, "progress", progress);
        animator.setDuration(1500);
        animator.setInterpolator(new LinearInterpolator());
        animator.start();

        mPlayerSongCurrentProgress.setText(MusicUtil.getReadableDurationString(progress));
        mSongTotalTime.setText(MusicUtil.getReadableDurationString(total));
    }


    public void show() {
        mPlayerPlayPauseFab.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }


    public void hide() {
        if (mPlayerPlayPauseFab != null) {
            mPlayerPlayPauseFab.setScaleX(0f);
            mPlayerPlayPauseFab.setScaleY(0f);
            mPlayerPlayPauseFab.setRotation(0f);
        }
    }

    public void setDark(int dark) {
        int color = ATHUtil.resolveColor(getActivity(), android.R.attr.colorBackground);
        if (ColorUtil.isColorLight(color)) {
            lastPlaybackControlsColor = MaterialValueHelper.getSecondaryTextColor(getActivity(), true);
            lastDisabledPlaybackControlsColor = MaterialValueHelper.getSecondaryDisabledTextColor(getActivity(), true);
        } else {
            lastPlaybackControlsColor = MaterialValueHelper.getPrimaryTextColor(getActivity(), false);
            lastDisabledPlaybackControlsColor = MaterialValueHelper.getPrimaryDisabledTextColor(getActivity(), false);
        }

        if (PreferenceUtil.getInstance(getContext()).getAdaptiveColor()) {
            updateTextColors(dark);
            setProgressBarColor(dark);
            TintHelper.setTintAuto(mPlayerPlayPauseFab, dark, true);
        } else {
            int accentColor = ThemeStore.accentColor(getContext());
            updateTextColors(accentColor);
            setProgressBarColor(accentColor);
        }

        updateRepeatState();
        updateShuffleState();
        updatePrevNextColor();
        updateProgressTextColor();
    }

    private void setProgressBarColor(int dark) {
        LayerDrawable ld = (LayerDrawable) progressSlider.getProgressDrawable();
        ClipDrawable clipDrawable = (ClipDrawable) ld.findDrawableByLayerId(android.R.id.progress);
        clipDrawable.setColorFilter(dark, PorterDuff.Mode.SRC_IN);
    }

    private void updateTextColors(int color) {
        mTitle.setBackgroundColor(color);
        mTitle.setTextColor(MaterialValueHelper.getPrimaryTextColor(getContext(), ColorUtil.isColorLight(color)));
        mText.setBackgroundColor(ColorUtil.darkenColor(color));
        mText.setTextColor(MaterialValueHelper.getSecondaryTextColor(getContext(), ColorUtil.isColorLight(color)));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onServiceConnected() {
        updatePlayPauseDrawableState(false);
        updateRepeatState();
        updateShuffleState();
        updateSong();
    }

    @Override
    public void onPlayingMetaChanged() {
        super.onPlayingMetaChanged();
        updateSong();
    }

    @Override
    public void onPlayStateChanged() {
        updatePlayPauseDrawableState(true);
    }

    protected void updatePlayPauseDrawableState(boolean animate) {
        if (MusicPlayerRemote.isPlaying()) {
            playerFabPlayPauseDrawable.setPause(animate);
        } else {
            playerFabPlayPauseDrawable.setPlay(animate);
        }
    }

    private void setUpPlayPauseFab() {
        playerFabPlayPauseDrawable = new PlayPauseDrawable(getActivity());

        mPlayerPlayPauseFab.setImageDrawable(playerFabPlayPauseDrawable); // Note: set the drawable AFTER TintHelper.setTintAuto() was called
        //playPauseFab.setColorFilter(MaterialValueHelper.getPrimaryTextColor(getContext(), ColorUtil.isColorLight(fabColor)), PorterDuff.Mode.SRC_IN);
        mPlayerPlayPauseFab.setOnClickListener(new PlayPauseButtonOnClickHandler());
        mPlayerPlayPauseFab.post(() -> {
            if (mPlayerPlayPauseFab != null) {
                mPlayerPlayPauseFab.setPivotX(mPlayerPlayPauseFab.getWidth() / 2);
                mPlayerPlayPauseFab.setPivotY(mPlayerPlayPauseFab.getHeight() / 2);
            }
        });
    }

    private void setUpMusicControllers() {
        setUpPlayPauseFab();
        setUpPrevNext();
        setUpRepeatButton();
        setUpShuffleButton();
        setUpProgressSlider();
    }

    private void setUpPrevNext() {
        updatePrevNextColor();
        //mPlayerNextButton.setOnClickListener(v -> MusicPlayerRemote.playNextSong());
        //mPlayerPrevButton.setOnClickListener(v -> MusicPlayerRemote.back());
    }

    private void updateProgressTextColor() {
        int color = MaterialValueHelper.getSecondaryTextColor(getContext(), false);
        //songTotalTime.setTextColor(color);
        //songCurrentProgress.setTextColor(color);
    }

    private void updatePrevNextColor() {
        //mPlayerNextButton.setColorFilter(lastPlaybackControlsColor, PorterDuff.Mode.SRC_IN);
        //mPlayerPrevButton.setColorFilter(lastPlaybackControlsColor, PorterDuff.Mode.SRC_IN);
    }

    private void updateSong() {
        //TransitionManager.beginDelayedTransition(viewGroup, new ChangeText().setChangeBehavior(ChangeText.CHANGE_BEHAVIOR_OUT_IN));
        Song song = MusicPlayerRemote.getCurrentSong();
        mTitle.setText(song.title);
        mText.setText(song.artistName);

    }

    private void setUpProgressSlider() {
        progressSlider.setOnSeekBarChangeListener(new SimpleOnSeekbarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    MusicPlayerRemote.seekTo(progress);
                    onUpdateProgressViews(MusicPlayerRemote.getSongProgressMillis(), MusicPlayerRemote.getSongDurationMillis());
                }
            }
        });
    }

    @Override
    public void onRepeatModeChanged() {
        updateRepeatState();
    }

    @Override
    public void onShuffleModeChanged() {
        updateShuffleState();
    }

    private void setUpRepeatButton() {
        mPlayerRepeatButton.setOnClickListener(v -> MusicPlayerRemote.cycleRepeatMode());
    }

    private void updateRepeatState() {
        switch (MusicPlayerRemote.getRepeatMode()) {
            case MusicService.REPEAT_MODE_NONE:
                mPlayerRepeatButton.setImageResource(R.drawable.ic_repeat_white_24dp);
                mPlayerRepeatButton.setColorFilter(lastDisabledPlaybackControlsColor, PorterDuff.Mode.SRC_IN);
                break;
            case MusicService.REPEAT_MODE_ALL:
                mPlayerRepeatButton.setImageResource(R.drawable.ic_repeat_white_24dp);
                mPlayerRepeatButton.setColorFilter(lastPlaybackControlsColor, PorterDuff.Mode.SRC_IN);
                break;
            case MusicService.REPEAT_MODE_THIS:
                mPlayerRepeatButton.setImageResource(R.drawable.ic_repeat_one_white_24dp);
                mPlayerRepeatButton.setColorFilter(lastPlaybackControlsColor, PorterDuff.Mode.SRC_IN);
                break;
        }
    }

    private void setUpShuffleButton() {
        mPlayerShuffleButton.setOnClickListener(v -> MusicPlayerRemote.toggleShuffleMode());
    }

    private void updateShuffleState() {
        switch (MusicPlayerRemote.getShuffleMode()) {
            case MusicService.SHUFFLE_MODE_SHUFFLE:
                mPlayerShuffleButton.setColorFilter(lastPlaybackControlsColor, PorterDuff.Mode.SRC_IN);
                break;
            default:
                mPlayerShuffleButton.setColorFilter(lastDisabledPlaybackControlsColor, PorterDuff.Mode.SRC_IN);
                break;
        }
    }
}
