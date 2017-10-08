package code.name.monkey.retromusic.mvp.contract;

import java.util.ArrayList;

import code.name.monkey.retromusic.mvp.BasePresenter;
import code.name.monkey.retromusic.mvp.BaseView;

/**
 * Created by hemanths on 20/08/17.
 */

public interface SearchContract {
    interface SearchView extends BaseView {
        void showList(ArrayList<Object> list);
    }

    interface SearchPresenter extends BasePresenter<SearchView> {
        void search(String query);
    }
}
