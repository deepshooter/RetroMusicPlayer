package code.name.monkey.retromusic.mvp.contract;

import code.name.monkey.retromusic.model.Album;
import code.name.monkey.retromusic.mvp.BasePresenter;
import code.name.monkey.retromusic.mvp.BaseView;

/**
 * Created by hemanths on 20/08/17.
 */

public interface AlbumDetailsContract {
    interface AlbumDetailsView extends BaseView {
        void showList(Album album);
    }

    interface Presenter extends BasePresenter<AlbumDetailsView> {
        void loadAlbumSongs(int albumId);
    }
}
