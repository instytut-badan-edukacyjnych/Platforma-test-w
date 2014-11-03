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

package pl.edu.ibe.loremipsum.tablet.assets;

import java.util.List;

import pl.edu.ibe.loremipsum.tablet.base.ServiceProvider;
import pl.edu.ibe.loremipsum.tablet.demo.DemoAssetsTaskAccessor;
import pl.edu.ibe.loremipsum.task.management.installation.InstallableTaskAccessor;
import pl.edu.ibe.loremipsum.task.management.installation.assets.AssetsTaskAccessor;
import pl.edu.ibe.loremipsum.tools.AssetsUtils;
import pl.edu.ibe.loremipsum.tools.BaseService;
import pl.edu.ibe.loremipsum.tools.RxExecutor;
import rx.Observable;

/**
 * Created by adam on 29.07.14.
 */
public class AssetsTaskSuiteService extends BaseService {

    public static final String SUITES_DIR = "suites";
    public static final String SUITES_EXTENSION = ".zip";


    public List<String> getAppSuitesAssetNames() {
        return AssetsUtils.listFiles(context(), SUITES_DIR);
    }

    /**
     * Creates service with context
     *
     * @param services Services provider
     */
    public AssetsTaskSuiteService(ServiceProvider services) {
        super(services);
    }

    public Observable<InstallableTaskAccessor> getSuitesInstallationAccessor(String suiteZipPath) {
        return RxExecutor.run(() -> new AssetsSuiteTaskAccesor(context().getAssets(), suiteZipPath));
    }
}
