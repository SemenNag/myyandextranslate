package edu.semnag.myyandextranslate.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by semna on 03.04.2017.
 */

public class LocalContentProvider extends ContentProvider {

    private static final String DB_NAME = "myyandextranslator.db";
    private static final int DB_VERSION = 4;

    private DataBaseHelper mDataBaseHelper;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final int PATH_SUPPORT_LANGS = 1;
    private static final int PATH_TRANSLATE_REGISTRY = 2;

    static {
        sUriMatcher.addURI(TranslatorContract.AUTHORITY, TranslatorContract.SupportLangs.CONTENT_PATH, PATH_SUPPORT_LANGS);
        sUriMatcher.addURI(TranslatorContract.AUTHORITY, TranslatorContract.TranslateRegistry.CONTENT_PATH, PATH_TRANSLATE_REGISTRY);
    }

    class DataBaseHelper extends SQLiteOpenHelper {
        private static final String TEXT_TYPE = " TEXT";
        private static final String INT_TYPE = " INTEGER";
        private static final String COMMA_SEP = ",";

        private static final String SQL_CREATE_SUPPORT_LANG =
                "CREATE TABLE " + TranslatorContract.SupportLangs.TABLE_NAME + " (" +
                        TranslatorContract.SupportLangs._ID + " INTEGER PRIMARY KEY," +
                        TranslatorContract.SupportLangs.COLUMN_LANG_CODE + TEXT_TYPE + COMMA_SEP +
                        TranslatorContract.SupportLangs.COLUMN_LANG_DESC + TEXT_TYPE +
                        " )";

        private static final String SQL_DELETE_SUPPORT_LANG =
                "DROP TABLE IF EXISTS " + TranslatorContract.SupportLangs.TABLE_NAME;

        private static final String SQL_CREATE_TRANSLATE_REGISTRY =
                "CREATE TABLE " + TranslatorContract.TranslateRegistry.TABLE_NAME + " (" +
                        TranslatorContract.TranslateRegistry._ID + " INTEGER PRIMARY KEY," +
                        TranslatorContract.TranslateRegistry.COLUMN_NAME_SOURCE_TEXT + TEXT_TYPE + COMMA_SEP +
                        TranslatorContract.TranslateRegistry.COLUMN_NAME_TRANSLATE_DIR + TEXT_TYPE + COMMA_SEP +
                        TranslatorContract.TranslateRegistry.COLUMN_NAME_OUTPUT_TEXT + TEXT_TYPE + COMMA_SEP +
                        TranslatorContract.TranslateRegistry.COLUMNT_NAME_TIMESTAMP + INT_TYPE + COMMA_SEP +
                        TranslatorContract.TranslateRegistry.COLUMN_NAME_IS_FAV + INT_TYPE +
                        " )";

        private static final String SQL_DELETE_TRANSLATE_REGISTRY =
                "DROP TABLE IF EXISTS " + TranslatorContract.TranslateRegistry.TABLE_NAME;


        public DataBaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            /**
             * creating table with supportive langs
             * */
            db.execSQL(SQL_CREATE_SUPPORT_LANG);
            /**
             * creating table with history of translate
             * */
            db.execSQL(SQL_CREATE_TRANSLATE_REGISTRY);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(SQL_DELETE_SUPPORT_LANG);
            db.execSQL(SQL_DELETE_TRANSLATE_REGISTRY);
            onCreate(db);
        }
    }

    @Override
    public boolean onCreate() {
        mDataBaseHelper = new DataBaseHelper(getContext(), DB_NAME, null, DB_VERSION);
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Cursor cursor;
        String tableName = getTableName(uri);
        cursor = mDataBaseHelper.getReadableDatabase().query(tableName, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case PATH_SUPPORT_LANGS:
                return TranslatorContract.SupportLangs.CONTENT_TYPE;
            case PATH_TRANSLATE_REGISTRY:
                return TranslatorContract.TranslateRegistry.CONTENT_TYPE;
        }
        return null;
    }


    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        String tableName = getTableName(uri);
        mDataBaseHelper.getWritableDatabase().insert(tableName, null, values);
        getContext().getContentResolver().notifyChange(uri, null);
        return uri;

    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        String tableName = getTableName(uri);
        mDataBaseHelper.getWritableDatabase().delete(tableName, selection, selectionArgs);
        return 0;

    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        String tableName = getTableName(uri);
        mDataBaseHelper.getWritableDatabase().update(tableName, values, selection, selectionArgs);
        return 0;
    }

    public int bulkInsert(Uri uri, ContentValues[] values) {
        int numInserted = 0;

        String table = getTableName(uri);

        if (table == null) {
            return 0;
        }

        SQLiteDatabase sqlDB = mDataBaseHelper.getWritableDatabase();
        sqlDB.beginTransaction();
        try {
            for (ContentValues cv : values) {
                long newID = sqlDB.insertOrThrow(table, null, cv);
                if (newID <= 0) {
                    throw new SQLException("Failed to insert row into " + uri);
                }
            }
            sqlDB.setTransactionSuccessful();

            getContext().getContentResolver().notifyChange(uri, null);
            numInserted = values.length;
        } finally {
            sqlDB.endTransaction();
        }
        return numInserted;
    }

    private String getTableName(Uri uri) {
        String tableName;
        switch (sUriMatcher.match(uri)) {
            case PATH_SUPPORT_LANGS:
                tableName = TranslatorContract.SupportLangs.TABLE_NAME;
                break;
            case PATH_TRANSLATE_REGISTRY:
                tableName = TranslatorContract.TranslateRegistry.TABLE_NAME;
                break;
            default:
                return null;
        }
        return tableName;
    }
}
