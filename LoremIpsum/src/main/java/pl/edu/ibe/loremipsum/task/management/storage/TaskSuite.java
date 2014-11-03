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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;

import pl.edu.ibe.loremipsum.task.management.Exceptions;
import pl.edu.ibe.loremipsum.tools.LogUtils;
import pl.edu.ibe.loremipsum.tools.Tuple;
import pl.edu.ibe.loremipsum.tools.io.CopyStream;
import pl.edu.ibe.loremipsum.tools.io.VirtualFile;
import rx.Observable;

/**
 * @author Mariusz Pluciński
 */
public class TaskSuite implements Iterable<TaskSuiteVersion> {
    private SuiteProvider suiteProvider;

    public TaskSuite(String name) throws Exceptions.TaskStorageException {
        setProvider(new LocalSuiteProvider(name));
    }

    public TaskSuite(SuiteProvider suiteProvider) throws Exceptions.TaskStorageException {
        setProvider(suiteProvider);
    }

    public String getName() {
        return suiteProvider.getName();
    }

    public int versionsCount() {
        return suiteProvider.versionCount();
    }

    public TaskSuiteVersion createVersion(String versionId) throws Exceptions.VersionInstallationFailed {
        TaskSuiteVersion version = new TaskSuiteVersion(versionId, this);
        suiteProvider.addVersion(version);
        return version;
    }

    public boolean hasVersion(String versionId) {
        return suiteProvider.hasVersion(versionId);
    }

    public TaskSuiteVersion getVersion(String versionId) throws Exceptions.VersionNotFound {
        return suiteProvider.getVersion(versionId, this);
    }

    public Iterator<TaskSuiteVersion> iterator() {
        try {
            return suiteProvider.versionIterator();
        } catch (Exceptions.SuiteNotFound e) {
            LogUtils.e("task.management", "Could not iterate task suite versions for \"" + getName() + "\"", e);
            return null;
        }
    }

    public void uninstall() throws Exceptions.UninstallationFailedException {
        suiteProvider.uninstall();
    }

    public void setProvider(SuiteProvider suiteProvider) throws Exceptions.VersionNotFound, Exceptions.SuiteNotFound {
        this.suiteProvider = suiteProvider;
        suiteProvider.setSuite(this);
    }

    public void uninstallVersion(String version) throws Exceptions.UninstallationFailedException {
        suiteProvider.uninstallVersion(version);
        if (versionsCount() == 0)
            uninstall();
    }

    Tuple.Three<OutputStream, Observable<CopyStream.CopyProgress>, Observable<ZipEntry>>
    getVersionOutputStream(String identifier) throws Exceptions.TaskStorageException, IOException {
        return suiteProvider.getVersionOutputStream(identifier);
    }

    InputStream getVersionInputStream(String identifier) throws Exceptions.TaskStorageException, IOException {
        return suiteProvider.getVersionInputStream(identifier);
    }

    public VirtualFile getVersionVirtualFile(String identifier) throws IOException {
        return suiteProvider.getVersionVirtualFile(identifier);
    }

    public void validateVersionStorage(String identifier) throws IOException {
        suiteProvider.validateVersionStorage(identifier);
    }

    private class LocalSuiteProvider implements SuiteProvider {
        private final String name;
        private List<TaskSuiteVersion> versions = new ArrayList<TaskSuiteVersion>();
        private TaskSuite suite;

        public LocalSuiteProvider(String name) {
            this.name = name;
        }

        private void throwNotInstalled(String identifier) throws Exceptions.SuiteNotInstalled {
            throw new Exceptions.SuiteNotInstalled("Cannot open input stream on non-installed suite " + suite.getName() + ", version " + identifier);
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public void addVersion(TaskSuiteVersion version) {
            versions.add(version);
        }

        @Override
        public int versionCount() {
            return versions.size();
        }

        @Override
        public Iterator<TaskSuiteVersion> versionIterator() {
            return versions.iterator();
        }

        @Override
        public void uninstallVersion(String version) throws Exceptions.UninstallationFailedException {
            try {
                versions.remove(getVersion(version, suite));
            } catch (Exceptions.VersionNotFound e) {
                throw new Exceptions.UninstallationFailedException(e);
            }
        }

        @Override
        public void setSuite(TaskSuite suite) {
            this.suite = suite;
        }

        @Override
        public void uninstall() throws Exceptions.UninstallationFailedException {
            throw new Exceptions.UninstallationFailedException("Suite \"" + name + "\" is not installed");
        }

        @Override
        public Tuple.Three<OutputStream, rx.Observable<CopyStream.CopyProgress>, rx.Observable<java.util.zip.ZipEntry>> getVersionOutputStream(String identifier) throws Exceptions.SuiteNotInstalled {
            throwNotInstalled(identifier);
            return null;
        }

        @Override
        public InputStream getVersionInputStream(String identifier) throws Exceptions.SuiteNotInstalled {
            throwNotInstalled(identifier);
            return null;
        }

        @Override
        public boolean hasVersion(String versionId) {
            for (TaskSuiteVersion version : versions)
                if (version.getIdentifier().equals(versionId))
                    return true;
            return false;
        }

        @Override
        public VirtualFile getVersionVirtualFile(String identifier) {
            throwNotInstalled(identifier);
            return null;
        }

        @Override
        public void validateVersionStorage(String identifier) {
            throwNotInstalled(identifier);
        }

        @Override
        public TaskSuiteVersion getVersion(String versionId, TaskSuite taskSuiteVersions) throws Exceptions.VersionNotFound {
            for (TaskSuiteVersion version : versions)
                if (version.getIdentifier().equals(versionId))
                    return version;
            throw new Exceptions.VersionNotFound("Not found task suite \"" + getName() + "\" with version \"" + versionId + "\"");
        }
    }
}
