package code.name.monkey.retromusic.ui.adapter.home;

import android.content.res.ColorStateList;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kabouzeid.appthemehelper.ThemeStore;
import com.kabouzeid.appthemehelper.util.ColorUtil;
import com.kabouzeid.appthemehelper.util.MaterialValueHelper;
import com.retro.musicplayer.backend.loaders.PlaylistSongsLoader;
import com.retro.musicplayer.backend.model.Playlist;

import java.util.ArrayList;

import butterknife.BindView;
import code.name.monkey.retromusic.R;
import code.name.monkey.retromusic.ui.adapter.base.MediaEntryViewHolder;
import code.name.monkey.retromusic.util.NavigationUtil;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by hemanths on 19/07/17.
 */

public class HomeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ArrayList<Playlist> dataSet = new ArrayList<>();
    private AppCompatActivity activity;

    public HomeAdapter(@NonNull AppCompatActivity activity) {
        this.activity = activity;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(this.activity)
                .inflate(R.layout.recycler_view_sec, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int i) {
        ViewHolder viewholder = (ViewHolder) holder;
        Playlist playlist = dataSet.get(holder.getAdapterPosition());
        if (viewholder.title != null) {
            viewholder.title.setVisibility(View.VISIBLE);
            viewholder.title.setText(playlist.name);
        }

        int aColor = ThemeStore.accentColor(activity);
        viewholder.dash.setBackgroundColor(aColor);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            viewholder.seeAll.setBackgroundTintList(ColorStateList.valueOf(aColor));
        } else {
            viewholder.seeAll.setBackgroundColor(aColor);
        }
        int color = MaterialValueHelper.getPrimaryTextColor(activity, ColorUtil.isColorLight(aColor));
        viewholder.seeAll.setTextColor(color);
        viewholder.seeAll.setOnClickListener(v -> {
            NavigationUtil.goToPlaylistNew(activity, playlist);
        });

        if (viewholder.recyclerView != null) {
            viewholder.recyclerView.setHasFixedSize(true);
            viewholder.recyclerView.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false));
            viewholder.recyclerView.setItemAnimator(new DefaultItemAnimator());
            PlaylistSongsLoader.getPlaylistSongList(activity, playlist)
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(songs -> viewholder.recyclerView.setAdapter(new HorizontalItemAdapter(activity, songs)));
        }


    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    public void swapData(@NonNull ArrayList<Playlist> data) {
        dataSet.clear();
        dataSet.addAll(data);
        notifyDataSetChanged();
    }

    public class ViewHolder extends MediaEntryViewHolder {
        @BindView(R.id.dash)
        View dash;
        @BindView(R.id.see_all)
        TextView seeAll;

        public ViewHolder(View itemView) {
            super(itemView);
        }
    }
}
