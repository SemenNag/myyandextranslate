package edu.semnag.myyandextranslate.provider;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * @author SemenNag
 */

public class TranslatorContract {
    public static final String AUTHORITY = "edu.semnag.myyandextranslate";
    public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);

    public TranslatorContract() {
    }


    public static class SupportLangs implements BaseColumns {
        public static final String CONTENT_PATH = "support_langs";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, CONTENT_PATH);
        public static final String CONTENT_TYPE = "vnd.android.cursor.item/vnd." + AUTHORITY + "." + CONTENT_PATH;
        public static final String TABLE_NAME = "support_langs";
        public static final String COLUMN_LANG_CODE = "lang_code";
        public static final String COLUMN_LANG_DESC = "lang_descr";
        public static final String COLUMNT_NAME_TIMESTAMP = "timestamp";

    }

    public static class TranslateRegistry implements BaseColumns {
        public static final String CONTENT_PATH = "translate_registry";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, CONTENT_PATH);
        public static final String CONTENT_TYPE = "vnd.android.cursor.item/vnd." + AUTHORITY + "." + CONTENT_PATH;
        public static final String TABLE_NAME = "translate_reqistry";
        public static final String COLUMN_NAME_SOURCE_TEXT = "source_text";
        public static final String COLUMN_NAME_TRANSLATE_DIR = "translate_direction";
        public static final String COLUMN_NAME_OUTPUT_TEXT = "output_text";
        public static final String COLUMN_NAME_LANG_FROM_DESC = "lang_from_desc";
        public static final String COLUMN_NAME_LANG_TO_DESC = "lang_to_desc";
        public static final String COLUMN_NAME_IS_FAV = "is_favorite";
        public static final String COLUMNT_NAME_TIMESTAMP = "timestamp";

    }

}
