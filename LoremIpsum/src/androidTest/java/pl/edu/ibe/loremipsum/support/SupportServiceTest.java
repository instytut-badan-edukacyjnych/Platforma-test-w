package pl.edu.ibe.loremipsum.support;

import java.util.Locale;

import pl.edu.ibe.loremipsum.BaseInstrumentationTestCase;
import pl.edu.ibe.loremipsum.util.TestServer;

/**
 * @author Mariusz Pluciński
 */
public class SupportServiceTest extends BaseInstrumentationTestCase {
    private static final String REPORT_MAIL = "abc@example.org";
    private static final String REPORT_PHONE = "+48 000000000";
    private static final String REPORT_DESC_1 = "something is really really wrong";
    private static final String REPORT_DESC_2 = "this app sucks";

    private static final Locale LOCALE_DEFAULT = null;
    private static final Locale LOCALE_PL = new Locale("pl");
    private static final Locale LOCALE_NONEXISTENT = new Locale("qaa");

    @Override
    public void setUp() throws Exception {
        super.setUp();
        setTestServer(TestServer.supportServer(getServiceProvider()));
        getServiceProvider().support().setSupportUrl(getTestServer().getServerAddress()+"submit");
    }

    public void testSendSupportReport() throws SupportResponse.SupportInfoNotFound {
        SupportResponse response1 = getServiceProvider().support()
                .reportBug(REPORT_MAIL, REPORT_PHONE, REPORT_DESC_1, true)
                .toBlockingObservable().single();
        SupportResponse response2 = getServiceProvider().support()
                .reportBug(REPORT_MAIL, REPORT_PHONE, REPORT_DESC_2, false)
                .toBlockingObservable().single();

        // second report's ID should be increased by one in comparison to first one's
        assertEquals(response1.getReportId()+1, response2.getReportId());

        // additional info for default locale and polish locale should not be empty
        assertTrue(!response1.getInfo().getAdditionalInfo().isEmpty());
        assertTrue(!response1.getInfo(LOCALE_PL).getAdditionalInfo().isEmpty());
        // this two calls should be equivalents
        assertTrue(response1.getInfo().equals(response1.getInfo(LOCALE_DEFAULT)));
        // and nonsupported one should fallback to the default one
        assertTrue(response1.getInfo(LOCALE_NONEXISTENT).equals(response1.getInfo()));
        // those two should not be the same
        assertTrue(!response1.getInfo().equals(response1.getInfo(LOCALE_PL)));

        // other fields should not be empty too
        assertTrue(!response1.getInfo().getPhoneNumber().isEmpty());
        assertTrue(response1.getInfo().getTimeSpan() != 0);

        // in second answer too
        assertTrue(!response2.getInfo().getAdditionalInfo().isEmpty());
        assertTrue(!response2.getInfo().getPhoneNumber().isEmpty());
        assertTrue(response2.getInfo().getTimeSpan() != 0);
    }
}
