package pl.edu.ibe.loremipsum.tools;

import pl.edu.ibe.loremipsum.BaseInstrumentationTestCase;

/**
 * @author Mariusz Pluciński
 */
public class StringUtilsTest extends BaseInstrumentationTestCase {
    public void testJoin() {
        String[] strs = {"those", "are", "some", "strings", "that", "are", "indeed", "meaningless"};
        assertEquals("thosearesomestringsthatareindeedmeaningless",
                StringUtils.join(strs));
        assertEquals("thoseFOOareFOOsomeFOOstringsFOOthatFOOareFOOindeedFOOmeaningless",
                StringUtils.join(strs, "FOO"));
        assertEquals("someFOOstringsFOOthat",
                StringUtils.join(strs, "FOO", 2, 3));
        assertEquals("strings",
                StringUtils.join(strs, "FOO", 3, 1));
        assertEquals("",
                StringUtils.join(strs, "FOO", 3, 0));
    }
}
