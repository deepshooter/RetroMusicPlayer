package com.retro.musicplayer.backend.model;

import android.content.Context;
import android.os.Parcel;
import android.support.annotation.NonNull;

import java.util.ArrayList;

import io.reactivex.Observable;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */

public abstract class AbsCustomPlaylist extends Playlist {
    public AbsCustomPlaylist(int id, String name) {
        super(id, name);
    }

    public AbsCustomPlaylist() {
    }

    public AbsCustomPlaylist(Parcel in) {
        super(in);
    }

    @NonNull
    public abstract Observable<ArrayList<Song>> getSongs(Context context);
}

