package edu.semnag.myyandextranslate.request.operations;

import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;

import com.foxykeep.datadroid.exception.ConnectionException;
import com.foxykeep.datadroid.exception.CustomRequestException;
import com.foxykeep.datadroid.exception.DataException;
import com.foxykeep.datadroid.network.NetworkConnection;
import com.foxykeep.datadroid.requestmanager.Request;
import com.foxykeep.datadroid.service.RequestService.Operation;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import edu.semnag.myyandextranslate.provider.TranslatorContract;

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

        /**
         * After receiving response
         * we need to adopt it to our model
         * */
        List<ContentValues> supportLangsValues;
        try {
            /**
             * parsing the response
             * */
            JSONObject response = new JSONObject(result.body);
            /**
             * Extracting dictionery with
             * lang code - description
             * */
            JSONObject langDictionary = response.getJSONObject("langs");
            /**
             * iterating via response result
             * */
            Iterator iterator = langDictionary.keys();
            supportLangsValues = new ArrayList<>();

            while (iterator.hasNext()) {
                ContentValues item = new ContentValues();
                String key = (String) iterator.next();
                item.put(TranslatorContract.SupportLangs.COLUMN_LANG_CODE, key);
                item.put(TranslatorContract.SupportLangs.COLUMN_LANG_DESC, langDictionary.getString(key));
                supportLangsValues.add(item);
            }
        } catch (JSONException e) {
            throw new DataException(e.getMessage());
        }

        context.getContentResolver().delete(TranslatorContract.SupportLangs.CONTENT_URI, null, null);
        context.getContentResolver().bulkInsert(TranslatorContract.SupportLangs.CONTENT_URI,
                supportLangsValues.toArray(new ContentValues[supportLangsValues.size()]));

        return null;
    }
}
