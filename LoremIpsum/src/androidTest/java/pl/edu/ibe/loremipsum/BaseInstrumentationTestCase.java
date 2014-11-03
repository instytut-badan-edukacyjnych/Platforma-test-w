package pl.edu.ibe.loremipsum;

import android.os.Environment;
import android.test.InstrumentationTestCase;

import junit.framework.AssertionFailedError;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import pl.edu.ibe.loremipsum.network.Auth;
import pl.edu.ibe.loremipsum.network.HttpBasicAuth;
import pl.edu.ibe.loremipsum.network.NetworkSupport;
import pl.edu.ibe.loremipsum.tablet.base.ServiceProvider;
import pl.edu.ibe.loremipsum.tablet.base.ServiceProviderBuilder;
import pl.edu.ibe.loremipsum.tools.DbAccess;
import pl.edu.ibe.loremipsum.tools.FileUtils;
import pl.edu.ibe.loremipsum.tools.LogUtils;
import pl.edu.ibe.loremipsum.tools.RxExecutor;
import pl.edu.ibe.loremipsum.util.TestServer;
import pl.edu.ibe.loremipsum.util.TestServiceProvider;
import rx.Observable;
import rx.functions.Func1;

/**
 * @author Mariusz Pluciński
 */
public class BaseInstrumentationTestCase extends InstrumentationTestCase {

    protected static final Auth alice = new HttpBasicAuth("test-Alice", "test-password");
    protected static final Auth bob = new HttpBasicAuth("test-Bob", "test-password");
    protected static final String testAssetName = "testing-suite-1.0.zip";
    protected static final String testAssetWithDemoName = "testing-suite-withdemo-1.0.zip";
    private static final String TAG = BaseInstrumentationTestCase.class.toString();
    protected File[] testFiles;
    private File storageDir;
    private File tempStorageDir;
    private List<File> toDelete = new ArrayList<>();
    private TestServer testServer = null;
    private TestServiceProvider testServiceProvider = null;

    static public void assertFilesContentsEquals(File file1, File file2) throws IOException {
        if (!FileUtils.contentEquals(file1, file2))
            throw new AssertionFailedError("Files \"" + file1 + "\" and \"" + file2 + "\" contents are not equal");
    }

    static public void assertFilesContentsNotEquals(File file1, File file2) throws IOException {
        if (FileUtils.contentEquals(file1, file2))
            throw new AssertionFailedError("Files \"" + file1 + "\" and \"" + file2 + "\" contents are equal");
    }

    static public void assertDirectoryContentEquals(List<String> expectedContents, File directory) {
        assertTrue(directory.isDirectory());

        List<String> expected = new ArrayList<>(expectedContents);
        List<String> actual = Arrays.asList(directory.list());

        Collections.sort(expected);
        Collections.sort(actual);

        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); ++i)
            assertEquals(expected.get(i), actual.get(i));
    }

    static public void assertThrows(Class<? extends Throwable> throwableClass, RunnableThrows runnable) {
        try {
            runnable.run();
        } catch (Throwable t) {
            t.printStackTrace();
            assertTrue(
                    "Expected to catch " + throwableClass + " instance, but " + t.getClass() + " caught",
                    throwableClass.isAssignableFrom(t.getClass())
            );
            return;
        }
        throw new AssertionFailedError(throwableClass + " not thrown");
    }

    static public void assertThrowsWrapped(Class<? extends Throwable> throwableClass, RunnableThrows runnable) {
        try {
            runnable.run();
        } catch (Throwable t) {
            Throwable orig_t = t;
            while (true) {
                if (t == null || t == t.getCause()) {
                    String str = "Class " + throwableClass + " not found in the cause hierarchy of "
                            + orig_t +"\nCaught exception stack trace:\n";
                    for(StackTraceElement element: orig_t.getStackTrace())
                        str += "> " + element.toString() + "\n";
                    fail(str);
                }

                if (throwableClass.isAssignableFrom(t.getClass()))
                    return;

                t = t.getCause();
            }
        }
        throw new AssertionFailedError(throwableClass + " not thrown");
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        RxExecutor.setDefaultScheduler(null);

        testServiceProvider = new TestServiceProvider();

        tempStorageDir = new File(Environment.getExternalStorageDirectory(), "LoremIpsum-Tests-Tmp");
        if (!tempStorageDir.exists() && !tempStorageDir.mkdir())
            throw new IOException("Could not create \"" + tempStorageDir + "\" directory");

        testFiles = new File[]{
                new File(tempStorageDir, "original_foo.zip"),
                new File(tempStorageDir, "original_bar.zip")
        };

        prepareSampleZipFile("foo", testFiles[0]);
        prepareSampleZipFile("bar", testFiles[1]);

        storageDir = new File(Environment.getExternalStorageDirectory(), "LoremIpsum-Tests");
        if (!storageDir.exists() && !storageDir.mkdir())
            throw new IOException("Could not create \"" + storageDir + "\" directory");
    }

    protected Func1<ServiceProviderBuilder, ServiceProviderBuilder> additionalBuilderParams() {
        return serviceProviderBuilder -> serviceProviderBuilder;
    }

    protected DbAccess getDb() {
        return getServiceProvider().getDbAccess();
    }

    protected ServiceProvider getServiceProvider() {
        return testServiceProvider.getServiceProvider(getInstrumentation().getContext(),
                getInstrumentation().getTargetContext(), getStorageDir(), additionalBuilderParams());
    }

    public NetworkSupport getNetworkSupport() {
        return getServiceProvider().getNetworkSupport();
    }

    @Override
    public void tearDown() throws Exception {
        if (testServer != null) {
            testServer.setServerTestMode(false);
            testServer = null;
        }
        testServiceProvider.dispose();

        for (File dir : toDelete)
            if (dir.exists())
                FileUtils.deleteRecursive(dir);
            else
                LogUtils.w(TAG, "Directory \"" + dir + "\" scheduled to remove, but does not exist");

        if (storageDir.exists())
            FileUtils.deleteRecursive(storageDir);
        FileUtils.deleteRecursive(tempStorageDir);

        super.tearDown();
    }

    private void prepareSampleZipFile(String contents, File file) throws IOException {
        FileOutputStream fileOutputStream = null;
        ZipOutputStream zipOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file);
            zipOutputStream = new ZipOutputStream(fileOutputStream);
            zipOutputStream.putNextEntry(new ZipEntry(contents + "/"));
            zipOutputStream.putNextEntry(new ZipEntry(contents + "/" + contents + ".txt"));
            zipOutputStream.write(contents.getBytes());
        } finally {
            if (zipOutputStream != null)
                zipOutputStream.close();
            if (fileOutputStream != null)
                fileOutputStream.close();
        }
    }

    public File getStorageDir() {
        return storageDir;
    }

    protected File getStorageDir(String suffix) throws IOException {
        File dir = new File(storageDir + "-" + suffix);
        if (!dir.exists() && !dir.mkdir())
            throw new IOException("Could not create \"" + dir + "\" directory");
        toDelete.add(dir);
        return dir;
    }

    protected <T> T execute(Observable<T> observable) {
        return observable.toBlockingObservable().single();
    }

    protected <T> T executeLast(Observable<T> observable) {
        return observable.toBlockingObservable().last();
    }

    protected <T> List<T> executeToList(Observable<T> observable) {
        List<T> list = new ArrayList<>();
        for (T i : observable.toBlockingObservable().toIterable())
            list.add(i);
        return list;
    }

    public TestServer getTestServer() {
        return testServer;
    }

    public void setTestServer(TestServer testServer) throws IOException, InterruptedException {
        this.testServer = testServer;
//        testServer.setServerTestMode(true);
    }

    static public interface RunnableThrows {

        public void run() throws Throwable;
    }
}
