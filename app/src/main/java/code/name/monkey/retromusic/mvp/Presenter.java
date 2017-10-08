package code.name.monkey.retromusic.mvp;

import android.support.annotation.NonNull;

import code.name.monkey.retromusic.Injection;
import code.name.monkey.retromusic.providers.interfaces.Repository;
import code.name.monkey.retromusic.util.schedulers.BaseSchedulerProvider;

import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by hemanths on 16/08/17.
 */

public abstract class Presenter {
    @NonNull
    protected Repository repository;
    @NonNull
    protected CompositeDisposable disposable;
    @NonNull
    protected BaseSchedulerProvider schedulerProvider;

    public Presenter(@NonNull Repository repository) {
        this.repository = repository;
        this.schedulerProvider = Injection.provideSchedulerProvider();
        this.disposable = new CompositeDisposable();
    }
}
