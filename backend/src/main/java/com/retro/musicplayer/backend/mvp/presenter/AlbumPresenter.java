package com.retro.musicplayer.backend.mvp.presenter;

import android.support.annotation.NonNull;

import com.retro.musicplayer.backend.model.Album;
import com.retro.musicplayer.backend.mvp.Presenter;
import com.retro.musicplayer.backend.mvp.contract.AlbumContract;
import com.retro.musicplayer.backend.providers.interfaces.Repository;

import java.util.ArrayList;


/**
 * Created by hemanths on 12/08/17.
 */

public class AlbumPresenter extends Presenter implements AlbumContract.Presenter {
    @NonNull
    private AlbumContract.AlbumView view;


    public AlbumPresenter(@NonNull Repository repository,
                          @NonNull AlbumContract.AlbumView view) {
        super(repository);
        this.view = view;
    }

    @Override
    public void subscribe() {
        loadAlbums();
    }

    @Override
    public void unsubscribe() {
        disposable.clear();
    }

    private void showList(@NonNull ArrayList<Album> albums) {
        view.showList(albums);
    }

    @Override
    public void loadAlbums() {
        disposable.add(repository.getAllAlbums()
                .subscribeOn(schedulerProvider.computation())
                .observeOn(schedulerProvider.ui())
                .doOnSubscribe(disposable1 -> view.loading())
                .subscribe(this::showList,
                        throwable -> view.showEmptyView(),
                        () -> view.completed()));
    }
}
