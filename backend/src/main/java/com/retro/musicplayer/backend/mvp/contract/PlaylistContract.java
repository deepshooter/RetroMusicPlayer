package com.retro.musicplayer.backend.mvp.contract;

import com.retro.musicplayer.backend.model.Playlist;
import com.retro.musicplayer.backend.mvp.BasePresenter;
import com.retro.musicplayer.backend.mvp.BaseView;

import java.util.ArrayList;

/**
 * Created by hemanths on 19/08/17.
 */

public interface PlaylistContract {
    interface PlaylistView extends BaseView {
        void showList(ArrayList<Playlist> playlists);
    }

    interface Presenter extends BasePresenter<PlaylistView> {
        void loadPlaylists();
    }
}
