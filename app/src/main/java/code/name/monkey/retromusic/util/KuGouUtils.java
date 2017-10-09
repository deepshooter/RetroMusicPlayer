package code.name.monkey.retromusic.util;

import android.content.Context;

import com.retro.musicplayer.backend.model.Song;
import com.retro.musicplayer.backend.util.schedulers.BaseSchedulerProvider;

import code.name.monkey.retromusic.Injection;
import code.name.monkey.retromusic.helper.MusicPlayerRemote;
import code.name.monkey.retromusic.lastfm.rest.KygouClient;
import code.name.monkey.retromusic.lastfm.rest.model.KuGouSearchLyricResult;
import io.reactivex.Observable;

/**
 * Created by hemanths on 27/08/17.
 */

public class KuGouUtils {
    private static KuGouUtils sKuGouUtils;
    private KygouClient mKygouClient;
    private Song mSong;
    private BaseSchedulerProvider mBaseSchedulerProvider;

    public KuGouUtils(Context context) {
        mBaseSchedulerProvider = Injection.provideSchedulerProvider();
        mKygouClient = new KygouClient(context);
    }

    public static KuGouUtils getInstance(Context context) {
        if (sKuGouUtils == null) {
            sKuGouUtils = new KuGouUtils(context);
        }
        return sKuGouUtils;
    }

    public KuGouUtils setSong(Song song) {
        mSong = song;
        return this;
    }

    public Observable<KuGouSearchLyricResult> load() {
        long duration = MusicPlayerRemote.getSongDurationMillis();
        return mKygouClient.getApiService().searchLyric(mSong.title, String.valueOf(duration));
    }

    private void parseKugouResult(KuGouSearchLyricResult kuGouSearchLyricResult) {
        if (kuGouSearchLyricResult != null && kuGouSearchLyricResult.status == 200 &
                kuGouSearchLyricResult.candidates != null &&
                kuGouSearchLyricResult.candidates.size() != 0) {
            KuGouSearchLyricResult.Candidates candidates = kuGouSearchLyricResult.candidates.get(0);
            loadLyricsFile(candidates);
        }
    }

    private void loadLyricsFile(KuGouSearchLyricResult.Candidates candidates) {
        mKygouClient.getApiService().getRawLyric(candidates.id, candidates.accesskey)
                .observeOn(mBaseSchedulerProvider.computation())
                .subscribeOn(mBaseSchedulerProvider.computation())
                .subscribe(kuGouRawLyric -> {
                    final Song song = MusicPlayerRemote.getCurrentSong();
                    final String title = song.title;
                    final String artist = song.artistName;
                    String rawLyric = LyricUtil.decryptBASE64(kuGouRawLyric.content);
                    LyricUtil.writeLrcToLoc(title, artist, rawLyric);
                });
    }
}
