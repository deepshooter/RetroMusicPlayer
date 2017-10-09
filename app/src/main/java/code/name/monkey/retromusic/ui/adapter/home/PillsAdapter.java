package code.name.monkey.retromusic.ui.adapter.home;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.retro.musicplayer.backend.model.Home;

import java.util.ArrayList;

import code.name.monkey.retromusic.ui.adapter.base.MediaEntryViewHolder;

/**
 * Created by hemanths on 01/08/17.
 */

public class PillsAdapter extends RecyclerView.Adapter<MediaEntryViewHolder> {

    private static final String TAG = "HomeAdapter";
    private Context mContext;
    private ArrayList<Home> mHomes = new ArrayList<>();
    private int mItemLayout;

    public PillsAdapter(Context context, ArrayList<Home> homes, int itemLayout) {
        mContext = context;
        mHomes = homes;
        mItemLayout = itemLayout;
    }

    @Override
    public MediaEntryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MediaEntryViewHolder(LayoutInflater.from(mContext).inflate(mItemLayout, parent, false));
    }

    @Override
    public void onBindViewHolder(MediaEntryViewHolder holder, int position) {
        Log.i(TAG, "onBindViewHolder: " + mHomes.get(position).getList().size());
        final Home home = mHomes.get(position);
        if (holder.title != null) {
            holder.title.setText(home.getSectionTitle());
        }

    }

    @Override
    public int getItemCount() {
        return mHomes.size();
    }
}
