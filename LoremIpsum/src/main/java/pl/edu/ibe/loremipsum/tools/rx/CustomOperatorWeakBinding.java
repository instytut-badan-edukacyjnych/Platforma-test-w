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

package pl.edu.ibe.loremipsum.tools.rx;

import android.util.Log;

import java.lang.ref.WeakReference;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import rx.functions.Functions;

/**
 * Ties a source sequence to the life-cycle of the given target object, and/or the subscriber
 * using weak references. When either object is gone, this operator automatically unsubscribes
 * from the source sequence.
 * <p>
 * You can also pass in an optional predicate function, which whenever it evaluates to false
 * on the target object, will also result in the operator unsubscribing from the sequence.
 * <p>
 * This is modified copy of class from RxJava, in which field declaration of
 * OperatorWeakBinding.WeakSubscriber.subscriberRef was changed to NOT use WeakReference
 *
 * @param <T> the type of the objects emitted to a subscriber
 * @param <R> the type of the target object to bind to
 */
public final class CustomOperatorWeakBinding<T, R> implements Observable.Operator<T, T> {

    private static final String LOG_TAG = "WeakBinding";

    final WeakReference<R> boundRef;
    private final Func1<? super R, Boolean> predicate;

    public CustomOperatorWeakBinding(R bound, Func1<? super R, Boolean> predicate) {
        boundRef = new WeakReference<R>(bound);
        this.predicate = predicate;
    }

    public CustomOperatorWeakBinding(R bound) {
        boundRef = new WeakReference<R>(bound);
        this.predicate = Functions.alwaysTrue();
    }

    @Override
    public Subscriber<? super T> call(final Subscriber<? super T> child) {
        return new WeakSubscriber(child);
    }

    final class WeakSubscriber extends Subscriber<T> {

        final Subscriber<? super T> subscriberRef;

        private WeakSubscriber(Subscriber<? super T> source) {
            super(source);
            subscriberRef = source;
        }

        @Override
        public void onCompleted() {
            final Subscriber<? super T> sub = subscriberRef;
            if (shouldForwardNotification(sub)) {
                sub.onCompleted();
            } else {
                handleLostBinding(sub, "onCompleted");
            }
        }

        @Override
        public void onError(Throwable e) {
            final Subscriber<? super T> sub = subscriberRef;
            if (shouldForwardNotification(sub)) {
                sub.onError(e);
            } else {
                handleLostBinding(sub, "onError");
            }
        }

        @Override
        public void onNext(T t) {
            final Subscriber<? super T> sub = subscriberRef;
            if (shouldForwardNotification(sub)) {
                sub.onNext(t);
            } else {
                handleLostBinding(sub, "onNext");
            }
        }

        private boolean shouldForwardNotification(Subscriber<? super T> sub) {
            final R target = boundRef.get();
            return sub != null && target != null && predicate.call(target);
        }

        private void handleLostBinding(Subscriber<? super T> sub, String context) {
            if (sub == null) {
                log("subscriber gone; skipping " + context);
            } else {
                final R r = boundRef.get();
                if (r != null) {
                    // the predicate failed to validate
                    log("bound component has become invalid; skipping " + context);
                } else {
                    log("bound component gone; skipping " + context);
                }
            }
            log("unsubscribing...");
            unsubscribe();
        }

        private void log(String message) {
            if (Log.isLoggable(LOG_TAG, Log.DEBUG)) {
                Log.d(LOG_TAG, message);
            }
        }
    }
}
