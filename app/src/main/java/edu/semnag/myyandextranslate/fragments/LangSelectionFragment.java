package edu.semnag.myyandextranslate.fragments;

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
import android.widget.ListView;

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
 * Created by semna on 05.04.2017.
 */

public class LangSelectionFragment extends ListFragment
        implements LoaderManager.LoaderCallbacks<Cursor>,
        RequestManager.RequestListener {
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

        mAdapter = new SimpleCursorAdapter(getActivity(), R.layout.lang_selection_item,
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

        return inflater.inflate(R.layout.fragment_lang_selection_list, container, false);
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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        /**
         * registering the cursor loader
         * */
        return new CursorLoader(getActivity(), TranslatorContract.SupportLangs.CONTENT_URI,
                PROJECTION, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    /**
     * Method with asking RequestManager to
     * pull data from api
     */
    private void update() {
        Request updateData = new Request(TranslateRequestFactory.REQUEST_SUPPORT_LANGS);
        requestManager.execute(updateData, this);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Cursor cursor = (Cursor) getListView().getItemAtPosition(position);
        String selectedLang = cursor.getString(cursor.getColumnIndex(TranslatorContract.SupportLangs.COLUMN_LANG_DESC));

        Map<String, String> mail = new HashMap<>();
        mail.put(TranslateActivity.ListFragmentItemClickListener.LANG_SELECTION, selectedLang);

        asker.onListFragmentItemClicked(mail);
        getFragmentManager().beginTransaction().remove(this).commit();
    }

    @Override
    public void onRequestFinished(Request request, Bundle resultData) {
        mAdapter.notifyDataSetChanged();
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
