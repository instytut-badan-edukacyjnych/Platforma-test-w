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

import android.util.Pair;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;

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
 * @author Mariousz Pluciński
 */
public class AssetsTaskSuite extends InstallableTaskSuite {
    private static final String TAG = AssetsTaskSuite.class.toString();
    private final InputStream input;
    private Integer size;

    public AssetsTaskSuite(String name, String version, InputStream input, Integer size) {
        super(name, version);
        this.input = input;
        this.size = size;
    }

    @Override
    public Pair<Subject<InstallationProgress, InstallationProgress>, Observable<TaskSuiteVersion>>
    installTo(TaskStorage storage) {
        Subject<InstallationProgress, InstallationProgress> s = BehaviorSubject.create((InstallationProgress) null);
        s.subscribeOn(Schedulers.io());
        Observable<TaskSuiteVersion> o = Observable.create((Subscriber<? super TaskSuiteVersion> subscriber) -> {
            try {
                TaskSuite suite = new TaskSuite(getName());
                TaskSuiteVersion suiteVersion = suite.createVersion(getVersion());
                storage.installSuite(suite);

                Tuple.Three<OutputStream, Observable<CopyStream.CopyProgress>, Observable<ZipEntry>> output
                        = suiteVersion.getOutputStream();
                OutputStream outputStream = output.first;

                Boolean[] completed = {false};

                LogUtils.v(TAG, "Starting repackaging progress");
                output.third.observeOn(Schedulers.newThread())
                        .subscribeOn(Schedulers.newThread())
                        .doOnCompleted(() -> {
                            synchronized (completed) {
                                completed[0] = true;
                                completed.notifyAll();
                            }
                        })
                        .subscribe();

                LogUtils.v(TAG, "Starting copy stream");
                CopyStream.copyStream(input, outputStream)
                        .setBufferSize(1024 * 1024)
                        .setProgressTimeInterval(250)
                        .execute()
                        .subscribe(
                                progress -> s.onNext(InstallationProgress.progress(
                                        getName(), getVersion(), size, progress))
                        );
                LogUtils.v(TAG, "Copy stream finished");

                synchronized (completed) {
                    while (!completed[0])
                        completed.wait();
                }
                LogUtils.v(TAG, "Repackage stream finished");
                subscriber.onNext(suiteVersion);
                subscriber.onCompleted();
            } catch (Exception e) {
                e.printStackTrace();
                throw ExecutionException.wrap(e);
            }
        });
        return Pair.create(s, o);
    }
}
