package edu.semnag.myyandextranslate.request;

import android.content.Context;
import com.foxykeep.datadroid.requestmanager.RequestManager;
import edu.semnag.myyandextranslate.request.service.RestService;

/**
 * Created by semna on 03.04.2017.
 */

public class TranslateRequestManager extends RequestManager {
    private TranslateRequestManager(Context context) {
        super(context, RestService.class);

    }

    private static TranslateRequestManager instance;

    public static TranslateRequestManager from(Context context) {
        if (instance == null) {
            instance = new TranslateRequestManager(context);
        }
        return instance;
    }
}
