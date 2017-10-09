package com.retro.musicplayer.backend.mvp.contract;

import com.retro.musicplayer.backend.model.Album;
import com.retro.musicplayer.backend.mvp.BasePresenter;
import com.retro.musicplayer.backend.mvp.BaseView;

import java.util.ArrayList;


/**
 * Created by hemanths on 12/08/17.
 */

public interface AlbumContract {

    interface AlbumView extends BaseView {
        void showList(ArrayList<Album> album);
    }

    interface Presenter extends BasePresenter<AlbumView> {
        void loadAlbums();
    }

}
