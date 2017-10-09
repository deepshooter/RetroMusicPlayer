package com.retro.musicplayer.backend.mvp.presenter;

import android.support.annotation.NonNull;

import com.retro.musicplayer.backend.mvp.contract.SearchContract;
import com.retro.musicplayer.backend.providers.interfaces.Repository;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import com.retro.musicplayer.backend.mvp.Presenter;

/**
 * Created by hemanths on 20/08/17.
 */

public class SearchPresenter extends Presenter implements SearchContract.SearchPresenter {
    @NonNull
    private SearchContract.SearchView mView;

    public SearchPresenter(@NonNull Repository repository,
                           @NonNull SearchContract.SearchView view) {
        super(repository);
        mView = view;
    }

    @Override
    public void subscribe() {
        search("");
    }

    @Override
    public void unsubscribe() {
        disposable.clear();
    }

    private void showList(@NonNull ArrayList<Object> albums) {
        if (albums.isEmpty()) {
            mView.showEmptyView();
        } else {
            mView.showList(albums);
        }
    }

    @Override
    public void search(String query) {
        disposable.add(repository.search(query)
                .debounce(500, TimeUnit.MILLISECONDS)
                .subscribeOn(schedulerProvider.computation())
                .observeOn(schedulerProvider.ui())
                .doOnSubscribe(disposable1 -> mView.loading())
                .subscribe(this::showList,
                        throwable -> mView.showEmptyView(),
                        () -> mView.completed()));
    }
}
