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
package pl.edu.ibe.loremipsum.support;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import pl.edu.ibe.loremipsum.db.schema.Credential;
import pl.edu.ibe.loremipsum.db.schema.ResearchersSuite;
import pl.edu.ibe.loremipsum.tablet.base.ServiceProvider;
import pl.edu.ibe.loremipsum.tools.BaseService;
import pl.edu.ibe.loremipsum.tools.FileUtils;
import pl.edu.ibe.loremipsum.tools.LogUtils;
import pl.edu.ibe.loremipsum.tools.RxExecutor;
import pl.edu.ibe.loremipsum.tools.StringUtils;
import pl.edu.ibe.loremipsum.tools.TimeUtils;
import pl.edu.ibe.loremipsum.tools.io.ReadStream;
import rx.Observable;

/**
 * Created by adam on 18.04.14.
 * Service for used to provide bug reports
 */
public class SupportService extends BaseService {

    public static final long MAX_FILE_AGE = 3 * 24 * 60 * 60 * 1000;
    private static String BUG_REPORT_PATH;
    private static String TAG = SupportService.class.getSimpleName();
    private String supportUrl;

    /**
     * Creates @link{SupportService} with proper dependencies
     *
     * @param serviceProvider
     */
    public SupportService(ServiceProvider serviceProvider) {
        super(serviceProvider);
        supportUrl = serviceProvider.getSupportUrl();

        File file = new File(context().getFilesDir() + File.separator + "logs" + File.separator + "bugReports");
        file.mkdirs();
        BUG_REPORT_PATH = file.getAbsolutePath();
    }

    public Observable<SupportResponse> reportBug(String email, String phoneNumber, String description, boolean attachLogs) {
        return RxExecutor.run(() -> {
            BugReport bugReport = new BugReport();
            bugReport.email = email;
            bugReport.phoneNumber = phoneNumber;
            bugReport.description = description;
            bugReport.appBuild = getAppBuild();
            bugReport.appVersion = getAppVersion();
            bugReport.deviceModel = getDeviceModelAndManufacturer();
            bugReport.androidVersion = Build.VERSION.SDK_INT;
            bugReport.credentials = new ArrayList<>();
            CredentialsPair<String, String> credentials;
            Credential credential;
            if (services.login().currentLoggedInUser != null) {
                for (ResearchersSuite researchersSuite : services.login().currentLoggedInUser.getResearchersSuiteList()) {
                    credential = researchersSuite.getCredential();
                    credentials = new CredentialsPair<>(credential.getUser(), credential.getManifestUrl());
                    bugReport.credentials.add(credentials);
                }
            }

            bugReport.logFiles = new HashMap<>();
            if (attachLogs) {
                List<File> list = FileUtils.getFilesYoungerThan(LogUtils.getLogsDirectory(), new Date(System.currentTimeMillis() - MAX_FILE_AGE))
                        .toBlockingObservable().last();

                
                    File zipFile = new File(context().getExternalCacheDir(), "support_" + new Date() + ".zip");
                    FileUtils.zipFile(list, zipFile);


                    try {
                        bugReport.logFiles.put(zipFile.getName(), ReadStream.readStream(new FileInputStream(zipFile))
                                .execute().toBlockingObservable().last().getBytes());
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            String bugReportString = getServiceProvider().getGson().toJson(bugReport);

            Log.d(TAG, bugReportString);

            StringUtils.stringToFile(new File(BUG_REPORT_PATH + File.separator + TimeUtils.dateToString(new Date(), TimeUtils.timePatern) + ".bugReport")
                    , bugReportString);

            URL url = new URL(supportUrl);
            String stringResponse = getServiceProvider().getNetworkSupport().openWithPost(url, bugReportString);
            return getServiceProvider().getGson().fromJson(stringResponse, SupportResponse.class);
        });
    }


    private String getDeviceModelAndManufacturer() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return model;
        } else {
            return manufacturer + " " + model;
        }
    }

    private PackageInfo getPackageInfo() throws PackageManager.NameNotFoundException {
        PackageManager manager = services.context().getPackageManager();
        return manager.getPackageInfo(services.context().getPackageName(), 0);
    }

    private String getAppVersion() throws PackageManager.NameNotFoundException {
        PackageInfo info = getPackageInfo();
        return info.versionName;
    }


    private String getAppBuild() throws PackageManager.NameNotFoundException {
        PackageInfo info = getPackageInfo();
        return info.versionCode + "";
    }


    public void setSupportUrl(String supportUrl) {
        this.supportUrl = supportUrl;
    }
}
