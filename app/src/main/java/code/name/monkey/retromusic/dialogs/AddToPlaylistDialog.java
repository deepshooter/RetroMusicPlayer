package code.name.monkey.retromusic.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;

import code.name.monkey.retromusic.R;
import code.name.monkey.retromusic.loaders.PlaylistLoader;
import code.name.monkey.retromusic.model.Playlist;
import code.name.monkey.retromusic.model.Song;
import code.name.monkey.retromusic.util.PlaylistsUtil;


/**
 * @author Karim Abou Zeid (kabouzeid), Aidan Follestad (afollestad)
 */
public class AddToPlaylistDialog extends DialogFragment {

    private ArrayList<Playlist> mPlaylist;

    @NonNull
    public static AddToPlaylistDialog create(Song song) {
        ArrayList<Song> list = new ArrayList<>();
        list.add(song);
        return create(list);
    }

    @NonNull
    public static AddToPlaylistDialog create(ArrayList<Song> songs) {
        AddToPlaylistDialog dialog = new AddToPlaylistDialog();
        Bundle args = new Bundle();
        args.putParcelableArrayList("songs", songs);
        dialog.setArguments(args);
        return dialog;
    }

    public ArrayList<Playlist> getPlaylist() {
        return mPlaylist;
    }

    public void setPlaylist(ArrayList<Playlist> playlist) {
        mPlaylist = playlist;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        PlaylistLoader.getAllPlaylists(getActivity()).subscribe(this::setPlaylist);

        CharSequence[] playlistNames = new CharSequence[getPlaylist().size() + 1];
        playlistNames[0] = getActivity().getResources().getString(R.string.action_new_playlist);
        for (int i = 1; i < playlistNames.length; i++) {
            playlistNames[i] = getPlaylist().get(i - 1).name;
        }
        return new MaterialDialog.Builder(getActivity())
                .title(R.string.add_playlist_title)
                .items(playlistNames)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(@NonNull MaterialDialog materialDialog, View view, int i, CharSequence charSequence) {
                        //noinspection unchecked
                        final ArrayList<Song> songs = getArguments().getParcelableArrayList("songs");
                        if (songs == null) return;
                        if (i == 0) {
                            materialDialog.dismiss();
                            CreatePlaylistDialog.create(songs).show(getActivity().getSupportFragmentManager(), "ADD_TO_PLAYLIST");
                        } else {
                            materialDialog.dismiss();
                            PlaylistsUtil.addToPlaylist(getActivity(), songs, getPlaylist().get(i - 1).id, true);
                        }
                    }
                })
                .build();
    }
}