package code.name.monkey.retromusic.ui.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

import code.name.monkey.retromusic.ui.activities.SettingsActivity;


/**
 * Created by hemanths on 15/06/17.
 */

public class SettingsPagerAdapter extends FragmentStatePagerAdapter {
    private String[] tabs = new String[]{"Normal", "Experimental"};
    private List<Fragment> mFragments = new ArrayList<>();

    public SettingsPagerAdapter(FragmentManager fm) {
        super(fm);
        mFragments.add(new SettingsActivity.SettingsFragment());
        mFragments.add(new SettingsActivity.AdvancedSettingsFragment());
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabs[position];
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }
}
