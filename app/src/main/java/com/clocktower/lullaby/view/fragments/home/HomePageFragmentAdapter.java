package com.clocktower.lullaby.view.fragments.home;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.clocktower.lullaby.model.utilities.Constants;

import java.util.Arrays;
import java.util.List;

public class HomePageFragmentAdapter extends FragmentStatePagerAdapter {

    private List<BaseFragment> fragmentList;
    private static List<String> pageTitles;

    static {
        pageTitles = Arrays.asList(new String[]{Constants.HOME, Constants.ALARM_SETTER,
                Constants.MUSIC_SELECTOR, Constants.FORUM, Constants.RANDOM});
    }

    public HomePageFragmentAdapter(FragmentManager fm, List<BaseFragment> fragList) {
        super(fm);
        this.fragmentList = fragList;
    }

    public void addNewFragmentTitle(String title){
        pageTitles.add(title);
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
        return pageTitles.get(position);
    }

    @Override
    public Fragment getItem(int position) {
        return fragmentList.get(position);
    }
}
