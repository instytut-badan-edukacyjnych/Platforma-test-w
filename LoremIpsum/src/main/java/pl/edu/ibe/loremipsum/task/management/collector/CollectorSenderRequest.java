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

package pl.edu.ibe.loremipsum.task.management.collector;

import com.squareup.mimecraft.Multipart;
import com.squareup.mimecraft.Part;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import pl.edu.ibe.loremipsum.network.LoremIpsumRequest;
import pl.edu.ibe.loremipsum.network.LoremIpsumResponse;
import pl.edu.ibe.loremipsum.network.NetworkSupport;

/**
 * Created by mikolaj on 10.04.14.
 */
public class CollectorSenderRequest extends LoremIpsumRequest<CollectorSenderRequest.CollectorSenderResponse> {

    private final String fileName;
    private final InputStream stream;

    public CollectorSenderRequest(URL url, String fileName, InputStream stream, NetworkSupport networkSupport) {
        super(makeUrl(url), CollectorSenderRequest.CollectorSenderResponse::new, networkSupport);
        this.fileName = new File(fileName).getName();
        this.stream = stream;
    }

    private static URL makeUrl(URL url) {
        try {
            return new URL(url, "submit");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void fancyNameForAdditionalHandlingConnection(HttpURLConnection connection) throws IOException {
        super.fancyNameForAdditionalHandlingConnection(connection);
        connection.setRequestMethod("POST");
        Multipart multipart = new Multipart.Builder()
                .type(Multipart.Type.FORM)
                .addPart(new Part.Builder()
                        .contentType("application/zip")
                        .contentDisposition("form-data; name=\"file\"; filename=\"" + fileName + "\"")
                        .body(stream)
                        .build())
                .build();
        for (Map.Entry<String, String> entry : multipart.getHeaders().entrySet()) {
            connection.setRequestProperty(entry.getKey(), entry.getValue());
        }
        BufferedInputStream bis = new BufferedInputStream(stream);
        OutputStream os = connection.getOutputStream();
        multipart.writeBodyTo(os);
        bis.close();
        os.flush();
        os.close();
    }

    public static class CollectorSenderResponse extends LoremIpsumResponse {

        public CollectorSenderResponse(URL url, Map<String, List<String>> headers, InputStream stream) {
            super(url, headers, stream);
        }
    }
}
