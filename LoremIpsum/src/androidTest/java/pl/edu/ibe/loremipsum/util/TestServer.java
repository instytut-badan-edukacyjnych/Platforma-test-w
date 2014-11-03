package pl.edu.ibe.loremipsum.util;

import junit.framework.AssertionFailedError;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import pl.edu.ibe.loremipsum.network.LoremIpsumRequest;
import pl.edu.ibe.loremipsum.network.LoremIpsumResponse;
import pl.edu.ibe.loremipsum.tablet.base.ServiceProvider;

/**
 * @author Mariusz Pluciński
 */
public class TestServer {

    private static final String testmodePath = "testmode/";
    private static final String testServerIdentifier = "LoremIpsumProvider/0.1";
    private static final String testmodeIdentifier = "-testmode";
    private static final String transferFailurePath = "testmode/transfer_failure/";
    private static final String testLevelPath = "testmode/version_level/";

    private final ServiceProvider services;
    private final String testServerAddress;
    private boolean hasTestMode = true;

    private TestServer(ServiceProvider services, String protocol, String port) {
        this.services = services;
        this.testServerAddress = protocol + "://10.0.2.2:" + port +"/";
//        this.testServerAddress = "https://10.100.30.125:" + port + "/";
//        this.testServerAddress = "https://10.0.3.2:" + port + "/";
    }

    public static TestServer collectorServer(ServiceProvider services) {
        TestServer testServer = new TestServer(services, "https", "40666");
        testServer.hasTestMode = false;
        return testServer;
    }

    public static TestServer testSuiteServer(ServiceProvider services) {
        return new TestServer(services, "https", "40555");
    }

    public static TestServer supportServer(ServiceProvider services) {
        TestServer testServer = new TestServer(services, "http", "40777");
        testServer.hasTestMode = false;
        return testServer;
    }

    public String getServerAddress() {
        return testServerAddress;
    }

    public void setServerTransferFailure(boolean state) throws MalformedURLException {
        URL url = new URL(testServerAddress + transferFailurePath + (state ? "on" : "off"));
        new LoremIpsumRequest<>(url, LoremIpsumResponse::new, services.getNetworkSupport())
                .prepare().toBlockingObservable().last();
    }

    public void setServerTestMode(final boolean state) throws IOException, InterruptedException {
        setServerTestMode(testServerAddress, state);
    }

    public void setServerTestMode(final String serverAddress, final boolean state) throws IOException, InterruptedException {
        if (!hasTestMode) {
            return;
        }
        URL url = new URL(serverAddress + testmodePath + (state ? "on" : "off"));
        LoremIpsumResponse response =
                new LoremIpsumRequest<>(url, LoremIpsumResponse::new, services.getNetworkSupport())
                        .prepare().toBlockingObservable().first();

        String server = response.getHeaders().get("Server").get(1);
        String expectedServerIdentifier = testServerIdentifier + (state ? testmodeIdentifier : "");

        if (!server.equals(expectedServerIdentifier))
            throw new AssertionFailedError("Could not connect with currentTaskSuite server."
                    + "Make sure that LoremIpsum TEST server is running on " + serverAddress);
    }

    public void setServedTaskSuiteLevel(int level) throws MalformedURLException {
        URL url = new URL(testServerAddress + testLevelPath + Integer.toString(level));
        new LoremIpsumRequest<>(url, LoremIpsumResponse::new, services.getNetworkSupport())
                .prepare().toBlockingObservable().first();
    }
}
