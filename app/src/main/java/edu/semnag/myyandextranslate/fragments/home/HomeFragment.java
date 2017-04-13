package edu.semnag.myyandextranslate.fragments.home;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.foxykeep.datadroid.requestmanager.Request;
import com.foxykeep.datadroid.requestmanager.RequestManager;

import java.util.Map;

import edu.semnag.myyandextranslate.MainActivity;
import edu.semnag.myyandextranslate.R;
import edu.semnag.myyandextranslate.request.TranslateRequestFactory;
import edu.semnag.myyandextranslate.request.TranslateRequestManager;
import edu.semnag.myyandextranslate.request.operations.TranslateOperations;

/**
 * Created by semna on 30.03.2017.
 */

public class HomeFragment extends Fragment {
    private View content;
    private EditText fromLangSelectionView;
    private EditText toLangSelectionView;
    private EditText sourceTextView;
    private TextView outPutTextView;
    private TranslateRequestManager requestManager;
    private TranslateRequestListener translateRequestListiner;
    /**
     * Parent Activity
     */
    MainActivity parentActivity;
    /**
     * keys for shared preferences
     */
    private static String PREFS_LANG_FROM = "PrefsLangFrom";
    private static String PREFS_LANG_TO = "PrefsLangTo";
    private static String PREFS_SOURCE_TEXT = "PrefsSourceText";
    private static String PREFS_OUTPUT_TEXT = "PrefsOutPutText";


    interface ListFragmentItemClickListener {
        String LANG_SELECTION = "lang_selection";

        void onListFragmentItemClicked(Map<String, String> values);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        parentActivity = (MainActivity) getActivity();
        SharedPreferences settings = parentActivity.getSharedPreferences(MainActivity.PREFS_NAME, 0);
        String fromLangSelection = settings.getString(PREFS_LANG_FROM, null);
        String toLangSelection = settings.getString(PREFS_LANG_TO, null);

        if (fromLangSelectionView != null && fromLangSelection != null) {
            fromLangSelectionView.setText(fromLangSelection);
        }

        if (toLangSelectionView != null && toLangSelection != null) {
            toLangSelectionView.setText(toLangSelection);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        content = inflater.inflate(R.layout.activity_home, container, false);

        requestManager = TranslateRequestManager.from(getContext());
        translateRequestListiner = new TranslateRequestListener();

        /**
         * Configure lang selections fields
         * */
        fromLangSelectionView = (EditText) content.findViewById(R.id.fromLangSelection);
        fromLangSelectionView.setOnClickListener(new LangSelectionOnClickHandler());

        toLangSelectionView = (EditText) content.findViewById(R.id.toLangSelection);
        toLangSelectionView.setOnClickListener(new LangSelectionOnClickHandler());

        /**
         * configure input text field*/
        sourceTextView = (EditText) content.findViewById(R.id.home_inputSourceText);
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
         * configure out put text field*/
        outPutTextView = (TextView) content.findViewById(R.id.home_outPutText);

        return content;
    }

    @Override
    public void onStop() {
        super.onStop();
        /**
         * saving prefs
         * */
        saveViewTextToPrefs(PREFS_LANG_FROM, fromLangSelectionView);
        saveViewTextToPrefs(PREFS_LANG_TO, toLangSelectionView);
    }

    private class TranslateRequestListener implements RequestManager.RequestListener {

        @Override
        public void onRequestFinished(Request request, Bundle resultData) {
            /**
             * setting text to view
             * */
            outPutTextView.setText(resultData.getString(TranslateOperations.TranslateParams.OUTPUT_TEXT));
            ;
            /**
             * saving to prefs result
             * */
            saveViewTextToPrefs(PREFS_SOURCE_TEXT, sourceTextView);
            saveViewTextToPrefs(PREFS_OUTPUT_TEXT, outPutTextView);

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
        getFragmentManager().beginTransaction().add(R.id.contentContainer, langSelectionFragment).commit();
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

    private boolean validateViewReadyToTranslate() {
        return false;
    }

    private void saveViewTextToPrefs(String key, TextView view) {
        SharedPreferences settings = parentActivity.getSharedPreferences(MainActivity.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, view.getText().toString());
        // Commit the edits!
        editor.apply();
    }
}
