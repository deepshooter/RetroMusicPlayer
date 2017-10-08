package code.name.monkey.retromusic.ui.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.transition.TransitionManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.ViewPager;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.TwoStatePreference;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.afollestad.materialdialogs.internal.ThemeSingleton;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.kabouzeid.appthemehelper.common.prefs.supportv7.ATEColorPreference;
import com.kabouzeid.appthemehelper.common.prefs.supportv7.ATEPreferenceFragmentCompat;
import com.kabouzeid.appthemehelper.util.ATHUtil;
import com.kabouzeid.appthemehelper.util.ColorUtil;
import com.kabouzeid.appthemehelper.util.TabLayoutUtil;
import com.kabouzeid.appthemehelper.util.ToolbarContentTintHelper;

import butterknife.BindView;
import butterknife.ButterKnife;
import code.name.monkey.retromusic.Constants;
import code.name.monkey.retromusic.R;
import code.name.monkey.retromusic.appshortcuts.DynamicShortcutManager;
import code.name.monkey.retromusic.helper.MusicPlayerRemote;
import code.name.monkey.retromusic.preferences.BlacklistPreference;
import code.name.monkey.retromusic.preferences.BlacklistPreferenceDialog;
import code.name.monkey.retromusic.preferences.NowPlayingScreenPreference;
import code.name.monkey.retromusic.preferences.NowPlayingScreenPreferenceDialog;
import code.name.monkey.retromusic.service.MusicService;
import code.name.monkey.retromusic.ui.activities.base.AbsBaseActivity;
import code.name.monkey.retromusic.ui.adapter.SettingsPagerAdapter;
import code.name.monkey.retromusic.util.NavigationUtil;
import code.name.monkey.retromusic.util.PreferenceUtil;
import de.psdev.licensesdialog.LicensesDialog;


/**
 * Created by BlackFootSanji on 2/19/2017.
 */

public class SettingsActivity extends AbsBaseActivity
        implements ColorChooserDialog.ColorCallback {
    private static final String TAG = "Settings";
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.app_bar)
    AppBarLayout mAppBarLayout;
    @BindView(R.id.tabs)
    TabLayout mTabLayout;
    @BindView(R.id.pager)
    ViewPager mViewPager;

    @Override
    public void onColorSelection(@NonNull ColorChooserDialog dialog, @ColorInt int selectedColor) {
        switch (dialog.getTitle()) {
            case R.string.primary_color:
                ThemeStore.editTheme(this).primaryColor(selectedColor).commit();
                break;
            case R.string.accent_color:
                ThemeStore.editTheme(this).accentColor(selectedColor).commit();
                break;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            new DynamicShortcutManager(this).updateDynamicShortcuts();
        }
        recreate();
    }

    @Override
    public void onColorChooserDismissed(@NonNull ColorChooserDialog dialog) {

    }

    @Override
    protected void onCreate(@Nullable Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        setStatusbarColorAuto();
        setNavigationbarColorAuto();
        setTaskDescriptionColorAuto();

        setupToolbar();

        SettingsPagerAdapter settingsPagerAdapter = new SettingsPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(settingsPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);

        int primaryColor = ThemeStore.primaryColor(this);
        int normalColor = ToolbarContentTintHelper.toolbarSubtitleColor(this, primaryColor);
        int selectedColor = ToolbarContentTintHelper.toolbarTitleColor(this, primaryColor);
        TabLayoutUtil.setTabIconColors(mTabLayout, normalColor, selectedColor);
        mTabLayout.setTabTextColors(normalColor, selectedColor);
        mTabLayout.setSelectedTabIndicatorColor(ThemeStore.accentColor(this));
    }

    private void setupToolbar() {
        mAppBarLayout.setBackgroundColor(ThemeStore.primaryColor(this));
        mToolbar.setBackgroundColor(ThemeStore.primaryColor(this));
        setTitle(R.string.action_settings);
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addAppbarLayoutElevation(float v) {
        TransitionManager.beginDelayedTransition(mAppBarLayout);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mAppBarLayout.setElevation(v);
        }
    }

    public static class AdvancedSettingsFragment extends ATEPreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.pref_advanced);
            addPreferencesFromResource(R.xml.pref_blacklist);
            addPreferencesFromResource(R.xml.pref_others);
        }

        @Nullable
        @Override
        public DialogFragment onCreatePreferenceDialog(Preference preference) {
            if (preference instanceof BlacklistPreference) {
                return BlacklistPreferenceDialog.newInstance();
            }
            return super.onCreatePreferenceDialog(preference);
        }

        private void openUrl(String url) {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            getListView().setPadding(0, 0, 0, 0);
            getListView().addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    if (recyclerView.canScrollVertically(RecyclerView.NO_POSITION)) {
                        ((SettingsActivity) getActivity()).addAppbarLayoutElevation(8f);
                    } else {
                        ((SettingsActivity) getActivity()).addAppbarLayoutElevation(0f);
                    }
                }
            });
            getListView().setBackgroundColor(ThemeStore.primaryColor(getContext()));
            invalidateSettings();
        }

        private void showLicenseDialog() {
            new LicensesDialog.Builder(getContext()).setNotices(R.raw.licences)
                    .setTitle(R.string.licenses)
                    .setNoticesCssStyle(getString(R.string.license_dialog_style)
                            .replace("{bg-color}", ThemeSingleton.get().darkTheme ? "424242" : "ffffff")
                            .replace("{text-color}", ThemeSingleton.get().darkTheme ? "ffffff" : "000000")
                            .replace("{license-bg-color}", ThemeSingleton.get().darkTheme ? "535353" : "eeeeee"))
                    .setIncludeOwnLicense(true).build().showAppCompat();
        }

        private void invalidateSettings() {
            Preference findPreference = findPreference("changelog");
            findPreference.setOnPreferenceClickListener(preference -> {
                openUrl(Constants.TELEGRAM_CHANGE_LOG);
                return true;
            });
            findPreference = findPreference("day_dream");
            findPreference.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(Settings.ACTION_DREAM_SETTINGS);
                startActivity(intent);
                return true;
            });
            findPreference = findPreference("user_info");
            findPreference.setOnPreferenceClickListener(preference -> {
                startActivity(new Intent(getContext(), UserInfoActivity.class));
                return true;
            });
            findPreference = findPreference("open_source");
            findPreference.setOnPreferenceClickListener(preference -> {
                showLicenseDialog();
                return true;
            });
            findPreference = findPreference("about");
            findPreference.setOnPreferenceClickListener(preference -> {
                startActivity(new Intent(getContext(), AboutActivity.class));
                return true;
            });
            findPreference = findPreference("app_version");
            try {
                PackageInfo packageInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
                findPreference.setSummary(packageInfo.versionName);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            TwoStatePreference toggleVolume = (TwoStatePreference) findPreference("toggle_volume");
            toggleVolume.setOnPreferenceChangeListener((preference, o) -> {
                getActivity().recreate();
                return true;
            });
            TwoStatePreference colorAppShortcuts = (TwoStatePreference) findPreference("should_color_app_shortcuts");
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) {
                colorAppShortcuts.setVisible(false);
            } else {
                colorAppShortcuts.setChecked(PreferenceUtil.getInstance(getActivity()).coloredAppShortcuts());
                colorAppShortcuts.setOnPreferenceChangeListener((preference, newValue) -> {
                    // Save preference
                    PreferenceUtil.getInstance(getActivity()).setColoredAppShortcuts((Boolean) newValue);

                    // Update app shortcuts
                    new DynamicShortcutManager(getActivity()).updateDynamicShortcuts();

                    return true;
                });
            }

            TwoStatePreference cornerWindow = (TwoStatePreference) findPreference("corner_window");
            cornerWindow.setOnPreferenceChangeListener((preference, newValue) -> {
                Toast.makeText(getContext(), "Restart app!", Toast.LENGTH_SHORT).show();
                getActivity().recreate();
                return true;
            });
            /*TwoStatePreference toggleExclude = (TwoStatePreference) findPreference("toggle_exclude");
            toggleExclude.setOnPreferenceChangeListener((preference, newValue) -> {
                getActivity().recreate();
                return true;
            });*/

        }
    }

    public static class SettingsFragment extends ATEPreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
        private static void setSummary(@NonNull Preference preference) {
            setSummary(preference, PreferenceManager
                    .getDefaultSharedPreferences(preference.getContext())
                    .getString(preference.getKey(), ""));
        }

        private static void setSummary(Preference preference, @NonNull Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);
                preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
            } else {
                preference.setSummary(stringValue);
            }
        }

        @Nullable
        @Override
        public DialogFragment onCreatePreferenceDialog(Preference preference) {
            if (preference instanceof NowPlayingScreenPreference) {
                return NowPlayingScreenPreferenceDialog.newInstance();
            }
            return super.onCreatePreferenceDialog(preference);
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.pref_general);
            addPreferencesFromResource(R.xml.pref_audio);
            addPreferencesFromResource(R.xml.pref_images);
            addPreferencesFromResource(R.xml.pref_lockscreen);
            addPreferencesFromResource(R.xml.pref_now_playing_screen);
            addPreferencesFromResource(R.xml.pref_notification);
            addPreferencesFromResource(R.xml.pref_playlists);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            getListView().setPadding(0, 0, 0, 0);
            getListView().addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    ((SettingsActivity) getActivity()).addAppbarLayoutElevation(recyclerView.canScrollVertically(RecyclerView.NO_POSITION) ? 8f : 0f);
                }
            });
            getListView().setBackgroundColor(ATHUtil.resolveColor(getActivity(), android.R.attr.colorPrimary));
            invalidateSettings();
            PreferenceUtil.getInstance(getActivity()).registerOnSharedPreferenceChangedListener(this);
        }

        private boolean hasEqualizer() {
            return getActivity().getPackageManager().resolveActivity(new Intent("android.media.action.DISPLAY_AUDIO_EFFECT_CONTROL_PANEL"), 0) != null;
        }

        private void invalidateSettings() {
            final Preference generalTheme = findPreference("general_theme");
            setSummary(generalTheme);
            generalTheme.setOnPreferenceChangeListener((preference, newValue) -> {
                setSummary(generalTheme, newValue);
                ThemeStore.editTheme(getActivity())
                        .activityTheme(PreferenceUtil.getThemeResFromPrefValue((String) newValue))
                        .commit();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                    getActivity().setTheme(PreferenceUtil.getThemeResFromPrefValue((String) newValue));
                    new DynamicShortcutManager(getActivity()).updateDynamicShortcuts();
                }
                getActivity().recreate();
                return true;
            });

            ATEColorPreference accentColorPref = (ATEColorPreference) findPreference("accent_color");
            final int accentColor = ThemeStore.accentColor(getActivity());
            accentColorPref.setColor(accentColor, ColorUtil.darkenColor(accentColor));

            accentColorPref.setOnPreferenceClickListener(preference -> {
                new ColorChooserDialog.Builder(((SettingsActivity) getActivity()), R.string.accent_color)
                        .accentMode(true)
                        .allowUserColorInput(true)
                        .allowUserColorInputAlpha(false)
                        .preselect(accentColor)
                        .show();
                return true;
            });
            final Preference autoDownloadImagesPolicy = findPreference("auto_download_images_policy");
            setSummary(autoDownloadImagesPolicy);
            autoDownloadImagesPolicy.setOnPreferenceChangeListener((preference, o) -> {
                setSummary(autoDownloadImagesPolicy, o);
                return true;
            });
            final TwoStatePreference classicNotification = (TwoStatePreference) findPreference("classic_notification");
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                classicNotification.setVisible(false);
            } else {
                classicNotification.setChecked(PreferenceUtil.getInstance(getActivity()).classicNotification());
                classicNotification.setOnPreferenceChangeListener((preference, newValue) -> {
                    // Save preference
                    PreferenceUtil.getInstance(getActivity()).setClassicNotification((Boolean) newValue);

                    final MusicService service = MusicPlayerRemote.musicService;
                    if (service != null) {
                        service.initNotification();
                        service.updateNotification();
                    }

                    return true;
                });
            }
            TwoStatePreference twoSatePreference = (TwoStatePreference) findPreference("adaptive_color_app");
            twoSatePreference.setOnPreferenceChangeListener((preference, newValue) -> {

                Toast.makeText(getContext(), "Restart app!", Toast.LENGTH_SHORT).show();
                getActivity().recreate();
                return true;
            });

            TwoStatePreference colorNavBar = (TwoStatePreference) findPreference("should_color_navigation_bar");
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                colorNavBar.setEnabled(false);
                colorNavBar.setSummary(R.string.pref_only_lollipop);
            } else {
                colorNavBar.setChecked(ThemeStore.coloredNavigationBar(getActivity()));
                colorNavBar.setOnPreferenceChangeListener((preference, newValue) -> {
                    ThemeStore.editTheme(getActivity())
                            .coloredNavigationBar((Boolean) newValue)
                            .commit();
                    getActivity().recreate();
                    return true;
                });
            }
            Preference findPreference = findPreference("equalizer");
            if (!hasEqualizer()) {
                findPreference.setEnabled(false);
                findPreference.setSummary(getResources().getString(R.string.no_equalizer));
            }
            findPreference.setOnPreferenceClickListener(preference -> {
                NavigationUtil.openEqualizer(SettingsFragment.this.getActivity());
                return true;
            });
            updateNowPlayingScreenSummary();
        }

        private void updateNowPlayingScreenSummary() {
            findPreference("now_playing_screen_id").setSummary(PreferenceUtil.getInstance(getActivity()).getNowPlayingScreen().titleRes);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            switch (key) {
                case PreferenceUtil.NOW_PLAYING_SCREEN_ID:
                    updateNowPlayingScreenSummary();
                    break;
            }
        }
    }
}
