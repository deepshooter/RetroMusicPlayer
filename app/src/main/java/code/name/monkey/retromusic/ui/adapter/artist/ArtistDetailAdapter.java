package code.name.monkey.retromusic.ui.adapter.artist;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kabouzeid.appthemehelper.ThemeStore;

import java.util.ArrayList;

import butterknife.ButterKnife;
import code.name.monkey.retromusic.R;
import code.name.monkey.retromusic.model.Album;
import code.name.monkey.retromusic.model.Song;
import code.name.monkey.retromusic.ui.adapter.HorizontalAlbumAdapter;
import code.name.monkey.retromusic.ui.adapter.base.MediaEntryViewHolder;
import code.name.monkey.retromusic.ui.adapter.song.SimpleSongAdapter;

/**
 * Created by hemanths on 19/09/17.
 */

public class ArtistDetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int ALBUMS = 0;
    private static final int HEADER = 1;
    private static final int SONGS = 2;
    private static final String TAG = "ArtistDetailAdapter";
    private ArrayList<Object> mList = new ArrayList<>();
    private AppCompatActivity mActivity;

    public ArtistDetailAdapter(AppCompatActivity activity) {
        mActivity = activity;
    }

    @Override
    public int getItemViewType(int position) {
        try {
            if (mList.get(position) instanceof ArrayList<?>) {
                if (((ArrayList<?>) mList.get(position)).get(0) instanceof Song) {
                    return SONGS;
                }
            }
            if (mList.get(position) instanceof ArrayList<?>) {
                if (((ArrayList<?>) mList.get(position)).get(0) instanceof Album) {
                    return ALBUMS;
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return HEADER;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = null;
        switch (i) {
            case ALBUMS:
            case SONGS:
                view = LayoutInflater.from(mActivity).inflate(R.layout.recycler_view, viewGroup, false);
                break;
            case HEADER:
                view = LayoutInflater.from(mActivity).inflate(R.layout.artist_sub_header, viewGroup, false);
                break;
        }
        return new ArtistDetailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
        switch (getItemViewType(i)) {
            case ALBUMS:
                ArtistDetailViewHolder holder = (ArtistDetailViewHolder) viewHolder;
                if (holder.recyclerView != null) {
                    holder.recyclerView.setLayoutManager(new LinearLayoutManager(mActivity, LinearLayoutManager.HORIZONTAL, false));
                    try {
                        if (mList.get(i) instanceof ArrayList<?>) {
                            if (((ArrayList<?>) mList.get(i)).get(0) instanceof Album) {
                                holder.recyclerView.setAdapter(new HorizontalAlbumAdapter(mActivity, (ArrayList<Album>) mList.get(i), false, null));
                            }
                        }
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case SONGS:
                ArtistDetailViewHolder songsHolder = (ArtistDetailViewHolder) viewHolder;
                if (songsHolder.recyclerView != null) {
                    songsHolder.recyclerView.setLayoutManager(new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false));
                    try {
                        if (mList.get(i) instanceof ArrayList<?>) {
                            if (((ArrayList<?>) mList.get(i)).get(0) instanceof Song) {
                                songsHolder.recyclerView.setAdapter(new SimpleSongAdapter(mActivity, (ArrayList<Song>) mList.get(i), R.layout.item_song));
                            }
                        }
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case HEADER:
                ArtistDetailViewHolder titleHolder = (ArtistDetailViewHolder) viewHolder;
                if (titleHolder.title != null) {
                    titleHolder.title.setText(mList.get(i).toString());
                    titleHolder.title.setTextColor(ThemeStore.accentColor(mActivity));
                }
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void swapData(ArrayList<Object> list) {
        mList = list;
        notifyDataSetChanged();
    }

    class ArtistDetailViewHolder extends MediaEntryViewHolder {
        ArtistDetailViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
