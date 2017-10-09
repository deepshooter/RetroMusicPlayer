package com.retro.musicplayer.backend.mvp.presenter;

import android.support.annotation.NonNull;

import com.retro.musicplayer.backend.model.Playlist;
import com.retro.musicplayer.backend.mvp.contract.PlaylistSongsContract;
import com.retro.musicplayer.backend.providers.interfaces.Repository;

import com.retro.musicplayer.backend.mvp.Presenter;
/**
 * Created by hemanths on 20/08/17.
 */

public class PlaylistSongsPresenter extends Presenter
        implements PlaylistSongsContract.Presenter {
    @NonNull
    private PlaylistSongsContract.PlaylistSongsView mView;
    @NonNull
    private Playlist mPlaylist;

    public PlaylistSongsPresenter(@NonNull Repository repository,
                                  @NonNull PlaylistSongsContract.PlaylistSongsView view,
                                  @NonNull Playlist playlist) {
        super(repository);
        mView = view;
        mPlaylist = playlist;
    }


    @Override
    public void subscribe() {
        loadSongs(mPlaylist);
    }

    @Override
    public void unsubscribe() {
        disposable.clear();
    }

    @Override
    public void loadSongs(Playlist playlist) {
        disposable.add(repository.getPlaylistSongs(playlist)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .doOnSubscribe(disposable1 -> mView.loading())
                .subscribe(songs -> mView.showSongs(songs),
                        throwable -> mView.showEmptyView(),
                        () -> mView.completed()));
    }
}
