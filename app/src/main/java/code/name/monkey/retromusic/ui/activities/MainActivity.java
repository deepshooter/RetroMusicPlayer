package code.name.monkey.retromusic.ui.activities;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import code.name.monkey.retromusic.R;
import code.name.monkey.retromusic.helper.MusicPlayerRemote;
import code.name.monkey.retromusic.helper.SearchQueryHelper;
import code.name.monkey.retromusic.interfaces.LibraryTabSelectedItem;
import code.name.monkey.retromusic.interfaces.MainActivityFragmentCallbacks;
import code.name.monkey.retromusic.loaders.AlbumLoader;
import code.name.monkey.retromusic.loaders.ArtistSongLoader;
import code.name.monkey.retromusic.loaders.PlaylistSongsLoader;
import code.name.monkey.retromusic.model.Song;
import code.name.monkey.retromusic.service.MusicService;
import code.name.monkey.retromusic.ui.activities.base.AbsSlidingMusicPanelActivity;
import code.name.monkey.retromusic.ui.fragments.mainactivity.AlbumsFragment;
import code.name.monkey.retromusic.ui.fragments.mainactivity.ArtistsFragment;
import code.name.monkey.retromusic.ui.fragments.mainactivity.LibraryFragment;
import code.name.monkey.retromusic.ui.fragments.mainactivity.PlaylistsFragment;
import code.name.monkey.retromusic.ui.fragments.mainactivity.SongsFragment;
import code.name.monkey.retromusic.ui.fragments.mainactivity.folders.FoldersFragment;
import code.name.monkey.retromusic.ui.fragments.mainactivity.home.HomeFragment;
import code.name.monkey.retromusic.util.Compressor;
import code.name.monkey.retromusic.util.PreferenceUtil;
import code.name.monkey.retromusic.util.Util;
import code.name.monkey.retromusic.util.ViewUtil;
import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static code.name.monkey.retromusic.Constants.USER_PROFILE;

public class MainActivity extends AbsSlidingMusicPanelActivity {
    public static final int APP_INTRO_REQUEST = 2323;
    public static final int APP_USER_INFO_REQUEST = 9003;
    public static final int REQUEST_CODE_THEME = 9002;
    private static final String TAG = "MainActivity";

    private static final int HOME = 0;
    private static final int LIBRARY = 1;
    private static final int FOLDERS = 2;
    private static final int SUPPORT_DIALOG = 3;
    private static final int SETTIINGS = 4;
    private static final int ABOUT = 5;
    @BindView(R.id.user_image)
    CircleImageView mUserImage;
    @Nullable
    MainActivityFragmentCallbacks mCurrentFragment;
    @Nullable
    LibraryTabSelectedItem mTabSelectedItem;
    @BindView(R.id.navigation_view)
    ViewGroup mNavigationView;
    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.today)
    TextView mToday;
    @BindView(R.id.user_info)
    LinearLayout mUserInfo;
    @BindView(R.id.welcome_message)
    TextView mWelcomeMessage;
    @BindView(R.id.navigation_item)
    RecyclerView mNavigationItems;
    private boolean mBlockRequestPermissions;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case Intent.ACTION_SCREEN_OFF:
                        if (PreferenceUtil.getInstance(context).getLockScreen() && MusicPlayerRemote.isPlaying()) {
                            context.startActivity(new Intent(context, LockScreenActivity.class));
                        }
                        break;
                    case Intent.ACTION_SCREEN_ON:
                        collapsePanel();
                        recreate();
                        break;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setDrawUnderStatusbar(true);
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        setBottomBarVisibility(View.VISIBLE);

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            Util.setStatusBarTranslucent(getWindow());
            mDrawerLayout.setFitsSystemWindows(false);
            mNavigationView.setFitsSystemWindows(false);
            //noinspection ConstantConditions
            findViewById(R.id.drawer_content_container).setFitsSystemWindows(false);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mDrawerLayout.setOnApplyWindowInsetsListener((view, windowInsets) -> {
                mNavigationView.dispatchApplyWindowInsets(windowInsets);
                return windowInsets.replaceSystemWindowInsets(0, 0, 0, 0);
            });
        }

        setUpNavigationView();

        if (checkUserName()) {
            startActivityForResult(new Intent(this, UserInfoActivity.class), APP_USER_INFO_REQUEST);
        }

        if (savedInstanceState == null) {
            setMusicChooser(PreferenceUtil.getInstance(this).getLastMusicChooser());
        } else {
            restoreCurrentFragment();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter screenOnOff = new IntentFilter();
        screenOnOff.addAction(Intent.ACTION_SCREEN_OFF);
        screenOnOff.addAction(Intent.ACTION_SCREEN_ON);
        registerReceiver(mBroadcastReceiver, screenOnOff);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBroadcastReceiver == null) {
            return;
        }
        unregisterReceiver(mBroadcastReceiver);
    }

    private boolean checkUserName() {
        return PreferenceUtil.getInstance(this).getUserName().isEmpty();
    }

    private void setMusicChooser(int key) {
        PreferenceUtil.getInstance(this).setLastMusicChooser(key);
        switch (key) {
            case FOLDERS:
                setCurrentFragment(FoldersFragment.newInstance(this));
                break;
            case HOME:
                setCurrentFragment(HomeFragment.newInstance());
                break;
            case LIBRARY:
            default:
                setCurrentFragment(LibraryFragment.newInstance());
                break;
        }
    }

    private void setupTitles() {
        if (!PreferenceUtil.getInstance(this).getUserName().isEmpty()) {
            mUserInfo.setVisibility(View.VISIBLE);
            mToday.setText(getCurrentDayText());
            mWelcomeMessage.setText(String.format("Hello, %s", PreferenceUtil.getInstance(this).getUserName()));
            loadImageFromStorage(PreferenceUtil.getInstance(this).getProfileImage());
        }
    }

    @OnClick(R.id.user_info)
    public void onViewClicked(View view) {
        startActivityForResult(new Intent(this, UserInfoActivity.class), APP_USER_INFO_REQUEST);
    }

    private String getCurrentDayText() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("d MMM, EEE", Locale.getDefault());
        return dateFormat.format(calendar.getTime());
    }


    private void setUpNavigationView() {
        setupTitles();
        mNavigationItems.setLayoutManager(new LinearLayoutManager(this));
        mNavigationItems.setItemAnimator(new DefaultItemAnimator());
        mNavigationItems.setAdapter(new NavigationItemsAdapter());
    }

    @Override
    protected View createContentView() {
        @SuppressLint("InflateParams")
        View contentView = getLayoutInflater().inflate(R.layout.activity_main_drawer_layout, null);
        ViewGroup drawerContent = contentView.findViewById(R.id.drawer_content_container);
        drawerContent.addView(wrapSlidingMusicPanel(R.layout.activity_main_content));
        return contentView;
    }

    public void setCurrentFragment(@Nullable Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment, TAG).commit();
        mCurrentFragment = (MainActivityFragmentCallbacks) fragment;
        mTabSelectedItem = (LibraryTabSelectedItem) fragment;
    }

    private void restoreCurrentFragment() {
        mTabSelectedItem = (LibraryTabSelectedItem) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        mCurrentFragment = (MainActivityFragmentCallbacks) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        PreferenceUtil.getInstance(this).setLastPage(item.getItemId());
        Observable.just(item)
                .throttleFirst(3, TimeUnit.SECONDS)
                .subscribe(menuItem -> {
                    if (mTabSelectedItem != null) {
                        switch (menuItem.getItemId()) {
                            case R.id.action_song:
                                mTabSelectedItem.selectedFragment(SongsFragment.newInstance());
                                break;
                            case R.id.action_album:
                                mTabSelectedItem.selectedFragment(AlbumsFragment.newInstance());
                                break;
                            case R.id.action_artist:
                                mTabSelectedItem.selectedFragment(ArtistsFragment.newInstance());
                                break;
                            case R.id.action_playlist:
                                mTabSelectedItem.selectedFragment(PlaylistsFragment.newInstance());
                                break;
                        }
                    }
                });
        return true;
    }

    private void handlePlaybackIntent(@Nullable Intent intent) {
        if (intent == null) {
            return;
        }

        Uri uri = intent.getData();
        String mimeType = intent.getType();
        boolean handled = false;

        if (intent.getAction() != null &&
                intent.getAction().equals(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH)) {
            final ArrayList<Song> songs = SearchQueryHelper.getSongs(this, intent.getExtras());

            if (MusicPlayerRemote.getShuffleMode() == MusicService.SHUFFLE_MODE_SHUFFLE) {
                MusicPlayerRemote.openAndShuffleQueue(songs, true);
            } else {
                MusicPlayerRemote.openQueue(songs, 0, true);
            }
            handled = true;
        }

        if (uri != null && uri.toString().length() > 0) {
            MusicPlayerRemote.playFromUri(uri);
            handled = true;
        } else if (MediaStore.Audio.Playlists.CONTENT_TYPE.equals(mimeType)) {
            final int id = (int) parseIdFromIntent(intent, "playlistId", "playlist");
            if (id >= 0) {
                int position = intent.getIntExtra("position", 0);
                PlaylistSongsLoader.getPlaylistSongList(this, id).subscribe(songs1 ->
                        MusicPlayerRemote.openQueue(songs1, position, true));

                handled = true;
            }
        } else if (MediaStore.Audio.Albums.CONTENT_TYPE.equals(mimeType)) {
            final int id = (int) parseIdFromIntent(intent, "albumId", "album");
            if (id >= 0) {
                int position = intent.getIntExtra("position", 0);
                AlbumLoader.getAlbum(this, id)
                        .subscribe(album -> MusicPlayerRemote.openQueue(album.songs, position, true));
                handled = true;
            }
        } else if (MediaStore.Audio.Artists.CONTENT_TYPE.equals(mimeType)) {
            final int id = (int) parseIdFromIntent(intent, "artistId", "artist");
            if (id >= 0) {
                int position = intent.getIntExtra("position", 0);
                ArtistSongLoader.getArtistSongList(this, id)
                        .subscribe(songs -> MusicPlayerRemote.openQueue(songs, position, true));
                handled = true;
            }
        }
        if (handled) {
            setIntent(new Intent());
        }
    }

    private long parseIdFromIntent(@NonNull Intent intent, String longKey,
                                   String stringKey) {
        long id = intent.getLongExtra(longKey, -1);
        if (id < 0) {
            String idString = intent.getStringExtra(stringKey);
            if (idString != null) {
                try {
                    id = Long.parseLong(idString);
                } catch (NumberFormatException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        }
        return id;
    }

    @Override
    public void onPanelExpanded(View view) {
        super.onPanelExpanded(view);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    @Override
    public void onPanelCollapsed(View view) {
        super.onPanelCollapsed(view);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case APP_INTRO_REQUEST:
                mBlockRequestPermissions = false;
                if (!hasPermissions()) {
                    requestPermissions();
                }
                break;
            case REQUEST_CODE_THEME:
            case APP_USER_INFO_REQUEST:
                setupTitles();
                break;
        }

    }

    @Override
    public boolean handleBackPress() {
        if (mDrawerLayout.isDrawerOpen(mNavigationView)) {
            mDrawerLayout.closeDrawers();
            return true;
        }
        return super.handleBackPress() || (mCurrentFragment != null &&
                mCurrentFragment.handleBackPress());
    }

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        handlePlaybackIntent(getIntent());
    }

    @Override
    protected void requestPermissions() {
        if (!mBlockRequestPermissions) super.requestPermissions();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (mDrawerLayout.isDrawerOpen(mNavigationView)) {
                mDrawerLayout.closeDrawer(mNavigationView);
            } else {
                mDrawerLayout.openDrawer(mNavigationView);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadImageFromStorage(@Nullable String path) {
        new Compressor(this)
                .setMaxHeight(300)
                .setMaxWidth(300)
                .setQuality(75)
                .setCompressFormat(Bitmap.CompressFormat.WEBP)
                .compressToBitmapAsFlowable(new File(path, USER_PROFILE))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bitmap -> mUserImage.setImageBitmap(bitmap),
                        throwable -> mUserImage.setImageDrawable(ContextCompat
                                .getDrawable(MainActivity.this, R.drawable.person_boy)));
    }

    class NavigationItemsAdapter extends RecyclerView.Adapter<NavigationItemsAdapter.ViewHolder> {
        List<Pair<Integer, Integer>> mList = new ArrayList<>();

        NavigationItemsAdapter() {
            mList.add(new Pair<>(R.drawable.ic_home_white_24dp, R.string.home));
            mList.add(new Pair<>(R.drawable.ic_library_add_white_24dp, R.string.library));
            mList.add(new Pair<>(R.drawable.ic_folder_white_24dp, R.string.folders));
            mList.add(new Pair<>(R.drawable.ic_favorite_white_24dp, R.string.support_development));
            mList.add(new Pair<>(R.drawable.ic_settings_white_24dp, R.string.action_settings));
            mList.add(new Pair<>(R.drawable.ic_help_white_24dp, R.string.action_about));
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            return new ViewHolder(LayoutInflater.from(MainActivity.this)
                    .inflate(R.layout.item_navigation_item, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int i) {
            Pair<Integer, Integer> pair = mList.get(i);
            viewHolder.mImageView.setImageResource(pair.first);
            viewHolder.mTitle.setText(pair.second);
            viewHolder.itemView.setOnClickListener(view -> {
                mDrawerLayout.closeDrawers();
                switch (viewHolder.getAdapterPosition()) {
                    case FOLDERS:
                        new Handler().postDelayed(() -> setMusicChooser(FOLDERS), 200);
                        break;
                    case HOME:
                        new Handler().postDelayed(() -> setMusicChooser(HOME), 200);
                        break;
                    case LIBRARY:
                        new Handler().postDelayed(() -> setMusicChooser(LIBRARY), 200);
                        break;
                    case SUPPORT_DIALOG:
                        new Handler().postDelayed(() -> ViewUtil.showDonationDialog(MainActivity.this), 200);
                        break;
                    case SETTIINGS:
                        new Handler().postDelayed(() -> startActivity(new Intent(MainActivity.this, SettingsActivity.class)), 200);
                        break;
                    case ABOUT:
                        new Handler().postDelayed(() -> startActivity(new Intent(MainActivity.this, AboutActivity.class)), 200);
                        break;
                }
            });
        }

        @Override
        public int getItemCount() {
            return mList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.title)
            TextView mTitle;
            @BindView(R.id.image)
            ImageView mImageView;

            public ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }

}
