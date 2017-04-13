package edu.semnag.myyandextranslate.fragments.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.TextView;

import edu.semnag.myyandextranslate.R;
import edu.semnag.myyandextranslate.provider.TranslatorContract;

/**
 * Created by semna on 13.04.2017.
 */

public class HistoryListAdapter extends SimpleCursorAdapter {
    Context context;
    LayoutInflater layoutInflater;

    public HistoryListAdapter(Context context, Cursor c, String[] from, int[] to, int flags) {
        super(context, R.layout.history_item, c, from, to, flags);
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return layoutInflater.inflate(R.layout.history_item, null);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkBox);
        TextView fromTextView = (TextView) view.findViewById(R.id.history_row_from);

        int fromTextIndex=cursor.getColumnIndexOrThrow(TranslatorContract.TranslateRegistry.COLUMN_NAME_SOURCE_TEXT);

        fromTextView.setText(cursor.getString(fromTextIndex));

        fromTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("hello from text view");
            }
        });

        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("hello from check box");
            }
        });

    }

}
