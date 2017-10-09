package com.retro.musicplayer.backend.mvp.contract;


import com.retro.musicplayer.backend.model.Artist;
import com.retro.musicplayer.backend.mvp.BasePresenter;
import com.retro.musicplayer.backend.mvp.BaseView;


/**
 * Created by hemanths on 20/08/17.
 */

public interface ArtistDetailContract {
    interface ArtistsDetailsView extends BaseView {
        void showArtist(Artist artist);
    }

    interface Presenter extends BasePresenter<ArtistsDetailsView> {
        void loadArtistById(int artistId);
    }
}
