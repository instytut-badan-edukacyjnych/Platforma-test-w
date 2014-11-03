package pl.edu.ibe.loremipsum.task.management;

import pl.edu.ibe.loremipsum.BaseInstrumentationTestCase;
import pl.edu.ibe.loremipsum.network.Auth;
import pl.edu.ibe.loremipsum.task.management.installation.network.NetworkTaskAccessor;
import pl.edu.ibe.loremipsum.task.management.installation.network.NetworkTaskSuite;
import pl.edu.ibe.loremipsum.task.management.storage.FilesystemTaskStorage;
import pl.edu.ibe.loremipsum.task.management.storage.TaskStorage;
import pl.edu.ibe.loremipsum.util.TestServer;

import java.io.IOException;
import java.net.URL;

/**
 * @author Mariusz Pluciński
 */
public class TaskSuiteDownloadTest extends BaseInstrumentationTestCase {
    protected static final String[] testDeviceId = {"test-device", "test-device-foo", "test-device-bar"};

    private static final String TAG = TaskSuiteDownloadTest.class.toString();

    @
Override
    public void setUp() throws Exception {
        super.setUp();
        setTestServer(TestServer.testSuiteServer(getServiceProvider()));
        getTestServer().setServerTestMode(true);
    }

    private void installSuite(Auth auth, String deviceId) throws IOException {
        installSuite(auth, deviceId, "1.0");
    }

    private void installSuite(Auth auth, String deviceId, String expectedVersion) throws IOException {
        NetworkTaskAccessor accessor = new NetworkTaskAccessor(
                new URL(getTestServer().getServerAddress()), auth, deviceId,
                getNetworkSupport());
        NetworkTaskSuite suite = accessor.getSuite().toBlockingObservable().first();
        assertEquals("test-suite", suite.getName());
        assertEquals(expectedVersion, suite.getVersion());

        TaskStorage storage = new FilesystemTaskStorage(getStorageDir(deviceId));
        execute(suite.installTo(storage).second);
        assertEquals(expectedVersion, storage.getSuite("test-suite")
                .getVersion(expectedVersion).getIdentifier());
    }

    public void testDownloadSuiteWithLimit() throws IOException, InterruptedException {
        /* Alice installs suite on first device */
        installSuite(alice, testDeviceId[0]);

        /* and on second one */
        installSuite(alice, testDeviceId[1]);

        /* but is unable to do so on third one - administrator set up limit for her */
        assertThrowsWrapped(Exceptions.InstallationLimitExceededException.class, () -> installSuite(alice, testDeviceId[2]));
    }

    public void testDownloadSuiteWithoutLimit() throws IOException, InterruptedException {
        /* Bob has no limits in installations */
        installSuite(bob, testDeviceId[0]);
        installSuite(bob, testDeviceId[1]);
        installSuite(bob, testDeviceId[2]);
    }

    public void testMultipleVersions() throws IOException {
        installSuite(alice, testDeviceId[0]);

        getTestServer().setServedTaskSuiteLevel(1);

        installSuite(alice, testDeviceId[0], "1.1");
    }
}
