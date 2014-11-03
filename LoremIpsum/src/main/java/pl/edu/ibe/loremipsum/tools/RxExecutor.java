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

import java.util.concurrent.Callable;

import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by adam on 11.03.14.
 */
public class RxExecutor {
    public static Object EMPTY_OBJECT = new Object();
    private static Scheduler scheduler = Schedulers.io();

    public static void setDefaultScheduler(Scheduler scheduler) {
        RxExecutor.scheduler = scheduler;
    }

    public static <R> Observable<R> run(Callable<R> callable) {
        Observable<R> o = runSingle(callable);
        if (scheduler != null)
            o = o.subscribeOn(scheduler);
        return o;
    }

    public static <R> Observable<R> runSingle(Callable<R> callable) {
        StackTraceElement[] stackTrace = StackUtils.getStackTrace();
        return Observable//
                .create((Subscriber<? super R> subscriber) -> {
                    try {
                        subscriber.onNext(callable.call());
                        subscriber.onCompleted();
                    } catch (Exception e) {
                        e = ExecutionException.wrap(e, stackTrace);
                        subscriber.onError(e);
                    }
                });
    }

    public static Observable<Object> run() {
        return RxExecutor.run(() -> EMPTY_OBJECT);
    }

    public static <T> Observable<T> runWithUiCallback(Observable<T> source) {
        return source.observeOn(AndroidSchedulers.mainThread());
    }

    public static void runWithUiCallback(Runnable runnable) {
        runSingle(() -> {
            runnable.run();
            return EMPTY_OBJECT;
        }).subscribeOn(AndroidSchedulers.mainThread()).subscribe();
    }
}
