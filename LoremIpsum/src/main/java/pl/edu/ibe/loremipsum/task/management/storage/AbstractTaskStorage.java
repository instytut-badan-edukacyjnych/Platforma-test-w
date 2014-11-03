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

package pl.edu.ibe.loremipsum.task.management.storage;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import pl.edu.ibe.loremipsum.task.management.Exceptions;
import pl.edu.ibe.loremipsum.tools.CancelException;
import pl.edu.ibe.loremipsum.tools.DynamicByteBuffer;
import pl.edu.ibe.loremipsum.tools.ExecutionException;
import pl.edu.ibe.loremipsum.tools.LogUtils;
import pl.edu.ibe.loremipsum.tools.Tuple;
import pl.edu.ibe.loremipsum.tools.io.CopyStream;
import rx.Observable;
import rx.Subscriber;
import rx.subjects.BehaviorSubject;
import rx.subjects.Subject;

/**
 * @author Mariusz Pluciński
 */
public abstract class AbstractTaskStorage implements TaskStorage {
    private static final String TAG = AbstractTaskStorage.class.toString();
    private EncryptionLayer encryptionLayer;

    public AbstractTaskStorage() {
        encryptionLayer = new EmptyEncryptionLayer();
    }

    public void installCipher(EncryptionLayer encryptionLayer) {
        this.encryptionLayer = encryptionLayer;
    }

    public EncryptionLayer getEncryptionLayer() {
        return encryptionLayer;
    }

    InputStream wrapInputStream(InputStream stream) throws Exceptions.TaskStorageException {
        return encryptionLayer.wrapInputStream(stream);
    }

    Tuple.Three<
            OutputStream,
            Observable<CopyStream.CopyProgress>,
            Observable<ZipEntry>
            > wrapOutputStream(OutputStream stream) throws Exceptions.TaskStorageException {
        RepackagingOutputStream output = new RepackagingOutputStream(stream);
        return Tuple.Three.create(output, output.getSingleProgress(), output.getTotalProgress());
    }

    private class RepackagingOutputStream extends OutputStream {
        private final ZipOutputStream zipOutputStream;
        private final DynamicByteArrayInputStream inputStream;
        private final ZipInputStream zipInputStream;
        private Subject<CopyStream.CopyProgress, CopyStream.CopyProgress> singleProgress;
        private Observable<ZipEntry> totalProgress;

        public RepackagingOutputStream(OutputStream outputStream) {
            zipOutputStream = new ZipOutputStream(new BufferedOutputStream(outputStream));
            inputStream = new DynamicByteArrayInputStream();
            zipInputStream = new ZipInputStream(new BufferedInputStream(inputStream));
            singleProgress = BehaviorSubject.create((CopyStream.CopyProgress) null);
            totalProgress = Observable.create((Subscriber<? super ZipEntry> subscriber) -> {
                try {
                    try {
                        Map<String, String> filesChecksums = new HashMap<>();
                        Set<String> directories = new HashSet<>();
                        boolean hasChecksums = false;

                        LogUtils.v(TAG, "Repackaging output stream starts repackaging");
                        while (true) {
                            if (subscriber.isUnsubscribed())
                                throw new CancelException();
                            ZipEntry entry = zipInputStream.getNextEntry();
                            if (entry == null)
                                break;

                            try {
                                zipOutputStream.putNextEntry(new ZipEntry(entry.getName()));
                                subscriber.onNext(entry);
                                if (entry.getName().equals("checksums.bin")) {
                                    LogUtils.v(TAG, "Package has checksums");
                                    hasChecksums = true;
                                    BufferedReader reader = new BufferedReader(new InputStreamReader(zipInputStream));
                                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(encryptionLayer.wrapOutputStream(zipOutputStream)));
                                    try {
                                        String line = reader.readLine();
                                        while (line != null) {
                                            writer.write(line + "\n");
                                            String[] lineParts = line.split(":");
                                            switch (lineParts[1]) {
                                                case "directory":
                                                    directories.add(lineParts[0]);
                                                    break;
                                                case "file":
                                                    filesChecksums.put(lineParts[0], lineParts[2]);
                                                    break;
                                                default:
                                                    throw new IOException("Wrong entry type in checksum file: " + lineParts[1]);
                                            }
                                            line = reader.readLine();
                                        }
                                    } finally {
                                        writer.flush();
                                    }
                                } else {
                                    if (entry.isDirectory()) {
                                        if (hasChecksums && directories.contains(entry.getName()))
                                            throw new IOException("Unexpected directory found: " + entry.getName());
                                    } else {
                                        OutputStream entryOutputStream = new CloseOverrideOutputStream(zipOutputStream, () -> {
                                            try {
                                                zipOutputStream.flush();
                                                zipOutputStream.closeEntry();
                                            } catch (Exception e) {
                                                throw ExecutionException.wrap(e);
                                            }
                                        });
                                        entryOutputStream = encryptionLayer.wrapOutputStream(entryOutputStream);

                                        MessageDigest md = MessageDigest.getInstance("MD5");
                                        DigestInputStream digestInputStream = new DigestInputStream(zipInputStream, md);

                                        CopyStream.copyStream(digestInputStream, entryOutputStream)
                                                .setCloseInput(false)
                                                .setName("Repackaging process")
                                                .setLogging(false)
                                                .execute()
                                                .subscribe(singleProgress::onNext);

                                        if (hasChecksums && !entry.getName().equals("checksums.bin")) {
                                            if (filesChecksums.containsKey(entry.getName())) {
                                                String actualChecksum = "";
                                                byte[] b = md.digest();
                                                for (byte aB : b)
                                                    actualChecksum += Integer.toString((aB & 0xff) + 0x100, 16).substring(1);
                                                String expectedChecksum = filesChecksums.get(entry.getName());
                                                if (!expectedChecksum.equals(actualChecksum))
                                                    throw new IOException("File checksum is invalid: "
                                                            + entry.getName() + "(expected: "
                                                            + expectedChecksum + "; actual: "
                                                            + actualChecksum + ")");
                                            } else {
                                                LogUtils.e(TAG, "Unexpected file found: " + entry.getName());
                                                throw new IOException("Unexpected file found: " + entry.getName());
                                            }
                                        }
                                    }
                                }
                            } finally {
                                zipOutputStream.flush();
                                zipOutputStream.closeEntry();
                            }
                        }

                        if (hasChecksums)
                            LogUtils.w(TAG, "Just installed package does NOT contain checksums!");
                    } finally {
                        zipOutputStream.flush();
                        zipOutputStream.close();
                    }
                    LogUtils.v(TAG, "Repackaging output stream ends repackaging");
                    singleProgress.onCompleted();
                    subscriber.onCompleted();
                } catch (Throwable t) {
                    LogUtils.e(TAG, "Repackaging output stream encountered error", t);
                    subscriber.onError(t);
                }
            });
        }

        @Override
        public void write(int oneByte) throws IOException {
            inputStream.write(oneByte);
        }

        @Override
        public void close() throws IOException {
            inputStream.close();
            super.close();
        }

        public Observable<CopyStream.CopyProgress> getSingleProgress() {
            return singleProgress;
        }

        public Observable<ZipEntry> getTotalProgress() {
            return totalProgress;
        }
    }

    private class DynamicByteArrayInputStream extends InputStream {
        private DynamicByteBuffer buffer = new DynamicByteBuffer(65536);
        private boolean closed = false;

        public DynamicByteArrayInputStream() {
            buffer.setLogging(false);
        }

        @Override
        public synchronized int read(byte[] b) throws IOException {
            return read(b, 0, b.length);
        }

        @Override
        public synchronized int read(byte[] b, int off, int len) throws IOException {
            try {
                while (buffer.empty() && !closed)
                    wait();
            } catch (InterruptedException e) {
                throw new IOException(e);
            }

            int i = 0;
            for (i = off; i < off + len; ++i) {
                if (buffer.empty())
                    break;
                b[i] = buffer.popFront();
            }
            return i - off;
        }

        @Override
        public synchronized int read() throws IOException {
            try {
                while (buffer.empty() && !closed)
                    wait();
            } catch (InterruptedException e) {
                throw new IOException(e);
            }

            if (!buffer.empty())
                return buffer.popFront();
            return -1;
        }

        public synchronized void write(int b) {
            buffer.pushBack((byte) b);
            notifyAll();
        }

        @Override
        public synchronized void close() throws IOException {
            closed = true;
            super.close();
            notifyAll();
        }
    }

    private class EmptyEncryptionLayer implements EncryptionLayer {
        @Override
        public InputStream wrapInputStream(InputStream stream) {
            return stream;
        }

        @Override
        public OutputStream wrapOutputStream(OutputStream stream) {
            return stream;
        }
    }

    private class CloseOverrideOutputStream extends OutputStream {
        private final OutputStream underlyingOutputStream;
        private final Runnable closeCallback;

        public CloseOverrideOutputStream(OutputStream underlyingOutputStream, Runnable closeCallback) {
            this.underlyingOutputStream = underlyingOutputStream;
            this.closeCallback = closeCallback;
        }

        @Override
        public void close() throws IOException {
            closeCallback.run();
        }

        @Override
        public void flush() throws IOException {
            underlyingOutputStream.flush();
        }

        @Override
        public void write(byte[] buffer) throws IOException {
            underlyingOutputStream.write(buffer);
        }

        @Override
        public void write(byte[] buffer, int offset, int count) throws IOException {
            underlyingOutputStream.write(buffer, offset, count);
        }

        @Override
        public void write(int oneByte) throws IOException {
            underlyingOutputStream.write(oneByte);
        }
    }
}
