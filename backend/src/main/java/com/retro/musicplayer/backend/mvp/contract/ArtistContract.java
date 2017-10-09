package com.retro.musicplayer.backend.mvp.contract;


import com.retro.musicplayer.backend.model.Artist;
import com.retro.musicplayer.backend.mvp.BasePresenter;
import com.retro.musicplayer.backend.mvp.BaseView;

import java.util.ArrayList;



/**
 * Created by hemanths on 16/08/17.
 */

public interface ArtistContract {
    interface ArtistView extends BaseView {

        void showList(ArrayList<Artist> artists);

    }

    interface Presenter extends BasePresenter<ArtistView> {
        void loadArtists();
    }
}
