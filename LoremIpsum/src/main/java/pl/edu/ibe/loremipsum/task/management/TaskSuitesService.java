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

package pl.edu.ibe.loremipsum.task.management;

import android.content.res.AssetManager;
import android.util.Pair;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.Callable;

import pl.edu.ibe.loremipsum.configuration.TaskSuiteConfig;
import pl.edu.ibe.loremipsum.db.schema.Credential;
import pl.edu.ibe.loremipsum.db.schema.Researcher;
import pl.edu.ibe.loremipsum.db.schema.ResearchersSuite;
import pl.edu.ibe.loremipsum.db.schema.ResearchersSuiteDao;
import pl.edu.ibe.loremipsum.db.schema.Result;
import pl.edu.ibe.loremipsum.db.schema.ResultDao;
import pl.edu.ibe.loremipsum.db.schema.TaskSuiteDao;
import pl.edu.ibe.loremipsum.network.Auth;
import pl.edu.ibe.loremipsum.network.HttpBasicAuth;
import pl.edu.ibe.loremipsum.tablet.LoremIpsumApp;
import pl.edu.ibe.loremipsum.tablet.base.ServiceProvider;
import pl.edu.ibe.loremipsum.task.management.installation.InstallableTaskSuite;
import pl.edu.ibe.loremipsum.task.management.installation.InstallationProgress;
import pl.edu.ibe.loremipsum.task.management.installation.assets.AssetsTaskAccessor;
import pl.edu.ibe.loremipsum.task.management.installation.network.NetworkTaskAccessor;
import pl.edu.ibe.loremipsum.task.management.storage.TaskStorage;
import pl.edu.ibe.loremipsum.task.management.storage.TaskSuiteVersion;
import pl.edu.ibe.loremipsum.tools.BaseService;
import pl.edu.ibe.loremipsum.tools.CancelException;
import pl.edu.ibe.loremipsum.tools.ExecutionException;
import pl.edu.ibe.loremipsum.tools.LogUtils;
import pl.edu.ibe.loremipsum.tools.RxExecutor;
import pl.edu.ibe.loremipsum.tools.Tuple;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;
import rx.subjects.Subject;

/**
 * @author Mariusz Pluciński
 */
public class TaskSuitesService extends BaseService {
    private static final String TAG = TaskSuitesService.class.toString();
    private String userName;
    private String url;

    /**
     * Creates service with context
     *
     * @param services Service provider
     */
    public TaskSuitesService(ServiceProvider services) {
        super(services);
    }

    public void storeDownloadData(String userName, String url) {
        this.userName = userName;
        this.url = url;
    }

    public void clearDownloadData() {
        userName = null;
        url = null;
    }

    public Pair<String, String> getDownloadData() {
        if (userName == null || url == null) {
            return new Pair<>("", "");
        }
        return new Pair<>(userName, url);
    }

    public TaskStorage getTaskStorage() {
        return getServiceProvider().getTaskStorage();
    }

    public Observable<ResearchersSuite> listResearchersSuitesForResearcher(Researcher researcher) {
        return Observable.create((Subscriber<? super ResearchersSuite> subscriber) -> {
            try {
                for (ResearchersSuite suite : dbAccess().getDaoSession().getResearchersSuiteDao()
                        .queryBuilder().where(ResearchersSuiteDao.Properties.Researcher_fk.eq(researcher.getId()))
                        .list())
                    subscriber.onNext(suite);
                subscriber.onCompleted();
            } catch (Throwable t) {
                subscriber.onError(t);
            }
        });
    }

    public Observable<TaskSuite> listSuitesForResearcher(Researcher researcher) {
        return listResearchersSuitesForResearcher(researcher)
                .map(suite -> new TaskSuite(dbAccess(), getTaskStorage(), suite.getTaskSuite()));
    }

    public Observable<TaskSuite> listSuites() {
        return Observable.from(dbAccess().getDaoSession().getTaskSuiteDao().loadAll())
                .map(taskSuite -> new TaskSuite(dbAccess(), getTaskStorage(), taskSuite));
    }

    public Observable<TaskSuite> listDemoSuites() {
        return Observable.from(dbAccess().getDaoSession().getTaskSuiteDao().queryBuilder()
                .where(TaskSuiteDao.Properties.Demo.eq(true)).list())
                .map(taskSuite -> new TaskSuite(dbAccess(), getTaskStorage(), taskSuite));
    }

    private Callable<ResearchersSuite> insert(Researcher researcher, Credential credential,
                                              pl.edu.ibe.loremipsum.db.schema.TaskSuite taskSuite) {
        return () -> {
            ResearchersSuite researchersSuite = new ResearchersSuite();
            researchersSuite.setResearcher(researcher);
            researchersSuite.setCredential(credential);
            researchersSuite.setTaskSuite(taskSuite);
            dbAccess().getDaoSession().getResearchersSuiteDao().insert(researchersSuite);
            return researchersSuite;
        };
    }

    private Callable<ResearchersSuite> insert(Researcher researcher, Credential credential,
                                              InstallableTaskSuite installableTaskSuite) {
        return () -> {
            pl.edu.ibe.loremipsum.db.schema.TaskSuite suite = null;
            List<pl.edu.ibe.loremipsum.db.schema.TaskSuite> suitesDbEntries
                    = dbAccess().getDaoSession().getTaskSuiteDao().queryBuilder()
                    .where(TaskSuiteDao.Properties.Name.eq(installableTaskSuite.getName()),
                            TaskSuiteDao.Properties.Version.eq(installableTaskSuite.getVersion())).list();
            if (suitesDbEntries.size() == 0) {
                LogUtils.v(TAG, "inserting new task suite: " + installableTaskSuite.getName() + ":"
                        + installableTaskSuite.getVersion());
                suite = new pl.edu.ibe.loremipsum.db.schema.TaskSuite();
                suite.setName(installableTaskSuite.getName());
                suite.setVersion(installableTaskSuite.getVersion());
                suite.setPilot(null);
                suite.setLatestVersionSeen(installableTaskSuite.getVersion());
                suite.setDownloaded(false);
                dbAccess().getDaoSession().getTaskSuiteDao().insert(suite);
            } else if (suitesDbEntries.size() == 1) {
                suite = suitesDbEntries.get(0);
                LogUtils.v(TAG, "using existing db entry for task suite " + suite.getName()
                        + ":" + suite.getVersion());
            } else
                throw new IllegalStateException("More than one suites in database matches " +
                        installableTaskSuite.getName() + ":" + installableTaskSuite.getVersion());
            return insert(researcher, credential, suite).call();
        };
    }

    private Callable<ResearchersSuite> insert(Researcher researcher, Auth auth, URL manifestUrl, InstallableTaskSuite installableTaskSuite) {
        return () -> {
            Credential credential = null;
            if (auth != null) {
                credential = auth.createCredentialEntry();
                credential.setManifestUrl(manifestUrl.toExternalForm());
                dbAccess().getDaoSession().getCredentialDao().insert(credential);
            }

            return insert(researcher, credential, installableTaskSuite).call();
        };
    }

    private Callable<Object> remove(ResearchersSuite oldTaskSuite, ResearchersSuite newResearchersSuite) {
        return () -> {
//Reattach results to new ResearcherSuite
            LogUtils.v(TAG, "Reattaching results " + oldTaskSuite.getTaskSuite().getName() + ":" + oldTaskSuite.getTaskSuite().getVersion() + " from researcher "
                    + oldTaskSuite.getResearcher().getTextId() + " to: " + newResearchersSuite.getTaskSuite().getName() + ":" + newResearchersSuite.getTaskSuite().getVersion());
            List<Result> oldResults = dbAccess().getDaoSession().getResultDao().queryBuilder().where(ResultDao.Properties.Suite_fk.eq(oldTaskSuite.getId())).list();
            LogUtils.v(TAG, "reataching " + oldResults.size() + " results from: " + oldTaskSuite.getId() + " to: " + newResearchersSuite.getId());
            for (Result oldResult : oldResults) {
                LogUtils.v(TAG, "reataching result: " + oldResult.getId() + " from researchers suite id: " + oldTaskSuite.getId() + " to: " + newResearchersSuite.getId());
                oldResult.setResearchersSuite(newResearchersSuite);
                dbAccess().getDaoSession().getResultDao().update(oldResult);
            }
/// delete
            String name = oldTaskSuite.getTaskSuite().getName();
            String version = oldTaskSuite.getTaskSuite().getVersion();
            LogUtils.v(TAG, "Removing suite " + name + ":" + version + " from researcher "
                    + oldTaskSuite.getResearcher().getTextId());
            dbAccess().getDaoSession().getResearchersSuiteDao().delete(oldTaskSuite);

            try {
                //this may fail if other researchers suites have this suite. This is normal situation
                dbAccess().getDaoSession().getTaskSuiteDao().delete(oldTaskSuite.getTaskSuite());
                //but, if it has succeeded, we now need to delete suite from storage
                getTaskStorage().getSuite(name).getVersion(version).uninstall();
            } catch (Throwable t) {
                t.printStackTrace();
            }

            try {
                //this may fail if other researchers suites have this credential. This is normal situation
                dbAccess().getDaoSession().getCredentialDao().delete(oldTaskSuite.getCredential());
            } catch (Throwable t) {
                t.printStackTrace();
            }

            return RxExecutor.EMPTY_OBJECT;
        };
    }

    private Callable<Object> remove(ResearchersSuite researchersSuite) {
        return () -> {
            String name = researchersSuite.getTaskSuite().getName();
            String version = researchersSuite.getTaskSuite().getVersion();
            LogUtils.v(TAG, "Removing suite " + name + ":" + version + " from researcher "
                    + researchersSuite.getResearcher().getTextId());
            dbAccess().getDaoSession().getResearchersSuiteDao().delete(researchersSuite);

            try {
                //this may fail if other researchers suites have this suite. This is normal situation
                dbAccess().getDaoSession().getTaskSuiteDao().delete(researchersSuite.getTaskSuite());
                //but, if it has succeeded, we now need to delete suite from storage
                getTaskStorage().getSuite(name).getVersion(version).uninstall();
            } catch (Throwable t) {
                t.printStackTrace();
            }

            try {
                //this may fail if other researchers suites have this credential. This is normal situation
                dbAccess().getDaoSession().getCredentialDao().delete(researchersSuite.getCredential());
            } catch (Throwable t) {
                t.printStackTrace();
            }

            return RxExecutor.EMPTY_OBJECT;
        };
    }

    private Callable<pl.edu.ibe.loremipsum.db.schema.TaskSuite>
    updateDownloaded(pl.edu.ibe.loremipsum.db.schema.TaskSuite suite) {
        return () -> {
            LogUtils.i(TAG, "Setting suite " + suite.getName() + ":" + suite.getVersion() + " to downloaded=true");

            TaskSuiteVersion storageSuite = getTaskStorage().getSuite(suite.getName()).getVersion(suite.getVersion());
            try {
                storageSuite.getRoot().getChildFile(LoremIpsumApp.APP_DEMO_DIRECTORY);
                suite.setDemo(true);
            } catch (FileNotFoundException e) {
                suite.setDemo(false);
            }

            suite.setPilot(TaskSuiteConfig.checkPilot(
                    storageSuite.getRoot().getChildFile(TaskSuiteConfig.APP_CONFIG_XML_FILENAME)
            ));

            suite.setDownloaded(true);
            dbAccess().getDaoSession().getTaskSuiteDao().update(suite);

            return suite;
        };
    }

    private void installData(InstallableTaskSuite installableTaskSuite,
                             ResearchersSuite researchersSuite,
                             Subscriber<? super TaskSuiteVersion> subscriber,
                             Subject<InstallationProgress, InstallationProgress> progressSubject) {
        if (!researchersSuite.getTaskSuite().getDownloaded()) {
            Pair<Subject<InstallationProgress, InstallationProgress>, Observable<TaskSuiteVersion>> install = installableTaskSuite.installTo(getTaskStorage());
//            Observable<TaskSuiteVersion> o2 = install.second
//                    .doOnError(throwable -> {
//                            dbAccess().tx(TaskSuitesService.this.remove(researchersSuite)).toBlockingObservable().last();
//                            LogUtils.w(TAG, "Suite installation failed", throwable);
//                            throw new Exceptions.SuiteInstallationFailed(throwable);
//                    })
//                    .onErrorFlatMap(throwable -> dbAccess().tx(TaskSuitesService.this.remove(researchersSuite))
//                            .flatMap(ignore -> {
//                                LogUtils.w(TAG, "Suite installation failed", throwable);
//                                throw new Exceptions.SuiteInstallationFailed(throwable);
//                            }))
//                    .flatMap(suiteVersion -> dbAccess().tx(updateDownloaded(researchersSuite.getTaskSuite()))
//                                    .map(ignore -> suiteVersion)
//                    );
            install.first.subscribe(progressSubject);
            install.second.doOnError(throwable -> {
                LogUtils.w(TAG, "Suite installation failed", throwable);
                dbAccess().tx(TaskSuitesService.this.remove(researchersSuite))
                        .subscribeOn(Schedulers.newThread()).subscribe(
                        ignore -> {
                        },
                        (t1) -> {
                            subscriber.onError(t1);
                        },
                        () -> {
                            LogUtils.v(TAG, "Propagating deeper", throwable);
                            subscriber.onError(throwable);
                        }
                );
            }).doOnNext(taskSuiteVersion -> {
                dbAccess().tx(updateDownloaded(researchersSuite.getTaskSuite())).toBlockingObservable().last();
            }).subscribe(subscriber);
//            install.second.subscribe(new Subscriber<TaskSuiteVersion>() {
//                @Override
//                public void onCompleted() {
//                    subscriber.onCompleted();
//                }
//
//                @Override
//                public void onError(Throwable throwable) {
//                    dbAccess().tx(TaskSuitesService.this.remove(researchersSuite)).toBlockingObservable().last();
//                    LogUtils.w(TAG, "Suite installation failed", throwable);
//                    subscriber.onError(throwable);
//                }
//
//                @Override
//                public void onNext(TaskSuiteVersion taskSuiteVersion) {
//                    dbAccess().tx(updateDownloaded(researchersSuite.getTaskSuite())).toBlockingObservable().last();
//                    subscriber.onNext(taskSuiteVersion);
//                }
//            });
        } else {
            RxExecutor.run(
                    () -> getTaskStorage().getSuite(installableTaskSuite.getName())
                            .getVersion(installableTaskSuite.getVersion())
            ).subscribe(subscriber);
        }
    }

    public Pair<Subject<InstallationProgress, InstallationProgress>, Observable<TaskSuiteVersion>>
    installSuite(Researcher researcher, URL manifestUrl, Auth auth) {
        NetworkTaskAccessor accessor = new NetworkTaskAccessor(manifestUrl, auth,
                getServiceProvider().getDeviceId(), getServiceProvider().getNetworkSupport());
        Subject<InstallationProgress, InstallationProgress> s = BehaviorSubject.create((InstallationProgress) null);
        s.subscribeOn(Schedulers.io());
        Observable<TaskSuiteVersion> o = accessor.getSuite()
                .flatMap(networkTaskSuite -> dbAccess().tx(insert(researcher, auth, manifestUrl, networkTaskSuite))
                        .flatMap(researchersSuite ->
                                Observable.create((Subscriber<? super TaskSuiteVersion> subscriber) -> {
                                    try {
                                        if (subscriber.isUnsubscribed())
                                            throw new CancelException();
                                        LogUtils.i(TAG, "Information about researcher's suite (" + researcher.getTextId() + ":*****) inserted (" + networkTaskSuite.getName() + ":" + networkTaskSuite.getVersion() + "), downloaded = " + Boolean.toString(researchersSuite.getTaskSuite().getDownloaded()));
                                        installData(networkTaskSuite, researchersSuite, subscriber, s);
                                    } catch (Throwable t) {
                                        t.printStackTrace();
                                        subscriber.onError(t);
                                    }
                                })));
        return Pair.create(s, o);
    }

    public Observable<TaskSuite> getTaskSuite(String suiteName, String suiteVersion) {
        return Observable.create((Subscriber<? super TaskSuite> subscriber) -> {
            try {
                subscriber.onNext(new TaskSuite(dbAccess(), getTaskStorage(), suiteName, suiteVersion));
                subscriber.onCompleted();
            } catch (Throwable t) {
                t.printStackTrace();
                subscriber.onError(t);
            }
        });
    }

    public Tuple.Two<Observable<TaskSuiteUpdate>, Observable<String>> checkUpdates(Researcher researcher) {
        Subject<String, String> s = BehaviorSubject.create((String) null);
        Observable<TaskSuiteUpdate> o = Observable.<ResearchersSuite>create((Subscriber<? super ResearchersSuite> subscriber) -> {
            try {
                for (ResearchersSuite suite : dbAccess().getDaoSession().getResearchersSuiteDao()
                        .queryBuilder().where(
                                ResearchersSuiteDao.Properties.Researcher_fk.eq(researcher.getId())
                        ).list()) {
                    LogUtils.v(TAG, "Checking updates for suite " + suite.getTaskSuite().getName() + ":" + suite.getTaskSuite().getVersion());
                    s.onNext(suite.getTaskSuite().getName());
                    subscriber.onNext(suite);
                }
                subscriber.onCompleted();
            } catch (Throwable t) {
                subscriber.onError(t);
            }
        }).flatMap(researchersSuite -> {
            try {
                return new NetworkTaskAccessor(
                        new URL(researchersSuite.getCredential().getManifestUrl()),
                        new HttpBasicAuth(
                                researchersSuite.getCredential().getUser(),
                                researchersSuite.getCredential().getPassword()
                        ),
                        getServiceProvider().getDeviceId(),
                        getServiceProvider().getNetworkSupport()
                ).getSuite()
                        .map(suite -> Tuple.Two.create(researchersSuite, suite));
            } catch (Throwable t) {
                throw ExecutionException.wrap(t);
            }
        }).map(suite -> {
            LogUtils.v(TAG, "Verifying updates for suite " + suite.first.getTaskSuite().getName() + ":"
                    + suite.first.getTaskSuite().getVersion());
            LogUtils.v(TAG, "Server provides " + suite.second.getName() + ":" + suite.second.getVersion());
            if (!suite.second.getName().equals(suite.first.getTaskSuite().getName()))
                throw new Exceptions.UpdateNameDoesNotMatch(suite.second.getName(), suite.first.getTaskSuite().getName());
            if (suite.second.getVersion().equals(suite.first.getTaskSuite().getVersion()))
                return new TaskSuiteUpdate.NoUpdate(suite.second.getName(), suite.second.getVersion());
            return new TaskSuiteUpdate(suite.first, suite.second, researcher, suite.first.getCredential());
        });
        return Tuple.Two.create(o, s);
    }

    public Pair<Observable<TaskSuite>, Observable<InstallationProgress>> installUpdate(TaskSuiteUpdate update) {
        Subject<InstallationProgress, InstallationProgress> s = BehaviorSubject.create((InstallationProgress) null);
        s.subscribeOn(Schedulers.io());
        Observable<TaskSuite> o = dbAccess().tx(insert(update.getResearcher(), update.getCredential(), update.getNewTaskSuite()))
                .flatMap(researchersSuite -> Observable.create((Subscriber<? super TaskSuiteVersion> subscriber) -> {
                    try {
                        if (subscriber.isUnsubscribed())
                            throw new CancelException();
                        LogUtils.i(TAG, "Information about researcher's suite inserted ("
                                + update.getName() + ":" + update.getVersion() + "), downloaded = "
                                + Boolean.toString(researchersSuite.getTaskSuite().getDownloaded()));
                        installData(update.getNewTaskSuite(), researchersSuite, subscriber, s);
                        update.setNewResearchersSuite(researchersSuite);
                    } catch (Throwable t) {
                        LogUtils.v(TAG, "Error in flatMap in installUpdate", t);
                        subscriber.onError(t);
                    }
                })).flatMap(suite -> dbAccess().tx(remove(update.getOldTaskSuite(), update.getNewResearchersSuite())).map(ignore -> suite))
                .map(suite -> new TaskSuite(dbAccess(), getTaskStorage(),
                        suite.getTaskSuite().getName(), suite.getIdentifier()));
        return Pair.create(o, s);
    }


    public Tuple.Two<Observable<TaskSuite>, Observable<InstallationProgress>>
    installSuiteFromAssets(Researcher researcher, AssetManager assetManager, String assetName) {
        AssetsTaskAccessor accessor = new AssetsTaskAccessor(assetManager, assetName);
        Subject<InstallationProgress, InstallationProgress> s = BehaviorSubject.create((InstallationProgress) null);
        Observable<TaskSuite> o = accessor.getSuite()
                .flatMap(assetsTaskSuite -> dbAccess().tx(insert(researcher, null, null, assetsTaskSuite))
                                .flatMap(researchersSuite -> Observable.create((Subscriber<? super TaskSuiteVersion> subscriber) -> {
                                    try {
                                        if (subscriber.isUnsubscribed())
                                            throw new CancelException();
                                        LogUtils.i(TAG, "Information about researcher's suite (" + researcher.getTextId() + ":*****) inserted (" + assetsTaskSuite.getName() + ":" + assetsTaskSuite.getVersion() + "), downloaded = " + Boolean.toString(researchersSuite.getTaskSuite().getDownloaded()));
                                        installData(assetsTaskSuite, researchersSuite, subscriber, s);
                                    } catch (Throwable t) {
                                        subscriber.onError(t);
                                    }
                                })).map(suite -> new TaskSuite(dbAccess(), getTaskStorage(),
                                        suite.getTaskSuite().getName(), suite.getIdentifier()))
                );
        return Tuple.Two.create(o, s);
    }

    public Observable<Object> uninstallSuite(ResearchersSuite researchersSuite) {
        return dbAccess().tx(remove(researchersSuite));
    }

    public Observable<ResearchersSuite> getResearchersSuite(TaskSuite taskSuite, Researcher researcher) {
        return listResearchersSuitesForResearcher(researcher)
                .filter(researchersSuite -> researchersSuite.getTaskSuite().getId().equals(taskSuite.getDbEntry().getId()));
    }

    public Pair<Observable<TaskSuite>, Observable<InstallationProgress>> repairSuite(ResearchersSuite researchersSuite) {
        try {
            Subject<InstallationProgress, InstallationProgress> s = BehaviorSubject.create((InstallationProgress) null);
            URL manifestUrl = new URL(researchersSuite.getCredential().getManifestUrl());
            Auth auth = HttpBasicAuth.fromCredentialEntry(researchersSuite.getCredential());
            NetworkTaskAccessor accessor = new NetworkTaskAccessor(manifestUrl, auth,
                    getServiceProvider().getDeviceId(), getServiceProvider().getNetworkSupport());
            Observable<TaskSuite> o = accessor.getSuite()
                    .flatMap(networkTaskSuite ->
                            Observable.create((Subscriber<? super TaskSuiteVersion> subscriber) -> {
                                try {
                                    if (subscriber.isUnsubscribed())
                                        throw new CancelException();
                                    try {
                                        getTaskStorage().getSuite(networkTaskSuite.getName())
                                                .getVersion(networkTaskSuite.getVersion())
                                                .uninstall();
                                    } catch (Exceptions.SuiteNotFound e) {
                                        LogUtils.w(TAG, "Suite not found", e);
                                    }
                                    pl.edu.ibe.loremipsum.db.schema.TaskSuite taskSuite = researchersSuite.getTaskSuite();
                                    taskSuite.setDownloaded(false);
                                    taskSuite.update();
                                    installData(networkTaskSuite, researchersSuite, subscriber, s);
                                } catch (Throwable t) {
                                    t.printStackTrace();
                                    subscriber.onError(t);
                                }
                            }))
                    .flatMap(v -> getTaskSuite(v.getTaskSuite().getName(), v.getIdentifier()));

            return Pair.create(o, s.filter(p -> p != null));
        } catch (MalformedURLException e) {
            LogUtils.wtf(TAG, "How the world is it possible?", e);
            throw ExecutionException.wrap(e);
        }
    }
}
