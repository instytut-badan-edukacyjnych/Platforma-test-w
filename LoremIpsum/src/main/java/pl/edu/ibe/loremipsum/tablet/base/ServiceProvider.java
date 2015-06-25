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

package pl.edu.ibe.loremipsum.tablet.base;

import android.content.Context;

import com.google.gson.Gson;

import org.simpleframework.xml.core.Persister;

import java.io.File;
import java.io.IOException;
import java.util.List;

import pl.edu.ibe.loremipsum.configuration.SharedPreferencesService;
import pl.edu.ibe.loremipsum.resultfixer.data.ImportExportService;
import pl.edu.ibe.loremipsum.db.TestDataService;
import pl.edu.ibe.loremipsum.db.schema.TaskSuite;
import pl.edu.ibe.loremipsum.db.schema.TaskSuiteDao;
import pl.edu.ibe.loremipsum.network.NetworkSupport;
import pl.edu.ibe.loremipsum.support.SupportService;
import pl.edu.ibe.loremipsum.tablet.assets.AssetsTaskSuiteService;
import pl.edu.ibe.loremipsum.tablet.demo.DemoService;
import pl.edu.ibe.loremipsum.tablet.department.DepartmentService;
import pl.edu.ibe.loremipsum.tablet.examinee.ExamineeService;
import pl.edu.ibe.loremipsum.tablet.handler.AppHandlerService;
import pl.edu.ibe.loremipsum.tablet.institution.InstitutionService;
import pl.edu.ibe.loremipsum.tablet.login.LoginService;
import pl.edu.ibe.loremipsum.tablet.report.ResultsService;
import pl.edu.ibe.loremipsum.tablet.researcher.ResearcherService;
import pl.edu.ibe.loremipsum.tablet.task.TaskService;
import pl.edu.ibe.loremipsum.tablet.test.CurrentTaskSuiteService;
import pl.edu.ibe.loremipsum.task.management.DownloadTaskSuiteService;
import pl.edu.ibe.loremipsum.task.management.TaskSuitesService;
import pl.edu.ibe.loremipsum.task.management.collector.Collector;
import pl.edu.ibe.loremipsum.task.management.collector.NetworkChangeReceiver;
import pl.edu.ibe.loremipsum.task.management.installation.InstallableTaskAccessor;
import pl.edu.ibe.loremipsum.task.management.installation.InstallableTaskSuite;
import pl.edu.ibe.loremipsum.task.management.storage.TaskStorage;
import pl.edu.ibe.loremipsum.tools.DbAccess;
import pl.edu.ibe.loremipsum.tools.FileUtils;
import pl.edu.ibe.loremipsum.tools.InstallationIdentifier;
import pl.edu.ibe.loremipsum.tools.LogUtils;
import pl.edu.ibe.testplatform.BuildConfig;
import rx.Observable;
import rx.Subscriber;

/**
 * Provider class for services
 * Created by adam on 17.03.14.
 */
public class ServiceProvider {
    private static final String TAG = ServiceProvider.class.toString();
    private static ServiceProvider serviceProvider;

    private InstallationIdentifier installationIdentifier;
    private TaskStorage taskStorage;
    private LoginService loginService;
    private ResearcherService researcherService;
    private ExamineeService examineeService;
    private InstitutionService institutionService;
    private DepartmentService departmentService;
    private CurrentTaskSuiteService currentTaskSuiteService;
    private TaskSuitesService taskSuitesService;
    private AppHandlerService appHandlerService;
    private ResultsService resultsService;
    private ImportExportService importExportService;
    private SharedPreferencesService sharedPreferencesService;
    private TestDataService testDataService;
    private DownloadTaskSuiteService downloadTaskSuiteService;
    private SupportService supportService;
    private DemoService demoService;
    private TaskService taskService;
    private AssetsTaskSuiteService assetsTaskSuite;
    private Context context;
    private DbAccess dbAccess;
    private NetworkChangeReceiver.NetworkUtil networkUtil;
    private NetworkSupport networkSupport;
    private Gson gson;
    private Persister persister;
    private Collector collector;
    private String supportUrl;

    private StackTraceElement[] createStackTrace = new StackTraceElement[0];


    ServiceProvider(Context context, InstallationIdentifier installationIdentifier,
                    TaskStorage taskStorage, DbAccess dbAccess,
                    NetworkChangeReceiver.NetworkUtil networkUtil, NetworkSupport networkSupport,
                    Gson gson, Persister persister, String supportUrl) {
        this.context = context;
        this.installationIdentifier = installationIdentifier;
        this.taskStorage = taskStorage;
        this.dbAccess = dbAccess;
        this.networkUtil = networkUtil;
        this.networkSupport = networkSupport;
        this.gson = gson;
        this.persister = persister;
        this.supportUrl = supportUrl;

        serviceProvider = this;
    }

    static public ServiceProvider obtain() {
        if (serviceProvider == null)
            throw new NullPointerException("Tried to obtain service provider, but no service provider has been created");
        return serviceProvider;
    }

    private static void cacheCleanup(Context context) throws IOException {
        File dir = context.getCacheDir();
        File[] elems = dir.listFiles();
        if (elems != null)
            for (File elem : elems)
                FileUtils.deleteRecursive(elem);
    }

    public Observable<Boolean> prepareApplication() {
        return Observable.create((Subscriber<? super Boolean> subscriber) -> {
            LogUtils.d(TAG, "prepareApplication");
            boolean firstRun = installationIdentifier.isFirstRun();
            subscriber.onNext(firstRun);
            if (firstRun) {
                LogUtils.v(TAG, "First application start");
                for (String demoZipPath : demo().getAppDemoAssetNames()) {
                    String parsedString = demoZipPath.substring(0, demoZipPath.length() - DemoService.DEMO_EXTENSION.length());
                    String[] demoData = parsedString.split("-");
                    TaskSuite taskSuite = new TaskSuite();
                    taskSuite.setName(demoData[0]);
                    taskSuite.setVersion(demoData[1]);
                    taskSuite.setPilot(false);
                    taskSuite.setLatestVersionSeen(null);
                    taskSuite.setDownloaded(true);
                    taskSuite.setDemo(true);
                    dbAccess.getDaoSession().getTaskSuiteDao().insert(taskSuite);

                    InstallableTaskAccessor accessor = demo().getDemoInstallationAccessor(demoZipPath).toBlockingObservable().single();
                    InstallableTaskSuite suite = accessor.getSuite().toBlockingObservable().single();
                    suite.installTo(getTaskStorage()).second.toBlockingObservable().last();
                }
            }
            if (BuildConfig.AUTO_LOAD_TASK_SUITE_FROM_ASSETS) {
                for (String suiteZipPath : assetsTaskSuite().getAppSuitesAssetNames()) {
                    String parsedString = suiteZipPath.substring(0, suiteZipPath.length() - AssetsTaskSuiteService.SUITES_EXTENSION.length());
                    String[] suiteData = parsedString.split("-");

                    List<TaskSuite> list = dbAccess.getDaoSession().getTaskSuiteDao().queryBuilder().where(TaskSuiteDao.Properties.Name.eq(suiteData[0])).list();
                    TaskSuite taskSuite = new TaskSuite();
                    if (list.size() > 0) {
                        taskSuite = list.get(0);


                        File file = new File(context().getFilesDir(), "suites/" + suiteData[0]);
                        if (file.exists() && file.isDirectory()) {
                            try {
                                FileUtils.deleteRecursive(file);
                                LogUtils.i(ServiceProvider.class.getSimpleName(), "File deleted: " + suiteData[0]);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        taskSuite.setVersion(suiteData[1]);
                        dbAccess.getDaoSession().getTaskSuiteDao().update(taskSuite);
                    } else {
                        taskSuite.setName(suiteData[0]);
                        taskSuite.setVersion(suiteData[1]);
                        taskSuite.setPilot(false);
                        taskSuite.setLatestVersionSeen(null);
                        taskSuite.setDownloaded(true);
                        taskSuite.setDemo(false);
                        dbAccess.getDaoSession().getTaskSuiteDao().insert(taskSuite);
                    }

                    InstallableTaskAccessor accessor = assetsTaskSuite().getSuitesInstallationAccessor(suiteZipPath).toBlockingObservable().single();
                    InstallableTaskSuite suite = accessor.getSuite().toBlockingObservable().single();
                    suite.installTo(getTaskStorage()).second.toBlockingObservable().last();
                }
            }
            LogUtils.d(TAG, "prepareApplication - done");
            subscriber.onCompleted();
        });
    }

    public void dispose() {
        LogUtils.i(TAG, "Service provider is disposed");
        serviceProvider = null;
    }


    public NetworkChangeReceiver.NetworkUtil getNetworkUtil() {
        return networkUtil;
    }

    public DbAccess getDbAccess() {
        return dbAccess;
    }


    public TaskStorage getTaskStorage() {
        return taskStorage;
    }

    /**
     * returns @link{loginService}. Note that service should be singleton.
     *
     * @return @link{loginService}
     */
    public synchronized LoginService login() {
        if (loginService == null)
            loginService = new LoginService(this);
        return loginService;
    }

    /**
     * returns @link{researcherService}.Note that service should be singleton.
     *
     * @return @link{researcherService}
     */
    public ResearcherService researcher() {
        if (researcherService == null)
            researcherService = new ResearcherService(this);
        return researcherService;
    }

    /**
     * returns @link{examineeService}. Note that service should be singleton.
     *
     * @return @link{examineeService}
     */
    public ExamineeService examinee() {
        if (examineeService == null)
            examineeService = new ExamineeService(this);
        return examineeService;
    }

    /**
     * returns @link{InstitutionService}.  Note that service should be singleton.
     *
     * @return @link{InstitutionService}
     */
    public InstitutionService institution() {
        if (institutionService == null)
            institutionService = new InstitutionService(this);
        return institutionService;
    }

    /**
     * Returns @link{DepartmentService}. Note that service should be singleton.
     *
     * @return @link{DepartmentService}
     */
    public DepartmentService department() {
        if (departmentService == null)
            departmentService = new DepartmentService(this);
        return departmentService;
    }

    /**
     * Returns @link{TaskSuitesService}. Note that service is singleton.
     *
     * @return @link{TaskSuitesService}
     */
    public TaskSuitesService taskSuites() {
        if (taskSuitesService == null)
            taskSuitesService = new TaskSuitesService(this);
        return taskSuitesService;
    }

    /**
     * Returns @link{CurrentTaskSuiteService}. Note that service should be singleton.
     *
     * @return @link{CurrentTaskSuiteService}
     */
    public CurrentTaskSuiteService currentTaskSuite() {
        if (currentTaskSuiteService == null)
            currentTaskSuiteService = new CurrentTaskSuiteService(this);
        return currentTaskSuiteService;
    }

    public Collector collector() {
        if (collector == null)
            collector = new Collector(this);
        return collector;
    }

    /**
     * Returns @link{AppHandlerService}. Note that service should be singleton.
     * <p>
     * Provides only backward compatibility better don't use it!
     *
     * @return @link{AppHandlerService}
     */
    @Deprecated
    public AppHandlerService appHandler() {
        if (appHandlerService == null) {
            appHandlerService = new AppHandlerService(this);
        }
        return appHandlerService;
    }

    public ResultsService results() {
        if (resultsService == null)
            resultsService = new ResultsService(this, gson);
        return resultsService;
    }

    /**
     * Returns @link{ImportExportService}  Note that service should be singleton.
     *
     * @return @link{ImportExportService}
     */
    public ImportExportService importExport() {
        if (importExportService == null)
            importExportService = new ImportExportService(this, persister);
        return importExportService;
    }

    /**
     * Returns @link{SharedPreferencesService}  Note that service should be singleton.
     *
     * @return @link{SharedPreferencesService}
     */
    public SharedPreferencesService sharedPreferences() {
        if (sharedPreferencesService == null)
            sharedPreferencesService = new SharedPreferencesService(this);
        return sharedPreferencesService;
    }

    /**
     * Returns @link{SupportService}  Note that service should be singleton.
     *
     * @return @link{SupportService}
     */
    public SupportService support() {
        if (supportService == null) {
            supportService = new SupportService(this);
        }
        return supportService;
    }


    public DemoService demo() {
        if (demoService == null) {
            demoService = new DemoService(this);
        }
        return demoService;
    }

    public AssetsTaskSuiteService assetsTaskSuite() {
        if (assetsTaskSuite == null) {
            assetsTaskSuite = new AssetsTaskSuiteService(this);
        }
        return assetsTaskSuite;
    }

    public TaskService task() {
        if (taskService == null) {
            taskService = new TaskService(this);
        }
        return taskService;
    }

    public TestDataService testData() {
        if (testDataService == null) {
            testDataService = new TestDataService(this);
        }
        return testDataService;
    }

    public DownloadTaskSuiteService downloadTaskSuiteService() {
        if (downloadTaskSuiteService == null) {
            downloadTaskSuiteService = new DownloadTaskSuiteService(this);
        }
        return downloadTaskSuiteService;
    }


    public Persister getXMLPersister() {
        return persister;
    }

    public Context context() {
        return context;
    }

    public String getDeviceId() {
        return installationIdentifier.getDeviceId();
    }

    public NetworkSupport getNetworkSupport() {
        return networkSupport;
    }

    public Gson getGson() {
        return gson;
    }

    public String getSupportUrl() {
        return supportUrl;
    }
}
