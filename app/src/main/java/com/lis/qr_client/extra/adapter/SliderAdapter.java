package com.lis.qr_client.extra.adapter;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

/** FragmentPagerAdapter for ViewPager. Tabs, sliding, be awesome*/

public class SliderAdapter<T extends Fragment> extends FragmentPagerAdapter {
    private final List<Pair<String, T>> fragmentPairList = new ArrayList<>();


    public SliderAdapter(FragmentManager fm) {
        super(fm);
    }

    public void addFragment(T fragment, String title) {
        fragmentPairList.add(new Pair<>(title, fragment));

    }

    @Override
    public T getItem(int i) {
        return fragmentPairList.get(i).second;
    }

    @Override
    public int getCount() {
        return fragmentPairList.size();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return fragmentPairList.get(position).first;
    }

    public List<Pair<String, T>> getFragmentPairList() {
        return fragmentPairList;
    }

    public T getFragmentByTitle(String title) {
        for (Pair<String, T> pair : fragmentPairList) {
            if (pair.first.contentEquals(title)) {
                return pair.second;
            }
        }
        return null;
    }


}
