package code.name.monkey.retromusic.util;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.widget.Toast;

import code.name.monkey.retromusic.R;
import code.name.monkey.retromusic.helper.MusicPlayerRemote;
import code.name.monkey.retromusic.model.Playlist;
import code.name.monkey.retromusic.ui.activities.AlbumDetailsActivity;
import code.name.monkey.retromusic.ui.activities.ArtistDetailActivity;
import code.name.monkey.retromusic.ui.activities.LyricsActivity;
import code.name.monkey.retromusic.ui.activities.PlayingQueueActivity;
import code.name.monkey.retromusic.ui.activities.PlaylistDetailActivity;

public class NavigationUtil {
    public static void goToAlbum(@NonNull Activity activity, int i, @Nullable Pair... pairArr) {
        Intent intent = new Intent(activity, AlbumDetailsActivity.class);
        intent.putExtra(AlbumDetailsActivity.EXTRA_ALBUM_ID, i);
        ActivityCompat.startActivity(activity, intent, ActivityOptionsCompat.makeSceneTransitionAnimation(activity, pairArr).toBundle());
    }

    public static void goToArtist(@NonNull Activity activity, int i, @Nullable Pair... pairArr) {
        Intent intent = new Intent(activity, ArtistDetailActivity.class);
        intent.putExtra(ArtistDetailActivity.EXTRA_ARTIST_ID, i);
        activity.startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(activity, pairArr).toBundle());
    }

    public static void goToPlaylistNew(@NonNull Activity activity, Playlist playlist) {
        Intent intent = new Intent(activity, PlaylistDetailActivity.class);
        intent.putExtra(PlaylistDetailActivity.EXTRA_PLAYLIST, playlist);
        activity.startActivity(intent);
    }

    public static void openEqualizer(@NonNull Activity activity) {
        int audioSessionId = MusicPlayerRemote.getAudioSessionId();
        if (audioSessionId == -4) {
            Toast.makeText(activity, activity.getResources().getString(R.string.no_audio_ID), Toast.LENGTH_LONG).show();
            return;
        }
        try {
            Intent intent = new Intent("android.media.action.DISPLAY_AUDIO_EFFECT_CONTROL_PANEL");
            intent.putExtra("android.media.extra.AUDIO_SESSION", audioSessionId);
            intent.putExtra("android.media.extra.CONTENT_TYPE", 0);
            activity.startActivityForResult(intent, 0);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(activity, activity.getResources().getString(R.string.no_equalizer), Toast.LENGTH_SHORT).show();
        }
    }

    public static void goToPlayingQueue(@NonNull Activity activity) {
        ActivityCompat.startActivity(activity, new Intent(activity, PlayingQueueActivity.class), null);
    }

    public static void goToLyrics(Activity activity) {
        ActivityCompat.startActivity(activity, new Intent(activity, LyricsActivity.class), null);
    }
}
