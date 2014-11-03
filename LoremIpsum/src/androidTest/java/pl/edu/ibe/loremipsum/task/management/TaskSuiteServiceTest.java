package pl.edu.ibe.loremipsum.task.management;

import android.util.Pair;

import java.net.MalformedURLException;
import java.net.URL;

import pl.edu.ibe.loremipsum.BaseInstrumentationTestCase;
import pl.edu.ibe.loremipsum.db.schema.Researcher;
import pl.edu.ibe.loremipsum.task.management.installation.InstallationProgress;
import pl.edu.ibe.loremipsum.task.management.storage.TaskSuiteVersion;
import pl.edu.ibe.loremipsum.tools.LogUtils;
import pl.edu.ibe.loremipsum.util.TestServer;
import rx.Observable;
import rx.Subscription;
import rx.schedulers.Schedulers;
import rx.subjects.Subject;

/**
 * @author Mariusz Pluciński
 */
public class TaskSuiteServiceTest extends BaseInstrumentationTestCase {
    private static final String TAG = TaskSuiteServiceTest.class.toString();

    private String idAlice = "alice";
    private String idBob = "bob";

    @Override
    public void setUp() throws Exception {
        super.setUp();
        super.setTestServer(TestServer.testSuiteServer(getServiceProvider()));
        getTestServer().setServerTestMode(true);

        Researcher researcherAlice = new Researcher();
        researcherAlice.setTextId(idAlice);
        researcherAlice.setFirstName("Alice");
        researcherAlice.setSurName("Anonymous");
        researcherAlice.setPassword("password_alice");
        execute(getServiceProvider().researcher().insertResearcher(researcherAlice));

        Researcher researcherBob = new Researcher();
        researcherBob.setTextId(idBob);
        researcherBob.setFirstName("Bob");
        researcherBob.setSurName("Bearded");
        researcherBob.setPassword("password_bob");
        execute(getServiceProvider().researcher().insertResearcher(researcherBob));
    }

    @Override
    public void tearDown() throws Exception {
        execute(getServiceProvider().researcher().removeResearcher(execute(getServiceProvider().researcher().getResearcherByTextId(idAlice))));
        execute(getServiceProvider().researcher().removeResearcher(execute(getServiceProvider().researcher().getResearcherByTextId(idBob))));

        super.tearDown();
    }

    public void testMultipleUsers() throws MalformedURLException {
        int requestsCountStart = getNetworkSupport().getRequestsCount();

        Researcher researcherAlice = execute(getServiceProvider().researcher().getResearcherByTextId(idAlice));
        Researcher researcherBob = execute(getServiceProvider().researcher().getResearcherByTextId(idBob));

        assertEquals(0, getServiceProvider().taskSuites().getTaskStorage().size());
        assertEquals(0, executeToList(getServiceProvider().taskSuites().listSuitesForResearcher(researcherAlice)) .size());
        assertEquals(0, executeToList(getServiceProvider().taskSuites().listSuitesForResearcher(researcherBob)).size());
        assertEquals(requestsCountStart, getNetworkSupport().getRequestsCount());

        // simulate installation failure
        LogUtils.i(TAG, "testing for simulated installation failure");
        getTestServer().setServerTransferFailure(true);
        assertEquals(requestsCountStart + 1, getNetworkSupport().getRequestsCount());

        assertThrowsWrapped(Exceptions.SuiteInstallationFailed.class, () -> {
            execute(getServiceProvider().taskSuites().installSuite(researcherAlice, new URL(getTestServer().getServerAddress()), alice).second);
        });
        assertEquals(0, getServiceProvider().taskSuites().getTaskStorage().size());
        assertEquals(0, executeToList(getServiceProvider().taskSuites().listSuitesForResearcher(researcherAlice)).size());
        assertEquals(0, executeToList(getServiceProvider().taskSuites().listSuitesForResearcher(researcherBob)).size());
        assertEquals(requestsCountStart+3, getNetworkSupport().getRequestsCount());

        //now it should work
        LogUtils.i(TAG, "testing for installation succeeded");
        getTestServer().setServerTransferFailure(false);
        assertEquals(requestsCountStart+4, getNetworkSupport().getRequestsCount());
        TaskSuiteVersion suiteVersion = execute(getServiceProvider().taskSuites().installSuite(researcherAlice, new URL(getTestServer().getServerAddress()), alice).second);
        assertNotNull(suiteVersion);

        assertEquals(1, getServiceProvider().taskSuites().getTaskStorage().size());
        assertEquals(1, executeToList(getServiceProvider().taskSuites().listSuitesForResearcher(researcherAlice)).size());
        assertEquals("test-suite", executeToList(getServiceProvider().taskSuites().listSuitesForResearcher(researcherAlice)).get(0).getName());
        assertEquals("1.0", executeToList(getServiceProvider().taskSuites().listSuitesForResearcher(researcherAlice)).get(0).getVersion());
        assertEquals(0, executeToList(getServiceProvider().taskSuites().listSuitesForResearcher(researcherBob)).size());
        assertEquals(requestsCountStart+6, getNetworkSupport().getRequestsCount());

        assertEquals("test-suite", suiteVersion.getTaskSuite().getName());
        assertEquals("1.0", suiteVersion.getIdentifier());

        LogUtils.i(TAG, "testing for installation succeeded without redownload");
        TaskSuiteVersion suiteVersionBob = execute(getServiceProvider().taskSuites().installSuite(researcherBob, new URL(getTestServer().getServerAddress()), bob).second);

        assertEquals(1, getServiceProvider().taskSuites().getTaskStorage().size());
        assertEquals(1, executeToList(getServiceProvider().taskSuites().listSuitesForResearcher(researcherAlice)).size());
        assertEquals("test-suite", executeToList(getServiceProvider().taskSuites().listSuitesForResearcher(researcherAlice)).get(0).getName());
        assertEquals("1.0", executeToList(getServiceProvider().taskSuites().listSuitesForResearcher(researcherAlice)).get(0).getVersion());
        assertEquals(1, executeToList(getServiceProvider().taskSuites().listSuitesForResearcher(researcherBob)).size());
        assertEquals("test-suite", executeToList(getServiceProvider().taskSuites().listSuitesForResearcher(researcherBob)).get(0).getName());
        assertEquals("1.0", executeToList(getServiceProvider().taskSuites().listSuitesForResearcher(researcherBob)).get(0).getVersion());
        assertEquals(requestsCountStart+7, getNetworkSupport().getRequestsCount());

        assertEquals("test-suite", suiteVersionBob.getTaskSuite().getName());
        assertEquals("1.0", suiteVersionBob.getIdentifier());
    }

    public void testWithDemo() throws MalformedURLException {
        getTestServer().setServedTaskSuiteLevel(2);

        Researcher researcherAlice = execute(getServiceProvider().researcher().getResearcherByTextId(idAlice));
        execute(getServiceProvider().taskSuites().installSuite(researcherAlice, new URL(getTestServer().getServerAddress()), alice).second);

        assertEquals(1, getServiceProvider().taskSuites().getTaskStorage().size());
        assertEquals(1, executeToList(getServiceProvider().taskSuites().listSuitesForResearcher(researcherAlice)).size());
        assertEquals("test-suite-withdemo", executeToList(getServiceProvider().taskSuites().listSuitesForResearcher(researcherAlice)).get(0).getName());
        assertEquals("1.1", executeToList(getServiceProvider().taskSuites().listSuitesForResearcher(researcherAlice)).get(0).getVersion());

        assertEquals(1, executeToList(getServiceProvider().taskSuites().listDemoSuites()).size());
        assertEquals("test-suite-withdemo", executeToList(getServiceProvider().taskSuites().listDemoSuites()).get(0).getName());
        assertEquals("1.1", executeToList(getServiceProvider().taskSuites().listDemoSuites()).get(0).getVersion());
    }

    public void testFromAssets() {
        Researcher researcherAlice = execute(getServiceProvider().researcher().getResearcherByTextId(idAlice));

        execute(getServiceProvider().taskSuites().installSuiteFromAssets(researcherAlice, getInstrumentation().getContext().getResources().getAssets(), testAssetName).first);

        assertEquals(1, getServiceProvider().taskSuites().getTaskStorage().size());
        assertEquals(1, executeToList(getServiceProvider().taskSuites().listSuitesForResearcher(researcherAlice)).size());
        assertEquals("testing-suite", executeToList(getServiceProvider().taskSuites().listSuitesForResearcher(researcherAlice)).get(0).getName());
        assertEquals("1.0", executeToList(getServiceProvider().taskSuites().listSuitesForResearcher(researcherAlice)).get(0).getVersion());

        assertEquals(0, executeToList(getServiceProvider().taskSuites().listDemoSuites()).size());
    }

    public void testFromAssetsWithDemo() {
        Researcher researcherAlice = execute(getServiceProvider().researcher().getResearcherByTextId(idAlice));

        execute(getServiceProvider().taskSuites().installSuiteFromAssets(researcherAlice, getInstrumentation().getContext().getResources().getAssets(), testAssetWithDemoName).first);

        assertEquals(1, getServiceProvider().taskSuites().getTaskStorage().size());
        assertEquals(1, executeToList(getServiceProvider().taskSuites().listSuitesForResearcher(researcherAlice)).size());
        assertEquals("testing-suite-withdemo", executeToList(getServiceProvider().taskSuites().listSuitesForResearcher(researcherAlice)).get(0).getName());
        assertEquals("1.0", executeToList(getServiceProvider().taskSuites().listSuitesForResearcher(researcherAlice)).get(0).getVersion());

        assertEquals(1, executeToList(getServiceProvider().taskSuites().listDemoSuites()).size());
        assertEquals("testing-suite-withdemo", executeToList(getServiceProvider().taskSuites().listDemoSuites()).get(0).getName());
        assertEquals("1.0", executeToList(getServiceProvider().taskSuites().listDemoSuites()).get(0).getVersion());
    }

    public void testPilot() throws MalformedURLException {
        getTestServer().setServedTaskSuiteLevel(3);

        Researcher researcherAlice = execute(getServiceProvider().researcher().getResearcherByTextId(idAlice));
        execute(getServiceProvider().taskSuites().installSuite(researcherAlice, new URL(getTestServer().getServerAddress()), alice).second);

        assertEquals(1, getServiceProvider().taskSuites().getTaskStorage().size());
        assertEquals(1, executeToList(getServiceProvider().taskSuites().listSuitesForResearcher(researcherAlice)).size());
        assertEquals("test-suite-pilot", executeToList(getServiceProvider().taskSuites().listSuitesForResearcher(researcherAlice)).get(0).getName());
        assertEquals("1.1", executeToList(getServiceProvider().taskSuites().listSuitesForResearcher(researcherAlice)).get(0).getVersion());
        assertTrue(executeToList(getServiceProvider().taskSuites().listSuitesForResearcher(researcherAlice)).get(0).getPilot());
    }

    public void testCancelInstallation() throws MalformedURLException, InterruptedException {
        Researcher researcherAlice = execute(getServiceProvider().researcher().getResearcherByTextId(idAlice));

        Pair<Subject<InstallationProgress, InstallationProgress>, Observable<TaskSuiteVersion>>
                install = getServiceProvider().taskSuites().installSuite(researcherAlice, new URL(getTestServer().getServerAddress()), alice);
        Subscription subscription = install.second.subscribeOn(Schedulers.newThread()).subscribe();

        Thread.sleep(1000);
        subscription.unsubscribe();

        Thread.sleep(1000);
        assertEquals(0, getServiceProvider().taskSuites().getTaskStorage().size());
        assertEquals(0, executeToList(getServiceProvider().taskSuites().listSuitesForResearcher(researcherAlice)).size());
    }

    public void testUpdate() throws MalformedURLException {
        Researcher researcherAlice = execute(getServiceProvider().researcher().getResearcherByTextId(idAlice));

        // first, install the suite
        LogUtils.v(TAG, "==========> INSTALL SUITE <==========");
        Pair<Subject<InstallationProgress, InstallationProgress>, Observable<TaskSuiteVersion>>
                install = getServiceProvider().taskSuites().installSuite(researcherAlice, new URL(getTestServer().getServerAddress()), alice);
        TaskSuiteVersion suiteVersion1_0 = execute(install.second);
        assertEquals("test-suite", suiteVersion1_0.getTaskSuite().getName());
        assertEquals("1.0", suiteVersion1_0.getIdentifier());

        assertTrue(getServiceProvider().getTaskStorage().hasSuite("test-suite"));
        assertTrue(getServiceProvider().getTaskStorage().getSuite("test-suite").hasVersion("1.0"));
        assertFalse(getServiceProvider().getTaskStorage().getSuite("test-suite").hasVersion("1.1"));

        // check for update - no updates available
        LogUtils.v(TAG, "==========> UPDATE CHECK #1 <==========");
        TaskSuiteUpdate update = execute(getServiceProvider().taskSuites().checkUpdates(researcherAlice).first);
        assertFalse(update.isUpdate());

        // switch to new version
        getTestServer().setServedTaskSuiteLevel(1);

        // now, there is an update
        LogUtils.v(TAG, "==========> UPDATE CHECK #2 <==========");
        update = execute(getServiceProvider().taskSuites().checkUpdates(researcherAlice).first);
        assertTrue(update.isUpdate());
        assertEquals("test-suite", update.getName());
        assertEquals("1.1", update.getVersion());
        assertEquals(researcherAlice, update.getResearcher());

        assertEquals(1, executeToList(getServiceProvider().taskSuites().listSuitesForResearcher(researcherAlice)).size());
        assertEquals("test-suite", executeToList(getServiceProvider().taskSuites().listSuitesForResearcher(researcherAlice)).get(0).getName());
        assertEquals("1.0", executeToList(getServiceProvider().taskSuites().listSuitesForResearcher(researcherAlice)).get(0).getVersion());

        //well, let's install it then
        LogUtils.v(TAG, "==========> UPDATE INSTALL <==========");
        execute(getServiceProvider().taskSuites().installUpdate(update).first);

        assertEquals(1, executeToList(getServiceProvider().taskSuites().listSuitesForResearcher(researcherAlice)).size());
        assertEquals("test-suite", executeToList(getServiceProvider().taskSuites().listSuitesForResearcher(researcherAlice)).get(0).getName());
        assertEquals("1.1", executeToList(getServiceProvider().taskSuites().listSuitesForResearcher(researcherAlice)).get(0).getVersion());

        assertTrue(getServiceProvider().getTaskStorage().hasSuite("test-suite"));
        assertFalse(getServiceProvider().getTaskStorage().getSuite("test-suite").hasVersion("1.0"));
        assertTrue(getServiceProvider().getTaskStorage().getSuite("test-suite").hasVersion("1.1"));
    }

    public void testUpdateMultipleUsers() throws MalformedURLException {
        int requestsCountStart = getNetworkSupport().getRequestsCount();

        Researcher researcherAlice = execute(getServiceProvider().researcher().getResearcherByTextId(idAlice));
        Researcher researcherBob = execute(getServiceProvider().researcher().getResearcherByTextId(idBob));

        assertEquals(0, executeToList(getServiceProvider().taskSuites().listSuitesForResearcher(researcherAlice)).size());
        assertEquals(0, executeToList(getServiceProvider().taskSuites().listSuitesForResearcher(researcherBob)).size());
        assertEquals(0, executeToList(getServiceProvider().taskSuites().listSuites()).size());
        assertEquals(0, getServiceProvider().getTaskStorage().size());
        assertEquals(requestsCountStart, getNetworkSupport().getRequestsCount());

        // Alice installs task suite
        execute(getServiceProvider().taskSuites().installSuite(researcherAlice, new URL(getTestServer().getServerAddress()), alice).second);

        assertEquals(1, executeToList(getServiceProvider().taskSuites().listSuitesForResearcher(researcherAlice)).size());
        assertEquals("test-suite", executeToList(getServiceProvider().taskSuites().listSuitesForResearcher(researcherAlice)).get(0).getName());
        assertEquals("1.0", executeToList(getServiceProvider().taskSuites().listSuitesForResearcher(researcherAlice)).get(0).getVersion());
        assertEquals(0, executeToList(getServiceProvider().taskSuites().listSuitesForResearcher(researcherBob)).size());
        assertEquals(1, executeToList(getServiceProvider().taskSuites().listSuites()).size());
        assertEquals("test-suite", executeToList(getServiceProvider().taskSuites().listSuites()).get(0).getName());
        assertEquals("1.0", executeToList(getServiceProvider().taskSuites().listSuites()).get(0).getVersion());
        assertEquals(1, getServiceProvider().getTaskStorage().size());
        assertTrue(getServiceProvider().getTaskStorage().hasSuite("test-suite"));
        assertTrue(getServiceProvider().getTaskStorage().getSuite("test-suite").hasVersion("1.0"));
        assertFalse(getServiceProvider().getTaskStorage().getSuite("test-suite").hasVersion("1.1"));
        assertEquals(requestsCountStart + 2, getNetworkSupport().getRequestsCount());

        // Bob is jealous and installs it too (it installs almost immediately)
        execute(getServiceProvider().taskSuites().installSuite(researcherBob, new URL(getTestServer().getServerAddress()), bob).second);

        assertEquals(1, executeToList(getServiceProvider().taskSuites().listSuitesForResearcher(researcherAlice)).size());
        assertEquals("test-suite", executeToList(getServiceProvider().taskSuites().listSuitesForResearcher(researcherAlice)).get(0).getName());
        assertEquals("1.0", executeToList(getServiceProvider().taskSuites().listSuitesForResearcher(researcherAlice)).get(0).getVersion());
        assertEquals(1, executeToList(getServiceProvider().taskSuites().listSuitesForResearcher(researcherBob)).size());
        assertEquals("test-suite", executeToList(getServiceProvider().taskSuites().listSuitesForResearcher(researcherBob)).get(0).getName());
        assertEquals("1.0", executeToList(getServiceProvider().taskSuites().listSuitesForResearcher(researcherBob)).get(0).getVersion());
        assertEquals(1, executeToList(getServiceProvider().taskSuites().listSuites()).size());
        assertEquals("test-suite", executeToList(getServiceProvider().taskSuites().listSuites()).get(0).getName());
        assertEquals("1.0", executeToList(getServiceProvider().taskSuites().listSuites()).get(0).getVersion());
        assertEquals(1, getServiceProvider().getTaskStorage().size());
        assertTrue(getServiceProvider().getTaskStorage().hasSuite("test-suite"));
        assertTrue(getServiceProvider().getTaskStorage().getSuite("test-suite").hasVersion("1.0"));
        assertFalse(getServiceProvider().getTaskStorage().getSuite("test-suite").hasVersion("1.1"));
        assertEquals(requestsCountStart + 3, getNetworkSupport().getRequestsCount());

        // update comes into server
        getTestServer().setServedTaskSuiteLevel(1);
        assertEquals(requestsCountStart + 4, getNetworkSupport().getRequestsCount());

        // Bob checks for update, but he does not want it
        execute(getServiceProvider().taskSuites().checkUpdates(researcherBob).first);

        assertEquals(1, executeToList(getServiceProvider().taskSuites().listSuitesForResearcher(researcherAlice)).size());
        assertEquals("test-suite", executeToList(getServiceProvider().taskSuites().listSuitesForResearcher(researcherAlice)).get(0).getName());
        assertEquals("1.0", executeToList(getServiceProvider().taskSuites().listSuitesForResearcher(researcherAlice)).get(0).getVersion());
        assertEquals(1, executeToList(getServiceProvider().taskSuites().listSuitesForResearcher(researcherBob)).size());
        assertEquals("test-suite", executeToList(getServiceProvider().taskSuites().listSuitesForResearcher(researcherBob)).get(0).getName());
        assertEquals("1.0", executeToList(getServiceProvider().taskSuites().listSuitesForResearcher(researcherBob)).get(0).getVersion());
        assertEquals(1, executeToList(getServiceProvider().taskSuites().listSuites()).size());
        assertEquals("test-suite", executeToList(getServiceProvider().taskSuites().listSuites()).get(0).getName());
        assertEquals("1.0", executeToList(getServiceProvider().taskSuites().listSuites()).get(0).getVersion());
        assertEquals(1, getServiceProvider().getTaskStorage().size());
        assertTrue(getServiceProvider().getTaskStorage().hasSuite("test-suite"));
        assertTrue(getServiceProvider().getTaskStorage().getSuite("test-suite").hasVersion("1.0"));
        assertFalse(getServiceProvider().getTaskStorage().getSuite("test-suite").hasVersion("1.1"));
        assertEquals(requestsCountStart + 5, getNetworkSupport().getRequestsCount());

        // Alice has no objections and installs update
        TaskSuiteUpdate updateAlice = execute(getServiceProvider().taskSuites().checkUpdates(researcherAlice).first);
        assertEquals(requestsCountStart + 6, getNetworkSupport().getRequestsCount());
        execute(getServiceProvider().taskSuites().installUpdate(updateAlice).first);

        assertEquals(1, executeToList(getServiceProvider().taskSuites().listSuitesForResearcher(researcherAlice)).size());
        assertEquals("test-suite", executeToList(getServiceProvider().taskSuites().listSuitesForResearcher(researcherAlice)).get(0).getName());
        assertEquals("1.1", executeToList(getServiceProvider().taskSuites().listSuitesForResearcher(researcherAlice)).get(0).getVersion());
        assertEquals(1, executeToList(getServiceProvider().taskSuites().listSuitesForResearcher(researcherBob)).size());
        assertEquals("test-suite", executeToList(getServiceProvider().taskSuites().listSuitesForResearcher(researcherBob)).get(0).getName());
        assertEquals("1.0", executeToList(getServiceProvider().taskSuites().listSuitesForResearcher(researcherBob)).get(0).getVersion());
        assertEquals(2, executeToList(getServiceProvider().taskSuites().listSuites()).size());
        assertEquals("test-suite", executeToList(getServiceProvider().taskSuites().listSuites()).get(0).getName());
        assertEquals("1.0", executeToList(getServiceProvider().taskSuites().listSuites()).get(0).getVersion());
        assertEquals("test-suite", executeToList(getServiceProvider().taskSuites().listSuites()).get(1).getName());
        assertEquals("1.1", executeToList(getServiceProvider().taskSuites().listSuites()).get(1).getVersion());
        assertEquals(1, getServiceProvider().getTaskStorage().size());
        assertTrue(getServiceProvider().getTaskStorage().hasSuite("test-suite"));
        assertEquals(2, getServiceProvider().getTaskStorage().getSuite("test-suite").versionsCount());
        assertTrue(getServiceProvider().getTaskStorage().getSuite("test-suite").hasVersion("1.0"));
        assertTrue(getServiceProvider().getTaskStorage().getSuite("test-suite").hasVersion("1.1"));
        assertEquals(requestsCountStart + 7, getNetworkSupport().getRequestsCount());

        // Now, Bob can do the update without download
        TaskSuiteUpdate updateBob = execute(getServiceProvider().taskSuites().checkUpdates(researcherBob).first);
        assertEquals(requestsCountStart + 8, getNetworkSupport().getRequestsCount());
        execute(getServiceProvider().taskSuites().installUpdate(updateBob).first);

        assertEquals(1, executeToList(getServiceProvider().taskSuites().listSuitesForResearcher(researcherAlice)).size());
        assertEquals("test-suite", executeToList(getServiceProvider().taskSuites().listSuitesForResearcher(researcherAlice)).get(0).getName());
        assertEquals("1.1", executeToList(getServiceProvider().taskSuites().listSuitesForResearcher(researcherAlice)).get(0).getVersion());
        assertEquals(1, executeToList(getServiceProvider().taskSuites().listSuitesForResearcher(researcherBob)).size());
        assertEquals("test-suite", executeToList(getServiceProvider().taskSuites().listSuitesForResearcher(researcherBob)).get(0).getName());
        assertEquals("1.1", executeToList(getServiceProvider().taskSuites().listSuitesForResearcher(researcherBob)).get(0).getVersion());
        assertEquals(1, executeToList(getServiceProvider().taskSuites().listSuites()).size());
        assertEquals("test-suite", executeToList(getServiceProvider().taskSuites().listSuites()).get(0).getName());
        assertEquals("1.1", executeToList(getServiceProvider().taskSuites().listSuites()).get(0).getVersion());
        assertEquals(1, getServiceProvider().getTaskStorage().size());
        assertTrue(getServiceProvider().getTaskStorage().hasSuite("test-suite"));
        assertFalse(getServiceProvider().getTaskStorage().getSuite("test-suite").hasVersion("1.0"));
        assertTrue(getServiceProvider().getTaskStorage().getSuite("test-suite").hasVersion("1.1"));
        assertEquals(requestsCountStart + 8, getNetworkSupport().getRequestsCount());
    }
}
