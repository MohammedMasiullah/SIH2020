package com.example.safetyappv1.ui.main;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.safetyappv1.ChatsFragment;
import com.example.safetyappv1.GroupsFragment;
import com.example.safetyappv1.R;

public class SectionsPagerAdapter extends FragmentPagerAdapter {


    private final Context mContext;

    public SectionsPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        switch (position)
        {
            case 0 :
                fragment = new ChatsFragment();
                break;

            case 1 :
                fragment = new GroupsFragment();
                break;
        }
        return fragment;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position)
        {
            case 0:
                return "Contacts";
            case 1 :
                return "Groups";
        }
        return null;
    }

    @Override
    public int getCount() {
        // Show 2 total pages.
        return 2;
    }
}