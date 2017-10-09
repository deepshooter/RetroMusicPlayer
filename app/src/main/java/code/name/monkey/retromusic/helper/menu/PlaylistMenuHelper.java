package code.name.monkey.retromusic.helper.menu;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Toast;

import com.retro.musicplayer.backend.loaders.PlaylistSongsLoader;
import com.retro.musicplayer.backend.model.AbsCustomPlaylist;
import com.retro.musicplayer.backend.model.Playlist;
import com.retro.musicplayer.backend.model.Song;

import java.util.ArrayList;

import code.name.monkey.retromusic.R;
import code.name.monkey.retromusic.dialogs.AddToPlaylistDialog;
import code.name.monkey.retromusic.dialogs.DeletePlaylistDialog;
import code.name.monkey.retromusic.dialogs.RenamePlaylistDialog;
import code.name.monkey.retromusic.helper.MusicPlayerRemote;

import code.name.monkey.retromusic.util.PlaylistsUtil;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class PlaylistMenuHelper {
    public static final int MENU_RES = R.menu.menu_item_playlist;

    public static boolean handleMenuClick(@NonNull AppCompatActivity activity, @NonNull final Playlist playlist, @NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_play:
                getPlaylistSongs(activity, playlist)
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(songs -> MusicPlayerRemote.openQueue((ArrayList<Song>) songs, 0, true)).dispose();
                return true;
            case R.id.action_play_next:
                getPlaylistSongs(activity, playlist)
                        .observeOn(Schedulers.computation())
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .subscribe(songs -> {
                            MusicPlayerRemote.playNext((ArrayList<Song>) songs);
                        }).dispose();
                return true;
            case R.id.action_add_to_playlist:
                getPlaylistSongs(activity, playlist)
                        .observeOn(Schedulers.computation())
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .subscribe(songs -> {
                            AddToPlaylistDialog.create((ArrayList<Song>) songs)
                                    .show(activity.getSupportFragmentManager(), "ADD_PLAYLIST");
                        }).dispose();
                return true;
            case R.id.action_add_to_current_playing:
                getPlaylistSongs(activity, playlist)
                        .observeOn(Schedulers.computation())
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .subscribe(songs -> MusicPlayerRemote.enqueue((ArrayList<Song>) songs)).dispose();
                return true;
            case R.id.action_rename_playlist:
                RenamePlaylistDialog.create(playlist.id).show(activity.getSupportFragmentManager(), "RENAME_PLAYLIST");
                return true;
            case R.id.action_delete_playlist:
                DeletePlaylistDialog.create(playlist).show(activity.getSupportFragmentManager(), "DELETE_PLAYLIST");
                return true;
            case R.id.action_save_playlist:
                final Toast toast = Toast.makeText(activity, R.string.saving_to_file, Toast.LENGTH_SHORT);
                PlaylistsUtil.savePlaylist(activity, playlist)
                        .doOnSubscribe(disposable -> toast.show())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.computation())
                        .subscribe(file -> {
                            if (toast != null) {
                                toast.setText(String.format(activity.getString(R.string.saved_playlist_to), file));
                                toast.show();
                            }
                        }).dispose();
                return true;
        }
        return false;
    }

    @NonNull
    private static Observable<ArrayList<? extends Song>> getPlaylistSongs(@NonNull Activity activity, Playlist playlist) {
        if (playlist instanceof AbsCustomPlaylist) {
            return Observable.create(e -> (
                    (AbsCustomPlaylist) playlist)
                    .getSongs(activity)
                    .subscribe(songs -> {
                                if (songs.size() > 0) {
                                    e.onNext(songs);
                                }
                                e.onComplete();
                            }
                    ));
        } else {
            return Observable.create(e -> PlaylistSongsLoader.getPlaylistSongList(activity, playlist.id)
                    .subscribe(playlistSongs -> {
                        if (playlistSongs.size() > 0) {
                            e.onNext(playlistSongs);
                        }
                        e.onComplete();
                    }));
        }
    }
}
