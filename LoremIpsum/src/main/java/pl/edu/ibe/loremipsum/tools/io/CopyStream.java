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

package pl.edu.ibe.loremipsum.tools.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import pl.edu.ibe.loremipsum.tools.CancelException;
import pl.edu.ibe.loremipsum.tools.LogUtils;
import rx.Observable;
import rx.Subscriber;

/**
 * @author Mariusz Pluciński
 */
public class CopyStream {
    final static public int copyBufferDefaultSize = 65535;
    private int bufferSize = copyBufferDefaultSize;
    final static public int progressTimeIntervalDefault = 1000;
    private long progressTimeInterval = progressTimeIntervalDefault;
    final static public boolean autoCloseDefault = true;
    private boolean closeInput = autoCloseDefault;
    private boolean closeOutput = autoCloseDefault;
    final static public boolean loggingDefault = true;
    private boolean logging = loggingDefault;
    private static final String TAG = CopyStream.class.toString();
    private InputStream input;
    private OutputStream output;
    private String name = "CopyStream operation";

    protected CopyStream(InputStream input, OutputStream output) {
        this.input = input;
        this.output = output;
    }

    public static CopyStream copyStream(InputStream input, OutputStream output) {
        return new CopyStream(input, output);
    }

    public InputStream getInput() {
        return input;
    }

    public OutputStream getOutput() {
        return output;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public CopyStream setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
        return this;
    }

    public int getProgressTimeInterval() {
        return (int) (progressTimeInterval / 1000000);
    }

    public CopyStream setProgressTimeInterval(int milliseconds) {
        this.progressTimeInterval = ((long) milliseconds) * 1000000;
        return this;
    }

    public boolean getCloseInput() {
        return closeInput;
    }

    public CopyStream setCloseInput(boolean closeInput) {
        this.closeInput = closeInput;
        return this;
    }

    public boolean getCloseOutput() {
        return closeOutput;
    }

    public CopyStream setCloseOutput(boolean closeOutput) {
        this.closeOutput = closeOutput;
        return this;
    }

    public String getName() {
        return name;
    }

    public CopyStream setName(String name) {
        this.name = name;
        return this;
    }

    public boolean getLogging() {
        return logging;
    }

    public CopyStream setLogging(boolean logging) {
        this.logging = logging;
        return this;
    }

    private void log(String msg) {
        if (logging)
            LogUtils.v(TAG, "CopyStream \"" + name + "\": " + msg);
    }

    public Observable<CopyProgress> execute() {
        return Observable.create((Subscriber<? super CopyProgress> subscriber) -> {

            try {
                byte[] buffer = new byte[bufferSize];
                int total = 0;

                long startTime = System.nanoTime();
                long lastTime = 0;

                subscriber.onNext(new CopyProgress(0, 0, false));

                while (true) {
                    if (subscriber.isUnsubscribed()) {
                        log("Copy stream is cancelling");
                        throw new CancelException();
                    }

                    int count = input.read(buffer);
                    if (count > 0)
                        output.write(buffer, 0, count);
                    long time = (System.nanoTime() - startTime);

                    if (count >= 0)
                        total += count;

                    if (count >= 0 && (count == 0 || (time - lastTime) >= progressTimeInterval)) {
                        lastTime = time;
                        subscriber.onNext(new CopyProgress(total, (int) (time / 1000000), false));
                    }

                    if (count < 0) {
                        subscriber.onNext(new CopyProgress(total, (int) (time / 1000000), true));
                        break;
                    }
                }

                log("Copy stream is completing");
                subscriber.onCompleted();
            } catch (Throwable t) {
                subscriber.onError(t);
            } finally {
                if (closeInput) {
                    try {
                        input.close();
                    } catch (Exception e) {
                        subscriber.onError(e);
                    }
                }
                try {
                    output.flush();
                } catch (IOException e) {
                    subscriber.onError(e);
                }
                if (closeOutput) {
                    try {
                        output.close();
                    } catch (Exception e) {
                        subscriber.onError(e);
                    }
                }
            }
        });
    }

    public class CopyProgress {
        private int count;
        private int time;
        private boolean finished;

        public CopyProgress(int count, int time, boolean finished) {
            this.count = count;
            this.time = time;
            this.finished = finished;
        }

        public CopyProgress(int count, int countDiff, int time, int timeDiff, boolean finished) {
            this.count = count;
            this.time = time;
            this.finished = finished;
        }

        public int getCount() {
            return count;
        }

        public int getTime() {
            return time;
        }

        public boolean isFinished() {
            return finished;
        }

        public double getSpeed() {
            return ((double) getCount()) / (((double) getTime()) / 1000);
        }
    }
}
