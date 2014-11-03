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

package pl.edu.ibe.loremipsum.task.management;

import pl.edu.ibe.loremipsum.tools.ExecutionException;

/**
 * @author Mariusz Pluciński
 */
public class Exceptions {
    public static class TaskStorageException extends ExecutionException {
        public TaskStorageException(Throwable t) {
            super(t);
        }

        public TaskStorageException(String message) {
            super(message);
        }

        public TaskStorageException(String message, Throwable t) {
            super(message, t);
        }

    }

    public static class SuiteHasNoVersions extends TaskStorageException {
        public SuiteHasNoVersions(String message) {
            super(message);
        }
    }

    public static class SuiteAlreadyExistsException extends TaskStorageException {
        public SuiteAlreadyExistsException(String message) {
            super(message);
        }
    }

    public static class SuiteNotInstalled extends TaskStorageException {
        public SuiteNotInstalled(String message) {
            super(message);
        }
    }

    public static class SuiteInstallationFailed extends TaskStorageException {
        public SuiteInstallationFailed(Throwable t) {
            super(t);
        }

        public SuiteInstallationFailed(String message, Throwable t) {
            super(message, t);
        }

        public SuiteInstallationFailed(String message) {
            super(message);
        }
    }

    public static class SuiteNotFound extends TaskStorageException {
        public SuiteNotFound(String message) {
            super(message);
        }
    }

    public static class VersionInstallationFailed extends TaskStorageException {
        public VersionInstallationFailed(String message) {
            super(message);
        }

        public VersionInstallationFailed(String message, Exception e) {
            super(message, e);
        }
    }

    public static class VersionNotFound extends TaskStorageException {
        public VersionNotFound(String message) {
            super(message);
        }

        public VersionNotFound(String message, Exception e) {
            super(message, e);
        }
    }

    public static class UninstallationFailedException extends TaskStorageException {
        public UninstallationFailedException(Exception e) {
            super(e);
        }

        public UninstallationFailedException(String message) {
            super(message);
        }

        public UninstallationFailedException(String message, Exception e) {
            super(message, e);
        }

    }

    public static class EncryptionException extends TaskStorageException {
        public EncryptionException(Exception e) {
            super(e);
        }
    }

    public static class InstallationLimitExceededException extends TaskStorageException {
        public InstallationLimitExceededException(Throwable t) {
            super(t);
        }
    }

    public static class UpdateNameDoesNotMatch extends ExecutionException {
        public UpdateNameDoesNotMatch(String updateName, String originalName) {
            super("Update name \"" + updateName + "\" does not match original \"" + originalName + "\"");
        }
    }
}
