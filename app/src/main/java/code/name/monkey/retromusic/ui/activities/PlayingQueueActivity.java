package code.name.monkey.retromusic.ui.activities;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.kabouzeid.appthemehelper.ThemeStore;

import butterknife.BindDrawable;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import code.name.monkey.retromusic.R;
import code.name.monkey.retromusic.helper.MusicPlayerRemote;
import code.name.monkey.retromusic.ui.activities.base.AbsMusicServiceActivity;
import code.name.monkey.retromusic.ui.fragments.PlayingQueueFragment;
import code.name.monkey.retromusic.util.MusicUtil;

/**
 * Created by BlackFootSanji on 4/15/2017.
 */

public class PlayingQueueActivity extends AbsMusicServiceActivity {
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindDrawable(R.drawable.ic_close_white_24dp)
    Drawable mClose;
    @BindView(R.id.player_queue_sub_header)
    TextView mPlayerQueueSubHeader;
    @BindString(R.string.queue)
    String queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playing_queue);
        ButterKnife.bind(this);

        setStatusbarColorAuto();
        setNavigationbarColorAuto();
        setTaskDescriptionColorAuto();

        setupToolbar();
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new PlayingQueueFragment())
                    .commit();
        }
    }

    protected String getUpNextAndQueueTime() {
        return getResources().getString(R.string.up_next) + "  •  " + MusicUtil.getReadableDurationString(MusicPlayerRemote.getQueueDurationMillis(MusicPlayerRemote.getPosition()));
    }

    private void setupToolbar() {
        mPlayerQueueSubHeader.setText(getUpNextAndQueueTime());
        mPlayerQueueSubHeader.setTextColor(ThemeStore.accentColor(this));
        mToolbar.setBackgroundColor(ThemeStore.primaryColor(this));
        mToolbar.setNavigationIcon(mClose);
        setSupportActionBar(mToolbar);
        setTitle(queue);
        mToolbar.setNavigationOnClickListener(v -> onBackPressed());
    }
}
