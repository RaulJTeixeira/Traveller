package com.rteixeira.traveller.uicontrol;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;


/***
 * Simple Page Adapter to control the fragments that are being used.
 */
public class PageAdapter extends FragmentStatePagerAdapter {
    public static final int MAP_FRAGMENT_POSITION = 0;
    public static final int JOURNEY_LIST_FRAGMENT_POSITION = 1;

    int mNumOfTabs;

    private MapViewFragment map;

    private JourneyFragment journeyList;

    public PageAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case MAP_FRAGMENT_POSITION:
                if(map == null){
                    map = new MapViewFragment();
                }
                return map;
            case JOURNEY_LIST_FRAGMENT_POSITION:
                 journeyList = new JourneyFragment();
                return journeyList;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }

    public MapViewFragment getMap() {
        return map;
    }

    public JourneyFragment getJourneyList() {
        return journeyList;
    }
}