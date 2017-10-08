package code.name.monkey.retromusic.mvp.contract;

import java.util.ArrayList;

import code.name.monkey.retromusic.model.Album;
import code.name.monkey.retromusic.mvp.BasePresenter;
import code.name.monkey.retromusic.mvp.BaseView;

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
