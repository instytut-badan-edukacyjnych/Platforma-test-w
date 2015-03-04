/*
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
 */

package pl.edu.ibe.loremipsum.tablet.report;

import android.app.Activity;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;

import pl.edu.ibe.loremipsum.tablet.base.ServiceProvider;
import pl.edu.ibe.loremipsum.tools.ExecutionException;
import pl.edu.ibe.loremipsum.tools.LogUtils;
import pl.edu.ibe.loremipsum.tools.StringUtils;
import pl.edu.ibe.loremipsum.tools.TimeUtils;
import pl.edu.ibe.testplatform.BuildConfig;

/**
 * Allows to communicate with @link{android.webkit.WebView}
 * Created by adam on 24.03.14.
 */
public class ResultsJSHandler {
    private static final String TAG = ResultsJSHandler.class.toString();
    private ServiceProvider serviceProvider;
    private Activity activity;
    private WebView webView;

    public ResultsJSHandler(ServiceProvider serviceProvider, Activity activity, WebView webView) {
        this.serviceProvider = serviceProvider;
        this.activity = activity;
        this.webView = webView;
    }

    public void install(boolean installProvider) {
        webView.getSettings().setJavaScriptEnabled(true);
        if (installProvider)
            webView.addJavascriptInterface(this, "resultsProvider");
    }

    @JavascriptInterface
    public String getResults() {
        LogUtils.v(TAG, "getResults()");
        String r = serviceProvider.results().getResults().toBlockingObservable().singleOrDefault("error");
        LogUtils.v(TAG, "returning: " + r);
        return r;
    }

    @JavascriptInterface
    public String getExaminee(String textId) {
        if (BuildConfig.SHOW_FAKE_DATA_IN_REPORT) {
            return "{\"birthday\":\"11-11-2014\",\"surname\":\"Test\",\"name\":\"" + textId + "\"}";
        }

        LogUtils.v(TAG, "getExaminee(textId = " + textId + ")");
        String r = serviceProvider.examinee().getExamineeByTextId(textId).map(examinee -> {
            try {
                JSONObject obj = new JSONObject();
                obj.put("name", examinee.getFirstName());
                obj.put("surname", examinee.getLastName());
                if (examinee.getBirthday() != null) {
                    obj.put("birthday", TimeUtils.dateToString(examinee.getBirthday(), TimeUtils.defaultPatern));
                } else {
                    obj.put("birthday", "-----");
                }
                return obj.toString();
            } catch (JSONException | ParseException e) {
                throw ExecutionException.wrap(e);
            }
        }).toBlockingObservable().singleOrDefault("error");
        LogUtils.v(TAG, "returning: " + r);
        return r;
    }

    @JavascriptInterface
    public String getInstitution(String textId) {
        LogUtils.v(TAG, "getInstitution(textId = " + textId + ")");
        String r = serviceProvider.institution().getInstitutionByTextId(textId).map(institution -> {
            try {
                JSONObject obj = new JSONObject();
                obj.put("name", institution.getName());
                obj.put("postal", institution.getPostalCode());
                obj.put("city", institution.getCity());
                return obj.toString();
            } catch (JSONException e) {
                throw ExecutionException.wrap(e);
            }
        }).toBlockingObservable().singleOrDefault("error");
        LogUtils.v(TAG, "returning: " + r);
        return r;
    }

    @JavascriptInterface
    public String getTprResults() {
        LogUtils.d(TAG, "getTprResults");
        String s = serviceProvider.results().getTprResults().toBlockingObservable().singleOrDefault("error");
        LogUtils.d(TAG, s);

        if (BuildConfig.SHOW_FAKE_DATA_IN_REPORT) {
            try {
                return StringUtils.inputStreamToString(activity.getAssets().open("tprResults.js"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return s;
    }
}
