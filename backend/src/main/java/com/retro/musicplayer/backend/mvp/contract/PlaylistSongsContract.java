package com.retro.musicplayer.backend.mvp.contract;

import com.retro.musicplayer.backend.model.Playlist;
import com.retro.musicplayer.backend.model.Song;
import com.retro.musicplayer.backend.mvp.BasePresenter;
import com.retro.musicplayer.backend.mvp.BaseView;

import java.util.ArrayList;


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
