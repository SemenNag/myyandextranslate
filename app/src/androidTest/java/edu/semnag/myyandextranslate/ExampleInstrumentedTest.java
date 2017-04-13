package edu.semnag.myyandextranslate;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.foxykeep.datadroid.exception.ConnectionException;
import com.foxykeep.datadroid.exception.CustomRequestException;
import com.foxykeep.datadroid.exception.DataException;
import com.foxykeep.datadroid.requestmanager.Request;
import com.foxykeep.datadroid.requestmanager.RequestManager;
import com.foxykeep.datadroid.service.RequestService;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import edu.semnag.myyandextranslate.request.operations.SupportLangsOperations;
import edu.semnag.myyandextranslate.provider.TranslatorContract;
import edu.semnag.myyandextranslate.request.TranslateRequestFactory;
import edu.semnag.myyandextranslate.request.TranslateRequestManager;
import edu.semnag.myyandextranslate.request.operations.TranslateOperations;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    @Test
    public void testOperation() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        RequestService.Operation operation = new SupportLangsOperations();
        Request request = new Request(TranslateRequestFactory.REQUEST_SUPPORT_LANGS);
        try {
            operation.execute(appContext, request);
        } catch (ConnectionException e) {
            e.printStackTrace();
        } catch (DataException e) {
            e.printStackTrace();
        } catch (CustomRequestException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testTranslateOperation() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        RequestService.Operation operation = new TranslateOperations();
        Request request = new Request(TranslateRequestFactory.REQUEST_TRANSLATE);
        request.put(TranslateOperations.TranslateParams.FROM_LANG, "Английский");
        request.put(TranslateOperations.TranslateParams.TO_LANG, "Русский");
        request.put(TranslateOperations.TranslateParams.SOURCE_TEXT, " Hello World");

        try {
            operation.execute(appContext, request);
        } catch (ConnectionException e) {
            e.printStackTrace();
        } catch (DataException e) {
            e.printStackTrace();
        } catch (CustomRequestException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testSupportLangCallAsync() throws Exception {
        // Context of the app under test.
        final Context appContext = InstrumentationRegistry.getTargetContext();

        //prepare rest factory and request
        TranslateRequestManager requestManager = TranslateRequestManager.from(appContext);
        Request updateRequest = new Request(TranslateRequestFactory.REQUEST_SUPPORT_LANGS);
        //to test async
        final CountDownLatch signal = new CountDownLatch(1);
        //creating data cursor loader
        final String[] PROJECTION = {
                TranslatorContract.SupportLangs._ID,
                TranslatorContract.SupportLangs.COLUMN_LANG_CODE,
                TranslatorContract.SupportLangs.COLUMN_LANG_DESC
        };

        Handler mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message message) {
                // This is where you do your work in the UI thread.
                // Your worker tells you in the message what to do.
            }
        };

        ContentObserver myObserver = new ContentObserver(mHandler) {
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
            }

            @Override
            public void onChange(boolean selfChange, Uri uri) {
                super.onChange(selfChange, uri);
                printData();
            }

            private void printData() {

                Cursor cursor = appContext.getContentResolver().query(TranslatorContract.SupportLangs.CONTENT_URI,
                        PROJECTION,
                        null,
                        null,
                        null);

                String data = " , ";
                cursor.moveToFirst();
                while (!cursor.isLast()) {
                    data = data.concat("-").concat(cursor.getString(cursor.getColumnIndex(TranslatorContract.SupportLangs.COLUMN_LANG_CODE))
                            .concat(cursor.getString(cursor.getColumnIndex(TranslatorContract.SupportLangs.COLUMN_LANG_DESC))));
                    cursor.moveToNext();
                }

                System.out.println(data);
                signal.countDown();
            }

        };

        requestManager.execute(updateRequest, new RequestManager.RequestListener() {
            @Override
            public void onRequestFinished(Request request, Bundle resultData) {
                System.out.println("Data Proceed");
            }

            @Override
            public void onRequestConnectionError(Request request, int statusCode) {
                System.out.println("RConnectionError");
                signal.countDown();
            }

            @Override
            public void onRequestDataError(Request request) {
                System.out.println("DataError");
                signal.countDown();
            }

            @Override
            public void onRequestCustomError(Request request, Bundle resultData) {
                System.out.println("CustomError");
                signal.countDown();
            }
        });

        appContext.getContentResolver()
                .registerContentObserver(TranslatorContract.SupportLangs.CONTENT_URI, false, myObserver);
        signal.await(30, TimeUnit.MINUTES);

    }

}
