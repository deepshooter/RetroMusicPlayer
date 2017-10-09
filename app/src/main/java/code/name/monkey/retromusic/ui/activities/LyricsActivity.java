package code.name.monkey.retromusic.ui.activities;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.retro.musicplayer.backend.model.Song;
import com.retro.musicplayer.backend.model.lyrics.Lyrics;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import code.name.monkey.retromusic.R;
import code.name.monkey.retromusic.glide.RetroMusicColoredTarget;
import code.name.monkey.retromusic.glide.SongGlideRequest;
import code.name.monkey.retromusic.helper.MusicPlayerRemote;
import code.name.monkey.retromusic.helper.MusicProgressViewUpdateHelper;
import code.name.monkey.retromusic.lastfm.rest.KygouClient;
import code.name.monkey.retromusic.lastfm.rest.model.KuGouSearchLyricResult;

import code.name.monkey.retromusic.ui.activities.base.AbsMusicServiceActivity;
import code.name.monkey.retromusic.util.LyricUtil;
import code.name.monkey.retromusic.util.MusicUtil;
import code.name.monkey.retromusic.views.LyricView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by hemanths on 23/08/17.
 */

public class LyricsActivity extends AbsMusicServiceActivity implements MusicProgressViewUpdateHelper.Callback {
    @BindView(R.id.image)
    ImageView mImage;
    @BindView(R.id.title)
    TextView mTitle;
    @BindView(R.id.text)
    TextView mText;
    @BindView(R.id.lyrics)
    LyricView lyricView;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.offline_lyrics)
    TextView mOfflineLyrics;
    private KygouClient kygouClient;
    private MusicProgressViewUpdateHelper mUpdateHelper;
    private AsyncTask updateLyricsAsyncTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lyrics);
        ButterKnife.bind(this);
        setStatusbarColorAuto();
        setNavigationbarColorAuto();
        setTaskDescriptionColorAuto();

        mToolbar.setBackgroundColor(ThemeStore.primaryColor(this));
        mToolbar.setTitle(R.string.lyrics);
        mToolbar.setNavigationOnClickListener(v -> onBackPressed());
        setSupportActionBar(mToolbar);

        kygouClient = new KygouClient(this);
        mUpdateHelper = new MusicProgressViewUpdateHelper(this, 700, 500);
    }

    @Override
    public void onPlayingMetaChanged() {
        super.onPlayingMetaChanged();
        loadLrcFile();
        loadDetails();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mUpdateHelper.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mUpdateHelper.stop();
    }

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        loadLrcFile();
        loadDetails();
    }

    private void loadDetails() {
        Song song = MusicPlayerRemote.getCurrentSong();
        mTitle.setText(song.title);
        mText.setText(song.artistName);
        SongGlideRequest.Builder.from(Glide.with(this), song)
                .checkIgnoreMediaStore(this)
                .generatePalette(this).build()
                .into(new RetroMusicColoredTarget(mImage) {
                    @Override
                    public void onColorReady(int color) {

                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        lyricView.setOnPlayerClickListener(null);
    }

    private void loadLrcFile() {
        Song song = MusicPlayerRemote.getCurrentSong();
        String title = song.title;
        String artist = song.artistName;
        if (lyricView == null) {
            return;
        }
        lyricView.reset();
        if (LyricUtil.isLrcFileExist(title, artist)) {
            lyricView.setDefaultHint("Loading from local");
            showLyricsLocal(LyricUtil.getLocalLyricFile(title, artist));
        } else {
            lyricView.setDefaultHint("Loading from network");
            long duration = MusicPlayerRemote.getSongDurationMillis();
            kygouClient.getApiService().searchLyric(title, String.valueOf(duration))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::parseKugouResult,
                            throwable -> {
                                lyricView.setDefaultHint(getString(R.string.error_loading_lyrics_from_network));
                                lyricView.setVisibility(View.GONE);
                                loadSongLyrics();
                            });

        }

        lyricView.setOnPlayerClickListener((progress, content) -> {
            MusicPlayerRemote.seekTo((int) progress);
        });
    }

    private void showLyricsLocal(File file) {
        if (file == null) {
            lyricView.reset();
        } else {
            lyricView.setLyricFile(file, "UTF-8");
        }
    }

    private void parseKugouResult(KuGouSearchLyricResult kuGouSearchLyricResult) {
        if (kuGouSearchLyricResult != null && kuGouSearchLyricResult.status == 200 &
                kuGouSearchLyricResult.candidates != null &&
                kuGouSearchLyricResult.candidates.size() != 0) {
            KuGouSearchLyricResult.Candidates candidates = kuGouSearchLyricResult.candidates.get(0);
            loadLyricsFile(candidates);
        } else {
            lyricView.setDefaultHint(getString(R.string.no_lyrics_found));
            lyricView.setVisibility(View.GONE);
            loadSongLyrics();
        }
    }

    private void loadLyricsFile(KuGouSearchLyricResult.Candidates candidates) {
        kygouClient.getApiService().getRawLyric(candidates.id, candidates.accesskey)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(kuGouRawLyric -> {
                    if (kuGouRawLyric == null) {
                        lyricView.setDefaultHint(getString(R.string.no_lyrics_found));
                        lyricView.setVisibility(View.GONE);
                        loadSongLyrics();
                        return;
                    }
                    final Song song = MusicPlayerRemote.getCurrentSong();
                    final String title = song.title;
                    final String artist = song.artistName;
                    String rawLyric = LyricUtil.decryptBASE64(kuGouRawLyric.content);
                    LyricUtil.writeLrcToLoc(title, artist, rawLyric);
                    new Handler().postDelayed(() -> showLyricsLocal(LyricUtil.getLocalLyricFile(title, artist)), 1);
                });
    }

    @SuppressLint("StaticFieldLeak")
    private void loadSongLyrics() {
        if (updateLyricsAsyncTask != null) updateLyricsAsyncTask.cancel(false);
        final Song song = MusicPlayerRemote.getCurrentSong();
        updateLyricsAsyncTask = new AsyncTask<Void, Void, Lyrics>() {
            @Override
            protected Lyrics doInBackground(Void... params) {
                String data = MusicUtil.getLyrics(song);
                if (TextUtils.isEmpty(data)) {
                    return null;
                }
                return Lyrics.parse(song, data);
            }

            @Override
            protected void onPostExecute(Lyrics l) {
                mOfflineLyrics.setVisibility(View.VISIBLE);
                if (l == null) {
                    return;
                }
                mOfflineLyrics.setText(l.data);
            }

            @Override
            protected void onCancelled(Lyrics s) {
                onPostExecute(null);
            }
        }.execute();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onUpdateProgressViews(int progress, int total) {
        lyricView.setCurrentTimeMillis(progress);
    }
}
