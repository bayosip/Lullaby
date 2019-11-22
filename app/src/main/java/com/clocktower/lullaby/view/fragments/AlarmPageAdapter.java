package com.clocktower.lullaby.view.fragments;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.clocktower.lullaby.model.utilities.Constants;

import java.util.List;

public class AlarmPageAdapter extends FragmentStatePagerAdapter {

    private List<BaseFragment> fragmentList;
    private static String[] pageTitles;

    static {
        pageTitles = new String[]{Constants.ALARM_SETTER, Constants.MUSIC_SELECTOR};
    }

    public AlarmPageAdapter(FragmentManager fm, List<BaseFragment> fragList) {
        super(fm);
        this.fragmentList = fragList;
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        return  fragmentList.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return pageTitles[position];
    }

    @Override
    public Fragment getItem(int position) {
        return fragmentList.get(position);
    }
}
