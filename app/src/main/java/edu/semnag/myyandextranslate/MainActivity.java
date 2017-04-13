package edu.semnag.myyandextranslate;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.Toast;

import edu.semnag.myyandextranslate.fragments.history.HistoryFragment;
import edu.semnag.myyandextranslate.fragments.home.HomeFragment;

/**
 * @author SemenNag
 * This home activity contains home page with translations tools
 * and plays role of parent activity for history activity in order to add nav bar to its child
 * This activity contains FrameLaout which would be rebased according to nav option selected
 * */

public class MainActivity extends AppCompatActivity {
    /**
     * PrefsId which is used to store cross session user data
     * */
    public static final String PREFS_NAME = "MyPrefsFile";
    /**
     *  Frame layout: Which is going to be used as parent layout for child activity layout.
     *  This layout is protected so that child activity can access this
     *  */
    protected FrameLayout frameLayout;
    /**
     * Static variable for selected item position. Which can be used in child activity to know which item is selected from the list.
     * */
    protected static int position;
    /**
     * Navigation bar down in the app screen which is used to switch between home screen and history with favs
     * */
    private BottomNavigationView navigation;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            onNavClickedChangeFragment(item.getItemId());
            return true;
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().add(R.id.contentContainer, new HomeFragment()).commit();
        }
        setContentView(R.layout.activity_main);

        /**
         * Configure navbar in bottom
         * */
        this.navigation = (BottomNavigationView) findViewById(R.id.navigation);
        this.navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }


    private void onNavClickedChangeFragment(int id) {
        Class fragmentClass = null;
        switch (id) {
            case R.id.navigation_home:
                fragmentClass = HomeFragment.class;
                break;
            case R.id.navigation_history:
                fragmentClass = HistoryFragment.class;
                break;
        }

        /**
         * Init class of fragment
         * */
        try {
            Fragment fragment = (Fragment) fragmentClass.newInstance();
            /**
             * Inserting fragment, replacing current
             */
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .addToBackStack(null)
                    .replace(R.id.contentContainer, fragment)
                    .commit();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

