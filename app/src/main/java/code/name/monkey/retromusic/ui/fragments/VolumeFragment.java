package code.name.monkey.retromusic.ui.fragments;

import android.content.Context;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.kabouzeid.appthemehelper.ThemeStore;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import code.name.monkey.retromusic.R;

import static android.content.Context.AUDIO_SERVICE;

/**
 * Created by BlackFootSanji on 5/5/2017.
 */

public class VolumeFragment extends Fragment implements SeekBar.OnSeekBarChangeListener {
    private static final String TAG = "VolumeFragment";
    @BindView(R.id.volume_seekbar)
    SeekBar volumeSeekbar;
    @BindView(R.id.volume_down)
    ImageView volumeDown;
    int color;
    @BindView(R.id.container)
    ViewGroup viewGroup;
    @BindView(R.id.volume_up)
    ImageView volumeUp;
    private Unbinder unbinder;
    private SettingsContentObserver mSettingsContentObserver;
    private AudioManager audioManager;

    public static VolumeFragment newInstance() {
        Bundle args = new Bundle();
        VolumeFragment fragment = new VolumeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_volume, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        volumeSeekbar.setOnSeekBarChangeListener(this);
        setColor(ThemeStore.accentColor(getContext()));
        setupSettingsContentObserver();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        getActivity().getApplicationContext().getContentResolver().unregisterContentObserver(mSettingsContentObserver);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        AudioManager audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        volumeDown.setImageResource(i == 0 ? R.drawable.ic_volume_off_white_24dp : R.drawable.ic_volume_down_white_24dp);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, i, 0);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @OnClick({R.id.volume_down, R.id.volume_up})
    public void onViewClicked(View view) {
        AudioManager audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        switch (view.getId()) {
            case R.id.volume_down:
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, 0);
                break;
            case R.id.volume_up:
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, 0);
                break;
        }
    }

    public void setColor(int color) {
        this.color = color;
        //setProgressBarColor(volumeSeekbar, color);
        //volumeSeekbar.getProgressDrawable().setColorFilter(color, PorterDuff.Mode.SRC_IN);
        //volumeSeekbar.getThumb().setColorFilter(color, PorterDuff.Mode.SRC_IN);
    }

    private void setupSettingsContentObserver() {
        mSettingsContentObserver = new SettingsContentObserver(getActivity(), new Handler());
        getActivity().getApplicationContext().getContentResolver().registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, mSettingsContentObserver);
    }

    private class SettingsContentObserver extends ContentObserver {
        int previousVolume;
        Context context;

        SettingsContentObserver(Context c, Handler handler) {
            super(handler);
            context = c;


            audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

            audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            int curVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            previousVolume = curVolume;

            volumeSeekbar.setMax(maxVolume);
            volumeSeekbar.setProgress(curVolume);

        }

        @Override
        public boolean deliverSelfNotifications() {
            return super.deliverSelfNotifications();
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);

            AudioManager audio = (AudioManager) context.getSystemService(AUDIO_SERVICE);
            volumeSeekbar.setMax(audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
            int currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);

            int delta = previousVolume - currentVolume;

            if (delta > 0) {
                previousVolume = currentVolume;
            } else if (delta < 0) {
                previousVolume = currentVolume;
            }
            volumeSeekbar.setProgress(previousVolume);

        }
    }
}
