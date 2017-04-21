package edu.semnag.myyandextranslate;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.ViewGroup;

import java.util.HashMap;
import java.util.Map;

import edu.semnag.myyandextranslate.fragments.HistoryPageFragment;

/**
 *@author SemenNag
 * Activity which holds working with history and favorate pages
 * Implemented a ScreenSlidePageAdapter which enables working with 2 paged-fragment
 * 1St page - simple history
 * 2nd page - favorites list
 *
 * */

public class HistoryActivity extends BaseActivity implements ViewPager.OnPageChangeListener {
    private ViewPager mPager;
    private ScreenSlidePagerAdapter mPagerAdapter;

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
        mPager.addOnPageChangeListener(this);
    }

    @Override
    public void onPageSelected(int position) {
        Fragment fragment = mPagerAdapter.getFragment(position);
        if (fragment != null) {
            fragment.onResume();
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private class ScreenSlidePagerAdapter extends FragmentPagerAdapter {
        private String tabs[] = new String[]{HistoryPageFragment.HISTORY_PAGE,
                HistoryPageFragment.HISTORY_FAV_PAGE};
        private static final int NUM_PAGES = 2;
        private Map<Integer, String> mFragmentTags;
        private FragmentManager mFragmentManager;


        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
            mFragmentManager = fm;
            mFragmentTags = new HashMap<>();
        }

        @Override
        public Fragment getItem(int position) {
            /**
             * init data list fragment
             * */
            HistoryPageFragment listFragment = new HistoryPageFragment();
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

        /**
         * instateItem enables refresh state of page fragment mechanism
         * */
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Object object = super.instantiateItem(container, position);
            if (object instanceof Fragment) {
                Fragment fragment = (Fragment) object;
                String tag = fragment.getTag();
                mFragmentTags.put(position, tag);
            }
            return object;
        }

        Fragment getFragment(int position) {
            Fragment fragment = null;
            String tag = mFragmentTags.get(position);
            if (tag != null) {
                fragment = mFragmentManager.findFragmentByTag(tag);
            }
            return fragment;
        }
    }

}

