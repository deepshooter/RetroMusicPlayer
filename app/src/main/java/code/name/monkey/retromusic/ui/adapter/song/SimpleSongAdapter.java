package code.name.monkey.retromusic.ui.adapter.song;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.retro.musicplayer.backend.model.Song;

import java.util.ArrayList;

import code.name.monkey.retromusic.helper.MusicPlayerRemote;

import code.name.monkey.retromusic.util.MusicUtil;

/**
 * Created by Monkey D Luffy on 3/31/2016.
 */
public class SimpleSongAdapter extends SongAdapter {

    public SimpleSongAdapter(AppCompatActivity context, ArrayList<Song> songs, @LayoutRes int i) {
        super(context, songs, i, false, null);
    }

    public void swapDataSet(ArrayList<Song> arrayList) {
        this.dataSet.clear();
        this.dataSet = arrayList;
        notifyDataSetChanged();
    }

    @NonNull
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(activity).inflate(itemLayoutRes, parent, false));
    }

    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        Song song = dataSet.get(position);

        int fixedTrackNumber = MusicUtil.getFixedTrackNumber(song.trackNumber);

        if (holder.imageText != null) {
            holder.imageText.setText(fixedTrackNumber > 0 ? String.valueOf(fixedTrackNumber) : "-");
        }
        if (holder.title != null) {
            holder.title.setText(song.title);
        }
        if (holder.text != null) {
            holder.text.setText(song.artistName);
        }
        if (holder.time != null) {
            holder.time.setText(MusicUtil.getReadableDurationString(song.duration));
        }
        holder.itemView.setOnClickListener(v -> MusicPlayerRemote.openQueue(dataSet, holder.getAdapterPosition(), true));

    }

    public int getItemCount() {
        return dataSet.size();
    }

}
