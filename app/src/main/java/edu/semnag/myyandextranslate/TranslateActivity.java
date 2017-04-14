package edu.semnag.myyandextranslate;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
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

public class TranslateActivity extends BaseActivity {
    /**
     * PrefsId which is used to store cross session user data
     */
    public static final String PREFS_NAME = "MyPrefsFile";

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

    /**
     * Keys for saving instance state
     */
    public static String KEY_LANG_FROM_SELECTED = "KeyLangFromSelected";
    public static String KEY_LANG_TO_SELECTED = "KeyLangToSelected";
    public static String KEY_SOURCE_TEXT = "KeySourceText";
    public static String KEY_OUTPUT_TEXT = "KeyOutPutText";

    /**
     * active fragment with language selection
     * on the screen
     */
    private LangSelectionFragment langSelectionFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getLayoutInflater().inflate(R.layout.activity_translate, frameLayout);

        /**
         * cause this activity extend from base activity
         * BaseActivity.oncreate is called each time and reinit all fields,
         * including bottonavview
         * hence we need to manually set menu item checked
         * */
        navigation.getMenu().getItem(0).setChecked(true);

        /**
         * init requests management
         * */
        requestManager = TranslateRequestManager.from(this);
        translateRequestListiner = new TranslateRequestListener();
        /**
         * init widgets
         * */
        fromLangSelectionView = (EditText) frameLayout.findViewById(R.id.fromLangSelection);
        toLangSelectionView = (EditText) frameLayout.findViewById(R.id.toLangSelection);
        sourceTextView = (EditText) frameLayout.findViewById(R.id.home_inputSourceText);
        outPutTextView = (TextView) frameLayout.findViewById(R.id.home_outPutText);

        /**
         * assign listener to widgets
         * */
        LangSelectionOnClickHandler langSelectionOnClickHandler = new LangSelectionOnClickHandler();
        fromLangSelectionView.setOnClickListener(langSelectionOnClickHandler);
        toLangSelectionView.setOnClickListener(langSelectionOnClickHandler);

        /**
         * checking whether lang selection fragment is already showing
         * */
        FragmentManager fm = getSupportFragmentManager();
        langSelectionFragment = (LangSelectionFragment) fm.findFragmentByTag("lang_selection_fragment");
        if (langSelectionFragment == null) {
            langSelectionFragment = new LangSelectionFragment();
        } else {
            LangSelectionOnClickHandler handler = (LangSelectionOnClickHandler) langSelectionFragment.getAsker();
            switch (handler.getSourceId()) {
                case R.id.fromLangSelection:
                    handler.setSourceView(fromLangSelectionView);
                    break;
                case R.id.toLangSelection:
                    handler.setSourceView(toLangSelectionView);
                    break;
            }
        }

        /**
         * creating icon for changing translate direction
         * */
        ImageView imageView = (ImageView) findViewById(R.id.changeTranslateDirection);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeTranslateDireaction();
            }
        });
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

        /**
         * if activity called from another with provided data
         * or we have user last entered data
         * */
        Bundle restoreState = getIntent().getExtras();
        if (restoreState != null) {
            fromLangSelectionView.setText(restoreState.getString(KEY_LANG_FROM_SELECTED));
            toLangSelectionView.setText(restoreState.getString(KEY_LANG_TO_SELECTED));
            sourceTextView.setText(restoreState.getString(KEY_SOURCE_TEXT));
            outPutTextView.setText(restoreState.getString(KEY_OUTPUT_TEXT));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(KEY_LANG_FROM_SELECTED, fromLangSelectionView.getText().toString());
        outState.putString(KEY_LANG_TO_SELECTED, toLangSelectionView.getText().toString());
        outState.putString(KEY_SOURCE_TEXT, sourceTextView.getText().toString());
        outState.putString(KEY_OUTPUT_TEXT, outPutTextView.getText().toString());
        super.onSaveInstanceState(outState);
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

    public class LangSelectionOnClickHandler implements View.OnClickListener,
            ListFragmentItemClickListener {
        private EditText sourceView;
        private int sourceId;

        @Override
        public void onClick(View v) {
            sourceView = (EditText) v;
            sourceId = v.getId();
            askForLangSelection(this);
        }

        @Override
        public void onListFragmentItemClicked(Map<String, String> values) {
            sourceView.setText(values.get(ListFragmentItemClickListener.LANG_SELECTION));
        }

        public void setSourceView(EditText sourceView) {
            this.sourceView = sourceView;
        }

        public int getSourceId() {
            return sourceId;
        }
    }

    private void askForLangSelection(ListFragmentItemClickListener asker) {
        langSelectionFragment.setItemClickListener(asker);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.contentContainer,
                        langSelectionFragment, "lang_selection_fragment")
                .addToBackStack(null).commit();
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
        return true;
    }


    private void changeTranslateDireaction() {
        String from = fromLangSelectionView.getText().toString();
        String to = toLangSelectionView.getText().toString();
        fromLangSelectionView.setText(to);
        toLangSelectionView.setText(from);
    }
}
