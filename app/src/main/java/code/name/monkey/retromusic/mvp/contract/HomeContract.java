package code.name.monkey.retromusic.mvp.contract;

import java.util.ArrayList;

import code.name.monkey.retromusic.model.Playlist;
import code.name.monkey.retromusic.mvp.BasePresenter;
import code.name.monkey.retromusic.mvp.BaseView;

/**
 * Created by hemanths on 20/08/17.
 */

public interface HomeContract {
    interface HomeView extends BaseView {
        void showList(ArrayList<Playlist> homes);
    }

    interface HomePresenter extends BasePresenter<HomeView> {
        void loadHomes();
    }
}
