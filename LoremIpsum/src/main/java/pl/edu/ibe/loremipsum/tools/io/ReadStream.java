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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import rx.Observable;

/**
 * @author Mariusz Pluciński
 */
public class ReadStream {
    private final ByteArrayOutputStream output;
    private final CopyStream copy;

    protected ReadStream(InputStream input) {
        output = new ByteArrayOutputStream();
        copy = new CopyStream(input, output);
    }

    public static ReadStream readStream(InputStream input) {
        return new ReadStream(input);
    }

    public int getBufferSize() {
        return copy.getBufferSize();
    }

    public ReadStream setBufferSize(int bufferSize) {
        copy.setBufferSize(bufferSize);
        return this;
    }

    public int getProgressTimeInterval() {
        return copy.getProgressTimeInterval();
    }

    public ReadStream setProgressTimeInterval(int milliseconds) {
        copy.setProgressTimeInterval(milliseconds);
        return this;
    }

    public boolean getCloseInput() {
        return copy.getCloseInput();
    }

    public ReadStream setCloseInput(boolean closeInput) {
        copy.setCloseInput(closeInput);
        return this;
    }

    public byte[] getBytes() {
        return output.toByteArray();
    }

    public Observable<ReadProgress> execute() {
        return copy.execute().map(ReadProgress::new);
    }

    public class ReadProgress {
        private final CopyStream.CopyProgress copyProgress;

        public ReadProgress(CopyStream.CopyProgress copyProgress) {
            this.copyProgress = copyProgress;
        }

        public byte[] getBytes() {
            return ReadStream.this.getBytes();
        }

        public boolean isFinished() {
            return copyProgress.isFinished();
        }
    }
}
