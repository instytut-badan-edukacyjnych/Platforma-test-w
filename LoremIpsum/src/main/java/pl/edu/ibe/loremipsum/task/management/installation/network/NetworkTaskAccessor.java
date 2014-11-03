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

package pl.edu.ibe.loremipsum.task.management.installation.network;

import java.net.URL;

import pl.edu.ibe.loremipsum.network.Auth;
import pl.edu.ibe.loremipsum.network.NetworkSupport;
import pl.edu.ibe.loremipsum.network.Requests;
import pl.edu.ibe.loremipsum.task.management.installation.InstallableTaskAccessor;
import rx.Observable;

/**
 * @author Mariusz Pluciński
 */
public class NetworkTaskAccessor implements InstallableTaskAccessor {
    private final URL url;
    private final Auth auth;
    private String deviceId;
    private NetworkSupport networkSupport;

    public NetworkTaskAccessor(URL url, Auth auth, String deviceId, NetworkSupport networkSupport) {
        this.url = url;
        this.auth = auth;
        this.deviceId = deviceId;
        this.networkSupport = networkSupport;
    }

    @Override
    public Observable<NetworkTaskSuite> getSuite() {
        return new Requests.ManifestRequest(url, auth, networkSupport)
                .prepare()
                .map(manifestResponse -> NetworkTaskSuite.fromManifestResponse(this, manifestResponse));
    }

    public URL getUrl() {
        return url;
    }

    public Auth getAuth() {
        return auth;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public NetworkSupport getNetworkSupport() {
        return networkSupport;
    }
}

