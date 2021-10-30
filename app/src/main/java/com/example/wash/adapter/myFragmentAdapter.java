package com.example.wash.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.List;

public class myFragmentAdapter extends FragmentPagerAdapter {
    private FragmentManager myFragmentManager;
    private List<Fragment> myFragmentList;

    public myFragmentAdapter(@NonNull FragmentManager fm, List<Fragment> myFragmentList) {
        super(fm);
        this.myFragmentManager = myFragmentManager;
        this.myFragmentList = myFragmentList;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return  myFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return myFragmentList.size();
    }
}
