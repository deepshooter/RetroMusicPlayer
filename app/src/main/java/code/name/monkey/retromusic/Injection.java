package code.name.monkey.retromusic;

import android.content.Context;
import android.support.annotation.NonNull;

import code.name.monkey.retromusic.providers.RepositoryImpl;
import code.name.monkey.retromusic.providers.interfaces.Repository;
import code.name.monkey.retromusic.util.schedulers.BaseSchedulerProvider;
import code.name.monkey.retromusic.util.schedulers.SchedulerProvider;

/**
 * Created by hemanths on 12/08/17.
 */

public class Injection {
    public static Repository provideRepository(@NonNull Context context) {
        return RepositoryImpl.getInstance(context);
    }

    public static BaseSchedulerProvider provideSchedulerProvider() {
        return SchedulerProvider.getInstance();
    }
}
