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

import com.squareup.okhttp.OkAuthenticator;
import com.squareup.okhttp.OkHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.List;

import pl.edu.ibe.loremipsum.tools.StringUtils;

/**
 * @author Mariusz Pluciński
 */
public class NetworkSupport {

    private OkHttpClient httpClient;
    private int requestsCount = 0;


    public NetworkSupport(OkHttpClient httpClient) {
        setHttpClient(httpClient);
    }

    public NetworkSupport setHttpClient(OkHttpClient httpClient) {
        this.httpClient = httpClient;
        return this;
    }

    public synchronized int getRequestsCount() {
        return requestsCount;
    }

    public synchronized void increaseRequestsCount() {
        requestsCount++;
    }

    public HttpURLConnection open(URL url) {
        return httpClient.open(url);
    }

    public NetworkSupport addAuth(Auth auth) {
        httpClient.setAuthenticator(new OkAuthenticator() {
            @Override
            public OkAuthenticator.Credential authenticate(Proxy proxy, URL url, List<Challenge> challenges) throws IOException {
                return auth.authenticate(challenges);
            }

            @Override
            public OkAuthenticator.Credential authenticateProxy(Proxy proxy, URL url, List<OkAuthenticator.Challenge> challenges) throws IOException {
                return null;
            }
        });
        return this;
    }

    public String openWithPost(URL url, String postParams) throws IOException {
        HttpURLConnection connection = httpClient.open(url);
        OutputStream out = null;
        InputStream in = null;
        try {
            connection.setRequestMethod("POST");
            out = connection.getOutputStream();
            out.write(postParams.getBytes());
            out.flush();
            out.close();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException("Unexpected HTTP response: "
                        + connection.getResponseCode() + " " + connection.getResponseMessage());
            }
            in = connection.getInputStream();
            return StringUtils.inputStreamToString(in);
        } finally {
            if (out != null) out.close();
            if (in != null) in.close();
        }
    }
}
