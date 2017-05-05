package com.valeo.psa.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * Created by l-avaratha on 05/05/2017
 */

public class CalibrationAdapter extends FragmentPagerAdapter {


    private final List<Fragment> fragments;

    public CalibrationAdapter(FragmentManager fm, List<Fragment> fragments) {
        super(fm);
        this.fragments = fragments;
    }

    @Override
    public Fragment getItem(int position) {
        if (fragments != null && fragments.size() >= position) {
            return this.fragments.get(position);
        }
        return null;
    }

    @Override
    public int getCount() {
        return this.fragments.size();
    }
}
