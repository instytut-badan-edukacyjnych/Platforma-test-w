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

import android.util.Log;

import com.google.gson.Gson;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import pl.edu.ibe.loremipsum.tools.LogUtils;

/**
 * @author Mariusz Pluciński
 */
public class Requests {
    public static class ManifestRequest extends LoremIpsumRequest<ManifestResponse> {
        public ManifestRequest(URL url, Auth auth, NetworkSupport networkSupport) {
            super(url, ManifestResponse::new, networkSupport.addAuth(auth));
        }
    }

    public static class ManifestResponse extends LoremIpsumResponse {
        private final Response response;

        public ManifestResponse(URL url, Map<String, List<String>> headers, InputStream stream) {
            super(url, headers, stream);
            response = new Gson().fromJson(new InputStreamReader(stream), Response.class);
            response.setManifestUrl(url);
        }

        public List<ResponseSuite> getSuites() {
            return response.suites;
        }

        public static class ResponseSuite {
            String name;
            String version;
            String url;
            private URL manifestUrl;

            public String getName() {
                return name;
            }

            public String getVersion() {
                return version;
            }

            public URL getDownloadUrl() throws MalformedURLException {
                try {
                    String url = this.url;
                    if (url.startsWith("/")) {
                        if (manifestUrl.toString().endsWith("/"))
                            url = url.substring(1);
                        url = manifestUrl + url;
                    }
                    return new URL(url);
                } catch (MalformedURLException e) {
                    LogUtils.e("Requests", "Invalid url: " + url, e);
                    throw e;
                }
            }

            public void setManifestUrl(URL url) {
                this.manifestUrl = url;
            }
        }

        private static class Response {
            public List<ResponseSuite> suites;
            public ManifestResponse enclosing;

            public void setManifestUrl(URL url) {
                for (ResponseSuite suite : suites)
                    suite.setManifestUrl(url);
            }
        }
    }

    public static class SuiteDownloadRequest extends LoremIpsumRequest<SuiteDownloadResponse> {
        public SuiteDownloadRequest(URL url, Auth auth, String deviceId, NetworkSupport networkSupport) {
            super(getUrl(url, deviceId), SuiteDownloadResponse::new, networkSupport.addAuth(auth));
        }

        private static URL getUrl(URL url, String deviceId) {
            try {
                return new URL(url.toString() + "?device_id=" + deviceId);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }

    public static class SuiteDownloadResponse extends LoremIpsumResponse {
        public SuiteDownloadResponse(URL url, Map<String, List<String>> headers, InputStream stream) {
            super(url, headers, stream);
        }
    }
}

