package edu.semnag.myyandextranslate.request.operations;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;

import com.foxykeep.datadroid.exception.ConnectionException;
import com.foxykeep.datadroid.exception.CustomRequestException;
import com.foxykeep.datadroid.exception.DataException;
import com.foxykeep.datadroid.network.NetworkConnection;
import com.foxykeep.datadroid.requestmanager.Request;
import com.foxykeep.datadroid.service.RequestService.Operation;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import edu.semnag.myyandextranslate.provider.TranslatorContract;

/**
 * Created by semna on 03.04.2017.
 */

public final class TranslateOperations implements Operation {
    private static final String PATH = "https://translate.yandex.net/api/v1.5/tr.json/translate";
    private static final String LOG_TAG = "TranslateOperations";

    @Override
    public Bundle execute(Context context, Request request)
            throws ConnectionException, DataException, CustomRequestException {

        Bundle result;
        /**
         * 1.Check whether such request was early done by user
         *  Get from request: LANG_FROM_DESC, LANG_TO_DESC and SOURCE_TEXT
         *  Lookup in local db for data with such params
         *  */
        result = retrieveRequestFromLocalDB(context, request);
        if (!result.isEmpty()) {
            return result;
        }
        /**
         * 2. If locally no such data -> start to work with api
         * 2.1 Extracting from local db
         * lang codes by lang description
         * */
        Map<String, String> mapWithLangCodes = retrieveLandCodesByLangDesc(context, request);
        if (!mapWithLangCodes.containsKey(TranslateParams.FROM_LANG)
                || !mapWithLangCodes.containsKey(TranslateParams.TO_LANG)) {
            return result;
        }
        /**
         * 2.1 Proceeding api call
         * */
        NetworkConnection connection = new NetworkConnection(context, PATH);
        HashMap<String, String> params = new HashMap<>();
        params.put("key", ApiKey.API_KEY);
        params.put("text", request.getString(TranslateParams.SOURCE_TEXT));
        params.put("lang", mapWithLangCodes.get(TranslateParams.FROM_LANG) + "-" + mapWithLangCodes.get(TranslateParams.TO_LANG));
        params.put("format", "plain");
        connection.setParameters(params);

        NetworkConnection.ConnectionResult networkResponse = connection.execute();
        /**
         * 2.2 Parsing the api call response
         * and saving it to local db
         * */
        result = parseTheNetworkResponse(context, request, networkResponse);

        /**2.3 If result is null or empty, */

        return result;
    }

    /**
     * interface with strings for maps key
     * keys used in:
     * 1) communication with activity via bundle
     * 2) retrieving lang codes from db for api uses
     */
    public interface TranslateParams {
        String FROM_LANG = "FROM_LANG";
        String TO_LANG = "TO_LANG";
        String SOURCE_TEXT = "SOURCE_TEXT";
        String OUTPUT_TEXT = "OUTPUT_TEXT";
    }

    /**
     * @param context
     * @param request, request from activity with lang desc
     * @return Map<TranslateParams, String> map with lang codes from local found db by lang descs
     */
    private Map<String, String> retrieveLandCodesByLangDesc(Context context, Request request) {
        String[] LANGS_CODES_PROJECTION = new String[]{
                TranslatorContract.SupportLangs._ID,
                TranslatorContract.SupportLangs.COLUMN_LANG_CODE,
                TranslatorContract.SupportLangs.COLUMN_LANG_DESC
        };

        Map<String, String> retrieve = new HashMap<>();
        /**
         * 1. Get lang descs*/
        String langFromDesc = request.getString(TranslateParams.FROM_LANG);
        String langToDesc = request.getString(TranslateParams.TO_LANG);
        /**
         * 2. Making sql selection
         * */
        String langCodesSelection = TranslatorContract.SupportLangs.COLUMN_LANG_DESC +
                " in ('"
                + langFromDesc + "', '" + langToDesc + "')";
        /**
         * 3. Look up cursor with data
         * */
        Cursor cursor = context.getContentResolver().query(TranslatorContract.SupportLangs.CONTENT_URI,
                LANGS_CODES_PROJECTION,
                langCodesSelection,
                null, null);
        /**
         * 4. Iterating via cursor rows
         * */
        if (cursor.moveToFirst()) {
            do {
                if (cursor.getString(2).equals(langFromDesc)) {
                    retrieve.put(TranslateParams.FROM_LANG, cursor.getString(1));
                } else if (cursor.getString(2).equals(langToDesc)) {
                    retrieve.put(TranslateParams.TO_LANG, cursor.getString(1));
                }
            } while (cursor.moveToNext());
        }
        /**
         * 5. Close cursor to avoid memory leak
         * */
        cursor.close();

        return retrieve;

    }

    /**
     * method which checks whether request data is available locally
     *
     * @param context
     * @param request
     * @return Map<TranslateParams, String> map with data, if exists, or empty
     */
    private Bundle retrieveRequestFromLocalDB(Context context, Request request) {
        /**
         * 1. Get search params
         * */
        String langFromDesc = request.getString(TranslateParams.FROM_LANG);
        String langToDesc = request.getString(TranslateParams.TO_LANG);
        String sourceText = request.getString(TranslateParams.SOURCE_TEXT);
        String selection = TranslatorContract.TranslateRegistry.COLUMN_NAME_LANG_FROM_DESC + "='" + langFromDesc + "' and " +
                TranslatorContract.TranslateRegistry.COLUMN_NAME_LANG_TO_DESC + "='" + langToDesc + "' and " +
                TranslatorContract.TranslateRegistry.COLUMN_NAME_SOURCE_TEXT + "='" + sourceText + "'";
        /**
         * 2. Make query to local db
         * */
        Bundle result = new Bundle();
        Cursor cursor = context.getContentResolver().query(TranslatorContract.TranslateRegistry.CONTENT_URI,
                new String[]{TranslatorContract.TranslateRegistry._ID, TranslatorContract.TranslateRegistry.COLUMN_NAME_OUTPUT_TEXT},
                selection,
                null, null);
        if (cursor.moveToFirst()) {
            /**
             * if result found
             * we can return it*/
            result.putString(TranslateParams.OUTPUT_TEXT, cursor.getString(1));
            /**
             * and update timestamp of this record to up it in the history list
             * */
            ContentValues values = new ContentValues();
            values.put(TranslatorContract.TranslateRegistry.COLUMNT_NAME_TIMESTAMP, System.currentTimeMillis());
            context.getContentResolver().update(TranslatorContract.TranslateRegistry.CONTENT_URI, values, TranslatorContract.TranslateRegistry._ID + "=? ", new String[]{cursor.getString(0)});
        }
        return result;
    }

    private Bundle parseTheNetworkResponse(Context context, Request request, NetworkConnection.ConnectionResult connectionResult) throws ConnectionException, DataException, CustomRequestException {

        Bundle resultBundle = new Bundle();
        try {
            JSONObject response = new JSONObject(connectionResult.body);
            String responseCode = response.getString("code");
            switch (responseCode) {
                case "200":
                    String translatedText = response.get("text").toString();
                    int start = translatedText.indexOf("[\"");
                    int end = translatedText.indexOf("\"]");
                    String formatedText = translatedText.substring(start + 2, end);

                    ContentValues contentValues = new ContentValues();
                    contentValues.put(TranslatorContract.TranslateRegistry.COLUMN_NAME_SOURCE_TEXT,
                            request.getString(TranslateParams.SOURCE_TEXT));
                    contentValues.put(TranslatorContract.TranslateRegistry.COLUMN_NAME_OUTPUT_TEXT,
                            formatedText);
                    contentValues.put(TranslatorContract.TranslateRegistry.COLUMN_NAME_TRANSLATE_DIR,
                            response.get("lang").toString());
                    contentValues.put(TranslatorContract.TranslateRegistry.COLUMN_NAME_LANG_FROM_DESC,
                            request.getString(TranslateParams.FROM_LANG));
                    contentValues.put(TranslatorContract.TranslateRegistry.COLUMN_NAME_LANG_TO_DESC,
                            request.getString(TranslateParams.TO_LANG));
                    contentValues.put(TranslatorContract.TranslateRegistry.COLUMN_NAME_IS_FAV,
                            0);
                    contentValues.put(TranslatorContract.TranslateRegistry.COLUMNT_NAME_TIMESTAMP,
                            System.currentTimeMillis());

                    context.getContentResolver().insert(TranslatorContract.TranslateRegistry.CONTENT_URI,
                            contentValues);

                    resultBundle.putString(TranslateParams.OUTPUT_TEXT, formatedText);

                    return resultBundle;
                case "401":
                case "402":
                case "404":
                    Log.e(LOG_TAG, "Api error");
                    throw new DataException("Problem with network response handling");
                case "413":
                    Log.w(LOG_TAG, "source text is too large");
                    throw new TranslateExeptions(TranslateExeptions.TranslateExeptionTypes.SOURCE_TEXT_LARGE);
                case "422":
                    Log.w(LOG_TAG, "cannot translate");
                    throw new TranslateExeptions(TranslateExeptions.TranslateExeptionTypes.CANNOT_TRANSLATE);
                case "501":
                    Log.w(LOG_TAG, "bad translate direction");
                    throw new TranslateExeptions(TranslateExeptions.TranslateExeptionTypes.BAD_TRANLATE_DIRECTION);
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Problem with network response handling");
            throw new DataException("Problem with network response handling");
        }
        return resultBundle;

    }
}
