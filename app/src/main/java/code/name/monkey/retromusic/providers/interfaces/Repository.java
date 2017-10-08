package code.name.monkey.retromusic.providers.interfaces;

import java.util.ArrayList;

import code.name.monkey.retromusic.model.Album;
import code.name.monkey.retromusic.model.Artist;
import code.name.monkey.retromusic.model.Playlist;
import code.name.monkey.retromusic.model.Song;
import io.reactivex.Observable;

/**
 * Created by hemanths on 11/08/17.
 */

public interface Repository {
    Observable<ArrayList<Song>> getAllSongs();

    Observable<Song> getSong(int id);

    Observable<ArrayList<Album>> getAllAlbums();

    Observable<Album> getAlbum(int albumId);

    Observable<ArrayList<Artist>> getAllArtists();

    Observable<Artist> getArtistById(long artistId);

    Observable<ArrayList<Playlist>> getAllPlaylists();

    Observable<ArrayList<Object>> search(String query);

    Observable<ArrayList<Song>> getPlaylistSongs(Playlist playlist);

    Observable<ArrayList<Playlist>> getHomeList();

}
