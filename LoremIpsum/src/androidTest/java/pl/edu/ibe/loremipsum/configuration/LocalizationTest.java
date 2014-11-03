package pl.edu.ibe.loremipsum.configuration;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import pl.edu.ibe.loremipsum.BaseInstrumentationTestCase;
import pl.edu.ibe.loremipsum.localization.Exceptions;
import pl.edu.ibe.loremipsum.localization.Localization;
import pl.edu.ibe.loremipsum.localization.LocalizationBackend;
import pl.edu.ibe.loremipsum.localization.XmlLocalizationBackend;
import pl.edu.ibe.testplatform.test.R;

/**
 * @author Mariusz Pluciński
 */
public class LocalizationTest extends BaseInstrumentationTestCase {
    private class TestLocalizationBackend implements LocalizationBackend {
        private final Map<Locale, String> helloWorld = new HashMap<Locale, String>(){{
            put(new Locale("pl"), "Witaj świecie!");
            put(new Locale("de"), "Grüße dich, Welt!");
            put(new Locale("de", "CH"), "Grüsse dich, Welt!");
        }};

        @Override
        public String get(String identifier, Locale locale) throws Exceptions.LocalizationUnavailable {
            if(identifier.equals("hello_world")) {
                if(locale == null)
                    return "Hello World!";
                if(helloWorld.containsKey(locale))
                    return helloWorld.get(locale);
            }
            throw new Exceptions.LocalizationUnavailable();
        }
    }

    public void checkLocalizations(LocalizationBackend backend) throws Exceptions.LocalizationUnavailable {
        Map<String, Localization> localizations = new HashMap<>();
        localizations.put("", new Localization(backend));
        for (String _l : new String[]{"en", "en_US", "en_GB", "pl", "pl_PL", "de", "de_DE", "de_CH"}) {
            String[] _l1 = _l.split("_");
            Locale locale = null;
            if (_l1.length == 1)
                locale = new Locale(_l1[0]);
            if (_l1.length == 2)
                locale = new Locale(_l1[0], _l1[1]);
            localizations.put(_l, new Localization(backend, locale));
        }

        for(Localization localization: localizations.values()) {
            assertThrows(Exceptions.LocalizationUnavailable.class, () -> localization.get("no_translation"));
            assertEquals("Something that has no translation", localization.get("no_translation", "Something that has no translation"));
        }

        String key = "hello_world";
        assertEquals("Hello World!", localizations.get("").get(key));
        assertEquals("Hello World!", localizations.get("en").get(key));
        assertEquals("Hello World!", localizations.get("en_US").get(key));
        assertEquals("Hello World!", localizations.get("en_GB").get(key));
        assertEquals("Witaj świecie!", localizations.get("pl").get(key));
        assertEquals("Witaj świecie!", localizations.get("pl_PL").get(key));
        assertEquals("Grüße dich, Welt!", localizations.get("de").get(key));
        assertEquals("Grüße dich, Welt!", localizations.get("de_DE").get(key));
        assertEquals("Grüsse dich, Welt!", localizations.get("de_CH").get(key));
    }

    public void testInterface() throws Exceptions.LocalizationUnavailable {
        LocalizationBackend backend = new TestLocalizationBackend();
        checkLocalizations(backend);
    }

    public void testXmlLocalization() throws Exceptions.LocalizationException {
        XmlLocalizationBackend backend = new XmlLocalizationBackend();
        backend.installSource(null, getInstrumentation().getContext().getResources().openRawResource(R.raw.test_localization));
        backend.installSource(new Locale("pl"), getInstrumentation().getContext().getResources().openRawResource(R.raw.test_localization_pl));
        backend.installSource(new Locale("de"), getInstrumentation().getContext().getResources().openRawResource(R.raw.test_localization_de));
        backend.installSource(new Locale("de", "CH"), getInstrumentation().getContext().getResources().openRawResource(R.raw.test_localization_de_ch));
        checkLocalizations(backend);
    }
}
