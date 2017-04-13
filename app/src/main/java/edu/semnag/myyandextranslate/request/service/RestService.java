package edu.semnag.myyandextranslate.request.service;

import com.foxykeep.datadroid.service.RequestService;

import edu.semnag.myyandextranslate.request.operations.SupportLangsOperations;
import edu.semnag.myyandextranslate.request.operations.TranslateOperations;
import edu.semnag.myyandextranslate.request.TranslateRequestFactory;

/**
 * Created by semna on 03.04.2017.
 */

public class RestService extends RequestService {

    @Override
    public Operation getOperationForType(int requestType) {
        switch (requestType) {
            case TranslateRequestFactory.REQUEST_SUPPORT_LANGS:
                return new SupportLangsOperations();
            case TranslateRequestFactory.REQUEST_TRANSLATE:
                return new TranslateOperations();
            default:
                return null;
        }
    }
}
