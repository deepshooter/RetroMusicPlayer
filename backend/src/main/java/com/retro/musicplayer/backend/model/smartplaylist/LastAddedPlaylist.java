package com.retro.musicplayer.backend.model.smartplaylist;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.retro.musicplayer.backend.R;
import com.retro.musicplayer.backend.loaders.LastAddedSongsLoader;
import com.retro.musicplayer.backend.model.Song;

import java.util.ArrayList;

import io.reactivex.Observable;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class LastAddedPlaylist extends AbsSmartPlaylist {

    public static final Parcelable.Creator<LastAddedPlaylist> CREATOR = new Parcelable.Creator<LastAddedPlaylist>() {
        public LastAddedPlaylist createFromParcel(Parcel source) {
            return new LastAddedPlaylist(source);
        }

        public LastAddedPlaylist[] newArray(int size) {
            return new LastAddedPlaylist[size];
        }
    };

    public LastAddedPlaylist(@NonNull Context context) {
        super(context.getString(R.string.last_added), R.drawable.ic_library_add_white_24dp);
    }

    protected LastAddedPlaylist(Parcel in) {
        super(in);
    }

    @NonNull
    @Override
    public Observable<ArrayList<Song>> getSongs(@NonNull Context context) {
        return LastAddedSongsLoader.getLastAddedSongs(context);
    }

    @Override
    public void clear(@NonNull Context context) {
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
