package code.name.monkey.retromusic.mvp.presenter;

import android.support.annotation.NonNull;

import code.name.monkey.retromusic.model.Artist;
import code.name.monkey.retromusic.mvp.Presenter;
import code.name.monkey.retromusic.mvp.contract.ArtistDetailContract;
import code.name.monkey.retromusic.providers.interfaces.Repository;

/**
 * Created by hemanths on 20/08/17.
 */

public class ArtistDetailsPresenter extends Presenter
        implements ArtistDetailContract.Presenter {
    private static final String TAG = "ArtistDetailsPresenter";
    @NonNull
    private final int artistId;
    @NonNull
    private final ArtistDetailContract.ArtistsDetailsView view;

    public ArtistDetailsPresenter(@NonNull Repository repository,
                                  @NonNull ArtistDetailContract.ArtistsDetailsView view,
                                  @NonNull int artistId) {
        super(repository);
        this.view = view;
        this.artistId = artistId;
    }

    @Override
    public void subscribe() {
        loadArtistById(artistId);
    }

    @Override
    public void unsubscribe() {
        disposable.clear();
    }

    @Override
    public void loadArtistById(int artistId) {
        disposable.add(repository.getArtistById(artistId)
                .subscribeOn(schedulerProvider.computation())
                .observeOn(schedulerProvider.ui())
                .doOnSubscribe(disposable1 -> view.loading())
                .subscribe(this::showArtist,
                        throwable -> view.showEmptyView(),
                        () -> view.completed()));
    }

    private void showArtist(Artist album) {
        if (album != null) {
            view.showArtist(album);
        } else {
            view.showEmptyView();
        }
    }
}
