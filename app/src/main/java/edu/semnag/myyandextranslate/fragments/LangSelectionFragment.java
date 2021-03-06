package edu.semnag.myyandextranslate.fragments;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.foxykeep.datadroid.requestmanager.Request;
import com.foxykeep.datadroid.requestmanager.RequestManager;

import java.util.HashMap;
import java.util.Map;

import edu.semnag.myyandextranslate.R;
import edu.semnag.myyandextranslate.TranslateActivity;
import edu.semnag.myyandextranslate.provider.TranslatorContract;
import edu.semnag.myyandextranslate.request.TranslateRequestFactory;
import edu.semnag.myyandextranslate.request.TranslateRequestManager;

/**
 * @author SemenNag
 *         <p>
 *         List Fragment which shows available languages.
 * @screen ListView + SimpleCursorAdapter
 * @back end
 * SimpleCursorLoader + RequestManager
 */

public class LangSelectionFragment
        extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>, RequestManager.RequestListener {
    private SimpleCursorAdapter mAdapter;
    private TranslateRequestManager requestManager;
    private TranslateActivity.ListFragmentItemClickListener asker;
    private static final String[] PROJECTION = new String[]{TranslatorContract.SupportLangs._ID,
            TranslatorContract.SupportLangs.COLUMN_LANG_DESC
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /**
         *  For the cursor adapter, specify which columns go into which views
         */
        String[] fromColumns = {TranslatorContract.SupportLangs.COLUMN_LANG_DESC};
        int[] toViews = {R.id.lang_selection_item};

        mAdapter = new SimpleCursorAdapter(getActivity(), R.layout.fragment_lang_selection_item,
                null, fromColumns, toViews, 0);
        setListAdapter(mAdapter);

        /**
         * init data loader
         * */
        getLoaderManager().initLoader(0, null, this);

        /**
         * getting instance of request manager
         * */
        requestManager = TranslateRequestManager.from(getContext());

        /**
         * firing new data request
         * */
        update();
        /**
         * saving self state cause orientation may be changed
         * */
        setRetainInstance(true);

        /**
         * Organazing up bar with navigation to home and text
         * */
        View view = inflater.inflate(R.layout.fragment_lang_selection_list, container, false);

        ImageView imageView = (ImageView) view.findViewById(R.id.lang_selection_up_bar_back);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goBack();
            }
        });

        TextView textView = (TextView) view.findViewById(R.id.lang_selection_up_bar_text);
        TranslateActivity.LangSelectionOnClickHandler handler = (TranslateActivity.LangSelectionOnClickHandler) getAsker();
        switch (handler.getSourceId()) {
            case R.id.fromLangSelection:
                textView.setText(R.string.select_lang_from);
                break;
            case R.id.toLangSelection:
                textView.setText(R.string.select_lang_to);
                break;
        }


        return view;
    }

    /**
     * Specifing item click listener
     *
     * @param asker external listener
     *              for list clicked response
     */
    public void setItemClickListener(TranslateActivity.ListFragmentItemClickListener asker) {
        this.asker = asker;
    }

    public TranslateActivity.ListFragmentItemClickListener getAsker() {
        return asker;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        /**
         * registering the cursor loader
         * */
        return new CursorLoader(getActivity(), TranslatorContract.SupportLangs.CONTENT_URI,
                PROJECTION, null, null, TranslatorContract.SupportLangs.COLUMNT_NAME_TIMESTAMP + " desc");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    /**
     * Method with asking RequestManager to
     * pull data from api
     */
    private void update() {
        Request updateData = new Request(TranslateRequestFactory.REQUEST_SUPPORT_LANGS);
        requestManager.execute(updateData, this);
    }

    private void goBack() {
        getFragmentManager().popBackStack();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Cursor cursor = (Cursor) getListView().getItemAtPosition(position);
        String selectedLang = cursor.getString(cursor.getColumnIndex(TranslatorContract.SupportLangs.COLUMN_LANG_DESC));

        Map<String, String> mail = new HashMap<>();
        mail.put(TranslateActivity.ListFragmentItemClickListener.LANG_SELECTION, selectedLang);

        asker.onListFragmentItemClicked(mail);

        /**
         * updating last selection time
         * */
        ContentValues contentValues = new ContentValues();
        contentValues.put(TranslatorContract.SupportLangs.COLUMNT_NAME_TIMESTAMP, System.currentTimeMillis());
        getContext().getContentResolver().update(TranslatorContract.SupportLangs.CONTENT_URI,
                contentValues,
                TranslatorContract.SupportLangs.COLUMN_LANG_DESC + "=?",
                new String[]{selectedLang});

        getFragmentManager().beginTransaction().remove(this).commit();
    }

    @Override
    public void onRequestFinished(Request request, Bundle resultData) {
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onRequestConnectionError(Request request, int statusCode) {
        Toast.makeText(getActivity(), "Problem with server", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onRequestDataError(Request request) {
        Toast.makeText(getActivity(), "Problem with server", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestCustomError(Request request, Bundle resultData) {
        Toast.makeText(getActivity(), "Problem with server", Toast.LENGTH_SHORT).show();
    }
}
