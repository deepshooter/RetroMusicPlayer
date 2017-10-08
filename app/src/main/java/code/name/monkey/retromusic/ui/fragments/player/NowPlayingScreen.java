package code.name.monkey.retromusic.ui.fragments.player;

import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

import code.name.monkey.retromusic.R;


public enum NowPlayingScreen {
    NORMAL(R.string.normal, R.drawable.np_normal, 0),
    FLAT(R.string.flat, R.drawable.np_flat, 1);
    //FULL(R.string.full, R.drawable.np_full, 2);

    @StringRes
    public final int titleRes;
    @DrawableRes
    public final int drawableResId;
    public final int id;

    NowPlayingScreen(@StringRes int titleRes, @DrawableRes int drawableResId, int id) {
        this.titleRes = titleRes;
        this.drawableResId = drawableResId;
        this.id = id;
    }
}
