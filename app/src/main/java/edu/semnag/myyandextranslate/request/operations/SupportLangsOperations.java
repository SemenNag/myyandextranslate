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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import edu.semnag.myyandextranslate.provider.TranslatorContract;

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
        /**
         * 1. Proceeding network call
         * */
        NetworkConnection connection = new NetworkConnection(context, PATH);
        HashMap<String, String> params = new HashMap<>();
        params.put("key", ApiKey.API_KEY);
        params.put("ui", UILANG);
        connection.setParameters(params);
        NetworkConnection.ConnectionResult result = connection.execute();
        try {
            /**
             * 2. Converting network response to list of content values
             * */
            List<ContentValues> parsedNetworkResponse = networkResponse2ContentValuesExtractor(result);
            if (parsedNetworkResponse.size() == 0) {
                throw new DataException("server response is empty");
            }
            /**
             * 3. Getting the local snapshot of languages
             * */
            List<ContentValues> localCopyOfData = new ArrayList<>();
            Cursor localData = context.getContentResolver().query(CONTENT_URI, new String[]{COLUMN_LANG_CODE, COLUMN_LANG_DESC}, null, null, null);
            if (localData.moveToFirst()) {
                do {
                    String langCodeFromDB = localData.getString(localData.getColumnIndex(COLUMN_LANG_CODE));
                    String langDescFromDB = localData.getString(localData.getColumnIndex(COLUMN_LANG_DESC));
                    ContentValues item = new ContentValues();
                    item.put(TranslatorContract.SupportLangs.COLUMN_LANG_CODE, langCodeFromDB);
                    item.put(TranslatorContract.SupportLangs.COLUMN_LANG_DESC, langDescFromDB);

                    localCopyOfData.add(item);
                } while (localData.moveToNext());
            }
            /**
             * 4. Because we have 2 lists with values, we want to find out
             * a) Values which we have locally, but api doesn't support them according to fresh api response
             * b) Values which both in local and network versions of data, in order not to touch them
             * c) Values which has in fresh network response, but not in local version of data, to insert them
             * */
            Collection<ContentValues> toDelete = new HashSet<>(localCopyOfData);
            toDelete.removeAll(parsedNetworkResponse);
            String[] toDeleteSelection = new String[toDelete.size()];
            int i = 0;
            for (ContentValues item : toDelete) {
                toDeleteSelection[i] = item.getAsString(TranslatorContract.SupportLangs.COLUMN_LANG_CODE);
            }
            /**
             * 4.a*/
            context.getContentResolver().delete(CONTENT_URI, TranslatorContract.SupportLangs.COLUMN_LANG_CODE + "=?", toDeleteSelection);
            /**
             * 4.c*/
            Collection<ContentValues> toInsert = new HashSet<>(parsedNetworkResponse);
            toInsert.removeAll(localCopyOfData);
            context.getContentResolver().bulkInsert(CONTENT_URI, toInsert.toArray(new ContentValues[toInsert.size()]));

        } catch (JSONException ex) {
            Log.e("SupportLangOperations", "Problem with parsing JSON response");
            throw new DataException("Error occured with server response handling");
        }
        return null;
    }

    private List<ContentValues> networkResponse2ContentValuesExtractor(NetworkConnection.ConnectionResult connectionResult) throws JSONException {
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
         * prepare result set of content values
         * */
        List<ContentValues> dataFromNet = new ArrayList<>();
        /**
         * iterating via response result
         * */
        Iterator iterator = langDictionary.keys();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            ContentValues item = new ContentValues();
            item.put(COLUMN_LANG_CODE, key);
            item.put(COLUMN_LANG_DESC, langDictionary.getString(key));
            dataFromNet.add(item);
        }
        return dataFromNet;
    }
}
