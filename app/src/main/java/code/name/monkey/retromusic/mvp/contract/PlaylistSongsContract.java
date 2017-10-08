package code.name.monkey.retromusic.mvp.contract;

import java.util.ArrayList;

import code.name.monkey.retromusic.model.Playlist;
import code.name.monkey.retromusic.model.Song;
import code.name.monkey.retromusic.mvp.BasePresenter;
import code.name.monkey.retromusic.mvp.BaseView;

/**
 * Created by hemanths on 20/08/17.
 */

public interface PlaylistSongsContract {
    interface PlaylistSongsView extends BaseView {
        void showSongs(ArrayList<Song> songs);
    }

    interface Presenter extends BasePresenter<PlaylistSongsView> {
        void loadSongs(Playlist playlist);
    }
}
