package com.retro.musicplayer.backend.mvp.contract;


import com.retro.musicplayer.backend.model.Song;
import com.retro.musicplayer.backend.mvp.BasePresenter;
import com.retro.musicplayer.backend.mvp.BaseView;

import java.util.ArrayList;


/**
 * Created by hemanths on 10/08/17.
 */

public interface SongContract {

    interface SongView extends BaseView {
        void showList(ArrayList<Song> songs);
    }

    interface Presenter extends BasePresenter<SongView> {
        void loadSongs();
    }
}
