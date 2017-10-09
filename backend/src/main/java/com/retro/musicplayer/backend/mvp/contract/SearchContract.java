package com.retro.musicplayer.backend.mvp.contract;

import com.retro.musicplayer.backend.mvp.BasePresenter;
import com.retro.musicplayer.backend.mvp.BaseView;

import java.util.ArrayList;


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
