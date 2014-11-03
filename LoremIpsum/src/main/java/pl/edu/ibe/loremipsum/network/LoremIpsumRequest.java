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

package pl.edu.ibe.loremipsum.network;

import org.apache.http.client.HttpResponseException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import pl.edu.ibe.loremipsum.tools.LogUtils;
import pl.edu.ibe.loremipsum.tools.RxExecutor;
import rx.Observable;

/**
 * @author Mariusz Pluciński
 */
public class LoremIpsumRequest<T extends LoremIpsumResponse> {
    private static final String TAG = LoremIpsumRequest.class.toString();

    // in miliseconds
    private static final int DEFAULT_CONNECT_TIMEOUT = 30000;
    private static final int DEFAULT_READ_TIMEOUT = 0; //0 means no timeout, it is good as we often download huge amount of data

    private final URL url;
    private final NetworkSupport networkSupport;
    private final LoremIpsumResponseFactory<T> factory;

    public LoremIpsumRequest(URL url, LoremIpsumResponseFactory<T> factory, NetworkSupport networkSupport) {
        this.url = url;
        this.factory = factory;
        this.networkSupport = networkSupport;
    }

    public URL getUrl() {
        return url;
    }

    public Observable<T> prepare() {
        return RxExecutor.run(() -> {
            networkSupport.increaseRequestsCount();
            LogUtils.i(TAG, "GET " + url.toExternalForm());
            HttpURLConnection connection = networkSupport.open(url);
            connection.setConnectTimeout(DEFAULT_CONNECT_TIMEOUT);
            connection.setReadTimeout(DEFAULT_READ_TIMEOUT);


            fancyNameForAdditionalHandlingConnection(connection);

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                LogUtils.i(TAG, "HTTP RESPONSE CODE: " + connection.getResponseCode() + " " + connection.getResponseMessage());
                throw new HttpResponseException(connection.getResponseCode(), connection.getResponseMessage());
            }

            InputStream is = connection.getInputStream();
            return factory.createResponse(url, connection.getHeaderFields(), is);
        });
    }

    protected void fancyNameForAdditionalHandlingConnection(HttpURLConnection connection) throws IOException {
        connection.setRequestMethod("GET");
    }
}
