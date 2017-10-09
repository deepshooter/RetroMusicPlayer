package com.retro.musicplayer.backend;

import android.content.Context;
import android.support.annotation.NonNull;

import com.retro.musicplayer.backend.providers.RepositoryImpl;
import com.retro.musicplayer.backend.providers.interfaces.Repository;
import com.retro.musicplayer.backend.util.schedulers.BaseSchedulerProvider;
import com.retro.musicplayer.backend.util.schedulers.SchedulerProvider;


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
