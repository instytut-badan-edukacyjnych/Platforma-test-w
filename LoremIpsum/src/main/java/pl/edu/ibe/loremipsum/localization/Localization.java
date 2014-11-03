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

import java.util.Locale;

import pl.edu.ibe.loremipsum.tools.LocaleUtils;
import pl.edu.ibe.loremipsum.tools.LogUtils;

/**
 * @author Mariusz Pluciński
 *         <p>
 *         Class that allows to load localized strings from particular source
 */
public class Localization {
    private static final String TAG = Localization.class.toString();

    /**
     * Backend used to load translations
     */
    private final LocalizationBackend backend;

    /**
     * Currently used locale
     */
    private final Locale locale;

    /**
     * Create localization instance associated with given backend and locale
     *
     * @param backend Backend to use to get translations from
     * @param locale  Locale to get translations associated to
     */
    public Localization(LocalizationBackend backend, Locale locale) {
        this.backend = backend;
        this.locale = locale;
    }

    /**
     * Create localization instance associated with given backend and default locale
     *
     * @param backend Backend to use to get translations from
     */
    public Localization(LocalizationBackend backend) {
        this(backend, null);
    }

    /**
     * Provides translation of phrase
     *
     * @param identifier Identifier of phrase for being translated
     * @return Translated string
     * @throws Exceptions.LocalizationUnavailable Thrown when given backend is unable to find translation
     *                                            for associated locale or any of more general locales.
     */
    public String get(String identifier) throws Exceptions.LocalizationUnavailable {
        Locale l = locale;
        while (true) {

            try {
                return backend.get(identifier, l);
            } catch (Exceptions.LocalizationUnavailable e) {
                try {
                    l = LocaleUtils.getOuterLocale(l);
                } catch (LocaleUtils.NoOuterLocale e1) {
                    LogUtils.e(TAG, "No outer locale", e1);
                    throw e;
                }
            }
        }
    }


    /**
     * Provides translation of phrase
     *
     * @param identifier   Identifier of phrase for being translated
     * @param defaultValue Default value to be used if phrase has not been found
     * @return Translated string, or default value
     */
    public String get(String identifier, String defaultValue) {
        try {
            return get(identifier);
        } catch (Exceptions.LocalizationUnavailable localizationUnavailable) {
            return defaultValue;
        }
    }

}
