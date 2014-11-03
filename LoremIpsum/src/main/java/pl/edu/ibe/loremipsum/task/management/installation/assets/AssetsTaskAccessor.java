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

package pl.edu.ibe.loremipsum.task.management.installation.assets;

import android.content.res.AssetManager;

import java.io.File;

import pl.edu.ibe.loremipsum.task.management.installation.InstallableTaskAccessor;
import pl.edu.ibe.loremipsum.tools.ExecutionException;
import pl.edu.ibe.loremipsum.tools.RxExecutor;
import rx.Observable;

/**
 * @author Mariusz Pluciński
 */
public class AssetsTaskAccessor implements InstallableTaskAccessor {
    private final AssetManager assetManager;
    private final String identifier;

    public AssetsTaskAccessor(AssetManager assetManager, String identifier) {
        this.assetManager = assetManager;
        this.identifier = identifier;
    }

    @Override
    public Observable<AssetsTaskSuite> getSuite() {
        return RxExecutor.run(() -> {
            int idx = identifier.lastIndexOf("-");
            if (idx == -1)
                throw new InvalidIdentifierException(identifier);

            String name = identifier.substring(0, idx);
            String version = identifier.substring(idx + 1);

            idx = name.lastIndexOf(File.separator);
            if (idx != -1)
                name = name.substring(idx + 1);

            idx = version.lastIndexOf(".");
            if (idx != -1)
                version = version.substring(0, idx);

            long size = assetManager.openFd(identifier).getLength();
            return new AssetsTaskSuite(name, version, assetManager.open(identifier), (int) size);
        });
    }

    private class InvalidIdentifierException extends ExecutionException {
        public InvalidIdentifierException(String identifier) {
            super("Provided asset identifier name \"" + identifier
                    + "\" does not follow task suite naming scheme");
        }
    }
}
