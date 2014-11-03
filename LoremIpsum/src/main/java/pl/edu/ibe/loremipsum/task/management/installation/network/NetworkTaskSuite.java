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

import android.util.Pair;

import org.apache.http.client.HttpResponseException;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;

import pl.edu.ibe.loremipsum.network.Requests;
import pl.edu.ibe.loremipsum.task.management.Exceptions;
import pl.edu.ibe.loremipsum.task.management.installation.InstallableTaskSuite;
import pl.edu.ibe.loremipsum.task.management.installation.InstallationProgress;
import pl.edu.ibe.loremipsum.task.management.storage.TaskStorage;
import pl.edu.ibe.loremipsum.task.management.storage.TaskSuite;
import pl.edu.ibe.loremipsum.task.management.storage.TaskSuiteVersion;
import pl.edu.ibe.loremipsum.tools.ExecutionException;
import pl.edu.ibe.loremipsum.tools.LogUtils;
import pl.edu.ibe.loremipsum.tools.Tuple;
import pl.edu.ibe.loremipsum.tools.io.CopyStream;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;
import rx.subjects.Subject;

/**
 * @author Mariusz Pluciński
 */
public class NetworkTaskSuite extends InstallableTaskSuite {
    public static final String INSTALLATION_LIMIT_EXCEEDED = "Installation limit exceeded";
    public static final String CONTENT_LENGTH = "Content-Length";
    private static final String TAG = NetworkTaskSuite.class.toString();
    private final NetworkTaskAccessor networkTaskAccessor;
    private URL downloadUrl;

    public NetworkTaskSuite(NetworkTaskAccessor networkTaskAccessor, String name, String version, URL downloadUrl) {
        super(name, version);
        this.networkTaskAccessor = networkTaskAccessor;
        this.downloadUrl = downloadUrl;
    }

    public static NetworkTaskSuite fromManifestResponse(NetworkTaskAccessor accessor, Requests.ManifestResponse response) throws Exceptions.TaskStorageException {
        try {
            List<Requests.ManifestResponse.ResponseSuite> suites = response.getSuites();
            if (suites.size() == 0)
                throw new Exceptions.SuiteNotFound("No task suite available at this address");
            if (suites.size() > 2)
                throw new Exceptions.TaskStorageException("More than one task suite on single address - this is not supported (yet?)");
            Requests.ManifestResponse.ResponseSuite responseSuite = suites.get(0);
            return new NetworkTaskSuite(accessor, responseSuite.getName(), responseSuite.getVersion(), responseSuite.getDownloadUrl());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new Exceptions.TaskStorageException(e);
        }
    }

    @Override
    public Pair<Subject<InstallationProgress, InstallationProgress>, Observable<TaskSuiteVersion>>
    installTo(TaskStorage storage) {
        Subject<InstallationProgress, InstallationProgress> s = BehaviorSubject.create((InstallationProgress) null);
        s.subscribeOn(Schedulers.io());
        Observable<TaskSuiteVersion> o = new Requests.SuiteDownloadRequest(downloadUrl, networkTaskAccessor.getAuth(), networkTaskAccessor.getDeviceId(), networkTaskAccessor.getNetworkSupport())
                .prepare()
                .doOnError(throwable -> {
                    LogUtils.v(TAG, "doOnError", throwable);
                    throwable = ExecutionException.unwrap(throwable);
                    if (throwable instanceof HttpResponseException) {
                        HttpResponseException e = (HttpResponseException) throwable;
                        if (e.getStatusCode() == 403 && e.getMessage().contains(INSTALLATION_LIMIT_EXCEEDED))
                            throw new Exceptions.InstallationLimitExceededException(throwable);
                    }
                    throw ExecutionException.wrap(throwable);
                })
                .flatMap(response -> Observable.create((Subscriber<? super TaskSuiteVersion> subscriber) -> {
                    try {
                        TaskSuite suite = storage.hasSuite(getName())
                                ? storage.getSuite(getName())
                                : new TaskSuite(getName());
                        TaskSuiteVersion suiteVersion = suite.createVersion(getVersion());
                        if (!storage.hasSuite(getName()))
                            storage.installSuite(suite);
                        try {

                            Map<String, List<String>> headers = response.getHeaders();
                            Integer size = (headers.containsKey(CONTENT_LENGTH) ?
                                    Integer.parseInt(response.getHeaders().get(CONTENT_LENGTH).get(0)) :
                                    null);
                            InputStream inputStream = response.getStream();

                            Tuple.Three<OutputStream, Observable<CopyStream.CopyProgress>, Observable<ZipEntry>> output
                                    = suiteVersion.getOutputStream();
                            OutputStream outputStream = output.first;
                            LogUtils.v(TAG, "Starting repackaging progress");
                            output.third = output.third.subscribeOn(Schedulers.newThread());

                            LogUtils.v(TAG, "Starting copy stream");
                            Observable<CopyStream.CopyProgress> copy
                                    = CopyStream.copyStream(inputStream, outputStream)
                                    .setBufferSize(1024 * 1024)
                                    .setProgressTimeInterval(250)
                                    .setName("NetworkTaskSuite-network read op")
                                    .execute()
                                    .doOnNext(progress -> s.onNext(InstallationProgress.progress(
                                            getName(), getVersion(), size, progress)));

                            Observable.merge(output.third, copy).subscribe(new Subscriber<Object>() {
                                @Override
                                public void onCompleted() {
                                    subscriber.onNext(suiteVersion);
                                    subscriber.onCompleted();
                                }

                                @Override
                                public void onError(Throwable e) {
                                    LogUtils.v(TAG, "merged onError", e);
                                    suiteVersion.uninstall();
                                    subscriber.onError(e);
                                }

                                @Override
                                public void onNext(Object object) {
                                    if (subscriber.isUnsubscribed())
                                        unsubscribe();
                                }
                            });
                        } catch (Throwable t) {
                            suiteVersion.uninstall();
                            subscriber.onError(t);
                        }
                    } catch (Throwable t) {
                        subscriber.onError(t);
                    }
                }));
        return Pair.create(s, o);
    }
}

