package edu.semnag.myyandextranslate;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.ListFragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;

import edu.semnag.myyandextranslate.fragments.HistoryPageFragment;

/**
 * Created by semna on 13.04.2017.
 */

public class HistoryActivity extends BaseActivity {
    private static final int NUM_PAGES = 2;
    private String tabs[] = new String[]{HistoryPageFragment.HISTORY_PAGE,
            HistoryPageFragment.HISTORY_FAV_PAGE};
    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_history, frameLayout);
        /**
         * cause this activity extend from base activity
         * BaseActivity.oncreate is called each time and reinit all fields,
         * including bottonavview
         * hence we need to manually set menu item checked
         * */
        navigation.getMenu().getItem(1).setChecked(true);
        /**
         * configure pager
         * */
        mPager = (ViewPager) findViewById(R.id.history_pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {

        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            /**
             * init data list fragment
             * */
            ListFragment listFragment = new HistoryPageFragment();
            Bundle args = new Bundle();
            args.putString(HistoryPageFragment.HISTORY_PAGE_TYPE, position == 0 ?
                    HistoryPageFragment.HISTORY_PAGE :
                    HistoryPageFragment.HISTORY_FAV_PAGE);
            listFragment.setArguments(args);
            return listFragment;
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabs[position];
        }


    }
}
