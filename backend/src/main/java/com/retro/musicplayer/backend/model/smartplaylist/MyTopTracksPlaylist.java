package com.retro.musicplayer.backend.model.smartplaylist;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.retro.musicplayer.backend.R;
import com.retro.musicplayer.backend.loaders.TopAndRecentlyPlayedTracksLoader;
import com.retro.musicplayer.backend.model.Song;
import com.retro.musicplayer.backend.providers.SongPlayCountStore;

import java.util.ArrayList;

import io.reactivex.Observable;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class MyTopTracksPlaylist extends AbsSmartPlaylist implements Parcelable {
    public static final Creator<MyTopTracksPlaylist> CREATOR = new Creator<MyTopTracksPlaylist>() {
        public MyTopTracksPlaylist createFromParcel(Parcel source) {
            return new MyTopTracksPlaylist(source);
        }

        public MyTopTracksPlaylist[] newArray(int size) {
            return new MyTopTracksPlaylist[size];
        }
    };

    public MyTopTracksPlaylist(@NonNull Context context) {
        super(context.getString(R.string.my_top_tracks), R.drawable.ic_trending_up_white_24dp);
    }

    protected MyTopTracksPlaylist(Parcel in) {
        super(in);
    }

    @NonNull
    @Override
    public Observable<ArrayList<Song>> getSongs(@NonNull Context context) {
        return TopAndRecentlyPlayedTracksLoader.getTopTracks(context);
    }

    @Override
    public void clear(@NonNull Context context) {
        SongPlayCountStore.getInstance(context).clear();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

}