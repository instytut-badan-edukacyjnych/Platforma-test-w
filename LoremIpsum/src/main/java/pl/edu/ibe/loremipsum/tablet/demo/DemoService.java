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
package pl.edu.ibe.loremipsum.tablet.demo;

import android.content.Context;
import android.content.Intent;

import java.util.List;

import pl.edu.ibe.loremipsum.tablet.base.ServiceProvider;
import pl.edu.ibe.loremipsum.tablet.report.ReportActivity;
import pl.edu.ibe.loremipsum.task.management.TaskSuite;
import pl.edu.ibe.loremipsum.task.management.installation.InstallableTaskAccessor;
import pl.edu.ibe.loremipsum.tools.AssetsUtils;
import pl.edu.ibe.loremipsum.tools.BaseService;
import pl.edu.ibe.loremipsum.tools.RxExecutor;
import rx.Observable;

/**
 * Created by adam on 22.04.14.
 */
public class DemoService extends BaseService {
    public static final String DEMO_DIR = "demo";
    public static final String DEMO_EXTENSION = ".zip";

    /**
     * Creates service with context
     *
     * @param services Services provider
     */
    public DemoService(ServiceProvider services) {
        super(services);
    }

    public List<String> getAppDemoAssetNames() {
        return AssetsUtils.listFiles(context(), DEMO_DIR);
    }

    public Observable<TaskSuite> listDemoTaskSuite() {
        return getServiceProvider().taskSuites().listDemoSuites();
    }

    public void showDemoReport(Context context) {
        Intent intent = new Intent();
        intent.setClass(context, ReportActivity.class);
        intent.putExtra(ReportActivity.EXTRA_DISABLE_PROVIDER, true);
        context.startActivity(intent);
    }

    public Observable<InstallableTaskAccessor> getDemoInstallationAccessor(String demoZipPath) {
        return RxExecutor.run(() -> new DemoAssetsTaskAccessor(context().getAssets(), demoZipPath));
    }
}
