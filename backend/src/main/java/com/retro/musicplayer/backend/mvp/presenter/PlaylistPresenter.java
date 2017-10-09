package com.retro.musicplayer.backend.mvp.presenter;

import android.support.annotation.NonNull;

import com.retro.musicplayer.backend.model.Playlist;
import com.retro.musicplayer.backend.mvp.Presenter;
import com.retro.musicplayer.backend.mvp.contract.PlaylistContract;
import com.retro.musicplayer.backend.providers.interfaces.Repository;

import java.util.ArrayList;


/**
 * Created by hemanths on 19/08/17.
 */

public class PlaylistPresenter extends Presenter
        implements PlaylistContract.Presenter {
    @NonNull
    private PlaylistContract.PlaylistView mView;

    public PlaylistPresenter(@NonNull Repository repository,
                             @NonNull PlaylistContract.PlaylistView view) {
        super(repository);
        mView = view;
    }

    @Override
    public void subscribe() {
        loadPlaylists();
    }

    @Override
    public void unsubscribe() {
        disposable.clear();
    }

    @Override
    public void loadPlaylists() {
        disposable.add(repository.getAllPlaylists()
                .subscribeOn(schedulerProvider.computation())
                .observeOn(schedulerProvider.ui())
                .doOnSubscribe(disposable1 -> mView.loading())
                .subscribe(this::showList,
                        throwable -> mView.showEmptyView(),
                        () -> mView.completed()));
    }

    private void showList(@NonNull ArrayList<Playlist> songs) {
        if (songs.isEmpty()) {
            mView.showEmptyView();
        } else {
            mView.showList(songs);
        }
    }
}
