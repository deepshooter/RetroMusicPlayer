package code.name.monkey.retromusic.loaders;

import android.content.Context;

import java.util.ArrayList;

import code.name.monkey.retromusic.model.Playlist;
import code.name.monkey.retromusic.model.smartplaylist.HistoryPlaylist;
import code.name.monkey.retromusic.model.smartplaylist.LastAddedPlaylist;
import code.name.monkey.retromusic.model.smartplaylist.MyTopTracksPlaylist;
import io.reactivex.Observable;

/**
 * Created by hemanths on 20/08/17.
 */

public class HomeLoader {


    public static Observable<ArrayList<Playlist>> getHomeLoader(Context context) {
        ArrayList<Playlist> playlists = new ArrayList<>();

        new MyTopTracksPlaylist(context).getSongs(context).subscribe(songs -> {
            if (songs.size() > 0)
                playlists.add(new MyTopTracksPlaylist(context));
        });

        new HistoryPlaylist(context).getSongs(context).subscribe(songs -> {
            if (songs.size() > 0)
                playlists.add(new HistoryPlaylist(context));
        });

        new LastAddedPlaylist(context).getSongs(context).subscribe(songs -> {
            if (songs.size() > 0)
                playlists.add(new LastAddedPlaylist(context));
        });

        /*LastAddedSongsLoader.getLastAddedArtists(context).subscribe(artists -> {
            if (arrayList.size() > 0)
                arrayList.add(new Home("Recent artists", (ArrayList) artists));
        }).dispose();

        LastAddedSongsLoader.getLastAddedAlbums(context).subscribe(albums -> {
            if (albums.size() > 0) arrayList.add(new Home("Recent albums", (ArrayList) albums));
        }).dispose();*/

        return Observable.just(playlists);
    }
}
