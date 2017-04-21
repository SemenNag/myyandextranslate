package edu.semnag.myyandextranslate;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.FrameLayout;

import java.util.Set;


/**
 * @author SemenNag
 *         This base aplays role of parent activity to others activity in order to add nav bar to its child
 *         This activity contains FrameLaout which would be rebased according to nav option selected
 */

public class BaseActivity extends AppCompatActivity {
    /**
     * Frame layout: Which is going to be used as parent layout for child activity layout.
     * This layout is protected so that child activity can access this
     */
    protected FrameLayout frameLayout;
    /**
     * Navigation bar down in the app screen which is used to switch between home screen and history with favs
     */
    protected BottomNavigationView navigation;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            onNavClickedChangeActivity(item.getItemId());
            return true;
        }
    };
    /**
     * This flag is used just to check that launcher activity is called first time
     * so that we can open appropriate Activity on launch and make list item position selected accordingly.
     */
    private static boolean isLaunch = true;
    /**
     * because all activities extends base activity
     * saveInsanceState alwayes overrides
     */
    private static Bundle localInstanceState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /**
         * setting route layout
         * */
        setContentView(R.layout.activity_base);

        frameLayout = (FrameLayout) findViewById(R.id.contentContainer);
        /**
         * Configure navbar in bottom
         * */
        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        /**
         * As we are calling BaseActivity from manifest file and this base activity is intended just to add navigation drawer in our app.
         * We have to open some activity with layout on launch. So we are checking if this BaseActivity is called first time then we are opening our first activity.
         * */
        if (isLaunch) {
            isLaunch = false;
            onNavClickedChangeActivity(R.id.navigation_home);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (localInstanceState == null) {
            localInstanceState = (Bundle) outState.clone();
        } else if (outState != null) {
            Set<String> data = outState.keySet();
            for (String key : data) {
                localInstanceState.putString(key, outState.getString(key));
            }
        }
        super.onSaveInstanceState(outState);
    }
    /**
     * method which prior to navigation calls
     * necessary activity to start*/
    private void onNavClickedChangeActivity(int id) {
        Intent intent = null;
        switch (id) {
            case R.id.navigation_home:
                intent = new Intent(this, TranslateActivity.class);
                /**
                 * Check whether was a some users inputs to restore
                 * */
                if (localInstanceState != null) {
                    intent.putExtras(localInstanceState);
                }
                break;
            case R.id.navigation_history:
                intent = new Intent(this, HistoryActivity.class);
                break;
        }
        startActivity(intent);
    }

}

