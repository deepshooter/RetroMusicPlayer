package code.name.monkey.retromusic.ui.adapter.home;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.retro.musicplayer.backend.model.Song;

import java.util.ArrayList;

import code.name.monkey.retromusic.R;
import code.name.monkey.retromusic.glide.SongGlideRequest;
import code.name.monkey.retromusic.helper.MusicPlayerRemote;

import code.name.monkey.retromusic.ui.adapter.base.MediaEntryViewHolder;

/**
 * Created by hemanths on 19/07/17.
 */

public class HorizontalItemAdapter extends RecyclerView.Adapter<HorizontalItemAdapter.ViewHolder> {
    private final static int ARTIST = 1;
    private final static int ALBUM = 2;
    private final static int SONG = 3;
    private static final int HEADER = 0;
    private static final String TAG = "HorizontalItemAdapter";
    private AppCompatActivity activity;
    private ArrayList<Song> dataSet = new ArrayList<>();

    public HorizontalItemAdapter(@NonNull AppCompatActivity activity, @NonNull ArrayList<Song> dataSet) {
        this.activity = activity;
        this.dataSet = dataSet;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(activity).inflate(R.layout.item_image, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        Song song = dataSet.get(position);
        if (viewHolder.title != null) {
            viewHolder.title.setText(song.title);
        }
        if (viewHolder.image != null) {
            SongGlideRequest.Builder.from(Glide.with(activity), song).checkIgnoreMediaStore(activity).asBitmap().build().into(viewHolder.image);
        }

    }

    @Override
    public int getItemCount() {
        return dataSet.size() < 10 ? dataSet.size() : 10;
    }

    public class ViewHolder extends MediaEntryViewHolder {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

        }

        @Override
        public void onClick(View view) {
            super.onClick(view);
            MusicPlayerRemote.openQueue(dataSet, getAdapterPosition(), true);
        }
    }
}
