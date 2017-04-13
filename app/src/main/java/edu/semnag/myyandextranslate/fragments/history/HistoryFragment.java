package edu.semnag.myyandextranslate.fragments.history;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.ListFragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import edu.semnag.myyandextranslate.R;

/**
 * Created by semna on 31.03.2017.
 */

public class HistoryFragment extends Fragment {
    private static final int NUM_PAGES = 2;
    private String tabs[] = new String[]{HistoryPageFragment.HISTORY_PAGE, HistoryPageFragment.HISTORY_FAV_PAGE};
    private ViewGroup content;
    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        content = (ViewGroup) inflater.inflate(R.layout.activity_history, container, false);
        /**
         * configure pager
         * */
        mPager = (ViewPager) content.findViewById(R.id.history_pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getFragmentManager());
        mPager.setAdapter(mPagerAdapter);

        return content;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        System.out.println("on destroy");
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
