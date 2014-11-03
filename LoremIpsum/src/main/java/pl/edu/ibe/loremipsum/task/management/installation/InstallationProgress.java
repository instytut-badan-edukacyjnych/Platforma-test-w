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

package pl.edu.ibe.loremipsum.task.management.installation;

import pl.edu.ibe.loremipsum.tools.io.CopyStream;

/**
 * @author Mariusz Pluciński
 */
public class InstallationProgress {
    private final String suiteName;
    private final String suiteVersion;
    private final Integer installedBytes;
    private final Integer totalBytes;
    private final Integer time;

    protected InstallationProgress(String suiteName, String suiteVersion, Integer installedBytes,
                                   Integer totalBytes, Integer time) {
        this.suiteName = suiteName;
        this.suiteVersion = suiteVersion;
        this.installedBytes = installedBytes;
        this.totalBytes = totalBytes;
        this.time = time;
    }

    public static InstallationProgress start(String suiteName, String suiteVersion, Integer size) {
        return new InstallationProgress(suiteName, suiteVersion, 0, size, 0);
    }

    public static InstallationProgress progress(String suiteName, String suiteVersion,
                                                Integer size, CopyStream.CopyProgress copyProgress) {
        return new InstallationProgress(suiteName, suiteVersion, copyProgress.getCount(), size, copyProgress.getTime());
    }

    public static InstallationProgress done(String suiteName, String suiteVersion,
                                            Integer size, CopyStream.CopyProgress done) {
        return new InstallationProgress(suiteName, suiteVersion, done.getCount(), size, done.getTime());
    }

    public static InstallationProgress done(String suiteName, String suiteVersion) {
        return new InstallationProgress(suiteName, suiteVersion, null, null, null);
    }

    public Integer getInstalledBytes() {
        return installedBytes;
    }

    public Integer getTotalBytes() {
        return totalBytes;
    }

    public Integer getTime() {
        return time;
    }

    public String getSuiteName() {
        return suiteName;
    }

    public String getSuiteVersion() {
        return suiteVersion;
    }
}
