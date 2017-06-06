package com.rteixeira.traveller.uicontrol;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;


import com.rteixeira.traveller.R;
import com.rteixeira.traveller.storage.models.Journey;
import com.rteixeira.traveller.uicontrol.interfaces.JourneyListListener;

/**
 * Simple Activity with a PageViewer to Handle the Tab management.
 */
public class HomeActivity extends AppCompatActivity implements JourneyListListener {

    private PageAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText(R.string.MAP_TAB));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.JOURNEY_TAB));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        adapter = new PageAdapter (getSupportFragmentManager(), tabLayout.getTabCount());

        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == PageAdapter.JOURNEY_LIST_FRAGMENT_POSITION) {
                    adapter.getJourneyList().loadInformation();
                }

                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                if (tab.getPosition() == PageAdapter.MAP_FRAGMENT_POSITION && adapter.getMap() != null) {
                    adapter.getMap().toggleTrackingOff();
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    @Override
    public void onJourneySelected(Journey mItem) {
        // Would be using this to load a selected Journey to the map
    }

    /**
     * TODO: SOLVE THIS! THIS IS NOT THE WAY TO DO IT!
     * But I was spending to much time I don't have at the moment, there is surely a way better way
     * to do this, I'm just not getting there for some reason. Temporary Workaround.
     * @param view
     */
    public void TrackerToggle(View view) {

        if (adapter.getMap() != null) {
            SwitchCompat sw = (SwitchCompat) view;
            if (sw.isChecked()) {
                adapter.getMap().toggleTrackingOn();
            } else {
                adapter.getMap().toggleTrackingOff();
            }
        }
    }
}
