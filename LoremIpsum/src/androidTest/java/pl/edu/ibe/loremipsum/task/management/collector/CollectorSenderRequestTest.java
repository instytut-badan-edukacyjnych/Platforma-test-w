package pl.edu.ibe.loremipsum.task.management.collector;

import java.io.ByteArrayInputStream;
import java.net.URL;

import pl.edu.ibe.loremipsum.BaseInstrumentationTestCase;
import pl.edu.ibe.loremipsum.util.TestServer;

/**
 * Created by mikolaj on 17.04.14.
 */
public class CollectorSenderRequestTest extends BaseInstrumentationTestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        setTestServer(TestServer.collectorServer(getServiceProvider()));
    }

    //TODO this test need some work, we should if file appeared on server and if checksum is correct
    public void testSendingRequests() throws Exception {

        ByteArrayInputStream stream = new ByteArrayInputStream("some data".getBytes());
        CollectorSenderRequest request = new CollectorSenderRequest(new URL(getTestServer().getServerAddress())
                , "a"
                , stream
                , getServiceProvider().getNetworkSupport());
        CollectorSenderRequest.CollectorSenderResponse response = execute(request.prepare());


        assertNotNull(response);
    }
}
