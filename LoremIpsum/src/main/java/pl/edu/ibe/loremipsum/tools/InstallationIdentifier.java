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

package pl.edu.ibe.loremipsum.tools;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.UUID;

/**
 * Created by adam on 20.03.14.
 * Provides unique identifier for app instalation.
 */
public class InstallationIdentifier {
    private static final String TAG = InstallationIdentifier.class.toString();

    private static final String INSTALLATION = "INSTALLATION";

    private static String deviceId = null;
    private static boolean firstRun = false;

    /**
     * Constructor
     *
     * @param context
     * @throws IOException
     */
    public InstallationIdentifier(Context context) throws IOException {
        if (deviceId == null) {
            File installation = new File(context.getFilesDir(), INSTALLATION);
            if (!installation.exists()) {
                LogUtils.v(TAG, "this is the first application run");
                firstRun = true;
                writeInstallationFile(installation);
            } else {
                LogUtils.v(TAG, "this is NOT the first application run");
                firstRun = false;
            }
            deviceId = readInstallationFile(installation);
            LogUtils.v(TAG, "device id= " + deviceId);
        }
    }

    /**
     * Constructor, that stores custom device id, instead one stored in application data.
     */
    protected InstallationIdentifier(String deviceId, boolean firstRun) {
        this.deviceId = deviceId;
        this.firstRun = firstRun;
    }

    /**
     * Reads application unique identifier
     *
     * @param installation
     * @return
     * @throws IOException
     */
    private static String readInstallationFile(File installation) throws IOException {
        RandomAccessFile f = new RandomAccessFile(installation, "r");
        byte[] bytes = new byte[(int) f.length()];
        f.readFully(bytes);
        f.close();
        return new String(bytes);
    }

    /**
     * @return deviceid
     */
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * @return true if application is running for the firsttime
     */
    public boolean isFirstRun() {
        return firstRun;
    }

    /**
     * Writes unique id to proper file
     *
     * @param installation
     * @throws IOException
     */
    private void writeInstallationFile(File installation) throws IOException {
        FileOutputStream out = new FileOutputStream(installation);
        String id = UUID.randomUUID().toString();
        out.write(id.getBytes());
        out.close();
    }
}
