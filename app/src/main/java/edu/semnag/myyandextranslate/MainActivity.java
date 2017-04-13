package edu.semnag.myyandextranslate;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Toast;

import edu.semnag.myyandextranslate.fragments.history.HistoryFragment;
import edu.semnag.myyandextranslate.fragments.home.HomeFragment;

public class MainActivity extends AppCompatActivity {
    public static final String PREFS_NAME = "MyPrefsFile";


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
            case R.id.navigation_notifications:
                Toast.makeText(getApplication().getApplicationContext(), "Settings Checked",
                        Toast.LENGTH_SHORT).show();
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

