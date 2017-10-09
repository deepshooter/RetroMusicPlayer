package com.retro.musicplayer.backend.mvp.presenter;

import android.support.annotation.NonNull;

import com.retro.musicplayer.backend.model.Album;
import com.retro.musicplayer.backend.mvp.Presenter;
import com.retro.musicplayer.backend.mvp.contract.AlbumDetailsContract;
import com.retro.musicplayer.backend.providers.interfaces.Repository;


/**
 * Created by hemanths on 20/08/17.
 */

public class AlbumDetailsPresenter extends Presenter
        implements AlbumDetailsContract.Presenter {
    @NonNull
    private final int albumId;
    @NonNull
    private AlbumDetailsContract.AlbumDetailsView view;

    public AlbumDetailsPresenter(@NonNull Repository repository,
                                 @NonNull AlbumDetailsContract.AlbumDetailsView view,
                                 @NonNull int albumId) {
        super(repository);
        this.view = view;
        this.albumId = albumId;
    }

    @Override
    public void subscribe() {
        loadAlbumSongs(albumId);
    }

    @Override
    public void unsubscribe() {
        disposable.clear();
    }

    @Override
    public void loadAlbumSongs(int albumId) {
        disposable.add(repository.getAlbum(albumId)
                .subscribeOn(schedulerProvider.computation())
                .observeOn(schedulerProvider.ui())
                .doOnSubscribe(disposable1 -> view.loading())
                .subscribe(this::showAlbum,
                        throwable -> view.showEmptyView(),
                        () -> view.completed()));
    }

    private void showAlbum(Album album) {
        if (album != null) {
            view.showList(album);
        } else {
            view.showEmptyView();
        }
    }
}
