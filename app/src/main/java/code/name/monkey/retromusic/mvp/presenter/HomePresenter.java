package code.name.monkey.retromusic.mvp.presenter;

import android.support.annotation.NonNull;

import code.name.monkey.retromusic.mvp.Presenter;
import code.name.monkey.retromusic.mvp.contract.HomeContract;
import code.name.monkey.retromusic.providers.interfaces.Repository;

/**
 * Created by hemanths on 20/08/17.
 */

public class HomePresenter extends Presenter implements HomeContract.HomePresenter {
    @NonNull
    private HomeContract.HomeView view;

    public HomePresenter(@NonNull Repository repository,
                         @NonNull HomeContract.HomeView view) {
        super(repository);
        this.view = view;
    }


    @Override
    public void subscribe() {
        loadHomes();
    }

    @Override
    public void unsubscribe() {
        disposable.clear();
    }

    @Override
    public void loadHomes() {
        disposable.add(repository.getHomeList()
                .subscribeOn(schedulerProvider.computation())
                .observeOn(schedulerProvider.ui())
                .doOnSubscribe(disposable1 -> view.loading())
                .doOnComplete(() -> view.completed())
                .subscribe(homes -> view.showList(homes),
                        throwable -> view.showEmptyView()));
    }
}
