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
import java.util.zip.ZipEntry;

import pl.edu.ibe.loremipsum.task.management.Exceptions;
import pl.edu.ibe.loremipsum.tools.Tuple;
import pl.edu.ibe.loremipsum.tools.io.CopyStream;
import pl.edu.ibe.loremipsum.tools.io.VirtualFile;
import rx.Observable;

/**
 * @author Mariusz Pluciński
 */
public class TaskSuiteVersion {
    private final String identifier;
    private final TaskSuite taskSuite;

    public TaskSuiteVersion(String identifier, TaskSuite taskSuite) {
        this.identifier = identifier;
        this.taskSuite = taskSuite;
    }

    public String getIdentifier() {
        return identifier;
    }

    public TaskSuite getTaskSuite() {
        return taskSuite;
    }

    public Tuple.Three<OutputStream, Observable<CopyStream.CopyProgress>, Observable<ZipEntry>>
    getOutputStream() throws Exceptions.TaskStorageException, IOException {
        return taskSuite.getVersionOutputStream(identifier);
    }

    public InputStream getInputStream() throws Exceptions.TaskStorageException, IOException {
        return taskSuite.getVersionInputStream(identifier);
    }

    public void uninstall() throws Exceptions.UninstallationFailedException {
        taskSuite.uninstallVersion(identifier);
    }

    public VirtualFile getRoot() throws IOException {
        return taskSuite.getVersionVirtualFile(identifier);
    }

    public void validateStorage() throws IOException {
        taskSuite.validateVersionStorage(identifier);
    }
}
