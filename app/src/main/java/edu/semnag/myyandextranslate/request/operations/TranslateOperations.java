package edu.semnag.myyandextranslate.request.operations;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;

import com.foxykeep.datadroid.exception.ConnectionException;
import com.foxykeep.datadroid.exception.CustomRequestException;
import com.foxykeep.datadroid.exception.DataException;
import com.foxykeep.datadroid.network.NetworkConnection;
import com.foxykeep.datadroid.requestmanager.Request;
import com.foxykeep.datadroid.service.RequestService.Operation;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import edu.semnag.myyandextranslate.provider.TranslatorContract;

/**
 * Created by semna on 03.04.2017.
 */

public final class TranslateOperations implements Operation {
    private static final String PATH = "https://translate.yandex.net/api/v1.5/tr.json/translate";
    private static final String[] LANGS_CODES_PROJECTION = new String[]{
            TranslatorContract.SupportLangs._ID,
            TranslatorContract.SupportLangs.COLUMN_LANG_CODE,
            TranslatorContract.SupportLangs.COLUMN_LANG_DESC
    };

    @Override
    public Bundle execute(Context context, Request request)
            throws ConnectionException, DataException, CustomRequestException {

        String langFromDesc = request.getString(TranslateParams.FROM_LANG);
        String langToDesc = request.getString(TranslateParams.TO_LANG);
        String langFromCode = "";
        String langToCode = "";

        String langCodesSelection = TranslatorContract.SupportLangs.COLUMN_LANG_DESC +
                " in ('"
                + langFromDesc + "', '" + langToDesc + "')";

        Cursor cursor = context.getContentResolver().query(TranslatorContract.SupportLangs.CONTENT_URI,
                LANGS_CODES_PROJECTION,
                langCodesSelection,
                null, null);

        if (cursor.moveToFirst()) {
            do {
                if (cursor.getString(2).equals(langFromDesc)) {
                    langFromCode = cursor.getString(1);
                } else if (cursor.getString(2).equals(langToDesc)) {
                    langToCode = cursor.getString(1);
                }
            } while (cursor.moveToNext());
        }

        cursor.close();

        if (langFromCode.equals("")
                || langToCode.equals("")) {
            return null;
        }

        NetworkConnection connection = new NetworkConnection(context, PATH);
        HashMap<String, String> params = new HashMap<>();
        params.put("key", ApiKey.API_KEY);
        params.put("text", request.getString(TranslateParams.SOURCE_TEXT));
        params.put("lang", langFromCode.concat("-").concat(langToCode));
        params.put("format", "plain");
        connection.setParameters(params);

        NetworkConnection.ConnectionResult result = connection.execute();

        try {
            JSONObject response = new JSONObject(result.body);

            String responseCode = response.getString("code");
            switch (responseCode) {
                case "200":

                    ContentValues contentValues = new ContentValues();

                    contentValues.put(TranslatorContract.TranslateRegistry.COLUMN_NAME_SOURCE_TEXT,
                            request.getString(TranslateParams.SOURCE_TEXT));
                    contentValues.put(TranslatorContract.TranslateRegistry.COLUMN_NAME_OUTPUT_TEXT,
                            response.get("text").toString());
                    contentValues.put(TranslatorContract.TranslateRegistry.COLUMN_NAME_TRANSLATE_DIR,
                            response.get("lang").toString());
                    contentValues.put(TranslatorContract.TranslateRegistry.COLUMN_NAME_LANG_FROM_DESC,
                            langFromDesc);
                    contentValues.put(TranslatorContract.TranslateRegistry.COLUMN_NAME_LANG_TO_DESC,
                            langToDesc);
                    contentValues.put(TranslatorContract.TranslateRegistry.COLUMN_NAME_IS_FAV,
                            0);
                    contentValues.put(TranslatorContract.TranslateRegistry.COLUMNT_NAME_TIMESTAMP,
                            System.currentTimeMillis());

                    context.getContentResolver().insert(TranslatorContract.TranslateRegistry.CONTENT_URI,
                            contentValues);

                    /**
                     * sending data to client
                     * */
                    Bundle bundle = new Bundle();
                    bundle.putString(TranslateParams.OUTPUT_TEXT, response.get("text").toString());
                    return bundle;
                case "401":
                case "402":
                case "404":
                    System.out.println("api error");
                    break;
                case "413":
                    System.out.println("source text is too large");
                    break;
                case "422":
                    System.out.println("cannot translate");
                    break;
                case "501":
                    System.out.println("bad translate direction");

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public interface TranslateParams {
        String FROM_LANG = "FROM_LANG";
        String TO_LANG = "TO_LANG";
        String SOURCE_TEXT = "SOURCE_TEXT";
        String OUTPUT_TEXT = "OUTPUT_TEXT";
    }
}
