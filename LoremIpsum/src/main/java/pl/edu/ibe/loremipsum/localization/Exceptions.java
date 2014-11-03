/************************************
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
 ************************************/

package pl.edu.ibe.loremipsum.localization;

/**
 * @author Mariusz Pluciński
 */
public class Exceptions extends Exception {
    public static class LocalizationException extends Exception {
        /**
         * Create an exception
         */
        public LocalizationException() {
        }

        /**
         * Create an exception
         *
         * @param detailMessage Message informing about the details of the exception
         */
        public LocalizationException(String detailMessage) {
            super(detailMessage);
        }

        /**
         * Create an exception
         *
         * @param detailMessage Message informing about the details of the exception
         * @param throwable     Nested throwable to be put into exception
         */
        public LocalizationException(String detailMessage, Throwable throwable) {
            super(detailMessage, throwable);
        }

        /**
         * Create an exception
         *
         * @param throwable Nested throwable to be put into exception
         */
        public LocalizationException(Throwable throwable) {
            super(throwable);
        }
    }

    public static class LocalizationUnavailable extends LocalizationException {
        /**
         * Create an exception
         */
        public LocalizationUnavailable() {
        }

        /**
         * Create an exception
         *
         * @param detailMessage Message informing about the details of the exception
         */
        public LocalizationUnavailable(String detailMessage) {
            super(detailMessage);
        }

        /**
         * Create an exception
         *
         * @param detailMessage Message informing about the details of the exception
         * @param throwable     Nested throwable to be put into exception
         */
        public LocalizationUnavailable(String detailMessage, Throwable throwable) {
            super(detailMessage, throwable);
        }

        /**
         * Create an exception
         *
         * @param throwable Nested throwable to be put into exception
         */
        public LocalizationUnavailable(Throwable throwable) {
            super(throwable);
        }
    }

    public static class LoadingException extends LocalizationException {
        /**
         * Create an exception
         */
        public LoadingException() {
        }

        /**
         * Create an exception
         *
         * @param detailMessage Message informing about the details of the exception
         */
        public LoadingException(String detailMessage) {
            super(detailMessage);
        }

        /**
         * Create an exception
         *
         * @param detailMessage Message informing about the details of the exception
         * @param throwable     Nested throwable to be put into exception
         */
        public LoadingException(String detailMessage, Throwable throwable) {
            super(detailMessage, throwable);
        }

        /**
         * Create an exception
         *
         * @param throwable Nested throwable to be put into exception
         */
        public LoadingException(Throwable throwable) {
            super(throwable);
        }
    }
}
