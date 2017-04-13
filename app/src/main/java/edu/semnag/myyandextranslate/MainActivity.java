package edu.semnag.myyandextranslate;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.foxykeep.datadroid.requestmanager.Request;
import com.foxykeep.datadroid.requestmanager.RequestManager;

import java.util.Map;

import edu.semnag.myyandextranslate.fragments.LangSelectionFragment;
import edu.semnag.myyandextranslate.request.TranslateRequestFactory;
import edu.semnag.myyandextranslate.request.TranslateRequestManager;
import edu.semnag.myyandextranslate.request.operations.TranslateOperations;

/**
 * @author SemenNag
 *         This home activity contains home page with translations tools
 *         and plays role of parent activity for history activity in order to add nav bar to its child
 *         This activity contains FrameLaout which would be rebased according to nav option selected
 */

public class MainActivity extends AppCompatActivity {
    /**
     * PrefsId which is used to store cross session user data
     */
    public static final String PREFS_NAME = "MyPrefsFile";
    /**
     * Frame layout: Which is going to be used as parent layout for child activity layout.
     * This layout is protected so that child activity can access this
     */
    protected FrameLayout frameLayout;
    /**
     * Static variable for selected item position. Which can be used in child activity to know which item is selected from the list.
     */
    protected static int position;
    /**
     * Navigation bar down in the app screen which is used to switch between home screen and history with favs
     */
    private BottomNavigationView navigation;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            onNavClickedChangeActivity(item.getItemId());
            return true;
        }
    };

    /**
     * Interface to interact with language selection fragment
     */
    public interface ListFragmentItemClickListener {
        String LANG_SELECTION = "lang_selection";

        void onListFragmentItemClicked(Map<String, String> values);
    }

    /**
     * Request manager build around DataDroid module,
     * which picks up appropriate operation (service) to yandex translate api according to request type
     * and executes
     */
    private TranslateRequestManager requestManager;
    /**
     * Inner class which provides a tool to async listen of api request execution
     * see @line TODO provide link
     */
    private TranslateRequestListener translateRequestListiner;
    /**
     * Couple of text views which is used to work with language selection
     */
    private EditText fromLangSelectionView;
    private EditText toLangSelectionView;
    /**
     * Couple of text view with is used for text input to translate and result of translation
     */
    private EditText sourceTextView;
    private TextView outPutTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /**
         * setting route layout
         * */
        setContentView(R.layout.activity_main);
        /**
         * Configure navbar in bottom
         * */
        this.navigation = (BottomNavigationView) findViewById(R.id.navigation);
        this.navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        /**
         * init requests management
         * */
        requestManager = TranslateRequestManager.from(this);
        translateRequestListiner = new TranslateRequestListener();

        frameLayout = (FrameLayout) findViewById(R.id.contentContainer);
        /**
         * init widgets
         * */
        fromLangSelectionView = (EditText) frameLayout.findViewById(R.id.fromLangSelection);
        toLangSelectionView = (EditText) frameLayout.findViewById(R.id.toLangSelection);
        sourceTextView = (EditText) frameLayout.findViewById(R.id.home_inputSourceText);
        outPutTextView = (TextView) frameLayout.findViewById(R.id.home_outPutText);
        /**
         * assign listeners to widgets
         * */
        fromLangSelectionView.setOnClickListener(new LangSelectionOnClickHandler());
        toLangSelectionView.setOnClickListener(new LangSelectionOnClickHandler());

        sourceTextView.setOnKeyListener(new TextView.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    //
                    TextView textView = (TextView) v;
                    makeRequestToTranslate(textView.getText().toString());
                    return true;
                }
                return false;
            }
        });
    }


    private void onNavClickedChangeActivity(int id) {
        MainActivity.position = id;

        switch (id) {
            case R.id.navigation_home:
                startActivity(new Intent(this, MainActivity.class));
                break;
            case R.id.navigation_history:
                startActivity(new Intent(this, HistoryActivity.class));
                break;
        }
    }

    private class TranslateRequestListener implements RequestManager.RequestListener {

        @Override
        public void onRequestFinished(Request request, Bundle resultData) {
            /**
             * setting text to view
             * */
            outPutTextView.setText(resultData.getString(TranslateOperations.TranslateParams.OUTPUT_TEXT));
        }

        @Override
        public void onRequestConnectionError(Request request, int statusCode) {

        }

        @Override
        public void onRequestDataError(Request request) {

        }

        @Override
        public void onRequestCustomError(Request request, Bundle resultData) {

        }
    }

    private class LangSelectionOnClickHandler implements View.OnClickListener,
            ListFragmentItemClickListener {
        private EditText sourceView;

        @Override
        public void onClick(View v) {
            sourceView = (EditText) v;
            askForLangSelection(this);
        }

        @Override
        public void onListFragmentItemClicked(Map<String, String> values) {
            sourceView.setText(values.get(ListFragmentItemClickListener.LANG_SELECTION));
        }
    }

    private void askForLangSelection(ListFragmentItemClickListener asker) {
        LangSelectionFragment langSelectionFragment = new LangSelectionFragment();
        langSelectionFragment.setItemClickListener(asker);
        getSupportFragmentManager().beginTransaction().add(R.id.contentContainer,
                langSelectionFragment).commit();
    }

    private void makeRequestToTranslate(String source) {
        if (validateViewReadyToTranslate()) {
            Request request = new Request(TranslateRequestFactory.REQUEST_TRANSLATE);
            request.put(TranslateOperations.TranslateParams.FROM_LANG, fromLangSelectionView.getText().toString());
            request.put(TranslateOperations.TranslateParams.TO_LANG, toLangSelectionView.getText().toString());
            request.put(TranslateOperations.TranslateParams.SOURCE_TEXT, source);
            requestManager.execute(request, translateRequestListiner);
        }
    }
    // FIXME: 13.04.2017 enlarge logic
    private boolean validateViewReadyToTranslate() {
        return false;
    }
}

