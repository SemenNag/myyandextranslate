package edu.semnag.myyandextranslate.request.operations;

import com.foxykeep.datadroid.exception.CustomRequestException;

/**
 * Created by semna on 18.04.2017.
 */

public class TranslateExeptions extends CustomRequestException {
    private String translateExeptionType;

    interface TranslateExeptionTypes {
        String SOURCE_TEXT_LARGE = "source text is too large";
        String CANNOT_TRANSLATE = "cannot translate";
        String BAD_TRANLATE_DIRECTION = "bad translate direction";
    }

    public TranslateExeptions(String translateExeptionType) {
        super(translateExeptionType);
        this.translateExeptionType = translateExeptionType;
    }

}
