package code.name.monkey.retromusic.ui.activities.base;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.FloatRange;
import android.support.annotation.LayoutRes;
import android.support.design.widget.BottomNavigationView;
import android.support.transition.TransitionManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SlidingPaneLayout;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.kabouzeid.appthemehelper.ThemeStore;
import com.kabouzeid.appthemehelper.util.ATHUtil;
import com.kabouzeid.appthemehelper.util.ColorUtil;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import code.name.monkey.retromusic.R;
import code.name.monkey.retromusic.helper.BottomNavigationViewHelper;
import code.name.monkey.retromusic.helper.MusicPlayerRemote;
import code.name.monkey.retromusic.ui.fragments.MiniPlayerFragment;
import code.name.monkey.retromusic.ui.fragments.base.AbsPlayerFragment;
import code.name.monkey.retromusic.ui.fragments.player.NowPlayingScreen;
import code.name.monkey.retromusic.ui.fragments.player.flat.FlatPlayerFragment;
import code.name.monkey.retromusic.ui.fragments.player.normal.PlayerFragment;
import code.name.monkey.retromusic.util.PreferenceUtil;

/**
 * @author Karim Abou Zeid (kabouzeid)
 *         <p/>
 *         Do not use {@link #setContentView(int)}. Instead wrap your layout with
 *         {@link #wrapSlidingMusicPanel(int)} first and then return it in {@link #createContentView()}
 */
public abstract class AbsSlidingMusicPanelActivity
        extends AbsMusicServiceActivity
        implements BottomNavigationView.OnNavigationItemSelectedListener,
        SlidingUpPanelLayout.PanelSlideListener, PlayerFragment.Callbacks {
    public static final String TAG = AbsSlidingMusicPanelActivity.class.getSimpleName();
    @BindView(R.id.bottom_navigation)
    BottomNavigationView mBottomNavigationView;
    @BindView(R.id.sliding_layout)
    SlidingUpPanelLayout mSlidingUpPanelLayout;
    @BindView(R.id.root_layout)
    ViewGroup mViewGroup;
    private int mTaskColor;
    private boolean mLightStatusbar;
    private NowPlayingScreen currentNowPlayingScreen;
    private AbsPlayerFragment mPlayerFragment;
    private MiniPlayerFragment mMiniPlayerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(createContentView());
        ButterKnife.bind(this);

        currentNowPlayingScreen = PreferenceUtil.getInstance(this).getNowPlayingScreen();
        Fragment fragment; // must implement AbsPlayerFragment
        switch (currentNowPlayingScreen) {
            case FLAT:
                fragment = new FlatPlayerFragment();
                break;
            /*case FULL:
                fragment = new FullPlayerFragment();
                break;*/
            case NORMAL:
            default:
                fragment = new PlayerFragment();
                break;
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.player_fragment_container, fragment).commit();
        getSupportFragmentManager().executePendingTransactions();

        mPlayerFragment = (AbsPlayerFragment) getSupportFragmentManager().findFragmentById(R.id.player_fragment_container);
        mMiniPlayerFragment = (MiniPlayerFragment) getSupportFragmentManager().findFragmentById(R.id.mini_player_fragment);

        //noinspection ConstantConditions
        mMiniPlayerFragment.getView().setOnClickListener(v -> expandPanel());
        mSlidingUpPanelLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mSlidingUpPanelLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                if (getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
                    onPanelSlide(mSlidingUpPanelLayout, 1);
                    onPanelExpanded(mSlidingUpPanelLayout);
                } else if (getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                    onPanelCollapsed(mSlidingUpPanelLayout);
                } else {
                    mPlayerFragment.onHide();
                }
            }
        });

        setupBottomView();
        mSlidingUpPanelLayout.addPanelSlideListener(this);
    }

    private void setupBottomView() {
        mBottomNavigationView.setOnNavigationItemSelectedListener(this);
        BottomNavigationViewHelper.disableShiftMode(this, mBottomNavigationView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentNowPlayingScreen != PreferenceUtil.getInstance(this).getNowPlayingScreen()) {
            postRecreate();
        }
    }

    public void setAntiDragView(View antiDragView) {
        mSlidingUpPanelLayout.setAntiDragView(antiDragView);
    }

    protected abstract View createContentView();

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        if (!MusicPlayerRemote.getPlayingQueue().isEmpty()) {
            mSlidingUpPanelLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mSlidingUpPanelLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    hideBottomBar(false);
                }
            });
        }// don't call hideBottomBar(true) here as it causes a bug with the SlidingUpPanelLayout
    }

    @Override
    public void onQueueChanged() {
        super.onQueueChanged();
        hideBottomBar(MusicPlayerRemote.getPlayingQueue().isEmpty());
    }

    @Override
    public void onPanelSlide(View panel, @FloatRange(from = 0, to = 1) float slideOffset) {
        mBottomNavigationView.setTranslationY(slideOffset * 300);
        setMiniPlayerAlphaProgress(slideOffset);
    }

    @Override
    public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
        switch (newState) {
            case COLLAPSED:
                onPanelCollapsed(panel);
                break;
            case EXPANDED:
                onPanelExpanded(panel);
                break;
            case ANCHORED:
                collapsePanel(); // this fixes a bug where the panel would get stuck for some reason
                break;
        }
    }

    public void onPanelCollapsed(View panel) {
        // restore values
        super.setLightStatusbar(mLightStatusbar);
        super.setTaskDescriptionColor(mTaskColor);
        //setNavigationbarColor(ColorUtil.darkenColor(ThemeStore.primaryColor(this)));
        setNavigationbarColor(ColorUtil.darkenColor(ThemeStore.primaryColor(this)));
        //super.setNavigationbarColor(mNavigationbarColor);

        mPlayerFragment.setMenuVisibility(false);
        mPlayerFragment.setUserVisibleHint(false);
        mPlayerFragment.onHide();

    }

    public void onPanelExpanded(View panel) {
        // setting fragments values
        int playerFragmentColor = mPlayerFragment.getPaletteColor();
        if (PreferenceUtil.getInstance(this).getAdaptiveColor() || ATHUtil.isWindowBackgroundDark(this)
                /*|| (currentNowPlayingScreen == NowPlayingScreen.FULL)*/) {
            super.setLightStatusbar(false);
        } else
            super.setLightStatusbar(true);
        super.setTaskDescriptionColor(playerFragmentColor);
        super.setNavigationbarColor(ColorUtil.darkenColor(ThemeStore.primaryColor(this)));

        mPlayerFragment.setMenuVisibility(true);
        mPlayerFragment.setUserVisibleHint(true);
        mPlayerFragment.onShow();
    }

    private void setMiniPlayerAlphaProgress(@FloatRange(from = 0, to = 1) float progress) {
        if (mMiniPlayerFragment.getView() == null) return;
        float alpha = 1 - progress;
        mMiniPlayerFragment.getView().setAlpha(alpha);
        // necessary to make the views below clickable
        mMiniPlayerFragment.getView().setVisibility(alpha == 0 ? View.GONE : View.VISIBLE);
    }

    public SlidingUpPanelLayout.PanelState getPanelState() {
        return mSlidingUpPanelLayout == null ? null : mSlidingUpPanelLayout.getPanelState();
    }

    public void collapsePanel() {
        mSlidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
    }

    public void expandPanel() {
        mSlidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
    }

    public void hideBottomBar(final boolean hide) {
        /*if (hide) {
            mSlidingUpPanelLayout.setPanelHeight(0);
            collapsePanel();
        } else {
            mSlidingUpPanelLayout.setPanelHeight(getResources().getDimensionPixelSize(R.dimen.mini_player_height_expanded));
        }*/
        if (hide) {
            mSlidingUpPanelLayout.setPanelHeight(0);
            collapsePanel();
        } else {
            if (!MusicPlayerRemote.getPlayingQueue().isEmpty())
                if (mBottomNavigationView.getVisibility() == View.VISIBLE) {
                    mSlidingUpPanelLayout.setPanelHeight(getResources().getDimensionPixelSize(R.dimen.mini_player_height_expanded));
                } else {
                    mSlidingUpPanelLayout.setPanelHeight(getResources().getDimensionPixelSize(R.dimen.mini_player_height));
                }
        }
    }

    public void setBottomBarVisibility(int gone) {
        if (mBottomNavigationView != null) {
            TransitionManager.beginDelayedTransition(mBottomNavigationView);
            mBottomNavigationView.setVisibility(gone);
            hideBottomBar(false);
        }
    }

    protected View wrapSlidingMusicPanel(@LayoutRes int resId) {
        @SuppressLint("InflateParams")
        View slidingMusicPanelLayout = getLayoutInflater().inflate(R.layout.sliding_music_panel_layout, null);
        ViewGroup contentContainer = ButterKnife.findById(slidingMusicPanelLayout, R.id.content_container);
        getLayoutInflater().inflate(resId, contentContainer);
        return slidingMusicPanelLayout;
    }

    @Override
    public void onBackPressed() {
        if (!handleBackPress())
            super.onBackPressed();
    }

    public boolean handleBackPress() {
        if (mSlidingUpPanelLayout.getPanelHeight() != 0 && mPlayerFragment.onBackPressed())
            return true;
        if (getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
            collapsePanel();
            return true;
        }
        return false;
    }

    @Override
    public void onPaletteColorChanged() {
        if (getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
            int playerFragmentColor = mPlayerFragment.getPaletteColor();
            super.setTaskDescriptionColor(playerFragmentColor);
            //animateNavigationBarColor(playerFragmentColor);
        }
    }


    @Override
    public void setLightStatusbar(boolean enabled) {
        mLightStatusbar = enabled;
        if (getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) {
            super.setLightStatusbar(enabled);
        }
    }

    @Override
    public void setTaskDescriptionColor(@ColorInt int color) {
        mTaskColor = color;
        if (getPanelState() == null || getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) {
            super.setTaskDescriptionColor(color);
        }
    }

    @Override
    protected View getSnackBarContainer() {
        return findViewById(R.id.content_container);
    }

    public SlidingUpPanelLayout getSlidingUpPanelLayout() {
        return mSlidingUpPanelLayout;
    }

    public MiniPlayerFragment getMiniPlayerFragment() {
        return mMiniPlayerFragment;
    }

    public AbsPlayerFragment getPlayerFragment() {
        return mPlayerFragment;
    }

    public BottomNavigationView getBottomNavigationView() {
        return mBottomNavigationView;
    }
}
