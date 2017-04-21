package edu.semnag.myyandextranslate.fragments;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
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
import android.widget.ImageView;
import android.widget.TextView;

import edu.semnag.myyandextranslate.R;
import edu.semnag.myyandextranslate.TranslateActivity;
import edu.semnag.myyandextranslate.provider.TranslatorContract;

/**
 * @author SemenNag
 *         List Fragment with Loader and Custom ListAdapter - HistoryListAdapter
 */

public class HistoryPageFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String HISTORY_PAGE_TYPE = "HISTORY_PAGE_TYPE";
    public static final String HISTORY_PAGE = "HISTORY";
    public static final String HISTORY_FAV_PAGE = "FAVORITE";
    private HistoryListAdapter simpleCursorAdapter;
    private static final String[] PROJECTION = new String[]{
            TranslatorContract.TranslateRegistry._ID,
            TranslatorContract.TranslateRegistry.COLUMN_NAME_SOURCE_TEXT,
            TranslatorContract.TranslateRegistry.COLUMN_NAME_OUTPUT_TEXT,
            TranslatorContract.TranslateRegistry.COLUMN_NAME_TRANSLATE_DIR,
            TranslatorContract.TranslateRegistry.COLUMN_NAME_LANG_FROM_DESC,
            TranslatorContract.TranslateRegistry.COLUMN_NAME_LANG_TO_DESC,
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
                TranslatorContract.TranslateRegistry.COLUMN_NAME_TRANSLATE_DIR
        };

        int[] toViews = {
                R.id.history_row_from,
                R.id.history_row_to,
                R.id.history_row_translate_direction
        };

        /**
         * Configure list adapter
         * */
        simpleCursorAdapter = new HistoryListAdapter(getActivity(), null, fromColumns, toViews, 0);
        setListAdapter(simpleCursorAdapter);
        /**
         * Register loader
         * */
        getLoaderManager().initLoader(0, getArguments(), this);

        return inflater.inflate(R.layout.fragment_history_list, container, false);

    }

    /**
     * Not the best solution, each opening to restart whole loader
     */
    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(0, getArguments(), this);
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
                TranslatorContract.TranslateRegistry.COLUMNT_NAME_TIMESTAMP + " desc");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        simpleCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        simpleCursorAdapter.swapCursor(null);
    }

    /**
     * Custom List Adapter
     * Because one row contains different views with different on click handlers
     */
    private class HistoryListAdapter extends SimpleCursorAdapter {
        Cursor cursor;
        LayoutInflater layoutInflater;

        public HistoryListAdapter(Context context, Cursor c, String[] from, int[] to, int flags) {
            super(context, R.layout.fragment_history_item, c, from, to, flags);
            this.layoutInflater = LayoutInflater.from(context);
            this.cursor = c;
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return layoutInflater.inflate(R.layout.fragment_history_item, null);
        }

        @Override
        public void bindView(final View view, Context context, final Cursor cursor) {

            /**
             * binding views to cursor data
             * and set it click handler
             * */
            TextView fromTextView = (TextView) view.findViewById(R.id.history_row_from);
            int fromTextIndex = cursor.getColumnIndexOrThrow(TranslatorContract.TranslateRegistry.COLUMN_NAME_SOURCE_TEXT);
            fromTextView.setText(cursor.getString(fromTextIndex));

            TextView toTextView = (TextView) view.findViewById(R.id.history_row_to);
            int toTextIndex = cursor.getColumnIndexOrThrow(TranslatorContract.TranslateRegistry.COLUMN_NAME_OUTPUT_TEXT);
            toTextView.setText(cursor.getString(toTextIndex));

            TextView langDireactionView = (TextView) view.findViewById(R.id.history_row_translate_direction);
            int langDireactionIndex = cursor.getColumnIndexOrThrow(TranslatorContract.TranslateRegistry.COLUMN_NAME_TRANSLATE_DIR);
            langDireactionView.setText(cursor.getString(langDireactionIndex));

            /**
             * configuring click handler that is used to start translate activity
             * */
            HistoryRowWithTranslateClickHandler clickHandler = new HistoryRowWithTranslateClickHandler();
            fromTextView.setOnClickListener(clickHandler);
            toTextView.setOnClickListener(clickHandler);
            langDireactionView.setOnClickListener(clickHandler);
            /**
             * Configuring star responsible to show whether the list item is favourite
             * */
            int isFavIndex = cursor.getColumnIndexOrThrow(TranslatorContract.TranslateRegistry.COLUMN_NAME_IS_FAV);
            int isFave = cursor.getInt(isFavIndex);
            ImageView isFavImage = (ImageView) view.findViewById(R.id.history_row_isFav);
            final int[] imageIds = {R.drawable.ic_star_border_black_24dp, R.drawable.ic_star_black_24dp};
            if (isFave == 0) {
                isFavImage.setImageResource(imageIds[0]);
                isFavImage.setTag(imageIds[0]);
            } else {
                isFavImage.setImageResource(imageIds[1]);
                isFavImage.setTag(imageIds[1]);
            }
            /**
             * Making custom on click listener which^
             * 1) Updateds record if it becomes if favorite and vise a verse
             * 2) Replace drawing star, according to its isFav status
             * */
            isFavImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ImageView imageView = (ImageView) v;
                    /**
                     * discover whether it is fav or not
                     * */

                    boolean isFave = (int) v.getTag() != imageIds[0];
                    /**
                     * changing icon of image
                     * */
                    if (isFave) {
                        imageView.setImageResource(imageIds[0]);
                        imageView.setTag(imageIds[0]);
                    } else {
                        imageView.setImageResource(imageIds[1]);
                        imageView.setTag(imageIds[1]);
                    }
                    /**
                     * updating item in db with changing it isFav attribute
                     * */
                    int position = getListView().getPositionForView(v);
                    Cursor recordCursor = (Cursor) getListView().getItemAtPosition(position);
                    String recordId = recordCursor.getString(cursor.getColumnIndex(TranslatorContract.TranslateRegistry._ID));
                    String selection = TranslatorContract.TranslateRegistry._ID + "=? ";
                    String[] selectionArgs = {recordId};

                    ContentValues mUpdateValues = new ContentValues();
                    int favFlag = isFave ? 0 : 1;
                    mUpdateValues.put(TranslatorContract.TranslateRegistry.COLUMN_NAME_IS_FAV, favFlag);

                    getContext().getContentResolver().update(TranslatorContract.TranslateRegistry.CONTENT_URI,
                            mUpdateValues,
                            selection,
                            selectionArgs);

                }
            });
        }

    }

    /**
     * Custom click handler which fires to new TranslateActivity Intent
     * when clicking on a row with history or favorite
     */
    private class HistoryRowWithTranslateClickHandler implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            int position = getListView().getPositionForView(v);
            Cursor cursor = (Cursor) getListView().getItemAtPosition(position);
            String sourceText = cursor.getString(cursor.getColumnIndexOrThrow(TranslatorContract.TranslateRegistry.COLUMN_NAME_SOURCE_TEXT));
            String outPutText = cursor.getString(cursor.getColumnIndexOrThrow(TranslatorContract.TranslateRegistry.COLUMN_NAME_OUTPUT_TEXT));
            String langFromSelection = cursor.getString(cursor.getColumnIndexOrThrow(TranslatorContract.TranslateRegistry.COLUMN_NAME_LANG_FROM_DESC));
            String langToSelection = cursor.getString(cursor.getColumnIndexOrThrow(TranslatorContract.TranslateRegistry.COLUMN_NAME_LANG_TO_DESC));

            Bundle message = new Bundle();
            message.putString(TranslateActivity.KEY_LANG_FROM_SELECTED, langFromSelection);
            message.putString(TranslateActivity.KEY_LANG_TO_SELECTED, langToSelection);
            message.putString(TranslateActivity.KEY_SOURCE_TEXT, sourceText);
            message.putString(TranslateActivity.KEY_OUTPUT_TEXT, outPutText);
            Intent intent = new Intent(getActivity(), TranslateActivity.class);
            intent.putExtras(message);

            startActivity(intent);

        }
    }

}
