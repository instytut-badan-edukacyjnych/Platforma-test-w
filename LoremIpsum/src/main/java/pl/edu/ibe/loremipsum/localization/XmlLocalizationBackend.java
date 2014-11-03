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

import org.simpleframework.xml.ElementMap;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author Mariusz Pluciński
 */
public class XmlLocalizationBackend implements LocalizationBackend {
    private Map<Locale, List<TranslationsList>> translations = new HashMap<>();
    private Serializer serializer = new Persister();

    @Override
    public String get(String identifier, Locale locale) throws Exceptions.LocalizationUnavailable {
        if (translations.containsKey(locale))
            for (TranslationsList list : translations.get(locale))
                if (list.translations.containsKey(identifier))
                    return convert(list.translations.get(identifier));
        throw new Exceptions.LocalizationUnavailable();
    }

    private String convert(String s) {
        return s.replace("\\n", "\n");
    }

    public void installSource(Locale locale, InputStream inputStream) throws Exceptions.LoadingException {
        if (!translations.containsKey(locale))
            translations.put(locale, new ArrayList<>());
        TranslationsList list;
        try {
            list = serializer.read(TranslationsList.class, inputStream);
        } catch (Exception e) {
            throw new Exceptions.LoadingException(e);
        }
        translations.get(locale).add(list);
    }

    @Root(name = "localization")
    private static class TranslationsList {
        @ElementMap(attribute = true, inline = true, entry = "string", key = "name")
        public Map<String, String> translations;
    }
}
