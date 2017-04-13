package edu.semnag.myyandextranslate.fragments.history;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import edu.semnag.myyandextranslate.R;
import edu.semnag.myyandextranslate.provider.TranslatorContract;

/**
 * Created by semna on 31.03.2017.
 */

public class HistoryPageFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String HISTORY_PAGE_TYPE = "HISTORY_PAGE_TYPE";
    public static final String HISTORY_PAGE = "HISTORY";
    public static final String HISTORY_FAV_PAGE = "FAVORITE";
    private SimpleCursorAdapter simpleCursorAdapter;
    private static final String[] PROJECTION = new String[]{
            TranslatorContract.TranslateRegistry._ID,
            TranslatorContract.TranslateRegistry.COLUMN_NAME_SOURCE_TEXT,
            TranslatorContract.TranslateRegistry.COLUMN_NAME_OUTPUT_TEXT,
            TranslatorContract.TranslateRegistry.COLUMN_NAME_TRANSLATE_DIR,
            TranslatorContract.TranslateRegistry.COLUMN_NAME_IS_FAV
    };


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        String[] fromColumns = {
                TranslatorContract.TranslateRegistry.COLUMN_NAME_SOURCE_TEXT,
                TranslatorContract.TranslateRegistry.COLUMN_NAME_OUTPUT_TEXT,
                TranslatorContract.TranslateRegistry.COLUMN_NAME_TRANSLATE_DIR,
                TranslatorContract.TranslateRegistry.COLUMN_NAME_IS_FAV
        };
        int[] toViews = {
                R.id.history_row_from,
                R.id.history_row_to,
                R.id.history_row_translate_direction,
                R.id.history_row_fav};

        simpleCursorAdapter = new SimpleCursorAdapter(getActivity(), R.layout.history_row,
                null, fromColumns, toViews, 0);
        setListAdapter(simpleCursorAdapter);

        getLoaderManager().initLoader(0, getArguments(), this);

        return (ViewGroup) inflater.inflate(R.layout.list_fragment_base, container, false);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String type = args.getString(HISTORY_PAGE_TYPE);
        String selection = null;
        if (type.equals(HISTORY_FAV_PAGE)) {
            selection = TranslatorContract.TranslateRegistry.COLUMN_NAME_IS_FAV + "=1";
        }
        return new CursorLoader(getActivity(), TranslatorContract.TranslateRegistry.CONTENT_URI,
                PROJECTION,
                selection,
                null,
                TranslatorContract.TranslateRegistry.COLUMNT_NAME_TIMESTAMP+" desc");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        simpleCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        simpleCursorAdapter.swapCursor(null);
    }
}
