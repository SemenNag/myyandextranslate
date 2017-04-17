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
import java.util.Iterator;

import static edu.semnag.myyandextranslate.provider.TranslatorContract.SupportLangs.COLUMN_LANG_CODE;
import static edu.semnag.myyandextranslate.provider.TranslatorContract.SupportLangs.COLUMN_LANG_DESC;
import static edu.semnag.myyandextranslate.provider.TranslatorContract.SupportLangs.CONTENT_URI;

/**
 * Created by semna on 03.04.2017.
 */

public final class SupportLangsOperations implements Operation {
    private static final String PATH = "https://translate.yandex.net/api/v1.5/tr.json/getLangs";
    private static final String UILANG = "ru";

    @Override
    public Bundle execute(Context context, Request request)
            throws ConnectionException, DataException, CustomRequestException {

        NetworkConnection connection = new NetworkConnection(context, PATH);
        HashMap<String, String> params = new HashMap<>();
        params.put("key", ApiKey.API_KEY);
        params.put("ui", UILANG);
        connection.setParameters(params);
        NetworkConnection.ConnectionResult result = connection.execute();
        try {
            ContentValues parsedNetworkResponse = networkResponse2ContentValuesExtractor(result);
            if (parsedNetworkResponse.size() == 0) {
                throw new DataException(Log.ERROR + " server response is empty");
            }

            String[] networkLangCodes = parsedNetworkResponse.keySet().toArray(new String[parsedNetworkResponse.keySet().size()]);
            /**
             * delete all lang codes that not avaialable in api
             * */
            context.getContentResolver().delete(CONTENT_URI, COLUMN_LANG_CODE + " NOT IN (?)", networkLangCodes);
            /**
             * cleaning network data only with new values
             * */
            Cursor localData = context.getContentResolver().query(CONTENT_URI, new String[]{COLUMN_LANG_CODE, COLUMN_LANG_DESC}, null, null, null);
            if (localData.moveToFirst()) {
                do {
                    String langCode = localData.getString(localData.getColumnIndex(COLUMN_LANG_CODE));
                    if (parsedNetworkResponse.containsKey(langCode)) {
                        parsedNetworkResponse.remove(langCode);
                    }
                } while (localData.moveToNext());
            }
            localData.close();
            /**
             * inserting left data to store
             * */
            context.getContentResolver().insert(CONTENT_URI, parsedNetworkResponse);

        } catch (JSONException jsException) {
            throw new DataException(Log.ERROR + " error handling server response");
        }

        return null;
    }

    private ContentValues networkResponse2ContentValuesExtractor(NetworkConnection.ConnectionResult connectionResult) throws JSONException {
        /**
         * parsing the response
         * */
        JSONObject response = new JSONObject(connectionResult.body);
        /**
         * Extracting dictionery with
         * lang code - description
         * */
        JSONObject langDictionary = response.getJSONObject("langs");
        /**
         * iterating via response result
         * */
        Iterator iterator = langDictionary.keys();
        ContentValues dataFromNet = new ContentValues();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            dataFromNet.put(COLUMN_LANG_CODE, key);
            dataFromNet.put(COLUMN_LANG_DESC, langDictionary.getString(key));
        }
        return dataFromNet;
    }
}
