/************************************
 * This file is part of Test Platform.
 *
 * Test Platform is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Test Platform is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Test Platform; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * Ten plik jest częścią Platformy Testów.
 *
 * Platforma Testów jest wolnym oprogramowaniem; możesz go rozprowadzać dalej
 * i/lub modyfikować na warunkach Powszechnej Licencji Publicznej GNU,
 * wydanej przez Fundację Wolnego Oprogramowania - według wersji 2 tej
 * Licencji lub (według twojego wyboru) którejś z późniejszych wersji.
 *
 * Niniejszy program rozpowszechniany jest z nadzieją, iż będzie on
 * użyteczny - jednak BEZ JAKIEJKOLWIEK GWARANCJI, nawet domyślnej
 * gwarancji PRZYDATNOŚCI HANDLOWEJ albo PRZYDATNOŚCI DO OKREŚLONYCH
 * ZASTOSOWAŃ. W celu uzyskania bliższych informacji sięgnij do
 * Powszechnej Licencji Publicznej GNU.
 *
 * Z pewnością wraz z niniejszym programem otrzymałeś też egzemplarz
 * Powszechnej Licencji Publicznej GNU (GNU General Public License);
 * jeśli nie - napisz do Free Software Foundation, Inc., 59 Temple
 * Place, Fifth Floor, Boston, MA  02110-1301  USA
 ************************************/
package pl.edu.ibe.loremipsum.tablet.report;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.IOException;

import pl.edu.ibe.testplatform.R;
import pl.edu.ibe.loremipsum.tools.LogUtils;
import pl.edu.ibe.loremipsum.tools.io.ReadStream;
import rx.schedulers.Schedulers;

/**
 * @author Mariusz Pluciński
 *         Display Google Print dialog
 */
public class PrintDialogActivity extends Activity {
    private static final String TAG = PrintDialogActivity.class.toString();

    private static final String PRINT_DIALOG_URL = "https://www.google.com/cloudprint/dialog.html";
    private static final String JS_INTERFACE = "AndroidPrintDialog";
    private static final String CONTENT_TRANSFER_ENCODING = "base64";

    private static final String ZXING_URL = "http://zxing.appspot.com";
    private static final int ZXING_SCAN_REQUEST = 65743;

    /**
     * Post message that is sent by Print Dialog web page when the printing dialog
     * needs to be closed.
     */
    private static final String CLOSE_POST_MESSAGE_NAME = "cp-dialog-on-close";
    /**
     * Intent that started the action.
     */
    Intent cloudPrintIntent;
    /**
     * Web view element to show the printing dialog in.
     */
    private WebView dialogWebView;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setContentView(R.layout.dialog_print);
        dialogWebView = (WebView) findViewById(R.id.webview);
        cloudPrintIntent = this.getIntent();

        WebSettings settings = dialogWebView.getSettings();
        settings.setJavaScriptEnabled(true);

        dialogWebView.setWebViewClient(new PrintDialogWebClient());
        dialogWebView.addJavascriptInterface(
                new PrintDialogJavaScriptInterface(), JS_INTERFACE);

        dialogWebView.loadUrl(PRINT_DIALOG_URL);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == ZXING_SCAN_REQUEST && resultCode == RESULT_OK) {
            dialogWebView.loadUrl(intent.getStringExtra("SCAN_RESULT"));
        }
    }

    final class PrintDialogJavaScriptInterface {
        public String getType() {
            return cloudPrintIntent.getType();
        }

        public String getTitle() {
            return cloudPrintIntent.getExtras().getString("title");
        }

        public String getContent() {
            try {
                return Base64.encodeToString(ReadStream
                        .readStream(getContentResolver().openInputStream(cloudPrintIntent.getData()))
                        .execute()
                        .subscribeOn(Schedulers.io())
                        .filter(progress -> progress != null)
                        .filter(ReadStream.ReadProgress::isFinished)
                        .map(ReadStream.ReadProgress::getBytes)
                        .toBlockingObservable().single(), Base64.DEFAULT);
            } catch (IOException e) {
                LogUtils.v(TAG, "Error while getting content");
            }
            return "";
        }

        public String getEncoding() {
            return CONTENT_TRANSFER_ENCODING;
        }

        public void onPostMessage(String message) {
            if (message.startsWith(CLOSE_POST_MESSAGE_NAME)) {
                finish();
            }
        }
    }

    private final class PrintDialogWebClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.startsWith(ZXING_URL)) {
                Intent intentScan = new Intent("com.google.zxing.client.android.SCAN");
                intentScan.putExtra("SCAN_MODE", "QR_CODE_MODE");
                try {
                    startActivityForResult(intentScan, ZXING_SCAN_REQUEST);
                } catch (ActivityNotFoundException error) {
                    view.loadUrl(url);
                }
            } else {
                view.loadUrl(url);
            }
            return false;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if (PRINT_DIALOG_URL.equals(url)) {
                // Submit print document.
                view.loadUrl("javascript:printDialog.setPrintDocument(printDialog.createPrintDocument("
                        + "window." + JS_INTERFACE + ".getType(),window." + JS_INTERFACE + ".getTitle(),"
                        + "window." + JS_INTERFACE + ".getContent(),window." + JS_INTERFACE + ".getEncoding()))");

                // Add post messages listener.
                view.loadUrl("javascript:window.addEventListener('message',"
                        + "function(evt){window." + JS_INTERFACE + ".onPostMessage(evt.data)}, false)");
            }
        }
    }
}