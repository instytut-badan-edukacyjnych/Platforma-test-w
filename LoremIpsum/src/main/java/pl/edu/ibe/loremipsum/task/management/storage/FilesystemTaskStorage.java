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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.zip.ZipEntry;

import pl.edu.ibe.loremipsum.task.management.Exceptions;
import pl.edu.ibe.loremipsum.tools.ExecutionException;
import pl.edu.ibe.loremipsum.tools.FileUtils;
import pl.edu.ibe.loremipsum.tools.LogUtils;
import pl.edu.ibe.loremipsum.tools.Tuple;
import pl.edu.ibe.loremipsum.tools.io.CopyStream;
import pl.edu.ibe.loremipsum.tools.io.ReadStream;
import pl.edu.ibe.loremipsum.tools.io.VirtualFile;
import rx.Observable;

/**
 * @author Mariusz Pluciński
 */
public class FilesystemTaskStorage extends AbstractTaskStorage {
    private static final String TAG = FilesystemTaskStorage.class.toString();
    private static final String dataFileName = "data.bin";
    private final File storageDir;

    public FilesystemTaskStorage(File storageDir) {
        this.storageDir = storageDir;
    }

    @Override
    public int size() {
        return storageDir.list().length;
    }

    @Override
    public void installSuite(TaskSuite suite) throws Exceptions.TaskStorageException {
        String suiteName = suite.getName();

        if (suite.versionsCount() == 0)
            throw new Exceptions.SuiteHasNoVersions("Suite \"" + suiteName + "\" contains no actual version");

        File suiteDirectory = getSuiteDir(suiteName);
        if (suiteDirectory.exists())
            throw new Exceptions.SuiteAlreadyExistsException("Suite with name \"" + suiteName + "\" is already installed");

        try {
            if (!suiteDirectory.mkdir())
                throw new IOException("Could not create \"" + suiteDirectory + "\" directory");

            FilesystemSuiteProvider provider = new FilesystemSuiteProvider(suiteName, suiteDirectory);
            for (TaskSuiteVersion version : suite)
                provider.addVersion(version);

            suite.setProvider(provider);
        } catch (IOException e) {
            throw new Exceptions.SuiteInstallationFailed("Could not install \"" + suiteName + "\" suite", e);
        }
    }

    @Override
    public TaskSuite getSuite(String suiteId) throws Exceptions.TaskStorageException {
        File suitePath = getSuiteDir(suiteId);
        if (!suitePath.isDirectory())
            throw new Exceptions.SuiteNotFound("Task suite \"" + suiteId + "\" not found");

        return new TaskSuite(new FilesystemSuiteProvider(suiteId, suitePath));
    }

    private File getSuiteDir(String suiteId) {
        return new File(storageDir, suiteId);
    }

    @Override
    public boolean hasSuite(String suiteId) {
        return getSuiteDir(suiteId).isDirectory();
    }

    @Override
    public Iterator<TaskSuite> iterator() {
        try {
            return new SuiteIterator();
        } catch (Exceptions.TaskStorageException e) {
            LogUtils.e("task.management", "Could not iterate installed currentTaskSuite suites", e);
            return null;
        }
    }

    private class FilesystemSuiteProvider implements SuiteProvider {
        private String suiteName;
        private File suitePath;
        private TaskSuite suite;

        public FilesystemSuiteProvider(String suiteName, File suitePath) {
            this.suiteName = suiteName;
            this.suitePath = suitePath;
        }

        @Override
        public String getName() {
            return suiteName;
        }

        @Override
        public void addVersion(TaskSuiteVersion version) throws Exceptions.VersionInstallationFailed {
            File versionPath = getVersionPath(version.getIdentifier());
            if (versionPath.exists())
                throw new Exceptions.VersionInstallationFailed("Directory " + versionPath + " already exists");

            if (!versionPath.mkdir())
                throw new Exceptions.VersionInstallationFailed("Could not create directory \"" + versionPath + "\"");
        }

        private File getVersionPath(String versionId) {
            return new File(suitePath, versionId);
        }

        @Override
        public int versionCount() {
            return suitePath.list().length;
        }

        @Override
        public TaskSuiteVersion getVersion(String versionId, TaskSuite taskSuiteVersions) throws Exceptions.VersionNotFound {
            File versionPath = getVersionPath(versionId);
            if (!versionPath.isDirectory())
                throw new Exceptions.VersionNotFound("Not found task suite \"" + suiteName + "\" with version \"" + versionId + "\"");
            return new TaskSuiteVersion(versionId, taskSuiteVersions);
        }

        @Override
        public Iterator<TaskSuiteVersion> versionIterator() throws Exceptions.SuiteNotFound {
            return new FilesystemVersionIterator(suitePath, suite);
        }

        @Override
        public void uninstallVersion(String version) throws Exceptions.UninstallationFailedException {
            try {
                FileUtils.deleteRecursive(getVersionDataDir(version));
            } catch (IOException e) {
                throw new Exceptions.UninstallationFailedException(e);
            }
        }

        @Override
        public void setSuite(TaskSuite suite) {
            this.suite = suite;
        }

        @Override
        public void uninstall() throws Exceptions.UninstallationFailedException {
            try {
                FileUtils.deleteRecursive(suitePath);
            } catch (IOException e) {
                throw new Exceptions.UninstallationFailedException(e);
            }
        }

        @Override
        public Tuple.Three<OutputStream, Observable<CopyStream.CopyProgress>, Observable<ZipEntry>>
        getVersionOutputStream(String identifier) throws Exceptions.TaskStorageException, FileNotFoundException {
            return wrapOutputStream(new BufferedOutputStream(new FileOutputStream(getVersionFileDataPath(identifier))));
        }

        @Override
        public InputStream getVersionInputStream(String identifier) throws Exceptions.TaskStorageException, FileNotFoundException {
            return wrapInputStream(new BufferedInputStream(new FileInputStream(getVersionFileDataPath(identifier))));
        }

        @Override
        public boolean hasVersion(String versionId) {
            return getVersionDataDir(versionId).isDirectory();
        }

        @Override
        public VirtualFile getVersionVirtualFile(String identifier) throws IOException {
            return new FilesystemTaskSuiteVirtualFile(getVersionFileDataPath(identifier), getEncryptionLayer());
        }

        @Override
        public void validateVersionStorage(String identifier) throws IOException {
            VirtualFile root = getVersionVirtualFile(identifier);
            try {
                VirtualFile fchecksums = root.getChildFile("checksums.bin");
                StringBuffer checksumstr = new StringBuffer();
                ReadStream.readStream(fchecksums.getInputStream()).execute().subscribe(
                        p -> {
                            try {
                                if (p.isFinished())
                                    checksumstr.append(new String(p.getBytes(), "UTF-8"));
                            } catch (UnsupportedEncodingException e) {
                                throw ExecutionException.wrap(e);
                            }
                        }
                );
                String[] rawChecksums = checksumstr.toString().split("\n");
                Map<String, String> checksums = new HashMap<>();
                Map<String, Boolean> isFile = new HashMap<>();
                for (String checksum : rawChecksums) {
                    String[] parts = checksum.split(":");
                    String filename = parts[0];

                    if (filename.startsWith("\""))
                        filename = filename.substring(1);
                    if (filename.endsWith("\""))
                        filename = filename.substring(0, filename.length() - 1);

                    if (filename.startsWith("./"))
                        filename = filename.substring(2);

                    Boolean fileIsFile = parts[1].equals("file");
                    isFile.put(filename, fileIsFile);
                    LogUtils.v(TAG, "Adding file to checker: \"" + filename + "\"");
                    if (fileIsFile)
                        checksums.put(filename, parts[2]);
                }

                LinkedList<VirtualFile> virtualFileList = new LinkedList<>();
                virtualFileList.addLast(root);
                while (!virtualFileList.isEmpty()) {
                    VirtualFile file = virtualFileList.pollFirst();
                    if (file.isDirectory())
                        for (VirtualFile childFile : file.listFiles())
                            virtualFileList.addLast(childFile);

                    String filePath = file.getPath();
                    if (filePath.equals("/") || filePath.equals("checksums.bin"))
                        continue;

                    if (filePath.endsWith("/"))
                        filePath = filePath.substring(0, filePath.length() - 1);

                    LogUtils.i(TAG, "Verifying \"" + filePath + "\"");
                    if (isFile.get(filePath) == null)
                        LogUtils.w(TAG, "Unexpected file found: " + filePath);

                    if (file.isDirectory()) {
                        if (isFile.get(filePath))
                            throw new IOException("File is expected to be a file, but is a directory: " + filePath);
                    } else {
                        if (!isFile.get(filePath))
                            throw new IOException("File is expected to be a directory, but is a file: " + filePath);

                        String expectedChecksum = checksums.get(filePath);

                        MessageDigest md = MessageDigest.getInstance("MD5");
                        InputStream is = file.getInputStream();
                        try {
                            DigestInputStream dis = new DigestInputStream(is, md);
                            ReadStream.readStream(dis).execute().subscribe();

                            String actualChecksum = new String();
                            byte[] b = md.digest();
                            for (int i = 0; i < b.length; i++) {
                                actualChecksum += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
                            }

                            if (!expectedChecksum.equals(actualChecksum))
                                throw new IOException("File checksum is invalid: " + filePath + "(expected: " + expectedChecksum + "; actual: " + actualChecksum + ")");
                            else
                                LogUtils.i(TAG, "Checksum is valid for: " + filePath);
                        } finally {
                            is.close();
                        }
                    }
                }
            } catch (FileNotFoundException e) {
                LogUtils.i(TAG, "checksums file not found", e);
            } catch (NoSuchAlgorithmException e) {
                LogUtils.e(TAG, "Could not verify checksums", e);
            }
        }

        private File getVersionDataDir(String identifier) {
            return new File(suitePath, identifier);
        }

        private File getVersionFileDataPath(String identifier) {
            return new File(getVersionDataDir(identifier), dataFileName);
        }
    }

    private class FilesystemVersionIterator implements Iterator<TaskSuiteVersion> {
        private Iterator<File> directories;
        private TaskSuite suite;

        public FilesystemVersionIterator(File suiteDirectory, TaskSuite suite) throws Exceptions.SuiteNotFound {
            File[] files = suiteDirectory.listFiles();
            if (files == null)
                throw new Exceptions.SuiteNotFound("Could not found suite directory \"" + suiteDirectory + "\"");
            directories = Arrays.asList(files).iterator();
            this.suite = suite;
        }

        @Override
        public boolean hasNext() {
            return directories.hasNext();
        }

        @Override
        public TaskSuiteVersion next() {
            File directory = directories.next();
            return new TaskSuiteVersion(directory.getName(), suite);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private class SuiteIterator implements Iterator<TaskSuite> {
        private Iterator<File> directories;

        public SuiteIterator() throws Exceptions.TaskStorageException {
            File[] files = storageDir.listFiles();
            if (files == null)
                throw new Exceptions.TaskStorageException("Could not found suites storage directory \"" + storageDir + "\"");
            this.directories = Arrays.asList(files).iterator();
        }

        @Override
        public boolean hasNext() {
            return directories.hasNext();
        }

        @Override
        public TaskSuite next() {
            File directory = directories.next();
            try {
                return new TaskSuite(new FilesystemSuiteProvider(directory.getName(), directory));
            } catch (Exceptions.TaskStorageException e) {
                LogUtils.e("task.management", "Not found task suite version in directory " + directory.getName(), e);
                return null;
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
